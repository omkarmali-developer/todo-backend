package com.tech.todo.service.impl;

import com.tech.todo.dto.SubscriptionDto;
import com.tech.todo.entity.Plan;
import com.tech.todo.exception.TodoApiException;
import com.tech.todo.repository.TodoRepository;
import com.tech.todo.repository.UserRepository;
import com.tech.todo.service.PlanService;
import com.tech.todo.service.SubscriptionService;
import com.tech.todo.tenant.TenantContext;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

	static final String RENEWAL_MESSAGE =
			"Your subscription period has ended. Renew your plan to create new todos.";

	@Autowired
	private PlanService planService;

	@Autowired
	private TodoRepository todoRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public SubscriptionDto getCurrentSubscription() {
		String tenantKey = requireTenantKey();
		Plan plan = planService.getPlanForTenant(tenantKey);
		LocalDateTime periodEnd = planService.getCurrentPeriodEnd(tenantKey).orElse(null);
		boolean expired = isPeriodExpired(periodEnd);

		if (expired) {
			planService.markSubscriptionExpiredIfNeeded(tenantKey);
		}

		String status = planService.getSubscriptionStatus(tenantKey);
		if (expired && !"expired".equalsIgnoreCase(status)) {
			status = "expired";
		}

		SubscriptionDto dto = new SubscriptionDto();
		dto.setTenantKey(tenantKey);
		dto.setPlanCode(plan.getCode());
		dto.setPlanName(plan.getName());
		dto.setSubscriptionStatus(status);
		dto.setCurrentPeriodEnd(periodEnd);
		dto.setExpired(expired);
		dto.setRenewalMessage(expired ? RENEWAL_MESSAGE : null);
		dto.setMaxTodos(plan.getMaxTodos());
		dto.setMaxUsers(plan.getMaxUsers());
		dto.setTodoCount(todoRepository.count());
		dto.setUserCount(userRepository.count());
		return dto;
	}

	@Override
	public void assertCanCreateTodo() {
		assertSubscriptionActive();

		Plan plan = requireCurrentPlan();
		if (plan.getMaxTodos() == null) {
			return;
		}
		long count = todoRepository.count();
		if (count >= plan.getMaxTodos()) {
			throw new TodoApiException(
					HttpStatus.FORBIDDEN,
					"Plan '" + plan.getName() + "' allows at most " + plan.getMaxTodos()
							+ " todos. Upgrade to Pro or Team for unlimited todos.");
		}
	}

	@Override
	public void assertCanRegisterUser() {
		assertSubscriptionActive();

		Plan plan = requireCurrentPlan();
		long count = userRepository.count();
		if (count >= plan.getMaxUsers()) {
			throw new TodoApiException(
					HttpStatus.FORBIDDEN,
					"Plan '" + plan.getName() + "' allows at most " + plan.getMaxUsers()
							+ " user(s). Upgrade to Team for more seats.");
		}
	}

	private void assertSubscriptionActive() {
		String tenantKey = requireTenantKey();
		LocalDateTime periodEnd = planService.getCurrentPeriodEnd(tenantKey).orElse(null);
		if (!isPeriodExpired(periodEnd)) {
			return;
		}
		planService.markSubscriptionExpiredIfNeeded(tenantKey);
		throw new TodoApiException(HttpStatus.FORBIDDEN, RENEWAL_MESSAGE);
	}

	private static boolean isPeriodExpired(LocalDateTime periodEnd) {
		return periodEnd != null && LocalDateTime.now().isAfter(periodEnd);
	}

	private Plan requireCurrentPlan() {
		return planService.getPlanForTenant(requireTenantKey());
	}

	private static String requireTenantKey() {
		String tenantKey = TenantContext.getTenantKey();
		if (tenantKey == null || tenantKey.isBlank()) {
			throw new TodoApiException(HttpStatus.BAD_REQUEST, "X-Tenant-Id is required");
		}
		return tenantKey;
	}
}
