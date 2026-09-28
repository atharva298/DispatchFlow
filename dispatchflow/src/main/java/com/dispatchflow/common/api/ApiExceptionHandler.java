package com.dispatchflow.common.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

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
