package com.tech.todo.tenant;

import com.tech.todo.entity.Plan;
import com.tech.todo.migration.TenantMigrationService;
import com.tech.todo.service.PlanService;
import com.zaxxer.hikari.HikariDataSource;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TenantService {

	private static final Logger log = LoggerFactory.getLogger(TenantService.class);
	private static final Pattern TENANT_KEY = Pattern.compile("^[a-z][a-z0-9_]{1,62}$");

	private final JdbcTemplate masterJdbcTemplate;
	private final TenantRegistry tenantRegistry;
	private final TenantMigrationService migrationService;
	private final TenantRoutingDataSource routingDataSource;
	private final PlanService planService;

	public TenantService(
			@Qualifier("masterDataSource") DataSource masterDataSource,
			TenantRegistry tenantRegistry,
			TenantMigrationService migrationService,
			TenantRoutingDataSource routingDataSource,
			PlanService planService) {
		this.masterJdbcTemplate = new JdbcTemplate(masterDataSource);
		this.tenantRegistry = tenantRegistry;
		this.migrationService = migrationService;
		this.routingDataSource = routingDataSource;
		this.planService = planService;
	}

	public TenantView provision(String rawTenantKey, String planCode) {
		String tenantKey = normalize(rawTenantKey);
		if (tenantRegistry.exists(tenantKey)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant already exists: " + tenantKey);
		}

		Plan plan = planService.getPlanByCode(planCode);
		String dbName = "todo_" + tenantKey;
		// Previous failed attempts may have left an orphan DB with no tenants row.
		dropDatabaseIfExists(dbName);

		boolean registered = false;
		boolean routed = false;
		DataSource tenantDs = null;

		try {
			masterJdbcTemplate.execute("CREATE DATABASE `" + dbName + "`");
			tenantDs = migrationService.createTenantDataSource(dbName);
			migrationService.migrateTenant(tenantDs);
			tenantRegistry.register(
					tenantKey,
					dbName,
					plan.getId(),
					resolveCurrentPeriodEnd(plan.getBillingPeriod()));
			registered = true;
			routingDataSource.addTenantDataSource(tenantKey, tenantDs);
			routed = true;
			return new TenantView(tenantKey, dbName, plan.getCode(), plan.getName());
		}
		catch (ResponseStatusException ex) {
			rollbackProvision(tenantKey, dbName, tenantDs, registered, routed);
			throw ex;
		}
		catch (Exception ex) {
			rollbackProvision(tenantKey, dbName, tenantDs, registered, routed);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to provision tenant '" + tenantKey + "': " + ex.getMessage(),
					ex);
		}
	}

	private void rollbackProvision(
			String tenantKey,
			String dbName,
			DataSource tenantDs,
			boolean registered,
			boolean routed) {
		log.warn("Rolling back tenant provision for key={}, db={}", tenantKey, dbName);
		try {
			if (routed) {
				routingDataSource.removeTenantDataSource(tenantKey);
			}
			else {
				closeQuietly(tenantDs);
			}
			if (registered) {
				tenantRegistry.unregister(tenantKey);
			}
			dropDatabaseIfExists(dbName);
		}
		catch (Exception cleanupEx) {
			log.error("Cleanup failed while rolling back tenant {}", tenantKey, cleanupEx);
		}
	}

	private void dropDatabaseIfExists(String dbName) {
		if (!databaseExists(dbName)) {
			return;
		}
		masterJdbcTemplate.execute("DROP DATABASE `" + dbName + "`");
	}

	private boolean databaseExists(String dbName) {
		try {
			Integer count = masterJdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?",
					Integer.class,
					dbName);
			return count != null && count > 0;
		}
		catch (EmptyResultDataAccessException ex) {
			return false;
		}
	}

	private static void closeQuietly(DataSource dataSource) {
		if (dataSource instanceof HikariDataSource hikari && !hikari.isClosed()) {
			hikari.close();
		}
	}

	public String normalize(String rawTenantKey) {
		if (rawTenantKey == null || rawTenantKey.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tenantKey is required");
		}
		String tenantKey = rawTenantKey.trim().toLowerCase(Locale.ROOT);
		if (!TENANT_KEY.matcher(tenantKey).matches()) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"tenantKey must match [a-z][a-z0-9_]{1,62}");
		}
		return tenantKey;
	}

	/**
	 * Free ({@code forever}) has no renewal date. Monthly/yearly plans get a period end
	 * from signup time so {@code current_period_end} is populated on the tenants row.
	 */
	static LocalDateTime resolveCurrentPeriodEnd(String billingPeriod) {
		LocalDateTime now = LocalDateTime.now();
		if (billingPeriod == null || billingPeriod.isBlank()) {
			return now.plusMonths(1);
		}
		return switch (billingPeriod.trim().toLowerCase(Locale.ROOT)) {
			case "forever" -> null;
			case "year", "yearly", "annual" -> now.plusYears(1);
			case "month", "monthly" -> now.plusMonths(1);
			default -> now.plusMonths(1);
		};
	}

	public record TenantView(String tenantKey, String dbName, String planCode, String planName) {
	}
}
