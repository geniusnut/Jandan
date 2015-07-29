package com.nut.dao;

/**
 * Created by Administrator on 2015/7/28.
 */
public class JokeModel {
    public long mId;
    public String mAuthor;
    public String mDate;
    public String mContent;
    public int mPositive;
    public int mNegative;
    public int mComments = 0;

    //duoshuo commentid
    public long mCommentId;
}
