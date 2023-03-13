package com.redhat.parodos.workflow.version;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class WorkFlowVersionServiceImplTest {

	private WorkFlowVersionServiceImpl service;

	@BeforeEach
	public void setUp() {
		this.service = new WorkFlowVersionServiceImpl();
	}

	@Test
	public void testGetHash() {
		// given
		Object obj = new Object();

		// when
		assertDoesNotThrow(() -> {
			String res = this.service.getHash(obj);
			assertEquals(res.toString(), "b0c8ed039dc102c0bab6a5e979931a0b");
		});
	}

	@Test
	public void testInvalidGetHash() {
		// when
		assertThrows(RuntimeException.class, () -> {
			String res = this.service.getHash(null);
			assertEquals(res.toString(), "");
		});
	}

}