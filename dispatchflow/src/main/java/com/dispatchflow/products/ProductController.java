package com.dispatchflow.products;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductService products;

	public ProductController(ProductService products) {
		this.products = products;
	}

	@PostMapping
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
		Product product = products.create(request.sku(), request.name(), request.description(), request.price());
		URI location = URI.create("/api/v1/products/" + product.getId());
		return ResponseEntity.created(location).body(ProductResponse.from(product));
	}

	@GetMapping("/{id}")
	public ProductResponse get(@PathVariable UUID id) {
		return ProductResponse.from(products.get(id));
	}
}
