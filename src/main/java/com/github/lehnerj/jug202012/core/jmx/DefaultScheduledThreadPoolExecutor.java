package com.github.lehnerj.jug202012.core.jmx;

import com.github.lehnerj.jug202012.core.thread.ThreadPoolStatistics;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static com.google.common.base.Preconditions.checkArgument;

@ThreadSafe
public class DefaultScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor implements ThreadPoolStatistics {

    // Store and use it also as ThreadPool name.
    private final String threadBaseName;

    /**
     * @param poolSize       Sets the maximum allowed number of threads in that pool
     * @param threadBaseName Not empty. Prefix to use for all thread names (plus some number)
     */
    public DefaultScheduledThreadPoolExecutor(
            @Nonnegative int poolSize,
            @Nonnull final String threadBaseName) {

        super(poolSize, GlobalDefaultThreadFactory.globalThreadFactory(threadBaseName, new LoggingUncaughtExceptionHandler()));
        // Maximum of 0 makes no sense.
        checkArgument(poolSize > 0);

        setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        // Remove immediately (may free memory of objects reference by a periodic).
        setRemoveOnCancelPolicy(true);
        this.threadBaseName = threadBaseName;
        // Core or default max size.
        this.setMaximumPoolSize(poolSize);
    }

    @Override
    public final void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) { // needs to be final, because it is called by the constructor
        super.setExecuteExistingDelayedTasksAfterShutdownPolicy(value);
    }

    @Override
    public final void setRemoveOnCancelPolicy(boolean value) { // needs to be final, because it is called by the constructor
        super.setRemoveOnCancelPolicy(value);
    }

    @Override
    public final void setMaximumPoolSize(int maximumPoolSize) { // needs to be final, because it is called by the constructor
        super.setMaximumPoolSize(maximumPoolSize);
    }

    @Override
    protected void afterExecute(final Runnable r, Throwable t) {
        // Invoke at the beginning for proper nesting.
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Object result = ((Future<?>) r).get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
            }
        }
        if (t != null)
            t.printStackTrace();
    }

    @Override
    public int getActiveThreadStatistics() {
        return getActiveCount();
    }

    @Override
    public int getMaxSizeStatistics() {
        //scheduled executors never have more than corePoolSize threads
        return getCorePoolSize();
    }

    @Override
    public int getQueueSizeStatistics() {
        return getQueue().size();
    }
}
