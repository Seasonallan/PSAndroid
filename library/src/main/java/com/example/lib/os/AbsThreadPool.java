package com.example.lib.os;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池基类
 */
public abstract class AbsThreadPool{

    protected BlockingQueue<Runnable> queue;
    protected ThreadPoolExecutor executor;
    private boolean isDestroy = false;

	/**
	 * 核心线程池大小
	 */
	protected abstract int getCorePoolSize();

	/**
	 * 最大线程池大小
	 */
	protected abstract int getMaximumPoolSize();

	/**
	 * 线程最大空闲时间
	 */
	protected abstract long getKeepAliveTime();

	/**
	 * 时间单位
	 */
    protected abstract TimeUnit getTimeUnit();

	/**
	 * 线程等待队列
	 */
    protected abstract BlockingQueue<Runnable> newQueue();

	/**
	 * 线程创建工厂
	 */
	protected ThreadFactory newThreadFactory(){
		return new DefaultThreadFactory();
    }

    /**
    * 增加新的任务
    * 每增加一个新任务，都要唤醒任务队列
    * @param newTask
    */
    public final void addTask(Runnable newTask) {
    	if(newTask == null){
    		return;
    	}
    	newTask = onAddTask(newTask);
    	if(newTask == null){
    		return;
    	}
    	synchronized (this) {
			if(!isDestroy){
				if(executor == null){
					queue = newQueue();
				}
				if(executor == null || executor.isShutdown()){
					executor = new ThreadPoolExecutor(
							getCorePoolSize(),
							getMaximumPoolSize(),
							getKeepAliveTime(),
							getTimeUnit(),
							queue,
							newThreadFactory());
				}
				executor.execute(newTask);
			}
		}
    }

	/**
     * 可通过重写此方法对添加的任务进行装饰
     * @param newTask
     * @return
     */
    protected Runnable onAddTask(Runnable newTask){
		return newTask;
    }


    /**
    * 销毁线程池
    */
    public final synchronized void destroy() {
    	synchronized (this) {
	    	if(!isDestroy){
				if(executor != null && !executor.isShutdown() ){
					executor.shutdown();
					if(queue != null) {
						queue.clear();
						queue =  null;
					}
					executor = null;
				}
	    		isDestroy = true;
	    	}
    	}
    }

}
