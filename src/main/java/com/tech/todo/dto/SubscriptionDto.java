package com.tech.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {

	private String tenantKey;
	private String planCode;
	private String planName;
	private String subscriptionStatus;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime currentPeriodEnd;
	private boolean expired;
	private String renewalMessage;
	private Integer maxTodos;
	private Integer maxUsers;
	private long todoCount;
	private long userCount;
}
