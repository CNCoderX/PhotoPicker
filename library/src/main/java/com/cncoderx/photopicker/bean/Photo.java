package com.cncoderx.photopicker.bean;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.cncoderx.photopicker.core.IImage;

/**
 * Created by wujie on 2017/2/15.
 */
public class Photo implements IImage {
    private static final int INDEX_ID = 0;
    private static final int INDEX_CAPTION = 1;
    private static final int INDEX_MIME_TYPE = 2;
    private static final int INDEX_LATITUDE = 3;
    private static final int INDEX_LONGITUDE = 4;
    private static final int INDEX_DATE_TAKEN = 5;
    private static final int INDEX_DATE_ADDED = 6;
    private static final int INDEX_DATE_MODIFIED = 7;
    private static final int INDEX_DATA = 8;
    private static final int INDEX_ORIENTATION = 9;
    private static final int INDEX_BUCKET_ID = 10;
    private static final int INDEX_SIZE = 11;
    private static final int INDEX_WIDTH = 12;
    private static final int INDEX_HEIGHT = 13;

    public int id;
    public String caption;
    public String mimeType;
    public long fileSize;
    public double latitude;
    public double longitude;
    public long dateTakenInMs;
    public long dateAddedInSec;
    public long dateModifiedInSec;
    public String filePath;
    public int rotation;
    public int bucketId;
    public int width;
    public int height;

    public Photo() {
    }

    public Photo(Cursor cursor) {
        id = cursor.getInt(INDEX_ID);
        caption = cursor.getString(INDEX_CAPTION);
        mimeType = cursor.getString(INDEX_MIME_TYPE);
        latitude = cursor.getDouble(INDEX_LATITUDE);
        longitude = cursor.getDouble(INDEX_LONGITUDE);
        dateTakenInMs = cursor.getLong(INDEX_DATE_TAKEN);
        dateAddedInSec = cursor.getLong(INDEX_DATE_ADDED);
        dateModifiedInSec = cursor.getLong(INDEX_DATE_MODIFIED);
        filePath = cursor.getString(INDEX_DATA);
        rotation = cursor.getInt(INDEX_ORIENTATION);
        bucketId = cursor.getInt(INDEX_BUCKET_ID);
        fileSize = cursor.getLong(INDEX_SIZE);
        width = cursor.getInt(INDEX_WIDTH);
        height = cursor.getInt(INDEX_HEIGHT);
    }

    public Photo(Parcel in) {
        id = in.readInt();
        caption = in.readString();
        mimeType = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        dateTakenInMs = in.readLong();
        dateAddedInSec = in.readLong();
        dateModifiedInSec = in.readLong();
        filePath = in.readString();
        rotation = in.readInt();
        bucketId = in.readInt();
        fileSize = in.readLong();
        width = in.readInt();
        height = in.readInt();
    }

    public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(caption);
        dest.writeString(mimeType);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeLong(dateTakenInMs);
        dest.writeLong(dateAddedInSec);
        dest.writeLong(dateModifiedInSec);
        dest.writeString(filePath);
        dest.writeInt(rotation);
        dest.writeInt(bucketId);
        dest.writeLong(fileSize);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getDateTakenInMs() {
        return dateTakenInMs;
    }

    public void setDateTakenInMs(long dateTakenInMs) {
        this.dateTakenInMs = dateTakenInMs;
    }

    public long getDateAddedInSec() {
        return dateAddedInSec;
    }

    public void setDateAddedInSec(long dateAddedInSec) {
        this.dateAddedInSec = dateAddedInSec;
    }

    public long getDateModifiedInSec() {
        return dateModifiedInSec;
    }

    public void setDateModifiedInSec(long dateModifiedInSec) {
        this.dateModifiedInSec = dateModifiedInSec;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getBucketId() {
        return bucketId;
    }

    public void setBucketId(int bucketId) {
        this.bucketId = bucketId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String getPath() {
        return getFilePath();
    }

    @Override
    public long getDateToken() {
        return getDateModifiedInSec();
    }
}
