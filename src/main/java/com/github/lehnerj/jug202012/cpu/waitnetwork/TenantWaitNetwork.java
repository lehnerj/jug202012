package com.github.lehnerj.jug202012.cpu.waitnetwork;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TenantWaitNetwork {
    private static final Logger logger = LoggerFactory.getLogger(TenantWaitNetwork.class);
    private final String tenantName;
    private final Supplier<Long> supplier;

    public TenantWaitNetwork(String tenantName, Supplier<Long> supplier) {
        this.tenantName = tenantName;
        this.supplier = supplier;
    }

    public void startUpTenant() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        Long bytes = supplier.get();
        stopwatch.stop();
        logger.info(String.format("DONE starting up '%s', took %d ms, read bytes %d", tenantName, stopwatch.elapsed(TimeUnit.MILLISECONDS), bytes));
    }
}
