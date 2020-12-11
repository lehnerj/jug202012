package com.github.lehnerj.jug202012.slowallocpulockdemoapp;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@EnableScheduling
public class MyAttributeService {

    private static final Logger logger = LoggerFactory.getLogger(MyAttributeService.class);
    private static final MyAttributeCache CACHE = new MyAttributeCache();

    public MyAttributeService() {

    }

    @GetMapping("/attribute")
    public MyAttribute getAttribute(@RequestParam(value = "id", defaultValue = "") String id) {
        return new MyAttributeBusinessLogic(CACHE).getAttribute(id);
    }

    @GetMapping("/attributes")
    public List<String> getAllAttributes() {
        if(CACHE.getAttributes().isEmpty()) {
            CACHE.refreshData();
        }
        return CACHE.getAttributes().stream().map(m -> m.getId().toString()).collect(Collectors.toList());
    }

    @Scheduled(cron = "${job.lock.cron:-}")
    public void cacheRefresh() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        logger.info("Cron task - Cache refresh: " + LocalDateTime.now());
        CACHE.refreshData(new RefreshFromDbJob());
        logger.info("Cache refresh took " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + " ms");
    }

    private class RefreshFromDbJob implements Function<UUID, UUID> {

        @Override
        public UUID apply(UUID uuid) {
            try {
                logger.debug("Simulate refreshing from DB one item: " + LocalDateTime.now());
                Thread.sleep(TimeUnit.SECONDS.toMillis(30) / MyAttributeCache.SIZE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return uuid;
        }
    }

    @Scheduled(cron = "${job.highretainedmemory.cron:-}")
    public void scheduledHighRetainedMemory() {

        logger.info("Cron task - high retained memory: start retaining memory " + LocalDateTime.now());
        final List<byte[]> TMP = new ArrayList<>();
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final int iterations = 500; // MB (of 1 GB heap) allocation, which retains for max 120 sec
        for (int i = 0; i < iterations; i++) {
            final byte[] byteArray = new byte[1024*1024]; // 1 MB
            synchronized (TMP) {
                TMP.add(byteArray);
            }
            byteArray[0] = (byte) ThreadLocalRandom.current().nextInt(0, 100);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Cron task - high retained memory: start retaining memory - cleanup " + LocalDateTime.now());
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(120));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Cleanup size=" + TMP.size() + " hash=" + TMP.hashCode());
                synchronized (TMP) {
                    TMP.clear();
                }
                logger.info("Cron task - high retained memory: end retaining memory - cleanup " + LocalDateTime.now());
            }
        }).start();

        logger.info("Cron task - high retained memory: end retaining memory " + LocalDateTime.now() + "\ttook: " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + " ms" + " size:" + TMP.size() + " hash=" + TMP.hashCode());
    }

}
