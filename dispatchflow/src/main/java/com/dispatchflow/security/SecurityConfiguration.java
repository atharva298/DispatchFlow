package com.dispatchflow.security;

import com.dispatchflow.common.api.ApiErrorResponse;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class SecurityConfiguration {
	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	SecretKey jwtSecretKey(@Value("${dispatchflow.jwt.secret}") String encodedSecret) {
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(encodedSecret);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("JWT_SECRET must be a Base64-encoded 256-bit key", exception);
		}
		if (keyBytes.length < 32) {
			throw new IllegalArgumentException("JWT_SECRET must decode to at least 32 bytes");
		}
		return new SecretKeySpec(keyBytes, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey key) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(key));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey key, @Value("${dispatchflow.jwt.issuer}") String issuer) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
		return decoder;
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName("roles");
		authorities.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authorities);
		return converter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper,
			JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/actuator/health").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers(HttpMethod.POST, "/api/v1/inventory/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers(HttpMethod.POST, "/api/v1/shipments/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers(HttpMethod.PATCH, "/api/v1/shipments/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.oauth2ResourceServer(resourceServer -> resourceServer
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
						.authenticationEntryPoint((request, response, exception) -> writeError(
								objectMapper, response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
								"Authentication is required or the credentials are invalid", request.getRequestURI())))
				.exceptionHandling(errors -> errors.accessDeniedHandler((request, response, exception) -> writeError(
						objectMapper, response, HttpStatus.FORBIDDEN, "FORBIDDEN",
						"You do not have permission to access this resource", request.getRequestURI())))
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable);
		return http.build();
	}

	private static void writeError(ObjectMapper objectMapper, jakarta.servlet.http.HttpServletResponse response,
			HttpStatus status, String error, String message, String path) throws java.io.IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(
				Instant.now(), status.value(), error, message, path, List.of()));
	}
}
