package com.cncoderx.photopicker.core;

import android.os.Parcelable;

/**
 * Created by wujie on 2017/2/22.
 */
public interface IImage extends Parcelable {
    String getPath();
    int getWidth();
    int getHeight();
    int getRotation();
    long getDateToken();
}
