package com.island.androidsftpdocumentsprovider.provider;

import android.os.Handler;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncNetworkUtil {
    private final static ExecutorService executorService = Executors.newCachedThreadPool();
    private final static Object NULL = new Object();

    public static <T> T invoke(final Supplier<T> tSupplier) throws IOException {
        final Object waitLock = new Object();
        final Object[] resultBox = new Object[1];
        final Exception[] exceptionBox = new Exception[1];

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    T result = tSupplier.get();
                    resultBox[0] = result == null ? NULL : result;
                } catch (Exception e) {
                    exceptionBox[0] = e;
                } finally {
                    synchronized (waitLock) {
                        waitLock.notify();
                    }
                }

            }
        });
        while (true) {
            synchronized (waitLock) {
                if (resultBox[0] != null) {
                    //noinspection unchecked
                    return resultBox[0] == NULL ? null : (T) resultBox[0];
                }
                if (exceptionBox[0] != null) {
                    if (exceptionBox[0] instanceof RuntimeException) {
                        throw (RuntimeException) exceptionBox[0];
                    } else if (exceptionBox[0] instanceof IOException) {
                        throw (IOException) exceptionBox[0];
                    } else {
                        throw new RuntimeException(exceptionBox[0]);
                    }
                }

                try {
                    waitLock.wait(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
