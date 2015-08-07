package com.alensw.support.picture;

import android.graphics.*;
import android.net.Uri;
import android.view.View;
import com.alensw.support.thread.AsyncExecutor;
import com.alensw.support.thread.BaseThread;

import java.util.Locale;

public class Picture extends Image {
	public static final int TYPE_ERROR = -1;
	public static final int TYPE_BLANK = 0;
	public static final int TYPE_THUMB = 1;
	public static final int TYPE_IMAGE = 2;
	public static final int TYPE_TILE = 3;
	public static final int TYPE_MOVIE = 4;

	public static final String MIME_TYPE_BMP = "image/bmp";

	protected static AsyncExecutor mExecutor = new AsyncExecutor(1, 2, 4, BaseThread.THREAD_PRIORITY_LOAD);

	public int mType;                        //	TYPE_*
	public String mMimeType = MIME_TYPE_BMP;    //	Can't be null
	public Uri mUri = Uri.EMPTY;                //	Can't be null

	public Picture(Bitmap bitmap, int ptype) {
		super(bitmap);
		init(ptype, null, MIME_TYPE_BMP);
	}

	public Picture(Bitmap bitmap, int ptype, Uri uri, BitmapOptions opts) {
		super(bitmap, opts.outComponents, opts.outRotation);
		init(ptype, uri, opts.outMimeType);
	}

	public Picture(int width, int height, int ptype, Uri uri, String mtype) {
		super(width, height, 0, 0);
		init(ptype, uri, mtype);
	}

	public Picture(Uri uri, BitmapOptions opts) {
		super(opts.outWidth, opts.outHeight, opts.outComponents, opts.outRotation);
		init(opts.isFailed() ? TYPE_ERROR : TYPE_BLANK, uri, opts.outMimeType);
	}

	private void init(int ptype, Uri uri, String mimeType) {
		mType = ptype;
		mMimeType = mimeType != null ? mimeType : MIME_TYPE_BMP;
		mUri = uri != null ? uri : Uri.EMPTY;
		mInfo.stereo = pictureIsStereo(mUri.toString());
	}

	/*	@Override
		protected void finalize() {
			super.finalize();
			if (mType >= TYPE_THUMB && refCount() > 0)
				Log.w("Picture", "leak picture: " + this + ", ref=" + refCount());
		}
	*/
	@Override
	public Picture addRef() {
		return (Picture) super.addRef();
	}

	@Override
	public String toString() {
		return super.toString() + ", type=" + mType + ", duration=" + duration() + ", uri=" + mUri;
	}

	public final boolean equalsUri(Uri uri) {
		return mUri == uri || mUri.equals(uri);
	}

	//	Overridable, can be called only in main UI thread!
	public void draw(Canvas canvas, Matrix matrix, Picture picture2, Matrix matrix2, Paint paint, boolean fast) {
		if (hasBitmap())
			canvas.drawBitmap(getBitmap(), matrix, paint);
		else if (picture2 != null && picture2.hasBitmap())
			canvas.drawBitmap(picture2.getBitmap(), matrix2, paint);
	}

	public long duration() {
		return 0;
	}

	public boolean start(View view, Paint bkPaint) {
		return true;
	}

	public void stop(boolean clearCache) {
	}

	public void update(int cxView, int cyView, Matrix matrix, RectF rcDraw) {
	}

	public static float getMatrixScale(float[] values) {
		final float scaleX = values[Matrix.MSCALE_X];
		final float skewY = values[Matrix.MSKEW_Y];
		return (float) Math.hypot(scaleX, skewY);
	}

	public static float getMatrixRotation(float[] values) {
		final double ang = Math.atan2(values[Matrix.MSKEW_Y], values[Matrix.MSCALE_Y]);
		return (float) Math.toDegrees(ang);
	}

	public static String getExtOfFilePath(String path, boolean toLowCase) {
		final int pos = path != null ? path.lastIndexOf('.') : -1;
		if (pos == -1)
			return "";

		String ext = path.substring(pos + 1);
		if (toLowCase)
			ext = ext.toLowerCase(Locale.ENGLISH);
		return ext;
	}

	public static boolean pictureIsStereo(String filename) {
		final String ext = getExtOfFilePath(filename, true);
		return "jps".equals(ext);
	}

	public static boolean pictureIs3D(String filename) {
		final String ext = getExtOfFilePath(filename, true);
		return "jps".equals(ext) || "mpo".equals(ext);
	}
}
