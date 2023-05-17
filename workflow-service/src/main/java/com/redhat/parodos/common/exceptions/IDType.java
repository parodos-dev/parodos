package com.redhat.parodos.common.exceptions;

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
