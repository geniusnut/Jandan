package com.alensw.http;

import android.graphics.Bitmap;
import com.alensw.Jandan.Pic;
import com.alensw.Jandan.Post;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/3/21.
 */
public class PicParser {
    final static String TAG = "";
    final static String PIC_URL = "http://i.jandan.net/?oxwlxojflwblxbsapi=jandan.get_pic_comments";

    private static final DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public ArrayList<Pic> parse(int page) {
        final String url = PIC_URL + "&page=" + page;
        final String content = HttpClient.downloadJson(url);


        ArrayList<Pic> pics = new ArrayList<Pic>();
        try {
            JSONObject json = new JSONObject(content);
            JSONArray jsonPics = json.getJSONArray("comments");
            for (int i = 0; i < jsonPics.length(); i++) {
                Pic pic = new Pic();
                JSONObject jsonPic = (JSONObject) jsonPics.get(i);
                pic.mId = jsonPic.getInt("comment_ID");
                pic.mAuthor = jsonPic.getString("comment_author");
                pic.mDesc = jsonPic.getString("text_content");
                pic.mPhotos = jsonPic.getJSONArray("pics").length();
                pic.mUrls = new ArrayList<String> (pic.mPhotos);
                for (int j = 0; j < pic.mPhotos; j++) {
                    pic.mUrls.add(jsonPic.getJSONArray("pics").getString(j));
                }
                pic.mOO = jsonPic.getInt("vote_positive");
                pic.mXX = jsonPic.getInt("vote_negative");
                pic.mTime = parseTime(jsonPic.getString("comment_date"));
                pics.add(pic);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pics;
    }

    private long parseTime(String time) {
        try {
            return mDateFormat.parse(time).getTime() / 1000;
        } catch (ParseException e) {
            return 0;
        }
    }
}
