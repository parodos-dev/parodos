package com.redhat.parodos.common.exceptions;

/**
 * The enum describes the type of the resource ID
 */
public enum IDType {

	ID("ID"), NAME("Name");

	private final String type;

	IDType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
