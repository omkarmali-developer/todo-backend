package com.tech.todo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Master-DB plan row. Queried via {@link com.tech.todo.repository.PlanRepository}
 * (JdbcTemplate on todo_master), not JPA — so tenant routing cannot hit the wrong DB.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

	private Long id;
	private String code;
	private String name;
	private String tagline;
	private int priceCents;
	private String billingPeriod;
	private String ctaLabel;
	private boolean highlighted;
	private Integer maxTodos;
	private Integer maxUsers;
	private String featuresJson;
	private boolean active;
}
