package com.cncoderx.photopicker.utils;

import android.util.Log;

import com.cncoderx.photopicker.BuildConfig;

/**
 * Created by admin on 2017/2/15.
 */
public class Logger {
    public static final String TAG = "PhotoPicker";
    public static final boolean DEBUG = BuildConfig.DEBUG;

    public static void v(String msg){
        if(DEBUG) {
            Log.v(TAG, msg);
        }
    }

    public static void d(String msg){
        if(DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if(DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void e(Throwable throwable) {
        if(DEBUG) {
            if (throwable != null)
                Log.e(TAG, throwable.getMessage());
        }
    }

    public static void i(String msg){
        if(DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void w(String msg){
        if(DEBUG) {
            Log.w(TAG, msg);
        }
    }
}
