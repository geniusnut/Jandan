package com.nut.ui;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nut.Jandan.Fragment.PostFragment;
import com.nut.Jandan.R;
import com.nut.cache.Post;
import com.nut.dao.PostFormater;

/**
 * Created by Administrator on 2015/5/25.
 */
public class HeaderAdapter extends RecyclerView.Adapter {
    private static final String TAG = "HeaderAdapter";
    private static int TYPE_HEADER = 0;
    private static int TYPE_ITEM = 1;

    private final Post mPost;

    public HeaderAdapter(Post post) {
        mPost = post;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_web_cover, viewGroup, false);
            return new VHHeader(view);
        } else if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_web_content, viewGroup, false);
            return new VHItem(view);
        } else
            return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof VHHeader) {
            final ImageView coverView = ((VHHeader) viewHolder).coverView;

            if (mPost.mCover.endsWith("jpg"))
                ImageLoader.getInstance().loadImage(mPost.mCover, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                        coverView.post(new Runnable() {
                            @Override
                            public void run() {
                                coverView.setImageBitmap(loadedImage);
                            }
                        });
                    }
                });
        } else if (viewHolder instanceof VHItem) {

            Log.d(TAG, "webViewLoad : " + mPost.mLink);
            new webViewLoad(((VHItem) viewHolder).mWebView, PostFormater.POST_FORMAT).execute(mPost.mLink);

            //((VHItem) viewHolder).commTextView.setText(getString(R.string.post_comments, mPost.mCont));

            new webViewLoad(((VHItem) viewHolder).mCommWebView, PostFormater.COMMENT_FORMAT).execute(mPost.mLink);
            ((VHItem) viewHolder).commToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((VHItem) viewHolder).mCommWebView.getVisibility() == View.VISIBLE) {
                        ((VHItem) viewHolder).ivCarat.setRotation(0);
                        ((VHItem) viewHolder).mCommWebView.removeAllViews();
                        ((VHItem) viewHolder).mCommWebView.setVisibility(View.GONE);
                    } else {
                        ((VHItem) viewHolder).ivCarat.setRotation(180);
                        ((VHItem) viewHolder).mCommWebView.setVisibility(View.VISIBLE);
					/*scrollView.post(new Runnable() {
						@Override
						public void run() {
							scrollView.smoothScrollTo(0, linearLayout.getTop()-10);
						}
					});*/
                    }
                }
            });

        } else {
            //
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else
            return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    class VHHeader extends RecyclerView.ViewHolder {
        private ImageView coverView;
        public VHHeader(View itemView) {
            super(itemView);
            coverView = (ImageView) itemView.findViewById(R.id.cover);
        }
    }

    class VHItem extends RecyclerView.ViewHolder {
        private final WebView mWebView;
        private final TextView commTextView;
        private final ImageView ivCarat;
        private final WebView mCommWebView;
        private final RelativeLayout commToggle;
        public VHItem(View itemView) {
            super(itemView);
            mWebView = (WebView) itemView.findViewById(R.id.webview);
            commTextView = (TextView) itemView.findViewById(R.id.btn_post_comm_text);
            ivCarat = (ImageView) itemView.findViewById(R.id.ivCarat);
            mCommWebView = (WebView) itemView.findViewById(R.id.post_comm);
            commToggle = (RelativeLayout) itemView.findViewById(R.id.btn_post_comm);
        }
    }

    public static class webViewLoad extends AsyncTask<String, Void, String> {
        private final WebView mWebView;
        private final int mType;

        public webViewLoad(WebView webView, int type) {
            mWebView = webView;
            mType = type;
        }

        @Override
        protected String doInBackground(String... strings) {
            PostFormater postFormater = new PostFormater();
            return mType == 0 ? postFormater.postFormater(strings[0]) : postFormater.commFormater(strings[0]);
        }

        @Override
        protected void onPostExecute(String data) {
            mWebView.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
        }
    }
}
