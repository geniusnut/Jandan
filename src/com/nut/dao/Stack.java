package com.nut.dao;

/**
 * Created by yw07 on 15-5-6.
 */
public class Stack<V> {
	private V[] mElements;
	private int size = 0;
	private final int DEFAULT_STACK_SIZE = 16;

	public Stack() {
		mElements = (V[]) new Object[DEFAULT_STACK_SIZE];
	}
}
