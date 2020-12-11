package com.github.lehnerj.jug202012.cpu.waitnetwork;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CpuWaitNetworkLongStartupDuration {
    private static final Logger logger = LoggerFactory.getLogger(CpuWaitNetworkLongStartupDuration.class);
    private final GeoServiceRestClient geoServiceRestClient = new GeoServiceRestClient();

    public static void main(String[] args) {
        new CpuWaitNetworkLongStartupDuration().prepareData();
    }

    private void prepareData() {
        List<TenantWaitNetwork> allTenants = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            int finalI = i;
            Supplier<Long> sup = () -> geoServiceRestClient.readGeoMappings("tenant-" + finalI);
            allTenants.add(new TenantWaitNetwork("Tenant-#" + i, sup));
        }
        startUpAllTenants(allTenants);
    }

    private void startUpAllTenants(List<TenantWaitNetwork> allTenants) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (TenantWaitNetwork tenant : allTenants) {
            tenant.startUpTenant();
        }
        stopwatch.stop();
        logger.info(String.format("Starting up took: %d ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }
}
