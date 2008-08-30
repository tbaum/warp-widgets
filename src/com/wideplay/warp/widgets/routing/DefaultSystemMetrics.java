package com.wideplay.warp.widgets.routing;

import com.google.inject.Singleton;
import com.wideplay.warp.widgets.compiler.CompileError;
import net.jcip.annotations.ThreadSafe;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe @Singleton //@Concurrent
class DefaultSystemMetrics implements SystemMetrics {
    private final ConcurrentMap<Class<?>, Metric> pages = new ConcurrentHashMap<Class<?>, Metric>();
    private final AtomicBoolean active = new AtomicBoolean(false);

    public void logPageRenderTime(Class<?> page, long time) {
        Metric metric = putIfAbsent(page);


        //requests fight it out for who wins this set
        metric.lastRenderTime.set(time);
    }

    public void logErrorsAndWarnings(Class<?> page, List<CompileError> errors, List<CompileError> warnings) {
        Metric metric = putIfAbsent(page);

        //these must always be set in unison, so we are forced to use a wrapper to avoid synchronization
        metric.lastErrors.set(new ErrorTuple(errors, warnings));

    }

    public void activate() {
        active.set(true);
    }

    public boolean isActive() {
        return active.get();
    }

    private Metric putIfAbsent(Class<?> page) {
        Metric metric = pages.get(page);

        //concurrent put if absent
        if (null == metric) {
            Metric newMetric = new Metric();

            //attempt to put it using CAS
            final Metric returned = pages.putIfAbsent(page, newMetric);

            //use the newly minted metric only if it was successfully put (otherwise re-obtain it)
            if (null == returned)
                metric = newMetric;
            else
                metric = pages.get(page);
        }

        return metric;
    }

    /**
     * Associates various metrics with a given page It is not guaranteed to represent any
     *  particular request, rather metrics are collected over time.
     *
     * Except for the errors and warnings, which are always the last ones available (roughly!) 
     *
     */
    private static class Metric {
        private final AtomicLong lastRenderTime = new AtomicLong(0);
        private final AtomicReference<ErrorTuple> lastErrors = new AtomicReference<ErrorTuple>();

    }

    //wrapper helps avoid locking when setting errors and warnings for a page atomically
    private static class ErrorTuple {
        private final List<CompileError> errors;
        private final List<CompileError> warnings;

        public ErrorTuple(List<CompileError> errors, List<CompileError> warnings) {
            this.errors = errors;
            this.warnings = warnings;
        }
    }
}
