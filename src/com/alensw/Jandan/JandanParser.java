package com.alensw.Jandan;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JandanParser {
    final String TAG = "JandanParser";

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
    OnImageChangedlistener listener;

    public interface OnImageChangedlistener{
        void OnImageChanged();
    }
    public void setOnImageChangedlistener(OnImageChangedlistener onImageChangedlistener){
        this.listener = onImageChangedlistener;
    }

    public JandanParser(Context context){
        this.context = context;
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
            pattern = Pattern.compile("src=(.*)\"");
            matcher = pattern.matcher(thumbs_b.toString());

            /*if (matcher.find()) {

                item.put("image",R.drawable.loading);

                final Matcher finalMatcher = matcher;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        item.put("image", getBitMap(
                                finalMatcher.group()
                                        .substring(5, finalMatcher.group().length()-1)
                                        .replaceAll("square","small")));
                        listener.OnImageChanged();
                    }
                }).start();

            }*/

            Elements indexs = i.getElementsByClass("indexs");
            Log.d(TAG, "indexs = " + indexs);
            //title
            pattern = Pattern.compile("l\">(.*)</a>");
            matcher = pattern.matcher(indexs.toString());
            if (matcher.find()){
                item.put("title",matcher.group(1));
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

            //by
            pattern = Pattern.compile("author.+?>(.+?)</a>");
            matcher = pattern.matcher(time_s.toString());
            if (matcher.find()){
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
        try {
            URL url = new URL(strUrl);
            URLConnection conn = url.openConnection();
            is = conn.getInputStream();
        } catch (IOException e) {
            return null;
        }
        bitmap = BitmapFactory.decodeStream(is);
        if  ( bitmap.getHeight() >= 4096 ){
            return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth()/(bitmap.getHeight()/4096),4096);
        }
        return bitmap;
    }

}
