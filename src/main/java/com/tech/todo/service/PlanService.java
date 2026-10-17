package com.tech.todo.service;

import com.tech.todo.dto.PlanDto;
import com.tech.todo.entity.Plan;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlanService {

	List<PlanDto> getAllPlans();

	Plan getPlanByCode(String planCode);

	Plan getPlanForTenant(String tenantKey);

	String getSubscriptionStatus(String tenantKey);

	Optional<LocalDateTime> getCurrentPeriodEnd(String tenantKey);

	void markSubscriptionExpiredIfNeeded(String tenantKey);
}
