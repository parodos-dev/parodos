package com.redhat.parodos.workflow.version;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkFlowVersionServiceImplTest {

	private WorkFlowVersionServiceImpl workFlowVersionService;

	@BeforeEach
	public void setUp() {
		this.workFlowVersionService = new WorkFlowVersionServiceImpl();
	}

	@Test
	public void testGetHashSameValue() {
		// given
		String foo1 = new String("foo");
		String foo2 = new String("foo");

		assertNotSame(foo1, foo2);

		// when
		assertDoesNotThrow(() -> {
			String hashFoo1 = this.workFlowVersionService.getHash(foo1);
			assertNotNull(hashFoo1);
			assertFalse(hashFoo1.isEmpty());
			assertFalse(hashFoo1.isBlank());

			String hashFoo2 = this.workFlowVersionService.getHash(foo2);
			assertNotNull(hashFoo2);
			assertFalse(hashFoo2.isEmpty());
			assertFalse(hashFoo2.isBlank());

			assertEquals(hashFoo1, hashFoo2);

		});
	}

	@Test
	public void testGetHashDifferentValue() {
		// given
		String foo = "foo";
		String bar = "bar";

		// when
		assertDoesNotThrow(() -> {
			String hashFoo1 = this.workFlowVersionService.getHash(foo);
			assertNotNull(foo);
			assertFalse(foo.isEmpty());
			assertFalse(foo.isBlank());

			String hashFoo2 = this.workFlowVersionService.getHash(bar);
			assertNotNull(bar);
			assertFalse(bar.isEmpty());
			assertFalse(bar.isBlank());

			assertNotEquals(foo, bar);
		});
	}

	@Test
	public void testInvalidGetHash() {
		// when
		assertThrows(RuntimeException.class, () -> {
			String hash = this.workFlowVersionService.getHash(null);
			assertEquals(hash, "");
		});
	}

}
