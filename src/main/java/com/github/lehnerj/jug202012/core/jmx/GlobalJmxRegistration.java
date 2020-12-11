package com.github.lehnerj.jug202012.core.jmx;

import com.google.common.collect.ImmutableMap;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ClassUtils.getPackageName;
import static org.apache.commons.lang3.ClassUtils.getSimpleName;

/**
 * A <em>singleton</em> global JMX registration for <em>MBeans</em>.
 */
@ParametersAreNonnullByDefault
@Immutable
public enum GlobalJmxRegistration {

    INSTANCE;

    @GuardedBy("this")
    private final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

    /**
     * Registers an existing object as an MBean with the MBean server.
     * <p>
     * <b>Note:</b> If the {@code domain} is empty, it implies the default domain for the MBean Server where the MBean is used.
     *
     * @param mbean         the MBean to register
     * @param domain        the domain part for the MBean Server where the MBean is used
     * @param keyProperties the key property map (e.g. <b>type=X,name=Y</b> is enough to name an MBean)
     * @return {@code true} if MBean registration was successful, {@code false} otherwise
     * @throws NullPointerException if any of the passed arguments are {@code null}
     * @throws JMException          if MBean registration with the MBean server failed
     */
    public boolean register(final Object mbean, final String domain, final Map<String, String> keyProperties) throws JMException {
        return internalRegister(requireNonNull(mbean), toObjectName(requireNonNull(domain), requireNonNull(keyProperties)));
    }

    private static ObjectName toObjectName(final String domain, final Map<String, String> keyProperties) throws MalformedObjectNameException {
        return new ObjectName(keyProperties.entrySet().stream()
                .filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(joining(",", String.format("%s:", domain), "")));
    }

    /**
     * Registers an existing object as an MBean with the MBean server.
     * <p>
     * <b>Note:</b> The MBeans <b>package name</b> is used as the <b>domain part</b> and the <b>class name</b> as the <b>type=</b> key property.
     *
     * @param mbean the MBean to register
     * @param name  the object name of the MBean (i.e. the <b>name=</b> key property) in addition to the <b>type=</b> key property
     * @return {@code true} if MBean registration was successful, {@code false} otherwise
     * @throws NullPointerException if any of the passed arguments are {@code null}
     * @throws JMException          if MBean registration with the MBean server failed
     */
    public boolean register(final Object mbean, final String name) throws JMException {
        requireNonNull(mbean);
        return register(mbean, getPackageName(mbean.getClass()), toKeyProperties(mbean, name));
    }

    private static ImmutableMap<String, String> toKeyProperties(final Object mbean, final String name) {
        return ImmutableMap.of("type", getSimpleName(mbean.getClass()),
                "name", name);
    }

    /**
     * Unregisters an existing MBean from the MBean server.
     * <p>
     * <b>Note:</b> If the {@code domain} is empty, it implies the default domain for the MBean Server where the MBean is used.
     *
     * @param domain        the domain part for the MBean Server where the MBean is used
     * @param keyProperties the key property map (e.g. <em>type=X,name=Y</em> is enough to name an MBean)
     * @return {@code true} if MBean deregistration was successful, {@code false} otherwise
     * @throws JMException if MBean deregistration from the MBean server failed
     */
    public boolean unregister(final String domain, final Map<String, String> keyProperties) throws JMException {
        return internalUnregister(toObjectName(requireNonNull(domain), requireNonNull(keyProperties)));
    }

    /**
     * Unregisters an existing MBean from the MBean server.
     * <p>
     * <b>Note:</b> The MBeans <em>package name</em> is used as the <em>domain part</em> and the <em>class name</em> as the <em>type=</em> key property.
     *
     * @param mbean the MBean to unregister
     * @param name  the object name of the MBean (i.e. the <b>name=</b> key property) in addition to the <b>type=</b> key property
     * @return {@code true} if MBean deregistration was successful, {@code false} otherwise
     * @throws JMException if MBean deregistration from the MBean server failed
     */
    public boolean unregister(final Object mbean, final String name) throws JMException {
        requireNonNull(mbean);
        return unregister(getPackageName(mbean.getClass()), toKeyProperties(mbean, name));
    }

    private boolean internalRegister(final Object mbean, final ObjectName objectName) throws JMException {
        synchronized (this) {
            if (mbeanServer.isRegistered(objectName)) {
                return false;
            }
            // register unwrapped
            mbeanServer.registerMBean(mbean, objectName);
            return true;
        }
    }

    private boolean internalUnregister(final ObjectName objectName) throws JMException {
        synchronized (this) {
            if (mbeanServer.isRegistered(objectName)) {
                mbeanServer.unregisterMBean(objectName);
                return true;
            }
            return false;
        }
    }
}
