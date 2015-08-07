package com.alensw.support.thread;

import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.*;

class PriorityThreadFactory implements ThreadFactory {
	private final int mPriority;

	public PriorityThreadFactory(int priority) {
		mPriority = priority;
	}

	@Override
	public Thread newThread(Runnable task) {
		return new Thread(task) {
			@Override
			public void run() {
				//	Log.d("AsyncExecutor", "new thread: " + this + ", priority=" + mPriority);
				Process.setThreadPriority(mPriority);
				super.run();
			}
		};
	}
}

public class AsyncExecutor extends ThreadPoolExecutor {
	//	assume 3.0+ has multi-cores
	public static final boolean MULTI_CORE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	//  availableProcessors() return only 1 in some dual-core devices?
	public static final int CORE_COUNT = Math.max(Runtime.getRuntime().availableProcessors(), MULTI_CORE ? 2 : 1);

	public static AsyncExecutor newFixedExecutor(int nThreads, int priority) {
		return new AsyncExecutor(nThreads, nThreads, Integer.MAX_VALUE, priority);
	}

	public AsyncExecutor(int corePoolSize, int maxPoolSize, int maxQueueSize, int priority) {
		super(corePoolSize, maxPoolSize, 10, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(maxQueueSize), new PriorityThreadFactory(priority));
		super.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				if (!executor.isShutdown()) try {
					final Runnable r0 = executor.getQueue().poll();
					onCancel(r0);
					executor.execute(r);
					Log.w("AsyncExecutor", "discard: " + r0 + " -> " + r);
				} catch (Throwable e) {
					Log.e("AsyncExecutor", "discard " + r, e);
				}
			}
		});
	}

	public void clear() {
		// clear all the runnable tasks
		try {
			final BlockingQueue<Runnable> queue = getQueue();
			Runnable r;
			while ((r = queue.poll()) != null)
				onCancel(r);
		} catch (Throwable e) {
			Log.e("AsyncExecutor", "clear", e);
		}
	}

	public boolean contains(Object obj) {
		try {
			return getQueue().contains(obj);
		} catch (Throwable e) {
			Log.e("AsyncExecutor", "contains: " + obj, e);
		}
		return false;
	}

	public <V> FutureTaskEx<V> submit(FutureTaskEx<V> futureTask) {
		try {
			super.execute(futureTask);
		} catch (Throwable e) {
			Log.e("AsyncExecutor", "execute", e);
		}
		return futureTask;
	}

	public <V> FutureTaskEx<V> submit(Task<V> task) {
		return submit(new FutureTaskEx<V>(task));
	}

	protected void onCancel(Runnable r) {
		if (r instanceof Future<?>)
			((Future<?>) r).cancel(false);
	}
}
