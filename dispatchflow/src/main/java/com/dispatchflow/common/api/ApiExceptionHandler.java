package com.dispatchflow.common.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		List<ApiErrorResponse.FieldErrorDetail> details = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new ApiErrorResponse.FieldErrorDetail(
						error.getField(), error.getDefaultMessage()))
				.toList();

		return errorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
				"Invalid request", request.getRequestURI(), details);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
			HttpMessageNotReadableException exception,
			HttpServletRequest request) {
		return errorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
				"Request body is missing or malformed", request.getRequestURI(), List.of());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(
			ResourceNotFoundException exception, HttpServletRequest request) {
		return errorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
				exception.getMessage(), request.getRequestURI(), List.of());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(
			ConflictException exception, HttpServletRequest request) {
		return errorResponse(HttpStatus.CONFLICT, "CONFLICT",
				exception.getMessage(), request.getRequestURI(), List.of());
	}

	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
			InvalidRequestException exception, HttpServletRequest request) {
		return errorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
				exception.getMessage(), request.getRequestURI(), List.of());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleBadCredentials(HttpServletRequest request) {
		return errorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
				"Email or password is incorrect", request.getRequestURI(), List.of());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataConflict(HttpServletRequest request) {
		return errorResponse(HttpStatus.CONFLICT, "DATA_CONFLICT",
				"The request conflicts with existing data", request.getRequestURI(), List.of());
	}

	private ResponseEntity<ApiErrorResponse> errorResponse(
			HttpStatus status,
			String error,
			String message,
			String path,
			List<ApiErrorResponse.FieldErrorDetail> details) {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(), status.value(), error, message, path, details);
		return ResponseEntity.status(status).body(body);
	}
}
