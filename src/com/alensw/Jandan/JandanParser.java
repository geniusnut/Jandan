package com.alensw.Jandan;


import android.content.ContentValues;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JandanParser {
    final static String TAG = "JandanParser";

    final Context context;
    Document document ;
    //String UA = "Mozilla/5.0 (Linux; Android 4.1.1; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Safari/535.19";
    String UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.94 Safari/537.36";
    final String Home_URL = "http://i.jandan.net/page/";
    final String PIC_URL = "http://i.jandan.net/pic";
    final String OOXX_URL = "http://i.jandan.net/ooxx";
    String PIC_PAGE ;
    String OOXX_PAGE ;
    int timeout = 5000;
    FileCache mCache;
    ThreadPoolExecutor mExecutor;
    OnImageChangedlistener listener;

    public interface OnImageChangedlistener{
        void OnImageChanged();
    }
    public void setOnImageChangedlistener(OnImageChangedlistener onImageChangedlistener){
        this.listener = onImageChangedlistener;
    }

    public JandanParser(Context context){
        this.context = context;
        mCache = new FileCache(context);
        mExecutor = new ThreadPoolExecutor(2, 64, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(25));
    }

    public List<Map<String, Object>> JandanHomePage(int Page){

        Log.d("HOMEPAGE",""+Page);
        List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();

        try {
            document = Jsoup.connect(Home_URL +Page)
                    .timeout(timeout)
                    .userAgent(UA)
                    .get();
        }
        catch (Exception e){
            Log.e(TAG,e.toString());
            return items;
        }

        Elements posts = document.getElementsByClass("post");

        for(Element i:posts){
            final Map<String, Object> item = new HashMap<String, Object>();

            Elements thumbs_b = i.getElementsByClass("thumbs_b");

            //link
            Pattern pattern = Pattern.compile("http://(.*)html");
            Matcher matcher = pattern.matcher(thumbs_b.toString());
            if (matcher.find()){
                item.put("link",matcher.group());
            }

            //image
            pattern = Pattern.compile("src=\"(.*?)\"");
            matcher = pattern.matcher(thumbs_b.toString());
            //Log.d(TAG, "thumbs_b = " + thumbs_b);
            if (matcher.find()) {
                item.put("image",R.drawable.loading);
                final String thumbUrl = matcher.group(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        item.put("image", getBitMap(thumbUrl));
                        listener.OnImageChanged();
                    }
                }).start();

            }

            Elements indexs = i.getElementsByClass("indexs");

            //title
            pattern = Pattern.compile("l\">(.*)</a>");
            matcher = pattern.matcher(indexs.toString());
            if (matcher.find()){
                item.put("title",matcher.group(1));
            }
            Elements title2 = i.getElementsByClass("title2");
            pattern = Pattern.compile("title=\"(.+?)\"");
            matcher = pattern.matcher(title2.toString());
            if (matcher.find()){
                item.put("title", matcher.group(1));
            }

            //cont
            pattern = Pattern.compile(">([0-9]*)</span>");
            matcher = pattern.matcher(indexs.toString());
            if (matcher.find()){
                item.put("cont",matcher.group(1));
            }else {
                item.put("cont","0");
            }

            Elements time_s = i.getElementsByClass("time_s");
            //Log.d("JandanParser", "time_s : " + time_s);
            //by
            pattern = Pattern.compile("author.+?>(.+?)</a>");
            matcher = pattern.matcher(time_s.toString());
            if (matcher.find()) {
                item.put("by",matcher.group(1));
            }

            //tag
            pattern = Pattern.compile("\"tag\">(.*)</a>");
            matcher = pattern.matcher(time_s.toString());
            if (matcher.find()){
                item.put("tag",matcher.group(1));
            }else {
                item.put("tag","");
            }


            //add item to items
            if(item.get("title") != null) {
                items.add(item);
            }
        }
        //Log.e(TAG,items.toString());
        return items;
    }


    public List<Map<String, Object>> JandanPicPage(int Page){

        List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
        ContentValues values = new ContentValues(6);

        //page
        if (Page == 0){

            try {
                document = Jsoup.connect(PIC_URL)
                        .timeout(timeout)
                        .userAgent(UA)
                        .get();
            }
            catch (Exception e){
                Log.e(TAG,e.toString());
                //Toast.makeText(context,"无法连接到服务器，请稍后再试",Toast.LENGTH_SHORT).show();
                return items;
            }

            Elements comment_page = document.getElementsByClass("current-comment-page");
            PIC_PAGE = comment_page.get(0).toString().substring(36,comment_page.get(1).toString().length()-8);
        }else {
            try {
                document = Jsoup.connect("http://i.jandan.net/pic/page-"+(Integer.parseInt(PIC_PAGE)-Page))
                        .timeout(timeout)
                        .userAgent(UA)
                        .get();
            }
            catch (Exception e){
                Log.e(TAG,e.toString());
                //Toast.makeText(context,"无法连接到服务器，请稍后再试",Toast.LENGTH_SHORT).show();
                return items;
            }

        }

        Elements posts = document.select("li");

        for(Element i:posts){
            final Map<String, Object> item = new HashMap<String, Object>();

            //id
            Pattern pattern = Pattern.compile("comment-[0-9]*");
            Matcher matcher = pattern.matcher(i.toString());
            if (matcher.find()){
                String id = (matcher.group().substring(8));
                item.put("id", id);
                values.put(NodeColumns.ID, id);
                //Log.e(TAG,item.get("id").toString());
            }

            Elements author = i.getElementsByClass("author");
            //updater
            pattern = Pattern.compile(">(.*?)</strong>");
            matcher = pattern.matcher(author.toString());
            if (matcher.find()){
                item.put("updater",matcher.group(1));
            }

            //time
            pattern = Pattern.compile(">@(.*?)<");
            matcher = pattern.matcher(author.toString());
            if (matcher.find()){
                item.put("time",matcher.group(1));
            }

            //text
            Elements text = i.getElementsByClass("text");
            pattern = Pattern.compile("<p>(\\S*)<br");
            matcher = pattern.matcher(text.toString());
            if (matcher.find()){
                item.put("text",matcher.group().substring(3, matcher.group().length() - 3));
            }

            //ooxx
            if (item.get("id") != null) {
                pattern = Pattern.compile("id=\"cos_support-" + item.get("id").toString() + "\">(.*)</span>");
                matcher = pattern.matcher(i.toString());
                if (matcher.find()) {
                    item.put("oo", matcher.group().substring(25, matcher.group().length() - 7));
                }
                pattern = Pattern.compile("id=\"cos_unsupport-" + item.get("id").toString() + "\">(.*)</span>");
                matcher = pattern.matcher(i.toString());
                if (matcher.find()) {
                    item.put("xx", matcher.group().substring(27, matcher.group().length() - 7));
                }
            }

            listener.OnImageChanged();
            //imageHashMap<String, Object>()
            //pattern = Pattern.compile("src=\"(\\S*)[^ ][jpg]\"");
            pattern = Pattern.compile("img src=\"(.+?)\"");
            matcher = pattern.matcher(i.toString());
            if (matcher.find()){
                item.put("image",R.drawable.loading);
                values.put(NodeColumns.URL, matcher.group(1));
                final Matcher finalMatcher = matcher;
                Future<?> future = mExecutor.submit(new dlTask(values.getAsString("url"), values.getAsString("_id")));
                File file = null;
                try {
                    file = (File) future.get();
                } catch (Throwable e) {
                    Log.e(TAG, "futureTask get : " + e);
                }
                if (file != null) {


                    item.put("image", createThumbnail(file.getPath()));
//                    Bitmap bitmap;
//                    bitmap = BitmapFactory.decodeFile(file.getPath());
//                    if (bitmap != null) {
//                        Log.d(TAG, "bitmap : " + bitmap.getWidth() + "   " + bitmap.getHeight());
//                        if (bitmap.getWidth() <= 600) {
//                            float scale = 600/bitmap.getWidth();
//                            item.put("image", createScaledBitmap(bitmap, scale));
//                        } else if (bitmap.getHeight() >= 4096) {
//                            item.put("image", Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() / (bitmap.getHeight() / 4096), 4096));
//                        } else {
//                            item.put("image", bitmap);
//                        }
//                        listener.OnImageChanged();
//                    }
                    listener.OnImageChanged();
                }
            }

            pattern = Pattern.compile("org_src=\"(.+?)\"");
            matcher = pattern.matcher(i.toString());
            item.put("isgif", false);
            if (matcher.find()) {
                Log.d(TAG, "org_url : " + matcher.group(1));
                values.put(NodeColumns.ORG_URL, matcher.group(1));
                if (matcher.group(1).endsWith("gif"))
                    item.put("isgif", true);
            }
            if (values.size() > 0)
                mCache.updateCache(values);

            //add item to items
            if(item.get("image") != null) {
                items.add(item);
            }
        }
        return items;
    }

    private class dlTask implements Callable<File> {
        private final String mUrl;
        private final String mId;
        public dlTask(String url, String id) {
            mUrl = url;
            mId = id;
        }

        @Override
        public File call() throws Exception {
            File file = mCache.generateCacheFile(mId);
            InputStream is = null;
            FileOutputStream os = new FileOutputStream(file);
            try {
                URL url = new URL(mUrl);
                URLConnection conn = url.openConnection();
                is = conn.getInputStream();
                final byte[] data = new byte[8192];
                int bytes = 0;
                while ((bytes = is.read(data)) >= 0) {
                    if (bytes > 0)
                        os.write(data, 0, bytes);
                }
                //listener.OnImageChanged();
            } catch (IOException e) {
                return null;
            } finally {
                is.close();
            }
            return file;
        }
    }


    public List<Map<String, Object>> JandanOoxxPage(int Page){

        List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();

        //page
        if (Page == 0){

            try {
                document = Jsoup.connect(OOXX_URL)
                        .timeout(timeout)
                        .userAgent(UA)
                        .get();
            }
            catch (Exception e){
                Log.e(TAG,e.toString());
                //Toast.makeText(context,"无法连接到服务器，请稍后再试",Toast.LENGTH_SHORT).show();
                return items;
            }

            Elements comment_page = document.getElementsByClass("current-comment-page");
            OOXX_PAGE = comment_page.get(0).toString().substring(36,comment_page.get(1).toString().length()-8);
        }else {
            try {
                document = Jsoup.connect("http://i.jandan.net/ooxx/page-"+(Integer.parseInt(OOXX_PAGE)-Page))
                        .timeout(timeout)
                        .userAgent(UA)
                        .get();
            }
            catch (Exception e){
                Log.e(TAG,e.toString());
                //Toast.makeText(context,"无法连接到服务器，请稍后再试",Toast.LENGTH_SHORT).show();
                return items;
            }

        }

        Elements posts = document.select("li");

        for(Element i:posts){
            final Map<String, Object> item = new HashMap<String, Object>();

            //id
            Pattern pattern = Pattern.compile("comment-[0-9]*");
            Matcher matcher = pattern.matcher(i.toString());
            if (matcher.find()){
                item.put("id",matcher.group().substring(8));
                //Log.e(TAG,item.get("id").toString());
            }

            //updater
            pattern = Pattern.compile("<b>(.*)</b>");
            matcher = pattern.matcher(i.toString());
            if (matcher.find()){
                item.put("updater",matcher.group().substring(3,matcher.group().length()-4));

            }

            Elements time = i.getElementsByClass("time");
            //time
            pattern = Pattern.compile("[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9]:[0-9][0-9]");
            matcher = pattern.matcher(time.toString());
            if (matcher.find()){
                item.put("time",matcher.group());
            }

            //text
            Elements text = i.getElementsByClass("commenttext");
            pattern = Pattern.compile("<p>(\\S*)<br");
            matcher = pattern.matcher(text.toString());
            if (matcher.find()){
                item.put("text",matcher.group().substring(3, matcher.group().length() - 3));
            }

            //ooxx
            if (item.get("id") != null) {
                pattern = Pattern.compile("id=\"cos_support-" + item.get("id").toString() + "\">(.*)</span>");
                matcher = pattern.matcher(i.toString());
                if (matcher.find()) {
                    item.put("oo", matcher.group().substring(25, matcher.group().length() - 7));
                }
                pattern = Pattern.compile("id=\"cos_unsupport-" + item.get("id").toString() + "\">(.*)</span>");
                matcher = pattern.matcher(i.toString());
                if (matcher.find()) {
                    item.put("xx", matcher.group().substring(27, matcher.group().length() - 7));
                }
            }

            //image
            pattern = Pattern.compile("src=\"(\\S*)[^ ][jpg]\"");
            matcher = pattern.matcher(i.toString());
            if (matcher.find()){
                item.put("image",R.drawable.loading);

                final Matcher finalMatcher = matcher;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.e(TAG,finalMatcher.group().substring(5, finalMatcher.group().length()-1));
                        item.put("image", getBitMap(
                                finalMatcher.group()
                                        .substring(5, finalMatcher.group().length()-1)));
                        listener.OnImageChanged();
                    }
                }).start();
            }

            //add item to items
            if(item.get("image") != null) {
                items.add(item);
            }
        }
        return items;
    }

    private Bitmap getBitMap(String strUrl) {
        Bitmap bitmap = null;
        InputStream is = null;
        //Log.d(TAG, "getBitmap url = " + strUrl);
        try {
            URL url = new URL(strUrl);
            URLConnection conn = url.openConnection();
            is = conn.getInputStream();
        } catch (IOException e) {
            return null;
        }
        bitmap = BitmapFactory.decodeStream(is);
        //Log.d(TAG, "bitmap : " + bitmap.getWidth() + "   " + bitmap.getHeight());
        if  ( bitmap.getHeight() >= 4096 ){
            return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth()/(bitmap.getHeight()/4096),4096);
        }
        return bitmap;
    }


    public Bitmap createThumbnail(String path) {
        Log.d(TAG, "");
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(path);
        //  Log.d(TAG, "bitmap height = " + bitmap.getHeight());
        if (bitmap.getHeight() > 4096) {
            BitmapRegionDecoder regionDecoder = null;
            try {
                regionDecoder = BitmapRegionDecoder.newInstance(path, false);
            } catch (Throwable e) {
                Log.e(TAG, "");
            }
            Rect rect = new Rect();
            rect.right = bitmap.getWidth();
            rect.bottom = (int) (rect.right * 0.62);
            return regionDecoder.decodeRegion(rect, null);
        }
        return bitmap;
    }
    public static Bitmap createScaledBitmap(Bitmap bitmap, float scale) {
        Bitmap newBmp = null;
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        while (newBmp == null) {
            try {
                final int width = bitmap.getWidth();
                final int height = bitmap.getHeight();
                final int outWidth = Math.round(width * scale);
                final int outHeight = Math.round(height * scale);
                newBmp = Bitmap.createBitmap(outWidth, outHeight, config);
                final Canvas canvas = new Canvas(newBmp);
                canvas.scale(scale, scale);
                canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG));
                //	Log.d("BitmapUtils", "create scaled: " + width + "x" + height + " -> " + outWidth + "x" + outHeight);
                return newBmp;
            } catch (OutOfMemoryError e) {
                if (config == Bitmap.Config.ARGB_8888) {
                    config = Bitmap.Config.RGB_565;
                    continue;
                }
            } catch (Throwable e) {
                Log.e("BitmapUtils", "create scaled: " + e);
            }
            break;
        }
        return null;
    }
}
