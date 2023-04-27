/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.patterndetection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */

class ScanningThreadPoolTest {

	@Test
	void testGetThreadPoolExecutor() {
		// Test the default thread pool executor
		ExecutorService executor = ScanningThreadPool.getThreadPoolExecutor();
		assertNotNull(executor);
		assertEquals(10, ((ThreadPoolExecutor) executor).getCorePoolSize());
		assertEquals(20, ((ThreadPoolExecutor) executor).getMaximumPoolSize());
		assertEquals(60, ((ThreadPoolExecutor) executor).getKeepAliveTime(TimeUnit.SECONDS));
		assertEquals(1000, ((ThreadPoolExecutor) executor).getQueue().remainingCapacity());
		assertTrue(executor instanceof ThreadPoolExecutor);
		assertTrue(((ThreadPoolExecutor) executor).getRejectedExecutionHandler() instanceof AbortPolicy);
	}

	@Test
	void testGetThreadPoolExecutorWithCustomParams() {
		// Test a custom thread pool executor
		BlockingQueue<Runnable> boundedQueue = new ArrayBlockingQueue<>(2000);
		RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
		ExecutorService executor = ScanningThreadPool.getThreadPoolExecutor(5, 10, 30, TimeUnit.SECONDS, boundedQueue, handler, true);
		assertNotNull(executor);
		assertEquals(5, ((ThreadPoolExecutor) executor).getCorePoolSize());
		assertEquals(10, ((ThreadPoolExecutor) executor).getMaximumPoolSize());
		assertEquals(30, ((ThreadPoolExecutor) executor).getKeepAliveTime(TimeUnit.SECONDS));
		assertEquals(2000, ((ThreadPoolExecutor) executor).getQueue().remainingCapacity());
		assertTrue(executor instanceof ThreadPoolExecutor);
		assertTrue(((ThreadPoolExecutor) executor).getRejectedExecutionHandler() instanceof ThreadPoolExecutor.CallerRunsPolicy);
	}
	
	@Test
	void testShutDownExecutorService() throws InterruptedException {
		// Test the shutdown of the executor service
		ExecutorService executor = ScanningThreadPool.getThreadPoolExecutor();
		assertFalse(executor.isShutdown());
		ScanningThreadPool.shutDownExecutorService();
		assertTrue(executor.isShutdown());
		assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
	}
}
