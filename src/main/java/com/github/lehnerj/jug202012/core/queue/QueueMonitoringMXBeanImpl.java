package com.github.lehnerj.jug202012.core.queue;

import javax.management.MXBean;
import java.util.Collection;

@MXBean
public class QueueMonitoringMXBeanImpl implements QueueMonitoringMXBean {

    private Collection<?> queue;

    public QueueMonitoringMXBeanImpl(Collection<?> queue) {
        this.queue = queue;
    }

    @Override
    public int getQueueSize() {
        if (queue == null) {
            return -1;
        }
        return queue.size();
    }

}