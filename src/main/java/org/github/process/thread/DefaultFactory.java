package org.github.process.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : zhilin
 * @date : 2020/06/18
 */
public class DefaultFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);



    @Override
    public Thread newThread(Runnable r) {
        String name = "process-thread-" + poolNumber.getAndIncrement();
        Thread t = new Thread(r, name);
        return t;
    }
}
