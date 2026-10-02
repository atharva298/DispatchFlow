package com.dispatchflow.users;

import com.dispatchflow.common.api.ConflictException;
import com.dispatchflow.common.api.InvalidRequestException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User register(String name, String email, String rawPassword) {
		if (rawPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new InvalidRequestException("Password must not exceed 72 UTF-8 bytes");
		}

		String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
		if (users.existsByEmail(normalizedEmail)) {
			throw new ConflictException("An account with that email already exists");
		}

		String encodedPassword = passwordEncoder.encode(rawPassword);
		return users.save(new User(name.trim(), normalizedEmail, encodedPassword, Role.CUSTOMER));
	}
}
