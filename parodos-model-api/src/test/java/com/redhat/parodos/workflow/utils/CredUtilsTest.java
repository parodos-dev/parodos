package com.redhat.parodos.workflow.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CredUtilsTest {

	@Test
	public void testGetBase64Creds() {
		String username = "user123";
		String password = "pass456";
		String expectedBase64Creds = "dXNlcjEyMzpwYXNzNDU2";

		String actualBase64Creds = CredUtils.getBase64Creds(username, password);

		assertEquals(expectedBase64Creds, actualBase64Creds);
	}

}
