package com.tech.todo.tenant;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

	private final TenantService tenantService;

	public TenantController(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TenantService.TenantView create(@RequestBody CreateTenantRequest request) {
		return tenantService.provision(request.tenantKey(), request.planCode());
	}

	public record CreateTenantRequest(String tenantKey, String planCode) {
	}
}
