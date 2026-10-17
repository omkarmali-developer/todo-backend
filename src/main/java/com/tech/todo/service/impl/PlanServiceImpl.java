package com.tech.todo.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tech.todo.dto.PlanDto;
import com.tech.todo.dto.PlanLimits;
import com.tech.todo.entity.Plan;
import com.tech.todo.exception.ResourceNotFoundException;
import com.tech.todo.exception.TodoApiException;
import com.tech.todo.repository.PlanRepository;
import com.tech.todo.service.PlanService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PlanServiceImpl implements PlanService {

	private static final String DEFAULT_PLAN_CODE = "starter";

	@Autowired
	private PlanRepository planRepository;

    ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public List<PlanDto> getAllPlans() {
		return planRepository.findAllActive().stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	@Override
	public Plan getPlanByCode(String planCode) {
		String code = normalizeCode(planCode);
		return planRepository.findActiveByCode(code)
				.orElseThrow(() -> new TodoApiException(HttpStatus.BAD_REQUEST, "Unknown plan: " + code));
	}

	@Override
	public Plan getPlanForTenant(String tenantKey) {
		return planRepository.findByTenantKey(tenantKey)
				.orElseThrow(() -> new ResourceNotFoundException(
						"No subscription found for tenant: " + tenantKey));
	}

	@Override
	public String getSubscriptionStatus(String tenantKey) {
		return planRepository.findSubscriptionStatusByTenantKey(tenantKey).orElse("unknown");
	}

	@Override
	public Optional<LocalDateTime> getCurrentPeriodEnd(String tenantKey) {
		return planRepository.findCurrentPeriodEndByTenantKey(tenantKey);
	}

	@Override
	public void markSubscriptionExpiredIfNeeded(String tenantKey) {
		planRepository.markSubscriptionExpired(tenantKey);
	}

	private PlanDto mapToDto(Plan plan) {
		PlanDto dto = new PlanDto();
		dto.setCode(plan.getCode());
		dto.setName(plan.getName());
		dto.setTagline(plan.getTagline());
		dto.setPriceCents(plan.getPriceCents());
		dto.setPriceLabel(formatPriceLabel(plan.getPriceCents()));
		dto.setPeriod(formatPeriod(plan.getBillingPeriod()));
		dto.setCta(plan.getCtaLabel());
		dto.setHighlighted(plan.isHighlighted());
		dto.setFeatures(parseFeatures(plan.getFeaturesJson()));
		dto.setLimits(new PlanLimits(plan.getMaxTodos(), plan.getMaxUsers()));
		return dto;
	}

	private String normalizeCode(String rawCode) {
		if (rawCode == null || rawCode.isBlank()) {
			return DEFAULT_PLAN_CODE;
		}
		return rawCode.trim().toLowerCase();
	}

	private static String formatPriceLabel(int priceCents) {
		if (priceCents <= 0) {
			return "Free";
		}
		int dollars = priceCents / 100;
		int cents = priceCents % 100;
		if (cents == 0) {
			return "$" + dollars;
		}
		return String.format("$%d.%02d", dollars, cents);
	}

	private static String formatPeriod(String billingPeriod) {
		if ("forever".equalsIgnoreCase(billingPeriod)) {
			return "forever";
		}
		if ("month".equalsIgnoreCase(billingPeriod)) {
			return "/ month";
		}
		return billingPeriod;
	}

	private List<String> parseFeatures(String featuresJson) {
		try {
			String json = featuresJson == null ? "[]" : featuresJson;
			return objectMapper.readValue(json, new TypeReference<>() {
			});
		}
		catch (Exception ex) {
			return List.of();
		}
	}
}
