package com.dispatchflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class DispatchflowApplicationTests {

	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@DynamicPropertySource
	static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("dispatchflow.jwt.secret", () -> "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
	}

	@Test
	void createsProductAndPersistsItInPostgres() throws Exception {
		mockMvc.perform(get("/api/v1/products/not-a-uuid"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

		String email = "customer-" + UUID.randomUUID() + "@example.com";
		mockMvc.perform(post("/api/v1/auth/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"name":"Test Customer","email":"%s","password":"correct-horse-battery"}
							""".formatted(email)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());

		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"email":"%s","password":"correct-horse-battery"}
							""".formatted(email)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andReturn();
		String token = objectMapper.readTree(login.getResponse().getContentAsString())
				.get("accessToken").asString();

		String sku = "SKU-" + UUID.randomUUID();
		MvcResult creation = mockMvc.perform(post("/api/v1/products")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"sku":"%s","name":"Desk Lamp","description":"Warm white","price":24.50}
							""".formatted(sku)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.sku").value(sku.toUpperCase(Locale.ROOT)))
				.andReturn();

		mockMvc.perform(get(creation.getResponse().getHeader("Location"))
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sku").value(sku.toUpperCase(Locale.ROOT)))
				.andExpect(jsonPath("$.price").value(24.50));
	}
}
