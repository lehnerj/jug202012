package com.github.lehnerj.jug202012.cpu.code.sample1arraylist;

import com.google.common.base.Stopwatch;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CpuCodeArrayList {

    private static final Logger logger = LoggerFactory.getLogger(CpuCodeArrayList.class);
    public static final int SIZE = 1_000_000;
    private static boolean fixProblem;
    List<String> list = new ArrayList<>(SIZE);
    List<String> copy = new ArrayList<>(SIZE);

    public static void main(String[] args) {
        parseCommandLineArgs(args);

        final CpuCodeArrayList service = new CpuCodeArrayList();
        service.init();
        service.prepareData();
        for(int i=0;i<100;i++) {
            service.execute(i);
        }
    }

    private void init() {
        if(fixProblem) {
            list = new LinkedList<String>();
            copy = new LinkedList<String>();
        }
    }

    private void execute(int iterationNumber) {
        copyData();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        businessLogic(iterationNumber);
    }

    private void businessLogic(int iterationNumber) {
        long hash=0;
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            hash += s.hashCode();
            iterator.remove();
        }
        stopwatch.stop();
        logger.info(String.format("Iteration #%2d, took: %d ms, hash: %d", iterationNumber, stopwatch.elapsed(TimeUnit.MILLISECONDS), hash));
    }

    private void prepareData() {
        for(int i = 0; i< SIZE; i++) {
            copy.add("Welcome to JUG - CPE Profiling");
        }
    }

    private void copyData() {
        list.addAll(copy);
    }

    private static void parseCommandLineArgs(String[] args) {
        Options options = new Options();

        final String fixProblemStr = "fix";
        Option fixOpt = new Option(fixProblemStr, fixProblemStr, true, "Fix problem?");
        fixOpt.setRequired(true);
        options.addOption(fixOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            fixProblem = Boolean.parseBoolean(cmd.getOptionValue(fixOpt.getOpt()));

            logger.info(CpuCodeArrayList.class.getName());
            logger.info("Fix problem=" + fixProblem);
        } catch (ParseException e) {
            logger.info(e.getMessage());
            formatter.printHelp(CpuCodeArrayList.class.getName(), options);

            System.exit(1);
        }
    }
}
