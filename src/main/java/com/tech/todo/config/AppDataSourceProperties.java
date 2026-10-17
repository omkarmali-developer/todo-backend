package com.tech.todo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.datasource")
public class AppDataSourceProperties {

	private final Master master = new Master();
	private final Tenant tenant = new Tenant();

	public Master getMaster() {
		return master;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public static class Master {
		private String jdbcUrl;
		private String username;
		private String password;

		public String getJdbcUrl() {
			return jdbcUrl;
		}

		public void setJdbcUrl(String jdbcUrl) {
			this.jdbcUrl = jdbcUrl;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	public static class Tenant {
		private String username;
		private String password;
		private String jdbcUrlPrefix;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getJdbcUrlPrefix() {
			return jdbcUrlPrefix;
		}

		public void setJdbcUrlPrefix(String jdbcUrlPrefix) {
			this.jdbcUrlPrefix = jdbcUrlPrefix;
		}
	}
}
