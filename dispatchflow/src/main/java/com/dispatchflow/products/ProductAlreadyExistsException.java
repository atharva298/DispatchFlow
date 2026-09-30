package com.dispatchflow.products;

import com.dispatchflow.common.api.ConflictException;

public class ProductAlreadyExistsException extends ConflictException {
	public ProductAlreadyExistsException(String sku) { super("A product with SKU '" + sku + "' already exists"); }
}
