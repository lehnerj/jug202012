package com.github.lehnerj.jug202012.core.thread;

import com.github.lehnerj.jug202012.core.jmx.DefaultScheduledThreadPoolExecutor;
import com.github.lehnerj.jug202012.core.jmx.GlobalJmxRegistration;
import com.google.common.collect.ImmutableMap;

import javax.management.JMException;
import java.util.Map;

public class ThreadPoolFactory {
    private static final String MBEAN_DOMAIN_PART = "com.example.threadpools";

    public static DefaultScheduledThreadPoolExecutor createNewMonitoredThreadPool(int size, String threadBaseName) {
        final ThreadPoolMonitoringMXBeanImpl threadPoolMonitoringMXBean;
        final DefaultScheduledThreadPoolExecutor threadPool = new DefaultScheduledThreadPoolExecutor(size, threadBaseName);
        threadPoolMonitoringMXBean = new ThreadPoolMonitoringMXBeanImpl(threadPool);
        try {
            Map<String, String> keyVal = ImmutableMap.of("name", "Thread Pool Statistics - " + threadBaseName);
            GlobalJmxRegistration.INSTANCE.register(threadPoolMonitoringMXBean, MBEAN_DOMAIN_PART, keyVal);
        } catch (JMException e) {
            throw new RuntimeException(e);
        }
        return threadPool;
    }
}
