package com.github.lehnerj.jug202012.core.queue;

import com.github.lehnerj.jug202012.core.jmx.MyMXBeanConstants;
import com.github.lehnerj.jug202012.core.jmx.GlobalJmxRegistration;
import com.google.common.collect.ImmutableMap;

import javax.management.JMException;
import java.util.Collection;
import java.util.Map;

public class QueueFactory {

    public static <T> Collection<T> createNewMonitoredQueue(String queueName, Collection<T> collection) {
        QueueMonitoringMXBeanImpl mxBean = new QueueMonitoringMXBeanImpl(collection);
        try {
            Map<String, String> keyVal = ImmutableMap.of("name", "Queue Statistics - " + queueName);
            GlobalJmxRegistration.INSTANCE.register(mxBean, MyMXBeanConstants.MBEAN_DOMAIN_PART+"queue", keyVal);
        } catch (JMException e) {
            throw new RuntimeException(e);
        }
        return collection;
    }
}
