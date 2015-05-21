package com.nut.thread;

public class Job {
	protected Job next;

	//	Overridable
	public boolean equals(int a0, int a1, int a2, Object... ar) {
		return false;
	}

	public void discard() {
	}
}

