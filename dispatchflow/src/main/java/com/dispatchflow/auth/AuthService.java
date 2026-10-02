package com.dispatchflow.auth;

import com.dispatchflow.common.api.ResourceNotFoundException;
import com.dispatchflow.users.User;
import com.dispatchflow.users.UserRepository;
import com.dispatchflow.users.UserResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final String issuer;
	private final long ttlSeconds;

	public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
			@Value("${dispatchflow.jwt.issuer}") String issuer,
			@Value("${dispatchflow.jwt.ttl-seconds}") long ttlSeconds) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.issuer = issuer;
		this.ttlSeconds = ttlSeconds;
	}

	@Transactional(readOnly = true)
	public AuthTokenResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		User user = users.findByEmail(email)
				.filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(ttlSeconds, ChronoUnit.SECONDS);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim("roles", java.util.List.of(user.getRole().name()))
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new AuthTokenResponse(token, "Bearer", expiresAt, UserResponse.from(user));
	}
}
