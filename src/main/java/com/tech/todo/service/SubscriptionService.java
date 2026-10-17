package com.tech.todo.service;

import com.tech.todo.dto.SubscriptionDto;

public interface SubscriptionService {

	SubscriptionDto getCurrentSubscription();

	void assertCanCreateTodo();

	void assertCanRegisterUser();
}
