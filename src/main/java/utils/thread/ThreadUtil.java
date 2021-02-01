package utils.thread;


import java.util.concurrent.ExecutorService;

public class ThreadUtil {

    public static ExecutorService obtainExecutor() {
        return ThreadPool.defaultPool();
    }

    public static void runOnBackground(Runnable con) {
        obtainExecutor().execute(con);
    }

    public static void runOnBackground(Runnable con, boolean noWait) {
        if (noWait) {
            new Thread(con).start();
        } else {
            runOnBackground(con);
        }
    }

}
