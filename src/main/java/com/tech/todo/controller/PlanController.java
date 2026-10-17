package com.tech.todo.controller;

import com.tech.todo.dto.PlanDto;
import com.tech.todo.service.PlanService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/plans")
public class PlanController {

	@Autowired
	private PlanService planService;

	@GetMapping
	public ResponseEntity<List<PlanDto>> listAllPlans() {
		List<PlanDto> plans = planService.getAllPlans();
		return new ResponseEntity<>(plans, HttpStatus.OK);
	}
}
