package com.redhat.parodos.common.exceptions;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String msg) {
		super(msg);
	}

	public ResourceNotFoundException(ResourceType resourceType, IDType idType, String resourceId) {
		super(String.format("%s with %s: %s not found", resourceType.getName(), idType.getType(), resourceId));
	}

	public ResourceNotFoundException(ResourceType resourceType, IDType idType, UUID resourceId) {
		super(String.format("%s with %s: %s not found", resourceType.getName(), idType.getType(),
				resourceId.toString()));
	}

}
