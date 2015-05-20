package com.nut.Jandan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.nut.Jandan.R;
import com.nut.Jandan.Utilities;
import com.nut.cache.Pic;
import com.nut.cache.PicFile;
import com.nut.dao.ImageLoader;
import com.nut.gif.GifDrawable;
import com.nut.gif.GifImageView;
import com.nut.ui.ExpandableTextView;

import java.util.HashMap;

/**
 * Created by yw07 on 15-5-19.
 */
public class PicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context mContext;
	private final LruCache<String, Drawable> mLruCache;
	private final ImageLoader mImageLoader;
	private PicFile mPicFile;
	private String TAG = "PicAdapter";
	private static HashMap<String, Float> mRatioMap = new HashMap<>(64);
	private static int TYPE_GIF = 0;
	private static int TYPE_JPG = 1;


	public PicAdapter(Context context, PicFile picFile) {
		mContext = context;
		mPicFile = picFile;
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 4;
		mLruCache = new LruCache<String, Drawable>(cacheSize) {
			@Override
			protected int sizeOf(String key, Drawable drawable) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				if (drawable instanceof BitmapDrawable && ((BitmapDrawable) drawable).getBitmap() != null) {
					return ((BitmapDrawable) drawable).getBitmap().getByteCount() / 1024;
				} else if (drawable instanceof GifDrawable) {
					return (int) ((GifDrawable) drawable).mInputSourceLength / 1024;
				}
				return 0;
			}
		};

		mImageLoader = new ImageLoader(mContext);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		if (viewType == TYPE_GIF) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.item_pic_gif, viewGroup, false);
			return new GifViewHolder(view);
		} else {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.item_pic, viewGroup, false);
			return new ViewHolder(view);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

		Pic pic = mPicFile.get(position);
		if (viewHolder.getItemViewType() == TYPE_GIF) {
			GifViewHolder holder = (GifViewHolder) viewHolder;
			holder.updater.setText(pic.mAuthor);
			holder.text.setText(pic.mDesc);
			holder.oo.setText(String.valueOf(pic.mOO));
			holder.xx.setText(String.valueOf(pic.mXX));
			holder.time.setText(Utilities.convertTime(pic.mTime));

			final String url = pic.mUrls.get(0);
			Float ratio = mRatioMap.get(url);
			FrameLayout.LayoutParams fll = (FrameLayout.LayoutParams) holder.image.getLayoutParams();
//		if (ratio != null && ratio > 0) {
//			fll.height = (int)(fll.width * ratio);
//			viewHolder.image.setLayoutParams(fll);
//		} else {
//			fll.height = fll.width;
//			viewHolder.image.setLayoutParams(fll);
//		}
			loadPic(holder.image, url);
		} else {
			ViewHolder holder = (ViewHolder) viewHolder;
			holder.updater.setText(pic.mAuthor);
			holder.text.setText(pic.mDesc);
			holder.oo.setText(String.valueOf(pic.mOO));
			holder.xx.setText(String.valueOf(pic.mXX));
			holder.time.setText(Utilities.convertTime(pic.mTime));

			final String url = pic.mUrls.get(0);
			Float ratio = mRatioMap.get(url);
			FrameLayout.LayoutParams fll = (FrameLayout.LayoutParams) holder.image.getLayoutParams();
			loadPic(holder.image, url);
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		String url = mPicFile.get(position).mUrls.get(0);
		if (url.endsWith("gif")) {
			return TYPE_GIF;
		} else
			return TYPE_JPG;
	}

	@Override
	public int getItemCount() {
		return mPicFile.size();
	}

	private void loadPic(final ImageView imageView, String thumbUrl) {
		imageView.setTag(thumbUrl);

		final Drawable drawable = mLruCache.get(thumbUrl);
		if (drawable != null && !thumbUrl.endsWith("gif")) {
			imageView.setImageDrawable(drawable);
		} else {
			imageView.setImageResource(R.drawable.loading);
			imageView.setTag(thumbUrl);
			mImageLoader.request(thumbUrl, imageView, new ImageLoader.Callback() {
				@Override
				public void onLoaded(final String thumbUrl, final Drawable drawable) {
					mLruCache.put(thumbUrl, drawable);
					if (drawable.getIntrinsicHeight() != 0) {
						float ratio = drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
						Log.d(TAG, "setImage Ratio: " + ratio);
						mRatioMap.put(thumbUrl, ratio);
					}

					Log.d(TAG, "setImageUri: " + drawable.toString());

					imageView.post(new Runnable() {
						@Override
						public void run() {
							if (imageView.getTag() == thumbUrl)
								imageView.setImageDrawable(drawable);
						}
					});

				}
			});
		}
	}

	public int getCount() {
		return 10;
	}


	public static class GifViewHolder extends RecyclerView.ViewHolder {
		public TextView updater;
		public ExpandableTextView text;
		public TextView oo;
		public TextView xx;
		public TextView time;
		public GifImageView image;

		public GifViewHolder(View itemView) {
			super(itemView);
			updater = (TextView) itemView.findViewById(R.id.updater);
			text = (ExpandableTextView) itemView.findViewById(R.id.text);
			oo = (TextView) itemView.findViewById(R.id.oo);
			xx = (TextView) itemView.findViewById(R.id.xx);
			time = (TextView) itemView.findViewById(R.id.time);
			image = (GifImageView) itemView.findViewById(R.id.gif_image);
		}
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView updater;
		public ExpandableTextView text;
		public TextView oo;
		public TextView xx;
		public TextView time;
		public ImageView image;

		public ViewHolder(View itemView) {
			super(itemView);
			updater = (TextView) itemView.findViewById(R.id.updater);
			text = (ExpandableTextView) itemView.findViewById(R.id.text);
			oo = (TextView) itemView.findViewById(R.id.oo);
			xx = (TextView) itemView.findViewById(R.id.xx);
			time = (TextView) itemView.findViewById(R.id.time);
			image = (ImageView) itemView.findViewById(R.id.image);
		}
	}
}
