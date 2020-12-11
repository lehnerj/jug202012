package com.github.lehnerj.jug202012.cpu.disk;

import com.google.common.base.Stopwatch;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CpuDiskLongStartupDuration {
    private static final Logger logger = LoggerFactory.getLogger(CpuDiskLongStartupDuration.class);
    private static String folderStr;

    public static void main(String[] args) {
        parseCommandLineArgs(args);
        prepareData();
    }

    private static void prepareData() {
        List<TenantDisk> allTenants = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            final String diskFolder = folderStr;
            allTenants.add(new TenantDisk("Tenant-#" + i, new File(diskFolder + "/tenant-" + i)));
        }
        startUpAllTenants(allTenants);
    }

    private static void startUpAllTenants(List<TenantDisk> allTenants) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (TenantDisk tenant : allTenants) {
            tenant.startUpTenant();
        }
        stopwatch.stop();
        logger.info(String.format("Starting up took: %d ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }

    private static void parseCommandLineArgs(String[] args) {
        Options options = new Options();

        final String folderWithTenants = "folder";
        Option folder = new Option(folderWithTenants, folderWithTenants, true, "Folder with tenant - structure");
        folder.setRequired(true);
        options.addOption(folder);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            folderStr = cmd.getOptionValue(folder.getOpt());

            logger.info(CpuDiskLongStartupDuration.class.getName());
            logger.info("Folder=" + folderStr);
        } catch (ParseException e) {
            logger.info(e.getMessage());
            formatter.printHelp(CpuDiskLongStartupDuration.class.getName(), options);

            System.exit(1);
        }
    }
}
