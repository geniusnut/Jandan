package com.alensw.support.picture;

public class Tile {
	public static final int MAX_LEVEL = 8;
	public static final int MAX_SCALE = 1 << MAX_LEVEL; // 256

	public static final int EMPTY = -1;

	public static int SHIFT = 8;
	public static int SIZE = 1 << 8; // 256

	// level: 4 bits
	// left:  14 bits
	// top:   14 bits
	public static final int BITS_LEVEL = 4;
	public static final int BITS_SIDE = 14;
	public static final int MASK_LEVEL = (1 << BITS_LEVEL) - 1;
	public static final int MASK_SIDE = (1 << BITS_SIDE) - 1;

	public static final int build(int left, int top, int level) {
		left >>= (SHIFT + level);
		top >>= (SHIFT + level);
		return level | (left << BITS_LEVEL) | (top << (BITS_LEVEL + BITS_SIDE));
	}

	public static final int getLevel(int tile) {
		return tile & MASK_LEVEL;
	}

	public static final int getScale(int tile) {
		return 1 << (tile & MASK_LEVEL);
	}

	public static final int getLeft(int tile) {
		final int level = tile & MASK_LEVEL;
		return ((tile >> BITS_LEVEL) & MASK_SIDE) << (SHIFT + level);
	}

	public static final int getTop(int tile) {
		final int level = tile & MASK_LEVEL;
		return ((tile >> (BITS_LEVEL + BITS_SIDE)) & MASK_SIDE) << (SHIFT + level);
	}

	public static String toString(int tile) {
		return "(" + getLeft(tile) + "," + getTop(tile) + ")x" + getScale(tile);
	}
}
