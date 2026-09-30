package com.dispatchflow.products;

import com.dispatchflow.common.api.ResourceNotFoundException;
import java.util.UUID;

public class ProductNotFoundException extends ResourceNotFoundException {
	public ProductNotFoundException(UUID id) { super("Product not found: " + id); }
}
