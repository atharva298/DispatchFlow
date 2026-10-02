package com.dispatchflow.auth;

import com.dispatchflow.users.UserResponse;
import java.time.Instant;

public record AuthTokenResponse(
		String accessToken,
		String tokenType,
		Instant expiresAt,
		UserResponse user) {
}
