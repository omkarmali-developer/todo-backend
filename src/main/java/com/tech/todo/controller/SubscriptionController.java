package com.tech.todo.controller;

import com.tech.todo.dto.SubscriptionDto;
import com.tech.todo.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

	@Autowired
	private SubscriptionService subscriptionService;

	@GetMapping
	public ResponseEntity<SubscriptionDto> getCurrentSubscription() {
		SubscriptionDto subscription = subscriptionService.getCurrentSubscription();
		return new ResponseEntity<>(subscription, HttpStatus.OK);
	}
}
