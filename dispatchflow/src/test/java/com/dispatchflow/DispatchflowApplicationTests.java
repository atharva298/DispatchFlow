package com.dispatchflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;
import java.util.UUID;
import com.dispatchflow.users.Role;
import com.dispatchflow.users.User;
import com.dispatchflow.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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

	@Autowired
	private UserRepository users;

	@Autowired
	private PasswordEncoder passwordEncoder;

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

		String operatorEmail = "operator-" + UUID.randomUUID() + "@example.com";
		users.save(new User("Test Operator", operatorEmail,
				passwordEncoder.encode("operator-password-123"), Role.OPERATOR));
		MvcResult operatorLogin = mockMvc.perform(post("/api/v1/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"email":"%s","password":"operator-password-123"}
							""".formatted(operatorEmail)))
				.andExpect(status().isOk())
				.andReturn();
		String operatorToken = objectMapper.readTree(operatorLogin.getResponse().getContentAsString())
				.get("accessToken").asString();

		String sku = "SKU-" + UUID.randomUUID();
		mockMvc.perform(post("/api/v1/products")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"sku":"CUSTOMER-SHOULD-NOT-CREATE","name":"Forbidden","description":"No","price":1.00}
							"""))
				.andExpect(status().isForbidden());

		MvcResult creation = mockMvc.perform(post("/api/v1/products")
					.header("Authorization", "Bearer " + operatorToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"sku":"%s","name":"Desk Lamp","description":"Warm white","price":24.50}
							""".formatted(sku)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.sku").value(sku.toUpperCase(Locale.ROOT)))
				.andReturn();
		String productId = objectMapper.readTree(creation.getResponse().getContentAsString())
				.get("id").asString();

		mockMvc.perform(get(creation.getResponse().getHeader("Location"))
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sku").value(sku.toUpperCase(Locale.ROOT)))
				.andExpect(jsonPath("$.price").value(24.50));

		mockMvc.perform(post("/api/v1/inventory")
					.header("Authorization", "Bearer " + operatorToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"productId":"%s","warehouseId":"wh-west","availableQuantity":12}
							""".formatted(productId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.warehouseId").value("WH-WEST"))
				.andExpect(jsonPath("$.reservedQuantity").value(0));
		mockMvc.perform(post("/api/v1/inventory")
					.header("Authorization", "Bearer " + operatorToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"productId":"%s","warehouseId":"wh-east","availableQuantity":4}
							""".formatted(productId)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/inventory/" + productId)
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].warehouseId").value("WH-EAST"))
				.andExpect(jsonPath("$[0].availableQuantity").value(4))
				.andExpect(jsonPath("$[1].availableQuantity").value(12));

		MvcResult confirmedOrder = mockMvc.perform(post("/api/v1/orders")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"items":[{"productId":"%s","quantity":5}]}
							""".formatted(productId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("CONFIRMED"))
				.andExpect(jsonPath("$.totalAmount").value(122.50))
				.andExpect(jsonPath("$.items[0].unitPrice").value(24.50))
				.andReturn();
		String orderLocation = confirmedOrder.getResponse().getHeader("Location");
		mockMvc.perform(get(orderLocation).header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMED"));
		mockMvc.perform(get("/api/v1/orders").header("Authorization", "Bearer " + token)
					.param("page", "0").param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].id").isNotEmpty());

		mockMvc.perform(post(orderLocation + "/cancel").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));
		mockMvc.perform(post(orderLocation + "/cancel").header("Authorization", "Bearer " + token))
				.andExpect(status().isConflict());
		mockMvc.perform(get("/api/v1/inventory/" + productId)
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].availableQuantity").value(4))
				.andExpect(jsonPath("$[0].reservedQuantity").value(0))
				.andExpect(jsonPath("$[1].availableQuantity").value(12))
				.andExpect(jsonPath("$[1].reservedQuantity").value(0));

		mockMvc.perform(post("/api/v1/orders")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"items":[{"productId":"%s","quantity":17}]}
							""".formatted(productId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("CANCELLED"));

		mockMvc.perform(get("/api/v1/inventory/" + productId)
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].availableQuantity").value(4))
				.andExpect(jsonPath("$[0].reservedQuantity").value(0))
				.andExpect(jsonPath("$[1].availableQuantity").value(12))
				.andExpect(jsonPath("$[1].reservedQuantity").value(0));

		MvcResult shippingOrder = mockMvc.perform(post("/api/v1/orders")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"items":[{"productId":"%s","quantity":6}]}
							""".formatted(productId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("CONFIRMED"))
				.andReturn();
		String shippingOrderId = objectMapper.readTree(shippingOrder.getResponse().getContentAsString())
				.get("id").asString();
		MvcResult shipmentCreation = mockMvc.perform(post("/api/v1/shipments")
					.header("Authorization", "Bearer " + operatorToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"orderId":"%s","carrier":"FastShip","estimatedDeliveryDate":"2099-12-31"}
							""".formatted(shippingOrderId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("CREATED"))
				.andExpect(jsonPath("$.carrier").value("FastShip"))
				.andReturn();
		String trackingNumber = objectMapper.readTree(shipmentCreation.getResponse().getContentAsString())
				.get("trackingNumber").asString();
		mockMvc.perform(get("/api/v1/shipments/" + trackingNumber)
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").value(shippingOrderId));
		mockMvc.perform(get("/api/v1/orders/" + shippingOrderId)
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PROCESSING"));
		mockMvc.perform(post("/api/v1/shipments")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"orderId":"%s","carrier":"FastShip","estimatedDeliveryDate":"2099-12-31"}
							""".formatted(shippingOrderId)))
				.andExpect(status().isForbidden());
	}
}
