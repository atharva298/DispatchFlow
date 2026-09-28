package com.dispatchflow.learning;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning/echo")
public class EchoController {

	@PostMapping
	public EchoResponse echo(@Valid @RequestBody EchoRequest request) {
		return new EchoResponse(request.message());
	}
}
