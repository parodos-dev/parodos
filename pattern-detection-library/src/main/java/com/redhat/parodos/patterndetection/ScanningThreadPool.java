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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

/**
 * Provides access to the ThreadPool executor for all Scans to share.
 *
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class ScanningThreadPool {

	private static ExecutorService executorService;

	private ScanningThreadPool() {
	}

	/**
	 * Gets a ExecutorService with defaults configured
	 * 
	 * @return new default ExecutorService, or the existing one if this method has already been called
	 */
	public static synchronized ExecutorService getThreadPoolExecutor() {
		if (executorService == null) {
			BlockingQueue<Runnable> boundedQueue = new ArrayBlockingQueue<>(1000);
			executorService = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, boundedQueue, new AbortPolicy());
		}
		return executorService;
	}

	/**
	 * This will create a  overwrite the existing ExecutorService with the specified values
	 * @param corePoolSize
	 * @param maxPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param blockingQueue
	 * @param rejectedExecutionHandler
	 * @return
	 */
	public static synchronized ExecutorService getThreadPoolExecutor(int corePoolSize, int maxPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> blockingQueue, RejectedExecutionHandler rejectedExecutionHandler, boolean overWriteExistingExecutor) {
		if (executorService == null || overWriteExistingExecutor) {
			executorService = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, blockingQueue,rejectedExecutionHandler);
		}
		return executorService;
	}
	
	/**
	 * Does a graceful shut down of the thread pool. All running threads will complete and no new threads will be accepted
	 */
	public static synchronized void shutDownExecutorService() {
		if (executorService != null) {
			executorService.shutdown();
		}
	}

}
