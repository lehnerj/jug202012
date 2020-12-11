package com.github.lehnerj.jug202012.core.thread;

public interface ThreadPoolMonitoringMXBean {

	int getQueueSizeOfThreadPool();
	int getMaxSizeOfThreadPool();
	int getActiveThreadsOfThreadPool();
}
