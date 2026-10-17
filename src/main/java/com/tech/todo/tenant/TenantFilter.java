package com.tech.todo.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class TenantFilter extends OncePerRequestFilter {

	public static final String TENANT_HEADER = "X-Tenant-Id";

	private final TenantRegistry tenantRegistry;

	public TenantFilter(TenantRegistry tenantRegistry) {
		this.tenantRegistry = tenantRegistry;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}
		String path = request.getRequestURI();
		// Auth still needs X-Tenant-Id (users live in tenant DB) — do not skip /api/auth
		return path.startsWith("/api/tenants")
				|| path.startsWith("/actuator")
				|| path.startsWith("/error");
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String tenantKey = request.getHeader(TENANT_HEADER);
		if (tenantKey == null || tenantKey.isBlank()) {
			response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing " + TENANT_HEADER + " header");
			return;
		}

		String normalized = tenantKey.trim().toLowerCase(Locale.ROOT);
		if (!tenantRegistry.exists(normalized)) {
			response.sendError(HttpStatus.NOT_FOUND.value(), "Unknown tenant: " + normalized);
			return;
		}

		TenantContext.setTenantKey(normalized);
		try {
			filterChain.doFilter(request, response);
		}
		finally {
			TenantContext.clear();
		}
	}
}
