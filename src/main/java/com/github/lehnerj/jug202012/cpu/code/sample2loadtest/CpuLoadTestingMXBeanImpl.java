package com.github.lehnerj.jug202012.cpu.code.sample2loadtest;

import javax.management.MXBean;

@MXBean
public class CpuLoadTestingMXBeanImpl implements CpuLoadTestingMXBean {

    private long maxBehindSchedule;
    private long patternsGeneratedCount;

    public void setMaxBehindSchedule(long maxBehindSchedule) {
        this.maxBehindSchedule = maxBehindSchedule;
    }

    @Override
    public long getMaxBehindSchedule() {
        return maxBehindSchedule;
    }

    public void setPatternsGeneratedCount(long count) {
        patternsGeneratedCount=count;
    }

    public long getPatternsGeneratedCount() {
        return patternsGeneratedCount;
    }
}