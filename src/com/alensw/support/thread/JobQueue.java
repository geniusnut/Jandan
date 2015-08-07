package com.alensw.support.thread;

public class JobQueue {
	private Job mHead;
	private Job mTail;

	public void clear() {
		for (Job job = mHead; job != null; job = job.next)
			job.discard();
		mHead = mTail = null;
	}

	public final Job find(int a0, int a1, int a2, Object... ar) {
		for (Job job = mHead; job != null; job = job.next)
			if (job.equals(a0, a1, a2, ar))
				return job;
		return null;
	}

	public final boolean isEmpty() {
		return mHead == null;
	}

	public final Job peek() {
		return mHead;
	}

	public Job poll() {
		final Job job = mHead;
		if (job != null) {
			mHead = job.next;
			job.next = null;
		}
		return job;
	}

	public void push(Job job) {
		if (mHead == null) {
			mHead = mTail = job;
		} else {
			mTail.next = job;
			mTail = job;
			job.next = null;
		}
	}

/*	public Job remove(int a0, int a1, int a2, Object... ar) {
		Job prev = null;
		for (Job job = mHead; job != null; job = job.next) {
			if (job.equals(a0, a1, a2, ar)) {
				if (prev != null)
					prev.next = job.next;
				else
					mHead = job.next;
				job.next = null;
				return job;
			}
			prev = job;
		}
		return null;
	}*/
}

