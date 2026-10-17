package com.tech.todo.config;

import com.tech.todo.tenant.TenantRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableConfigurationProperties(AppDataSourceProperties.class)
public class DataSourceConfig {

	@Bean(name = "masterDataSource")
	public DataSource masterDataSource(AppDataSourceProperties properties) {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(properties.getMaster().getJdbcUrl());
		dataSource.setUsername(properties.getMaster().getUsername());
		dataSource.setPassword(properties.getMaster().getPassword());
		dataSource.setPoolName("master");
		dataSource.setMaximumPoolSize(5);
		return dataSource;
	}

	@Bean(name = "masterJdbcTemplate")
	public JdbcTemplate masterJdbcTemplate(@Qualifier("masterDataSource") DataSource masterDataSource) {
		return new JdbcTemplate(masterDataSource);
	}

	/**
	 * Routing DS defaults to master only so the pool can start. App tables
	 * (todos/user/role) must never be created on master — master Flyway uses
	 * {@code classpath:db/master} only; tenant Flyway uses {@code db/migration}.
	 * Keep {@code spring.jpa.hibernate.ddl-auto=none} so Hibernate cannot create
	 * entity tables on this default (master) target.
	 */
	@Bean
	@Primary
	public TenantRoutingDataSource dataSource(@Qualifier("masterDataSource") DataSource masterDataSource) {
		TenantRoutingDataSource routing = new TenantRoutingDataSource();
		routing.setDefaultTargetDataSource(masterDataSource);
		routing.setLenientFallback(true);
		routing.afterPropertiesSet();
		return routing;
	}
}
