package com.nut.Jandan;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.DisplayMetrics;
import com.dao.PictureLoader;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nut.Jandan.Utility.ExternalFile;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.File;

/**
 * Created by yw07 on 15-5-22.
 */
public class JandanApp extends Application {

	public static final int SCREEN_MAX_SIDE[] = new int[]{2560, 2048, 1920, 1800, 1600, 1280, 1024, 960, 800, 480, 320};

	private static Application mContext;

	public static PictureLoader mPictureLoader;


	public static int mAnimationDuration;
	public static int mScreenMinSideDP;
	public static int mScreenMaxSidePX;
	public static int mScreenMinSidePX;
	public static ImageLoader mImageLoader;

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = this;
		SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());
		initImageLoader();

		final Resources res = getResources();
		final Configuration config = res.getConfiguration();
		final DisplayMetrics metrics = res.getDisplayMetrics();

		mScreenMinSideDP = (int) (Math.min(metrics.widthPixels, metrics.heightPixels) / metrics.density);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			if (mScreenMinSideDP < config.smallestScreenWidthDp)
				mScreenMinSideDP = config.smallestScreenWidthDp;
		}
		mScreenMinSidePX = Math.round(mScreenMinSideDP * metrics.density);

		//	Round the max side screen to known size

		int maxSide = Math.max(metrics.widthPixels, metrics.heightPixels);
		int lastSide = maxSide;
		for (int side : SCREEN_MAX_SIDE) {
			if (maxSide == side) {
				break;
			} else if (maxSide > side) {
				maxSide = lastSide;
				break;
			}
			lastSide = side;
		}
		mScreenMaxSidePX = maxSide;

		mPictureLoader = new PictureLoader(this);
	}

	private void initImageLoader() {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.EXACTLY)
				.cacheOnDisk(true)
				.displayer(new FadeInBitmapDisplayer(200))
				.showImageOnLoading(R.drawable.loading)
				.build();

		File cacheDir;
		cacheDir = ExternalFile.getExternalCacheDir(this);
//		if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
//			cacheDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//		} else {
//			cacheDir = getCacheDir();
//		}
		ImageLoaderConfiguration.Builder configBuilder = new ImageLoaderConfiguration.Builder(mContext)
				.threadPoolSize(2)
				.memoryCache(new WeakMemoryCache())
				.denyCacheImageMultipleSizesInMemory()
				.diskCache(new UnlimitedDiscCache(cacheDir))
				.defaultDisplayImageOptions(options);
		if (BuildConfig.DEBUG) {
			configBuilder.writeDebugLogs();
		}
		ImageLoader.getInstance().init(configBuilder.build());
		mImageLoader = ImageLoader.getInstance();
	}

	public static Application getInstance() {
		return mContext;
	}
}
