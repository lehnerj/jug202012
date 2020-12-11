package com.github.lehnerj.jug202012.cpu.disk;

import com.google.common.base.Stopwatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TenantDisk {
    private final String tenantName;
    private final File folder;

    public TenantDisk(String tenantName, File folder) {
        this.tenantName = tenantName;
        this.folder = folder;
    }

    public void startUpTenant() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        long bytesSum = 0;
        if (folder.isDirectory() && folder.exists()) {
            try (Stream<Path> paths = Files.walk(Paths.get(folder.toURI()))) {
                final List<Path> allFiles = paths.filter(Files::isRegularFile).collect(Collectors.toList());
                for (Path file : allFiles) {
                    bytesSum += readFileContent(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopwatch.stop();
        System.out.printf("DONE starting up '%s', took %d ms, read bytes %d%n", tenantName, stopwatch.elapsed(TimeUnit.MILLISECONDS), bytesSum);
    }

    private long readFileContent(Path path) {
        AtomicLong bytesSum = new AtomicLong();
        final byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
            bytesSum.addAndGet(bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytesSum.get();
    }
}
