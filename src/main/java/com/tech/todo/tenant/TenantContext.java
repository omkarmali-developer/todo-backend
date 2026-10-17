package com.tech.todo.tenant;

public final class TenantContext {

	private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

	private TenantContext() {
	}

	public static void setTenantKey(String tenantKey) {
		CURRENT.set(tenantKey);
	}

	public static String getTenantKey() {
		return CURRENT.get();
	}

	public static void clear() {
		CURRENT.remove();
	}
}
