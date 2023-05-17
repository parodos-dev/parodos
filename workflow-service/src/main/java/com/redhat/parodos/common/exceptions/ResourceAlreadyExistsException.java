package com.redhat.parodos.common.exceptions;

public class ResourceAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceAlreadyExistsException(String msg) {
		super(msg);
	}

	public ResourceAlreadyExistsException(ResourceType resourceType, IDType idType, String resourceId) {
		super(String.format("%s with %s: %s already exists", resourceType.getName(), idType.getType(), resourceId));
	}

}
