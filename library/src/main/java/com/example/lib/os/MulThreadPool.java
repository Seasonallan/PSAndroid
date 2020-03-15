package com.example.lib.os;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 多线程线程池
 */
public class MulThreadPool extends AbsThreadPool {
	@Override
	protected int getCorePoolSize() {
		return 4;
	}

	@Override
	protected int getMaximumPoolSize() {
		return 100;
	}

	@Override
	protected long getKeepAliveTime() {
		return 200;
	}

	@Override
	protected TimeUnit getTimeUnit() {
		return TimeUnit.MILLISECONDS;
	}

	@Override
	protected BlockingQueue<Runnable> newQueue() {
		return new LinkedBlockingQueue<Runnable>();
	}
	
	@Override
	protected ThreadFactory newThreadFactory(){
		return new DefaultThreadFactory(Thread.MIN_PRIORITY);
	}
}
