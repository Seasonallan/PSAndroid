package com.example.lib.os;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程
 */
public class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private int mPriority;
    DefaultThreadFactory() {
        this(Thread.NORM_PRIORITY);
    }

    DefaultThreadFactory(int priority) {
        mPriority = priority;
        SecurityManager s = System.getSecurityManager();
        group = (s != null)? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();//根据thread group定义使用
        namePrefix = "AbsThreadPool - " + poolNumber.getAndIncrement() +  " - thread - ";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon())
            t.setDaemon(false); //非守护线程
        t.setPriority(mPriority);
        return t;
    }
}