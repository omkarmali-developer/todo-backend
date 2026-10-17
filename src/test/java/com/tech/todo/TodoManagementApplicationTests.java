package com.tech.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tech.todo.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TodoManagementApplicationTests {

	@AfterEach
	void clearTenant() {
		TenantContext.clear();
	}

	@Test
	void tenantContextHoldsCurrentTenant() {
		assertNull(TenantContext.getTenantKey());
		TenantContext.setTenantKey("acme");
		assertEquals("acme", TenantContext.getTenantKey());
		TenantContext.clear();
		assertNull(TenantContext.getTenantKey());
	}
}
