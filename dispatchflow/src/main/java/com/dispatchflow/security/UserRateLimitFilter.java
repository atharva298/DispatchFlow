package com.dispatchflow.security;

import com.dispatchflow.common.api.ApiErrorResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class UserRateLimitFilter extends OncePerRequestFilter {

	private static final String COUNTER_SCRIPT = """
			local count = redis.call('INCR', KEYS[1])
			if count == 1 then
				redis.call('EXPIRE', KEYS[1], ARGV[1])
			end
			return count
			""";
	private static final DefaultRedisScript<Long> INCREMENT_COUNTER = new DefaultRedisScript<>(COUNTER_SCRIPT, Long.class);

	private final StringRedisTemplate redis;
	private final ObjectMapper objectMapper;
	private final int requestsPerWindow;
	private final int windowSeconds;

	UserRateLimitFilter(StringRedisTemplate redis, ObjectMapper objectMapper,
			int requestsPerWindow, int windowSeconds) {
		this.redis = redis;
		this.objectMapper = objectMapper;
		this.requestsPerWindow = requestsPerWindow;
		this.windowSeconds = windowSeconds;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (!request.getRequestURI().startsWith("/api/v1/")) {
			filterChain.doFilter(request, response);
			return;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			filterChain.doFilter(request, response);
			return;
		}

		String key = "dispatchflow:rate-limit:user:" + authentication.getName();
		Long requestCount = redis.execute(INCREMENT_COUNTER, List.of(key), Integer.toString(windowSeconds));
		if (requestCount == null) {
			throw new IllegalStateException("Redis did not return a rate-limit counter value");
		}

		response.setHeader("X-RateLimit-Limit", Integer.toString(requestsPerWindow));
		response.setHeader("X-RateLimit-Remaining", Long.toString(Math.max(0, requestsPerWindow - requestCount)));
		if (requestCount > requestsPerWindow) {
			long retryAfter = Math.max(1, redis.getExpire(key));
			response.setHeader("Retry-After", Long.toString(retryAfter));
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(
					Instant.now(), HttpStatus.TOO_MANY_REQUESTS.value(), "RATE_LIMIT_EXCEEDED",
					"Request limit exceeded. Try again after " + retryAfter + " seconds.",
					request.getRequestURI(), List.of()));
			return;
		}

		filterChain.doFilter(request, response);
	}
}
