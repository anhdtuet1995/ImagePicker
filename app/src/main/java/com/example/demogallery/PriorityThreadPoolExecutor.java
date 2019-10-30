package com.example.demogallery;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<Runnable>());
    }
    @Override
    public Future<?> submit(Runnable task) {
        PriorityFutureTask futureTask = new PriorityFutureTask((ThumbnailUpdateRunnable) task);
        execute(futureTask);
        return futureTask;
    }
    private static final class PriorityFutureTask extends FutureTask<ThumbnailUpdateRunnable>
            implements Comparable<PriorityFutureTask> {
        private final ThumbnailUpdateRunnable priorityRunnable;

        public PriorityFutureTask(ThumbnailUpdateRunnable thumbnailUpdateRunnable) {
            super(thumbnailUpdateRunnable, null);
            this.priorityRunnable = thumbnailUpdateRunnable;
        }
        /*
         * compareTo() method is defined to compare 2 tasks with their priority.
         */
        @Override
        public int compareTo(PriorityFutureTask other) {
            int p1 = priorityRunnable.getPriority();
            int p2 = other.priorityRunnable.getPriority();
            return p1 - p2;
        }
    }
}
