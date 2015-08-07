package com.alensw.support.thread;

import java.util.concurrent.Callable;

public interface Task<V> extends Callable<V> {
	public abstract void cancel();

	public abstract void done();
}
