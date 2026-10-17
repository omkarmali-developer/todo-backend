package com.tech.todo.tenant;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

	private final Map<Object, Object> tenantDataSources = new ConcurrentHashMap<>();

	public TenantRoutingDataSource() {
		setTargetDataSources(tenantDataSources);
		setLenientFallback(false);
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return TenantContext.getTenantKey();
	}

	public void addTenantDataSource(String tenantKey, DataSource dataSource) {
		tenantDataSources.put(tenantKey, dataSource);
		setTargetDataSources(tenantDataSources);
		afterPropertiesSet();
	}

	public void removeTenantDataSource(String tenantKey) {
		Object removed = tenantDataSources.remove(tenantKey);
		setTargetDataSources(tenantDataSources);
		afterPropertiesSet();
		closeQuietly(removed);
	}

	public boolean hasTenant(String tenantKey) {
		return tenantDataSources.containsKey(tenantKey);
	}

	private static void closeQuietly(Object dataSource) {
		if (dataSource instanceof HikariDataSource hikari && !hikari.isClosed()) {
			hikari.close();
		}
	}
}
