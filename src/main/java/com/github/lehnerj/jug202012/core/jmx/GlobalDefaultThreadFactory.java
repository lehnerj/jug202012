package com.github.lehnerj.jug202012.core.jmx;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import javax.annotation.Nonnull;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ThreadFactory to use with global thread pools.
 * implementation copied from java.util.concurrent.Executors.DefaultThreadFactory.
 * <p/>
 * <b>Note:</b> Uses daemon threads!
 */
public class GlobalDefaultThreadFactory implements ThreadFactory {

    private final ThreadGroup threadGroup;

    /**
     * Hidden constructor (<em>prevent creation from outside</em>).
     */
    private GlobalDefaultThreadFactory(final ThreadGroup threadGroup) {
        this.threadGroup = threadGroup;
    }

    /**
     * On purpose, there is no version without an {@link UncaughtExceptionHandler} parameter. All thread pools should have that installed.
     *
     * @param baseName        will be set on all threads using {@link Thread#setName(String)}.
     * @param uncaughtHandler will be set on all threads using {@link Thread#setUncaughtExceptionHandler(UncaughtExceptionHandler)}.
     * @return a newly created {@linkplain ThreadFactory} instance
     * @throws NullPointerException     if any of the passed arguments are {@code null}
     * @throws IllegalArgumentException if any of the passed arguments are invalid
     */
    public static ThreadFactory globalThreadFactory(final String baseName, final UncaughtExceptionHandler uncaughtHandler) {
        return new ThreadFactoryBuilder()
                .setNameFormat("pool-" + baseName + "-thread-%d")
                .setDaemon(true)
                .setPriority(Thread.NORM_PRIORITY)
                .setUncaughtExceptionHandler(uncaughtHandler)
                .setThreadFactory(new GlobalDefaultThreadFactory(createThreadGroup(baseName)))
                .build();
    }

    private static ThreadGroup createThreadGroup(final String baseName) {
        // Same code as java.util.concurrent.Executors.DefaultThreadFactory
        final SecurityManager securityManager = System.getSecurityManager();
        final ThreadGroup parentThreadGroup = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        return new ThreadGroup(parentThreadGroup, baseName);
    }

    @Override
    public Thread newThread(@Nonnull final Runnable runnable) {
        checkNotNull(runnable);
        return new Thread(threadGroup, runnable);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("threadGroup", threadGroup)
                .add("activeThreads", threadGroup.activeCount())
                .toString();
    }
}
