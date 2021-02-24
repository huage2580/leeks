package utils.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static ThreadPoolExecutor sDefaultPool;
    private static ThreadPoolExecutor sDefaultSingleThread;

    public static ThreadPoolExecutor defaultPool() {
        if (sDefaultPool == null) {
            synchronized (ThreadPool.class) {
                if (sDefaultPool == null) {
                    sDefaultPool = newThreadPool(
                            Math.max(4, CPU_COUNT - 1),
                            CPU_COUNT * 2 + 1,
                            "FrameworkPool"
                    );
                    sDefaultPool.allowCoreThreadTimeOut(true);
                }
            }
        }
        return sDefaultPool;
    }

    public static ThreadPoolExecutor defaultSingleThreadPool() {
        if (sDefaultSingleThread == null) {
            synchronized (ThreadPool.class) {
                if (sDefaultSingleThread == null) {
                    sDefaultSingleThread = newThreadPool(1, 1, "SingleThreadPool");
                }
            }
        }
        return sDefaultSingleThread;
    }

    public static ThreadPoolExecutor newThreadPool(int corePoolSize, int maximumPoolSize, String namePrefix) {
        if (corePoolSize < 0) {
            corePoolSize = 0;
        }
        if (maximumPoolSize < corePoolSize) {
            maximumPoolSize = corePoolSize;
        }
        if (maximumPoolSize <= 0) {
            maximumPoolSize = 1;
        }
        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                2,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new FrameworkThreadFactory(namePrefix)
        );
    }

    static class FrameworkThreadFactory implements ThreadFactory {
        private final AtomicInteger count = new AtomicInteger(1);

        private String namePrefix;

        FrameworkThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix + "#" + count.getAndIncrement());
        }
    }
}
