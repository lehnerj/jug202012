package com.github.lehnerj.jug202012.core.thread;

public interface ThreadPoolStatistics {
	/**
	 * @return the number of currently active/busy threads
	 */
	int getActiveThreadStatistics();

	/**
	 * @return the maximum number of allowed threads in the pool
	 */
	int getMaxSizeStatistics();

	/**
	 * @return the number of waiting runnables in the pool's queue
	 */
	int getQueueSizeStatistics();

	/**
	 * @return largest number of threads that have ever simultaneously been in the pool.
	 */
	int getLargestPoolSize();

	/**
	 * @return the current number of threads in the pool.
	 */
	int getPoolSize();
}