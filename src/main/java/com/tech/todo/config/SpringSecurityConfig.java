package com.tech.todo.config;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

import com.tech.todo.tenant.TenantFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

/**
 * {@code todo_master} holds {@code tenants}, {@code plans} (+ Flyway history).
 * Users/roles/todos live in each {@code todo_<tenant>} DB.
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	DaoAuthenticationProvider tenantAuthenticationProvider(
			@Qualifier("tenantUserDetailsService") UserDetailsService tenantUserDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(tenantUserDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	/** Required by login API ({@code AuthServiceImpl}). */
	@Bean
	AuthenticationManager authenticationManager(DaoAuthenticationProvider tenantAuthenticationProvider) {
		return new ProviderManager(tenantAuthenticationProvider);
	}

	@Bean
	FilterRegistrationBean<TenantFilter> tenantFilterRegistration(TenantFilter tenantFilter) {
		FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>(tenantFilter);
		registration.setEnabled(false);
		return registration;
	}

	/** Public control-plane APIs — no auth (no platform users on master). */
	@Bean
	@Order(1)
	SecurityFilterChain tenantProvisionSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.securityMatcher(new OrRequestMatcher(
						pathPattern("/api/tenants"),
						pathPattern("/api/tenants/**"),
						pathPattern("/api/plans"),
						pathPattern("/api/plans/**")))
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
		return http.build();
	}

	/** Tenant user register — needs X-Tenant-Id, no Basic auth. */
	@Bean
	@Order(2)
	SecurityFilterChain tenantAuthSecurityFilterChain(HttpSecurity http, TenantFilter tenantFilter)
			throws Exception {
		http
				.securityMatcher(new OrRequestMatcher(
						pathPattern("/api/auth"),
						pathPattern("/api/auth/**")))
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
				.addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	/** Todos + subscription — tenant Basic auth + X-Tenant-Id. */
	@Bean
	@Order(3)
	SecurityFilterChain tenantAppSecurityFilterChain(
			HttpSecurity http,
			TenantFilter tenantFilter,
			DaoAuthenticationProvider tenantAuthenticationProvider) throws Exception {
		http
				.securityMatcher(new OrRequestMatcher(
						pathPattern("/api/todos"),
						pathPattern("/api/todos/**"),
						pathPattern("/api/subscription"),
						pathPattern("/api/subscription/**")))
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.authenticationProvider(tenantAuthenticationProvider)
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.anyRequest().authenticated())
				.httpBasic(Customizer.withDefaults())
				.addFilterBefore(tenantFilter, BasicAuthenticationFilter.class);
		return http.build();
	}
}
