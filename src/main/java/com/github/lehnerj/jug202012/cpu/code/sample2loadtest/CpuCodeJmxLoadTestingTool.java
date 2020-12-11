package com.github.lehnerj.jug202012.cpu.code.sample2loadtest;

import com.github.lehnerj.jug202012.core.queue.QueueFactory;
import com.github.lehnerj.jug202012.core.jmx.DefaultScheduledThreadPoolExecutor;
import com.github.lehnerj.jug202012.core.jmx.GlobalJmxRegistration;
import com.github.lehnerj.jug202012.core.thread.ThreadPoolFactory;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.github.lehnerj.jug202012.core.jmx.MyMXBeanConstants.MBEAN_DOMAIN_PART;

public class CpuCodeJmxLoadTestingTool {
    private static final Logger logger = LoggerFactory.getLogger(CpuCodeJmxLoadTestingTool.class);
    public static final double DEBUG_LOG_PROBABILITY = 0.99;
    private static double patternsPerSec;

    private static CpuLoadTestingMXBeanImpl mxBean;
    private static Queue<String> queueStageDispatcher;
    private static DefaultScheduledThreadPoolExecutor threadPoolStage2DiskWriter;
    private static boolean fixCpuCodeReplaceAllBug;
    private static boolean fixCpuLockBug;
    private static boolean fixCpuDiskIoBug;
    private static boolean fixCpuDiskIoSleepBug;

    public static void main(String[] args) throws Exception {
        parseCommandLineArgs(args);

        registerLoadTestingMxBean();
        queueStageDispatcher = (Queue<String>) QueueFactory.createNewMonitoredQueue("Stage-2-Dispatcher", new LinkedList<String>());
        threadPoolStage2DiskWriter = ThreadPoolFactory.createNewMonitoredThreadPool(10, "Stage-2-DiskWriter");

        Thread generator = createPatternGeneratorThread();
        Thread dispatcher = createDispatcherThread();

        generator.start();
        dispatcher.start();

        generator.join();
        dispatcher.join();
    }

    private static void parseCommandLineArgs(String[] args) {
        Options options = new Options();

        final String patternsPerSecOpt = "patternsPerSec";
        Option patterns = new Option(patternsPerSecOpt, patternsPerSecOpt, true, "Patterns / sec");
        patterns.setRequired(true);
        options.addOption(patterns);

        final String fixLockOpt = "fixCpuLock";
        Option fixCpuLock = new Option(fixLockOpt, fixLockOpt, true, "Fix the obvious lock problem");
        fixCpuLock.setRequired(true);
        options.addOption(fixCpuLock);

        final String fixReplaceAllOpt = "fixCpuCodeReplaceAll";
        Option fixCpuCodeReplaceAll = new Option(fixReplaceAllOpt, fixReplaceAllOpt, true, "Fix the replaceAll problem");
        fixCpuCodeReplaceAll.setRequired(true);
        options.addOption(fixCpuCodeReplaceAll);

        final String fixDiskIoOpt = "fixCpuDiskIo";
        Option fixCpuDiskIo = new Option(fixDiskIoOpt, fixDiskIoOpt, true, "Fix the disk writing problem");
        fixCpuDiskIo.setRequired(true);
        options.addOption(fixCpuDiskIo);

        final String fixDiskSleepIoOpt = "fixCpuDiskSleepIo";
        Option fixDiskSleepIo = new Option(fixDiskSleepIoOpt, fixDiskSleepIoOpt, true, "Fix the disk sleep writing problem");
        fixDiskSleepIo.setRequired(true);
        options.addOption(fixDiskSleepIo);


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            patternsPerSec = Integer.parseInt(cmd.getOptionValue(patterns.getOpt()));
            fixCpuCodeReplaceAllBug = Boolean.parseBoolean(cmd.getOptionValue(fixCpuCodeReplaceAll.getOpt(), "false"));
            fixCpuLockBug = Boolean.parseBoolean(cmd.getOptionValue(fixCpuLock.getOpt(), "false"));
            fixCpuDiskIoBug = Boolean.parseBoolean(cmd.getOptionValue(fixCpuDiskIo.getOpt(), "false"));
            fixCpuDiskIoSleepBug = Boolean.parseBoolean(cmd.getOptionValue(fixDiskSleepIo.getOpt(), "false"));

            logger.info("LoadTesting Tool");
            logger.info("PatternsPerSec=" + patternsPerSec);
            logger.info("Fix CPU Code ReplaceAll bug=" + fixCpuCodeReplaceAllBug);
            logger.info("Fix CPU Lock bug=" + fixCpuLockBug);
            logger.info("Fix CPU DiskIO sleep bug=" + fixCpuDiskIoSleepBug);
            logger.info("Fix CPU DiskIO bug=" + fixCpuDiskIoBug);
        } catch (ParseException e) {
            logger.info(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }
    }

    private static Thread createPatternGeneratorThread() {
        return new Thread(new Runnable() {
            private long delay;
            private long maxBehindSchedule;

            @Override
            public void run() {
                long patternIntervalNsec = Math.round(1000000.0 / patternsPerSec);
                long plannedTimeNsec = 0;
                long patternsGeneratedCount = 0;
                long actualTimeNsec;
                long startTime = System.currentTimeMillis();

                while (true) {
                    final Stopwatch stopwatch = Stopwatch.createStarted();
                    plannedTimeNsec += patternIntervalNsec;
                    actualTimeNsec = (System.currentTimeMillis() - startTime) * 1000;

                    setDelay(plannedTimeNsec, actualTimeNsec);
                    mxBean.setMaxBehindSchedule(maxBehindSchedule);

                    if (delay > 0) {
                        waitTime(delay);
                    }

                    generateOnePattern();

                    patternsGeneratedCount++;
                    mxBean.setPatternsGeneratedCount(patternsGeneratedCount);

                    if (ThreadLocalRandom.current().nextDouble() > 0.99) {
                        logger.info("Generation took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "   " + Thread.currentThread().getName());
                    }
                }
            }

            private void generateOnePattern() {
                final String part1 = "https://www.dynatrace.com/platform/application-performance-monitoring/";
                final String part2 = "+'()'~";
                final String part3 = RandomStringUtils.random(50);

                StringBuilder sb = new StringBuilder(part1.length() + part2.length() + part3.length());
                sb.append(part1).append(part2).append(part3);

                String result = emulateEncodeUriComponent(sb.toString());
                for (int i = 0; i < 30; i++) {
                    result = emulateEncodeUriComponent(result);
                }

                synchronized (queueStageDispatcher) {
                    queueStageDispatcher.add(result);
                }
            }

            /**
             * This function emulates the JavaScript function <code>encodeUriComponent</code>
             */
            private String emulateEncodeUriComponent(String string) {
                // FIXME ReplaceAll bug
                if (!CpuCodeJmxLoadTestingTool.fixCpuCodeReplaceAllBug) {
                    return emulateEncodeUriComponentV1(string);
                } else {
                    return emulateEncodeUriComponentV2WithFix(string);
                }
            }

            private String emulateEncodeUriComponentV1(String string) {
                string = string.replaceAll("\\+", "%20")
                        .replaceAll("\\%21", "!")
                        .replaceAll("\\%27", "'")
                        .replaceAll("\\%28", "(")
                        .replaceAll("\\%29", ")")
                        .replaceAll("\\%7E", "~");

                return string;
            }

            private String emulateEncodeUriComponentV2WithFix(String string) {
                StringBuilder sb = new StringBuilder(string);

                replaceAll(sb, "+", "%20");
                replaceAll(sb, "%21", "!");
                replaceAll(sb, "%27", "'");
                replaceAll(sb, "%28", "(");
                replaceAll(sb, "%29", ")");
                replaceAll(sb, "%7E", "~");

                return sb.toString();
            }

            private void replaceAll(StringBuilder sb, String s1, String s2) {
                // NOT TESTED BUT SHOULD PROBABLY WORK - does not handle recursion
                int index;
                while ((index = sb.indexOf(s1)) != -1) {
                    sb.replace(index, index + s1.length(), s2);
                }
            }

            private void waitTime(long delay) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            private synchronized void setDelay(long plannedTimeNsec, long actualTimeNsec) {
                delay = (plannedTimeNsec - actualTimeNsec) / 1000;
                if (-delay > 0) {
                    maxBehindSchedule = -delay;
                }
            }
        }, "LoadTestGeneratorThread");
    }

    private static Thread createDispatcherThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                // FIXME cpu lock sleep
                if (!CpuCodeJmxLoadTestingTool.fixCpuLockBug) {
                    dispatchV1();
                } else {
                    dispatchV2WithFix();
                }
            }

            private void dispatchV1() {
                while (true) {
                    List<String> l = new ArrayList<>();
                    synchronized (queueStageDispatcher) {
                        while (true) {
                            if (queueStageDispatcher.peek() == null) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }

                            l.add(queueStageDispatcher.remove());
                        }
                    }

                    dispatchElements(l);
                }
            }

            private void dispatchV2WithFix() {
                while (true) {
                    int peekCount = 0;
                    List<String> l = new ArrayList<>();
                    synchronized (queueStageDispatcher) {
                        while (true) {
                            if (queueStageDispatcher.peek() == null) {
                                break;
                            }
                            peekCount++;
                            l.add(queueStageDispatcher.remove());
                        }
                    }

                    if (peekCount == 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    dispatchElements(l);
                }
            }

            private void dispatchElements(List<String> l) {
                for (String s : l) {
                    threadPoolStage2DiskWriter.execute(new Runnable() {
                        @Override
                        public void run() {
                            final Stopwatch stopwatch = Stopwatch.createStarted();
                            final File dir = new File("tmp/loadtestingtool");
                            dir.mkdirs();

                            // FIXME cpu disk
                            if (!CpuCodeJmxLoadTestingTool.fixCpuDiskIoBug) {
                                writeFileV1(dir, s);
                            } else {
                                writeFileV2WithFix(dir, s);
                            }

                            if (ThreadLocalRandom.current().nextDouble() > DEBUG_LOG_PROBABILITY) {
                                logger.info("Disk write took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "   " + Thread.currentThread().getName());
                            }
                        }

                        private void writeFileV1(File dir, String s) {
                            try (FileWriter fw = new FileWriter(new File(dir, Thread.currentThread().getName() + "-" + ThreadLocalRandom.current().nextInt(0, 1) + ".txt"))) {
                                for (int i = 0; i < 10; i++) {
                                    final char[] chars = s.toCharArray();
                                    for (char aChar : chars) {
                                        fw.write(aChar);
                                    }
                                    fw.write("\n");
                                    if (!CpuCodeJmxLoadTestingTool.fixCpuDiskIoSleepBug) {
                                        try {
                                            Thread.sleep(50);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        private void writeFileV2WithFix(File dir, String s) {
                            try (BufferedWriter fw = new BufferedWriter(new FileWriter(new File(dir, Thread.currentThread().getName() + "-" + ThreadLocalRandom.current().nextInt(0, 1) + ".txt")))) {
                                final int iterationCount = 10;
                                final String newline = "\n";
                                StringBuilder sb = new StringBuilder((s.length() + newline.length()) * iterationCount);
                                for (int i = 0; i < iterationCount; i++) {
                                    sb.append(s);
                                    sb.append(newline);
                                }
                                fw.write(sb.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }, "LoadTest-DispatcherThread");
    }


    private static void registerLoadTestingMxBean() throws JMException {
        mxBean = new CpuLoadTestingMXBeanImpl();
        Map<String, String> keyVal = ImmutableMap.of("name", "LoadTesting Statistics");
        GlobalJmxRegistration.INSTANCE.register(mxBean, MBEAN_DOMAIN_PART + "loadtest", keyVal);
    }
}
