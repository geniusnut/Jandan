package com.android.volley;

import android.content.Context;

import com.android.volley.toolbox.Volley;

/**
 * Created by yw07 on 15/12/15.
 */
public class VolleyWrapper {

    private static RequestQueue mInstance;
    public static RequestQueue getInstance(Context context) {
        if (mInstance == null) {
            synchronized (VolleyWrapper.class) {
                if (mInstance == null) {
                    mInstance = Volley.newRequestQueue(context);
                }
            }
        }
        return mInstance;
    }
}
