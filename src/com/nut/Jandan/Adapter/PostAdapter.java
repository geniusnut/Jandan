package com.nut.Jandan.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nut.dao.CommentModel;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nut.Jandan.R;
import com.nut.cache.Post;
import com.nut.dao.PostFormater;
import com.nut.http.PostParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/5/25.
 */

// refer to https://github.com/kanytu/android-parallax-recyclerview
public class PostAdapter extends RecyclerView.Adapter {
    private static final String TAG = "PostAdapter";
    private static int TYPE_HEADER = 0;
    private static int TYPE_FIRST_ITEM = 1;
    private static int TYPE_ITEM = 2;
    private static int TYPE_COMMENT = 3;

    private final Post mPost;
    private float SCROLL_MULTIPLIER = 0.5f;
    private int mTotalYScrolled;

    private boolean mShowComment = false;
    private RecyclerView mRecyclerView;
    private HeaderWrapper mHeader;
    private OnParallaxScroll mParallaxScroll;
    private final LinearLayoutManager mLayoutManager;
    private List<CommentModel> mComments;

    public interface OnParallaxScroll {
        /**
         * Event triggered when the parallax is being scrolled.
         *
         * @param percentage
         * @param offset
         * @param parallax
         */
        void onParallaxScroll(float percentage, float offset, View parallax);
    }

    public PostAdapter(Post post, RecyclerView recyclerView) {
        mPost = post;
        mRecyclerView = recyclerView;
        mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        mComments = new ArrayList<CommentModel>(0);
    }

    public void translateHeader(float of) {
        float ofCalculated = of * SCROLL_MULTIPLIER;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mHeader.setTranslationY(ofCalculated);
        } else {
            TranslateAnimation anim = new TranslateAnimation(0, 0, ofCalculated, ofCalculated);
            anim.setFillAfter(true);
            anim.setDuration(0);
            mHeader.startAnimation(anim);
        }
        mHeader.setClipY(Math.round(ofCalculated));
        if (mParallaxScroll != null) {
            float left = Math.min(1, ((ofCalculated) / (mHeader.getHeight() * SCROLL_MULTIPLIER)));
            mParallaxScroll.onParallaxScroll(left, of, mHeader);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_HEADER) {
            mHeader = new HeaderWrapper(viewGroup.getContext(), true);
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_web_cover, viewGroup, false);
            mHeader.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (mHeader != null) {
                        mTotalYScrolled += dy;
                        translateHeader(mTotalYScrolled);
                    }
                }
            });
            return new VHHeader(mHeader);
        } else if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_web_content, viewGroup, false);
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForPosition(0);
            if (holder != null) {
                translateHeader(-holder.itemView.getTop());
                mTotalYScrolled = -holder.itemView.getTop();
            }
            return new VHItem(view);
        } else if (viewType == TYPE_COMMENT) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment, viewGroup, false);
            return new VHComment(view);
        } else
            return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
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

            ((VHItem) viewHolder).commToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mShowComment = !mShowComment;
                    if (mShowComment) {
                        ((VHItem) viewHolder).commToggle.setEnabled(false);
                        ((VHItem) viewHolder).ivCarat.setRotation(0);
                        new CommentsTask(((VHItem) viewHolder).commToggle).execute(String.valueOf(mPost.mId));
                    } else {
                        ((VHItem) viewHolder).ivCarat.setRotation(180);
                        int scrollY = mRecyclerView.getScrollY();
                        int offsetY = ((VHItem) viewHolder).ivCarat.getBottom();
                        int size = mComments.size();
                        mComments.clear();
                        notifyItemRangeRemoved(2, size);
                        notifyItemRangeChanged(2, size);
                    }
                }
            });

        } else if (viewHolder instanceof VHComment) {
            CommentModel comment = mComments.get(position - 2);
            ((VHComment) viewHolder).mAuthor.setText(comment.mAuthor);
            ((VHComment) viewHolder).mDate.setText(comment.mDate);
            ((VHComment) viewHolder).mContent.setText(Html.fromHtml(comment.mContent));
        }
    }

    @Override
    public int getItemCount() {
        return 2 + mComments.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else if (position == 1) {
            return TYPE_ITEM;
        } else {
            return TYPE_COMMENT;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private static class VHHeader extends RecyclerView.ViewHolder {
        private ImageView coverView;
        public VHHeader(View itemView) {
            super(itemView);
            coverView = (ImageView) itemView.findViewById(R.id.cover);
        }
    }

    private static class VHItem extends RecyclerView.ViewHolder {
        private final WebView mWebView;
        private final TextView commTextView;
        private final ImageView ivCarat;
        private final WebView mCommWebView;
        private final ViewGroup commToggle;
        public VHItem(View itemView) {
            super(itemView);
            mWebView = (WebView) itemView.findViewById(R.id.webview);
            mWebView.getSettings().setJavaScriptEnabled(true);

            mWebView.setWebViewClient(new WebViewClient());
            commTextView = (TextView) itemView.findViewById(R.id.btn_post_comm_text);
            ivCarat = (ImageView) itemView.findViewById(R.id.ivCarat);
            mCommWebView = (WebView) itemView.findViewById(R.id.post_comm);
            commToggle = (ViewGroup) itemView.findViewById(R.id.btn_post_comm);
        }
    }

    private static class VHComment extends RecyclerView.ViewHolder {

        private final TextView mAuthor;
        private final TextView mDate;
        private final TextView mContent;
        public VHComment(View itemView) {
            super(itemView);
            mAuthor = (TextView) itemView.findViewById(R.id.author);
            mDate = (TextView) itemView.findViewById(R.id.time);
            mContent = (TextView) itemView.findViewById(R.id.comment_content);

        }
    }

    public void setOnParallaxScroll(OnParallaxScroll parallaxScroll) {
        mParallaxScroll = parallaxScroll;
        mParallaxScroll.onParallaxScroll(0, 0, mHeader);
    }

    static class HeaderWrapper extends RelativeLayout {
        private int mOffset;
        private boolean mIsClipped;

        public HeaderWrapper(Context context, boolean isClipped) {
            super(context);
            mIsClipped = isClipped;
        }

        public void setClipY(int offset) {
            mOffset = offset;
            invalidate();
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            if (mIsClipped) {
                canvas.clipRect(getLeft(), getTop(), getRight(), getBottom() + mOffset);
            }
            super.dispatchDraw(canvas);
        }
    }

    public class webViewLoad extends AsyncTask<String, Void, String> {
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
            data = addJS(data);
            mWebView.addJavascriptInterface(new WebAppInterface(mWebView.getContext()), "Android");
            mWebView.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
        }
    }

    public static String addJS(String data) {
        Pattern patt = Pattern.compile("<img src=.+?>");
        Matcher matcher = patt.matcher(data);

        while (matcher.find()) {
            String str = matcher.group(0);
            String str1 = str.substring(0, str.length() - 1).concat(" onClick=\"showAndroidToast('Hello Android!')\" />");
            // str1 = str1.replace("<img", "<input type=\"image\"");
            // str1 = "<input type=\"button\" value=\"Say hello\" onClick=\"showAndroidToast('Hello Android!')\" />";
            data = data.replace(str, str1);
        }
        Log.d(TAG, "data: " + data);
        return data;
    }


    public class WebAppInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

    public class CommentsTask extends AsyncTask<String, Void, ArrayList<CommentModel>> {

        View mCommToggle;

        public CommentsTask(View commToggle) {
            mCommToggle = commToggle;
        }

        @Override
        protected ArrayList<CommentModel> doInBackground(String... params) {
            return PostParser.parseComments(params[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<CommentModel> commentModels) {
            if (mComments.size() > 0 )
                mComments.clear();
            mComments.addAll(commentModels);
            notifyItemRangeChanged(2, mComments.size());
            //  mCommToggle.setEnabled(true);
            if (mRecyclerView != null)
                mRecyclerView.smoothScrollToPosition(2);
        }
    }
}
