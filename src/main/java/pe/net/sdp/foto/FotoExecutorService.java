package pe.net.sdp.foto;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FotoExecutorService {

    private static ExecutorService EXECUTOR_SERVICE;

    public static ExecutorService getExecutorService() {
        return EXECUTOR_SERVICE;
    }

    public static void init() {
        EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);
    }

    public static void shutdown() throws FotoException {
        if (EXECUTOR_SERVICE != null) {
            EXECUTOR_SERVICE.shutdown();
            try {
                if (!EXECUTOR_SERVICE.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                    throw new FotoException("el executor service no terminó su shutdown");
                }
            } catch (InterruptedException e) {
                throw new FotoException("Execution interrupted", e);
            }
        }
    }

}
