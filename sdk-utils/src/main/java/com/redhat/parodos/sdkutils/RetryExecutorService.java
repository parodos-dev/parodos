package com.redhat.parodos.sdkutils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RetryExecutorService<T> implements AutoCloseable {

	private final ExecutorService executor;

	public RetryExecutorService() {
		executor = Executors.newFixedThreadPool(1);
	}

	/**
	 * Submit a task to the executor service, retrying on failure until the task succeeds
	 * @param task The task to submit
	 * @return The result of the task
	 */
	public T submitWithRetry(Callable<T> task) {
		// @formatter:off
		return submitWithRetry(task, () -> {}, () -> {}, 10 * 60 * 1000, 5000);
		// @formatter:on
	}

	/**
	 * Submit a task to the executor service, retrying on failure until the task
	 * @param task The task to submit
	 * @param onSuccess A callback to invoke when the task succeeds
	 * @param onFailure A callback to invoke when the task fails
	 * @param maxRetryTime The maximum time to retry the task for
	 * @param retryDelay The delay between retries
	 * @return The result of the task
	 */
	public T submitWithRetry(Callable<T> task, Runnable onSuccess, Runnable onFailure, long maxRetryTime,
			long retryDelay) {
		Future<T> future = executor.submit(() -> {
			long startTime = System.currentTimeMillis();
			long endTime = startTime + maxRetryTime;

			while (System.currentTimeMillis() < endTime) {
				try {
					T result = task.call();
					onSuccess.run();
					return result; // Success, no need to retry
				}
				catch (Exception e) {
					// Task failed, invoke onFailure callback
					onFailure.run();

					// Sleep for the retry delay
					try {
						// FIXME: This is a blocking call, we should use a non-blocking
						// sleep
						Thread.sleep(retryDelay);
					}
					catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						return null; // Interrupted, exit the task
					}
				}
			}

			return null; // Retry limit reached
		});

		try {
			return future.get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws Exception {
		executor.shutdown();
		boolean awaited = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		if (!awaited) {
			throw new RuntimeException("Failed to await termination of executor service");
		}
	}

}
