package com.dispatchflow.users;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, Role role, Instant createdAt) {
	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
	}
}
