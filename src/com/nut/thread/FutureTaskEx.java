package com.nut.thread;

import java.util.concurrent.FutureTask;

public class FutureTaskEx<V> extends FutureTask<V> {
	private final Task<V> mTask;

	public FutureTaskEx(Task<V> task) {
		super(task);
		mTask = task;
	}

	@Override
	public boolean cancel(boolean mayInterrupt) {
		final boolean ret = super.cancel(mayInterrupt);
		if (ret) {
			mTask.cancel();
			//	Log.d("FutureTaskEx", "cancel: " + mTask);
		}
		return ret;
	}

	@Override
	public void done() {
		mTask.done();
		//	Log.d("FutureTaskEx", "done: " + mTask);
	}

	@Override
	public int hashCode() {
		return mTask.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof FutureTaskEx<?> && mTask.equals(((FutureTaskEx<?>) obj).mTask);
	}

	public Task<V> getTask() {
		return mTask;
	}
}
