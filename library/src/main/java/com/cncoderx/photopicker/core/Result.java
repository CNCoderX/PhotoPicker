package com.cncoderx.photopicker.core;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wujie on 2017/2/24.
 */
public class Result implements Parcelable {
    private int size;
    private List<IImage> data;

    public Result() {
    }

    protected Result(Parcel in) {
        size = in.readInt();
        data = new ArrayList<>();
        in.readList(data, getClass().getClassLoader());
    }

    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(size);
        dest.writeList(data);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<IImage> getData() {
        return data;
    }

    public void setData(List<IImage> data) {
        this.data = data;
    }
}
