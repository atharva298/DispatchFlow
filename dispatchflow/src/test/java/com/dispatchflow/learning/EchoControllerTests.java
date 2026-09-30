package com.dispatchflow.learning;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dispatchflow.common.api.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EchoController.class)
@Import(ApiExceptionHandler.class)
class EchoControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void acceptsNonBlankMessage() throws Exception {
		mockMvc.perform(post("/api/v1/learning/echo")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"message\":\"hello\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("hello"));
	}

	@Test
	void returnsConsistentValidationErrorForBlankMessage() throws Exception {
		mockMvc.perform(post("/api/v1/learning/echo")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"message\":\"   \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.path").value("/api/v1/learning/echo"))
				.andExpect(jsonPath("$.details[0].field").value("message"));
	}
}
