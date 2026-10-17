package com.tech.todo.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {

	private String code;
	private String name;
	private String tagline;
	private int priceCents;
	private String priceLabel;
	private String period;
	private String cta;
	private boolean highlighted;
	private List<String> features;
	private PlanLimits limits;
}
