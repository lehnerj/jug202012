package com.github.lehnerj.jug202012.core.thread;

import javax.management.MXBean;

@MXBean
public class ThreadPoolMonitoringMXBeanImpl implements ThreadPoolMonitoringMXBean {

    private ThreadPoolStatistics stage1ThreadPoolStatistics;

    public ThreadPoolMonitoringMXBeanImpl(ThreadPoolStatistics stage1ThreadPoolStatistics) {
        this.stage1ThreadPoolStatistics = stage1ThreadPoolStatistics;
    }

    @Override
    public int getQueueSizeOfThreadPool() {
        if (stage1ThreadPoolStatistics == null) {
            return -1;
        }
        return stage1ThreadPoolStatistics.getQueueSizeStatistics();
    }

    @Override
    public int getMaxSizeOfThreadPool() {
        if (stage1ThreadPoolStatistics == null) {
            return -1;
        }
        return stage1ThreadPoolStatistics.getMaxSizeStatistics();
    }

    @Override
    public int getActiveThreadsOfThreadPool() {
        if (stage1ThreadPoolStatistics == null) {
            return -1;
        }
        return stage1ThreadPoolStatistics.getActiveThreadStatistics();
    }

}