package com.dispatchflow.products;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

	private final ProductRepository products;

	public ProductService(ProductRepository products) {
		this.products = products;
	}

	@Transactional
	public Product create(String sku, String name, String description, BigDecimal price) {
		String normalizedSku = sku.trim().toUpperCase(Locale.ROOT);
		if (products.existsBySku(normalizedSku)) {
			throw new ProductAlreadyExistsException(normalizedSku);
		}
		return products.save(new Product(normalizedSku, name.trim(), description, price));
	}

	@Transactional(readOnly = true)
	public Product get(UUID id) {
		return products.findById(id)
				.orElseThrow(() -> new ProductNotFoundException(id));
	}
}
