package com.tech.todo.migration;

import com.tech.todo.config.AppDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

@Service
public class TenantMigrationService {

	private final AppDataSourceProperties properties;

	public TenantMigrationService(AppDataSourceProperties properties) {
		this.properties = properties;
	}

	public void migrateMaster(DataSource masterDataSource) {
		Flyway.configure()
				.dataSource(masterDataSource)
				.locations("classpath:db/master")
				.load()
				.migrate();
	}

	public void migrateTenant(DataSource tenantDataSource) {
		Flyway.configure()
				.dataSource(tenantDataSource)
				.locations("classpath:db/migration")
				.load()
				.migrate();
	}

	public DataSource createTenantDataSource(String dbName) {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(properties.getTenant().getJdbcUrlPrefix() + dbName
				+ "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
		dataSource.setUsername(properties.getTenant().getUsername());
		dataSource.setPassword(properties.getTenant().getPassword());
		dataSource.setPoolName("tenant-" + dbName);
		dataSource.setMaximumPoolSize(5);
		return dataSource;
	}
}
