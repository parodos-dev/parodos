package com.redhat.parodos.sdkutils;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RetryExecutorService<T> implements AutoCloseable {

	private static final int MAX_RETRY_TIME = 4 * 60 * 1000; // 4 minutes

	public static final int RETRY_DELAY = 5 * 1000; // 5 seconds

	private final ScheduledExecutorService scheduledExecutor;

	public RetryExecutorService() {
		scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * Submit a task to the executor service, retrying on failure until the task succeeds
	 * @param task The task to submit
	 * @return The result of the task
	 */
	public T submitWithRetry(Callable<T> task) {
		// @formatter:off
		return submitWithRetry(task, () -> {}, () -> {}, MAX_RETRY_TIME, RETRY_DELAY);
		// @formatter:on
	}

	public T submitWithRetry(Callable<T> task, long maxRetryTime) {
		// @formatter:off
		return submitWithRetry(task, () -> {}, () -> {}, maxRetryTime, RETRY_DELAY);
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
		CompletableFuture<T> future = new CompletableFuture<>();
		long startTime = System.currentTimeMillis();
		long endTime = startTime + maxRetryTime;

		ScheduledFuture<?> scheduledFuture = scheduledExecutor.scheduleWithFixedDelay(() -> {
			if (System.currentTimeMillis() >= endTime) {
				future.completeExceptionally(new TimeoutException("Retry limit reached."));
				return;
			}

			try {
				T result = task.call();
				onSuccess.run();
				future.complete(result); // Success, complete the future with the result
			}
			catch (WorkFlowServiceUtils.InProgressStatusException e) {
				return;
			}
			catch (Exception e) {
				onFailure.run();
				future.completeExceptionally(e);
				return;
			}
		}, 0, retryDelay, TimeUnit.MILLISECONDS);

		try {
			return future.get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		finally {
			scheduledFuture.cancel(false);
			scheduledExecutor.shutdown();
		}
	}

	@Override
	public void close() throws Exception {
		boolean awaited = scheduledExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		if (!awaited) {
			throw new RuntimeException("Failed to await termination of executor service");
		}
	}

}
