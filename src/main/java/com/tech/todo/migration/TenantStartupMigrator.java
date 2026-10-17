package com.tech.todo.migration;

import com.tech.todo.tenant.TenantRegistry;
import com.tech.todo.tenant.TenantRoutingDataSource;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * UpgradeDb on startup: migrate control DB, then every tenant DB.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantStartupMigrator implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(TenantStartupMigrator.class);

	private final DataSource masterDataSource;
	private final TenantMigrationService migrationService;
	private final TenantRegistry tenantRegistry;
	private final TenantRoutingDataSource routingDataSource;

	public TenantStartupMigrator(
			@Qualifier("masterDataSource") DataSource masterDataSource,
			TenantMigrationService migrationService,
			TenantRegistry tenantRegistry,
			TenantRoutingDataSource routingDataSource) {
		this.masterDataSource = masterDataSource;
		this.migrationService = migrationService;
		this.tenantRegistry = tenantRegistry;
		this.routingDataSource = routingDataSource;
	}

	@Override
	public void run(ApplicationArguments args) {
		migrationService.migrateMaster(masterDataSource);
		tenantRegistry.reload();

		for (Map.Entry<String, String> entry : tenantRegistry.allTenants().entrySet()) {
			String tenantKey = entry.getKey();
			String dbName = entry.getValue();
			if (routingDataSource.hasTenant(tenantKey)) {
				continue;
			}
			DataSource tenantDs = migrationService.createTenantDataSource(dbName);
			migrationService.migrateTenant(tenantDs);
			routingDataSource.addTenantDataSource(tenantKey, tenantDs);
			log.info("Migrated tenant DB {} ({})", tenantKey, dbName);
		}

		log.info("Tenant registry ready: {} tenant(s) {}",
				tenantRegistry.allTenants().size(),
				tenantRegistry.allTenants().keySet());
	}
}
