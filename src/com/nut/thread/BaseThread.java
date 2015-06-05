package com.nut.thread;

public class BaseThread extends Thread {
	public static final int THREAD_PRIORITY_SCANNER = 3;
	public static final int THREAD_PRIORITY_LOAD = 5;
	public static final int THREAD_PRIORITY_SAVE = 7;

	private volatile boolean mQuit;

	@Override
	public synchronized void start() {
		mQuit = false;
		try {
			super.start();
			//	Log.d("BaseThread", "start: " + this);
		} catch (Throwable e) {
			//	Log.e("BaseThread", "start: " + this + ", " + e);
		}
	}

	public void quit(Object obj) {
		mQuit = true;
		try {
			if (obj != null) synchronized (obj) {
				obj.notifyAll();
			}
			synchronized (this) {
				super.notifyAll();
			}
			super.join();
			//	Log.d("BaseThread", "quit: " + this);
		} catch (Throwable e) {
			//	Log.e("BaseThread", "quit: " + this + ", " + e);
		}
	}

	protected boolean waitQuit(int timeout) {
		if (timeout > 0 && !mQuit) {
			try {
				synchronized (this) {
					super.wait(timeout);
				}
			} catch (Throwable e) {
				//	Log.e("BaseThread", "wait: " + this + ", " + e);
			}
		}
		return mQuit;
	}
}

