package com.tech.todo.tenant;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TenantRegistry {

	private final JdbcTemplate masterJdbcTemplate;
	private final Map<String, String> tenantKeyToDbName = new ConcurrentHashMap<>();

	public TenantRegistry(@Qualifier("masterJdbcTemplate") JdbcTemplate masterJdbcTemplate) {
		this.masterJdbcTemplate = masterJdbcTemplate;
	}

	public void reload() {
		tenantKeyToDbName.clear();
		List<Map<String, Object>> rows = masterJdbcTemplate.queryForList(
				"SELECT tenant_key, db_name FROM tenants");
		for (Map<String, Object> row : rows) {
			tenantKeyToDbName.put(
					String.valueOf(row.get("tenant_key")),
					String.valueOf(row.get("db_name")));
		}
	}

	public Optional<String> findDbName(String tenantKey) {
		return Optional.ofNullable(tenantKeyToDbName.get(tenantKey));
	}

	public boolean exists(String tenantKey) {
		return tenantKeyToDbName.containsKey(tenantKey);
	}

	public Map<String, String> allTenants() {
		return Map.copyOf(tenantKeyToDbName);
	}

	public void register(String tenantKey, String dbName, long planId, LocalDateTime currentPeriodEnd) {
		masterJdbcTemplate.update(
				"""
						INSERT INTO tenants (
						  tenant_key, db_name, plan_id, subscription_status, current_period_end
						)
						VALUES (?, ?, ?, 'active', ?)
						""",
				tenantKey,
				dbName,
				planId,
				currentPeriodEnd == null ? null : Timestamp.valueOf(currentPeriodEnd));
		tenantKeyToDbName.put(tenantKey, dbName);
	}

	public void unregister(String tenantKey) {
		masterJdbcTemplate.update("DELETE FROM tenants WHERE tenant_key = ?", tenantKey);
		tenantKeyToDbName.remove(tenantKey);
	}
}
