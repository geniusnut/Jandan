package com.nut.Jandan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nut.Jandan.Activity.PicActivity;
import com.nut.Jandan.Fragment.NewsFragment;
import com.nut.Jandan.R;
import com.nut.Jandan.Utility.Utilities;
import com.nut.cache.Pic;
import com.nut.cache.PicFile;
import com.nut.dao.ParcelFile;
import com.nut.gif.GifMovie;
import com.nut.ui.ExpandableTextView;
import com.nut.ui.ScaleImageView;

import java.io.File;
import java.util.HashMap;

/**
 * Created by yw07 on 15-5-19.
 */
public class PicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context mContext;
	private final ImageLoader mImageLoader;
	private Handler mHandler;
	private PicFile mPicFile;
	private String TAG = "PicAdapter";
	private static HashMap<String, Float> mRatioMap = new HashMap<>(64);
	private static int TYPE_GIF = 0;
	private static int TYPE_JPG = 1;


	public PicAdapter(Context context, PicFile picFile, Handler handler) {
		mContext = context;
		mPicFile = picFile;
		mImageLoader = ImageLoader.getInstance();
		mHandler = handler;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.item_pic, viewGroup, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

		final Pic pic = mPicFile.get(position);


		ViewHolder holder = (ViewHolder) viewHolder;
		holder.updater.setText(pic.mAuthor);
		holder.text.setText(pic.mDesc);
		holder.oo.setText(String.valueOf(pic.mOO));
		holder.xx.setText(String.valueOf(pic.mXX));
		holder.time.setText(Utilities.convertTime(pic.mTime));
		holder.scaleImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.loading));


		int height = calcLayoutParams();
		FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(-1, height);
		holder.mWrapper.setLayoutParams(flp);
		final String url = pic.mUrls.get(0);
		loadPic(holder.scaleImage, url);

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, PicActivity.class);
				intent.putExtra(PicActivity.EXTRA_PIC, pic);
				mContext.startActivity(intent);
			}
		});
	}

	private int calcLayoutParams() {
		return 600;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}


	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		if (((ViewHolder) holder).scaleImage.getGifMovie() != null) {
			Log.d(TAG, "recycle scaleImage at:" + ((ViewHolder) holder).scaleImage.getTag());
			((ViewHolder) holder).scaleImage.getGifMovie().recycle();
		}
	}

	@Override
	public int getItemCount() {
		return mPicFile.size();
	}

	private void loadPic(final ImageView imageView, final String thumbUrl) {
		imageView.setTag(thumbUrl);

		mImageLoader.loadImage(thumbUrl, NewsFragment.options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
				// Do whatever you want with Bitmap

				if (thumbUrl.endsWith("gif")) {
					File file = DiskCacheUtils.findInCache(imageUri, mImageLoader.getDiskCache());
					// File file = new File("/storage/emulated/0/Wowtu/Download/2684669.gif");
					ParcelFile pfd = null;
					try {
						pfd = ParcelFile.openFile(file, true);
					} catch (Exception e) {
						e.printStackTrace();
					}


					if (pfd != null) {
						final GifMovie gifMovie = GifMovie.create(pfd, Uri.EMPTY, mHandler);
						imageView.post(new Runnable() {
							@Override
							public void run() {
								if (imageView.getTag() == thumbUrl) {
									((ScaleImageView) imageView).setGifMovie(gifMovie);
								}
							}
						});

					}
				} else {
					if (loadedImage.getWidth() != 0) {
						float ratio = ((float) loadedImage.getHeight()) / ((float) loadedImage.getWidth());
						mRatioMap.put(thumbUrl, ratio);
					}
					imageView.post(new Runnable() {
						@Override
						public void run() {
							if (imageView.getTag() == thumbUrl) {
								imageView.setImageBitmap(loadedImage);
							}
						}
					});
				}
			}
		});
	}

	public int getCount() {
		return 10;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView updater;
		public ExpandableTextView text;
		public TextView oo;
		public TextView xx;
		public TextView time;
		public ScaleImageView scaleImage;
		public LinearLayout mWrapper;

		public ViewHolder(View itemView) {
			super(itemView);
			updater = (TextView) itemView.findViewById(R.id.updater);
			text = (ExpandableTextView) itemView.findViewById(R.id.text);
			oo = (TextView) itemView.findViewById(R.id.oo);
			xx = (TextView) itemView.findViewById(R.id.xx);
			time = (TextView) itemView.findViewById(R.id.time);
			scaleImage = (ScaleImageView) itemView.findViewById(R.id.scale_image);
			mWrapper = (LinearLayout) itemView.findViewById(R.id.card_wraper);
		}
	}

	public static Bitmap drawBitmap(int width, int height) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		int color = Color.rgb(255, 245, 238);
		Paint paint = new Paint();
		paint.setColor(color);
		Rect rect = new Rect(0, 0, 1, 1);
		canvas.drawRect(rect, paint);
		return bitmap;
	}
}
