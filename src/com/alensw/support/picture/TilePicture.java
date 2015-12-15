package com.alensw.support.picture;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import com.alensw.support.cache.ConcurrentLruHashMap;
import com.alensw.support.file.ParcelFile;
import com.alensw.support.pool.BitmapPool;
import com.alensw.support.pool.IntQueue;
import com.alensw.support.thread.Task;

public class TilePicture extends Picture {    //	Blank picture to compute the size and matrix
	//	GINGERBREAD_MR1 && HONEYCOMB have BitmapRegionDecoder,
	//	but GINGERBREAD_MR1 don't have Hardware Acceleration to drawing tiles
	//	and both decode some error bitmaps sometime
	public static final boolean HAS_REGION_DECODER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	//	BitmapRegionDecoder use BitmapFactory.Options.inBitmap since JELLY_BEAN
	public static final boolean REUSE_BITMAP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;

	private final BitmapRegionDecoder mRegionDecoder;
	private Loader mLoader;
	private View mView;

	private final ConcurrentLruHashMap<Integer, TileImage> mCache = new ConcurrentLruHashMap<Integer, TileImage>(mMaxTiles) {
		protected void discard(TileImage image) {
			//	Log.d("TilePicture", "discard tile image: " + image);
			image.release();
		}
	};

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	public static TilePicture create(ParcelFile pfd, Uri uri, String mtype, Handler handler) {
		BitmapRegionDecoder decoder = null;
		try {
			decoder = BitmapRegionDecoder.newInstance(pfd.getDescriptor(), false);
		} catch (Throwable e) {
			Log.e("TilePicture", "load fd decoder: " + pfd.getUri() + ", " + e);
		}
		if (decoder == null) try {
			//	It seems File Descriptor is shared by the system, so call by path name if failed
			decoder = BitmapRegionDecoder.newInstance(pfd.getPath(), false);
		} catch (Throwable e) {
			Log.e("TilePicture", "load fn decoder: " + pfd.getPath() + ", " + e);
		}
		if (decoder == null)
			return null;

		//	Log.d("TilePicture", "create: " + uri);
		return new TilePicture(decoder, uri, mtype, handler);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	public TilePicture(BitmapRegionDecoder decoder, Uri uri, String mtype, Handler handler) {
		super(decoder.getWidth(), decoder.getHeight(), Picture.TYPE_TILE, uri, mtype);
		mRegionDecoder = decoder;
	}

	public TileImage getCache(int tile) {
		final TileImage image = mCache.get(tile);
		if (image != null)
			return (TileImage) image.addRef();
		return null;
	}

	private TileImage findCache(int tile, int maxLevel) {
		final int left = Tile.getLeft(tile);
		final int top = Tile.getTop(tile);
		int level = Tile.getLevel(tile);
		while (level <= maxLevel) {
			final TileImage image = getCache(tile);
			if (image != null) {
				//	Log.d("TilePicture", "find tile: " + Tile.toString(tile));
				return image;
			}
			level++;
			int shift = Tile.SHIFT + level;
			tile = Tile.build(left >> shift << shift, top >> shift << shift, level);
		}
		return null;
	}

	private final Matrix mTileItemMatrix = new Matrix();

	@Override
	public void draw(Canvas canvas, Matrix matrix, Picture picture2, Matrix matrix2, Paint paint, boolean fast) {
		// components read from ExifParser (not from BitmapRegionDecoder)
		// if 1 or 3 should never has alpha
		final Paint bkPaint = mBkgndPaint;
		final int components = picture2 != null ? picture2.mInfo.components : 3;
		final boolean drawBk = !(components == 3 || components == 1) && bkPaint != null;

		if (picture2 != null && picture2.hasBitmap()) {
			if (drawBk && picture2.hasAlpha()) {
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				canvas.concat(matrix2);
				canvas.drawRect(0, 0, picture2.getBitmapWidth(), picture2.getBitmapHeight(), bkPaint);
				canvas.restore();
			}
			canvas.drawBitmap(picture2.getBitmap(), matrix2, paint);
		} else {
			//  If the preview isn't valid, we may see some blank squares if the tiles are not enough
			Log.w("TilePicture", "draw without preview: " + mUri);
			return;
		}

		final int level = mCurLevel;
		final int maxLevel = Math.min(level + 1, mMaxLevel);
		final int size = Tile.SIZE << level;
		final Loader loader = mLoader;
		final Matrix matrixItem = mTileItemMatrix;

		final int left = mRegionRect.left;
		final int top = mRegionRect.top;
		final int right = mRegionRect.right;
		final int bottom = mRegionRect.bottom;
		//	Log.d("TilePicture", "draw background: " + drawBk + ", " + components);

		//	Don't request too many to save CPU
		int requested = (!fast && mView != null) ? Math.max(right - left, bottom - top) >> (Tile.SHIFT + level) : 1;
		//	int count = 0;
		for (int y = top; y < bottom; y += size) {
			for (int x = left; x < right; x += size) {
				final int tile = Tile.build(x, y, level);
				final TileImage image = findCache(tile, maxLevel);
				final int imgTile = image != null ? image.mTile : Tile.EMPTY;
				if (image != null) {
					final Bitmap bitmap = image.getBitmap();
					if (bitmap != null && !bitmap.isRecycled()) {
						final int scale = Tile.getScale(imgTile);
						final int xTile = Tile.getLeft(imgTile);
						final int yTile = Tile.getTop(imgTile);
						matrixItem.setScale(scale, scale);
						matrixItem.postTranslate(xTile, yTile);
						matrixItem.postConcat(matrix);
						if (drawBk && image.mHasAlpha) {
							canvas.save(Canvas.MATRIX_SAVE_FLAG);
							canvas.concat(matrixItem);
							canvas.drawRect(0, 0, Math.min(right - xTile, Tile.SIZE), Math.min(bottom - yTile, Tile.SIZE), bkPaint);
							canvas.drawBitmap(bitmap, 0, 0, paint);
							canvas.restore();
						} else {
							canvas.drawBitmap(bitmap, matrixItem, paint);
						}
					}
					image.release();
					//	count++;
				}
				if (requested > 0 && imgTile != tile) {
					if (loader != null && loader.request(tile))
						requested--;
					//	Log.d("TilePicture", "request tile: " + Tile.toString(tile) + ", image=" + (image != null));
				}
			}
		}
		//	Log.d("TilePicture", "draw tiles: " + "count=" + count);
	}

	private Paint mBkgndPaint;

	@Override
	public boolean start(View view, Paint bkPaint) {
		mView = view;
		mBkgndPaint = bkPaint;

		if (mLoader != null)
			mLoader.cancel();
		mLoader = new Loader();
		mExecutor.submit(mLoader);
		return true;
	}

	@Override
	public void stop(boolean clearCache) {
		synchronized (mRegionRect) {
			mRegionRect.setEmpty();
		}

		mView = null;

		if (mLoader != null) {
			mLoader.cancel();
			mLoader = null;
		}

		if (clearCache)
			mCache.clear();
	}

	public static int computeLevel(float factor, float accuracy) {
		int level = 0;
		while (factor > (1 << (level + 1)) * accuracy && level < Tile.MAX_SCALE)
			level++;
		return level;
	}

	private volatile int mCurLevel = 1;
	private int mMaxLevel = 1;
	private final Rect mRegionRect = new Rect();
	private final RectF mClipRect = new RectF();
	private final Matrix mInverseMatrix = new Matrix();
	private final float[] mMatrixValues = new float[9];

	@Override
	public void update(int cxView, int cyView, Matrix matrix, RectF rcDraw) {
		final RectF rcClip = mClipRect;
		rcClip.set(0, 0, cxView, cyView);
		if (!rcClip.intersect(rcDraw)) {
			synchronized (mRegionRect) {
				mRegionRect.setEmpty();
			}
			//	Log.d("TilePicture", "update empty: " + mUri);
			return;
		}

		matrix.getValues(mMatrixValues);
		final float factor = 1f / Picture.getMatrixScale(mMatrixValues);
		final int level = computeLevel(factor, 0.85f);
		if (mMaxLevel < level)
			mMaxLevel = level;

		matrix.invert(mInverseMatrix);
		mInverseMatrix.mapRect(rcClip);

		// align the rectangle to tile boundary
		final int shift = Tile.SHIFT + level;
		final int left = Math.max((int) Math.floor(rcClip.left), 0) >> shift << shift;
		final int top = Math.max((int) Math.floor(rcClip.top), 0) >> shift << shift;
		final int right = Math.min((int) Math.ceil(rcClip.right), mInfo.width);
		final int bottom = Math.min((int) Math.ceil(rcClip.bottom), mInfo.height);

		synchronized (mRegionRect) {
			mCurLevel = level;
			mRegionRect.left = left;
			mRegionRect.top = top;
			mRegionRect.right = right;
			mRegionRect.bottom = bottom;
		}
		//	Log.d("TilePicture", "update: scale=" + (1<<level) + ", factor=" + factor + ", region=" + mRegionRect);
	}

	@Override
	protected void recycle() {
		super.recycle();
		//	Log.d("TilePicture", "recycle: " + this);

		stop(true);

		synchronized (mRegionDecoder) {
			mRegionDecoder.recycle();
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	private Bitmap decodeRegion(Rect rect, BitmapOptions opts) {
		try {
			opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
			synchronized (mRegionDecoder) {
				if (!mRegionDecoder.isRecycled())
					return mRegionDecoder.decodeRegion(rect, opts);
			}
		} catch (Throwable e) {
			Log.e("TilePicture", "decode region: " + rect + ", scale=" + opts.inSampleSize + ", " + e);
		}
		return null;
	}

	private Bitmap decodeTileWithPool(Rect rect, BitmapOptions opts) {
		final Bitmap inBitmap = obtainBitmap();
		if (inBitmap == null) {
			Log.e("TilePicture", "create bitmap out of memory");
			return null;
		}

		boolean clip = false;
		if (rect.right > mInfo.width) {
			rect.right = mInfo.width;
			clip = true;
		}
		if (rect.bottom > mInfo.height) {
			rect.bottom = mInfo.height;
			clip = true;
		}
		if (clip) try {
			inBitmap.eraseColor(0);
		} catch (Throwable e) {
		}

		opts.inBitmap = inBitmap;
		Bitmap outBitmap = decodeRegion(rect, opts);
		opts.inBitmap = null;

		if (outBitmap != inBitmap) {
			recycleBitmap(inBitmap);
			if (outBitmap != null) {
				outBitmap = BitmapUtils.ensureGLCompatibleBitmap(outBitmap);
				Log.w("TilePicture", "decoder create bitmap: " + rect);
			}
		}
		return outBitmap;
	}

	private Bitmap decodeTileWithoutPool(Rect rect, BitmapOptions opts) {
		if (rect.right > mInfo.width)
			rect.right = mInfo.width;
		if (rect.bottom > mInfo.height)
			rect.bottom = mInfo.height;

		final Bitmap bitmap = decodeRegion(rect, opts);
		if (bitmap != null)
			return BitmapUtils.ensureGLCompatibleBitmap(bitmap);
		return null;
	}

	public Bitmap decodeTile(Rect rect, BitmapOptions opts) {
		Bitmap bitmap;
		if (REUSE_BITMAP)
			bitmap = decodeTileWithPool(rect, opts);
		else
			bitmap = decodeTileWithoutPool(rect, opts);
		return bitmap;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private boolean decodeTileToCache(int tile, Rect rect, BitmapOptions opts) {
		opts.inSampleSize = Tile.getScale(tile);

		final Bitmap bitmap = decodeTile(rect, opts);
		Log.d("TilePicture", "decode tile: " + Tile.toString(tile) + ", " + bitmap + ", " + opts);
		if (bitmap == null) {
			if (opts.mCancel) // region decoder can not be cancelled by opts.requestCancel()?
				return false;
			if (opts.isFailed())
				Log.e("TilePicture", "decoder tile: " + Tile.toString(tile));
		}

		final TileImage image = bitmap != null ? new TileImage(tile, bitmap) : new TileImage(tile, Tile.SIZE, Tile.SIZE);
		mCache.put(tile, image);
		return true;
	}

	private static int mMaxTiles = 64;
	private static BitmapPool mBitmapPool;

	public static void init(Context context, int screenMinSidePx) {
		if (screenMinSidePx >= 1440)
			Tile.SHIFT = 9; // 512
		else if (screenMinSidePx >= 480)
			Tile.SHIFT = 8; // 256
		else
			Tile.SHIFT = 7; // 128
		Tile.SIZE = 1 << Tile.SHIFT;

		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		final int poolCount = Math.round((float) metrics.widthPixels * metrics.heightPixels / Tile.SIZE / Tile.SIZE);
		mMaxTiles = BitmapUtils.MAX_PIXELS_32 / (Tile.SIZE * Tile.SIZE) - poolCount;

		mBitmapPool = new BitmapPool(REUSE_BITMAP ? poolCount : 1, Tile.SIZE, Tile.SIZE, Bitmap.Config.ARGB_8888);
	}

	public static Bitmap obtainBitmap() {
		return mBitmapPool.obtain(Tile.SIZE, Tile.SIZE, Bitmap.Config.ARGB_8888);
	}

	public static void recycleBitmap(Bitmap bitmap) {
		if (REUSE_BITMAP)
			mBitmapPool.recycle(bitmap);
		else if (bitmap != null)
			bitmap.recycle();
	}

	private class TileImage extends Image {
		protected final boolean mHasAlpha;
		protected final int mTile;

		public TileImage(int tile, Bitmap bitmap) {
			super(bitmap);
			mHasAlpha = bitmap.hasAlpha() && bitmap.getConfig() == Bitmap.Config.ARGB_8888;
			mTile = tile;
		}

		public TileImage(int tile, int width, int height) {
			super(width, height, 0, 0);
			mHasAlpha = false;
			mTile = tile;
		}

		@Override
		protected void recycle(Bitmap bitmap) {
			recycleBitmap(bitmap);
		}

		@Override
		public String toString() {
			return super.toString() + ", tile=" + Tile.toString(mTile);
		}
	}

	private class Loader implements Task<Void> {
		private volatile boolean mCancelled;
		private int mCurTile = Tile.EMPTY;
		private BitmapOptions mOptions;
		private final IntQueue mQueue = new IntQueue(1024);

		public boolean request(int tile) {
			synchronized (mQueue) {
				if (!mQueue.contains(tile) && mCurTile != tile) {
					mQueue.pushTail(tile);
					mQueue.notifyAll();
					return true;
				}
			}
			return false;
		}

		public void cancelPending() {
			synchronized (mQueue) {
				mQueue.clear();
				mQueue.notifyAll();
			}
			synchronized (this) {
				if (mOptions != null)
					mOptions.requestCancelDecode();
			}
		}

		private int pollTile() {
			synchronized (mQueue) {
				if (mQueue.size() == 0 && !mCancelled) {
					try {
						mQueue.wait(100);
					} catch (Throwable e) {
					}
				}
				mCurTile = mQueue.size() > 0 ? mQueue.popHead() : Tile.EMPTY;
				return mCurTile;
			}
		}

		@Override
		public void cancel() {
			mCancelled = true;
			cancelPending();
			//	Log.d("TilePicture", "cancel: " + mUri);
		}

		@Override
		public void done() {
		}

		@Override
		public Void call() {
			final Rect rect = new Rect();

			while (!mCancelled) {
				final int tile = pollTile();
				if (tile == Tile.EMPTY)
					continue;

				final BitmapOptions opts = BitmapOptions.obtain();
				synchronized (this) {
					mOptions = opts;
				}

				final int level = Tile.getLevel(tile);
				final int size = Tile.SIZE << level;
				rect.left = Tile.getLeft(tile);
				rect.top = Tile.getTop(tile);
				rect.right = rect.left + size;
				rect.bottom = rect.top + size;

				boolean ret;
				synchronized (mRegionRect) {
					ret = level == mCurLevel && Rect.intersects(rect, mRegionRect);
				}
				if (ret)
					ret = decodeTileToCache(tile, rect, opts);
				else Log.i("TilePicture", "ignore tile: " + Tile.toString(tile));

				final View view;
				synchronized (mQueue) {
					mCurTile = Tile.EMPTY;
					view = mQueue.size() == 0 ? mView : null; // only post invalidate when the queue is empty
				}
				if (view != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
						view.postInvalidateOnAnimation();
					else
						view.postInvalidate();
				}

				synchronized (this) {
					mOptions = null;
				}
				BitmapOptions.recycle(opts);
			}
			return null;
		}
	}
}
