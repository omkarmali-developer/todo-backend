package com.tech.todo.repository;

import com.tech.todo.entity.Plan;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PlanRepository {

	private static final RowMapper<Plan> PLAN_ROW_MAPPER = (rs, rowNum) -> {
		Plan plan = new Plan();
		plan.setId(rs.getLong("id"));
		plan.setCode(rs.getString("code"));
		plan.setName(rs.getString("name"));
		plan.setTagline(rs.getString("tagline"));
		plan.setPriceCents(rs.getInt("price_cents"));
		plan.setBillingPeriod(rs.getString("billing_period"));
		plan.setCtaLabel(rs.getString("cta_label"));
		plan.setHighlighted(rs.getBoolean("highlighted"));
		plan.setMaxTodos((Integer) rs.getObject("max_todos"));
		plan.setMaxUsers(rs.getInt("max_users"));
		plan.setFeaturesJson(rs.getString("features_json"));
		plan.setActive(rs.getBoolean("active"));
		return plan;
	};

	private final JdbcTemplate masterJdbcTemplate;

	public PlanRepository(@Qualifier("masterJdbcTemplate") JdbcTemplate masterJdbcTemplate) {
		this.masterJdbcTemplate = masterJdbcTemplate;
	}

	public List<Plan> findAllActive() {
		return masterJdbcTemplate.query(
				"""
						SELECT id, code, name, tagline, price_cents, billing_period, cta_label,
						       highlighted, max_todos, max_users, features_json, active
						FROM plans
						WHERE active = 1
						ORDER BY price_cents ASC, id ASC
						""",
				PLAN_ROW_MAPPER);
	}

	public Optional<Plan> findActiveByCode(String code) {
		try {
			Plan plan = masterJdbcTemplate.queryForObject(
					"""
							SELECT id, code, name, tagline, price_cents, billing_period, cta_label,
							       highlighted, max_todos, max_users, features_json, active
							FROM plans
							WHERE code = ? AND active = 1
							""",
					PLAN_ROW_MAPPER,
					code);
			return Optional.ofNullable(plan);
		}
		catch (EmptyResultDataAccessException ex) {
			return Optional.empty();
		}
	}

	public Optional<Plan> findByTenantKey(String tenantKey) {
		try {
			Plan plan = masterJdbcTemplate.queryForObject(
					"""
							SELECT p.id, p.code, p.name, p.tagline, p.price_cents, p.billing_period,
							       p.cta_label, p.highlighted, p.max_todos, p.max_users,
							       p.features_json, p.active
							FROM tenants t
							INNER JOIN plans p ON p.id = t.plan_id
							WHERE t.tenant_key = ?
							""",
					PLAN_ROW_MAPPER,
					tenantKey);
			return Optional.ofNullable(plan);
		}
		catch (EmptyResultDataAccessException ex) {
			return Optional.empty();
		}
	}

	public Optional<String> findSubscriptionStatusByTenantKey(String tenantKey) {
		try {
			String status = masterJdbcTemplate.queryForObject(
					"SELECT subscription_status FROM tenants WHERE tenant_key = ?",
					String.class,
					tenantKey);
			return Optional.ofNullable(status);
		}
		catch (EmptyResultDataAccessException ex) {
			return Optional.empty();
		}
	}

	public Optional<LocalDateTime> findCurrentPeriodEndByTenantKey(String tenantKey) {
		try {
			Timestamp periodEnd = masterJdbcTemplate.queryForObject(
					"SELECT current_period_end FROM tenants WHERE tenant_key = ?",
					Timestamp.class,
					tenantKey);
			return Optional.ofNullable(periodEnd).map(Timestamp::toLocalDateTime);
		}
		catch (EmptyResultDataAccessException ex) {
			return Optional.empty();
		}
	}

	public void markSubscriptionExpired(String tenantKey) {
		masterJdbcTemplate.update(
				"""
						UPDATE tenants
						SET subscription_status = 'expired'
						WHERE tenant_key = ?
						  AND current_period_end IS NOT NULL
						  AND current_period_end < CURRENT_TIMESTAMP
						  AND subscription_status <> 'expired'
						""",
				tenantKey);
	}
}
