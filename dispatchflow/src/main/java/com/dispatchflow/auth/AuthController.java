package com.dispatchflow.auth;

import com.dispatchflow.users.RegisterUserRequest;
import com.dispatchflow.users.UserResponse;
import com.dispatchflow.users.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final UserService userService;
	private final AuthService authService;

	public AuthController(UserService userService, AuthService authService) {
		this.userService = userService;
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
		return UserResponse.from(userService.register(request.name(), request.email(), request.password()));
	}

	@PostMapping("/login")
	public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}
}
