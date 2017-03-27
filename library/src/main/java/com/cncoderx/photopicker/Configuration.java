package com.cncoderx.photopicker;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 2017/3/24.
 */
public class Configuration implements Parcelable {
    private int maxCount = 1;
    private int aspectX = 1;
    private int aspectY = 1;
    private int cropOutputX;
    private int cropOutputY;
    private boolean crop;
    private boolean cropScale;
    private boolean cropScaleUp;
    private boolean circleCrop;
    private boolean hideCamera;

    Configuration() {
    }

    Configuration(Parcel in) {
        maxCount = in.readInt();
        aspectX = in.readInt();
        aspectY = in.readInt();
        cropOutputX = in.readInt();
        cropOutputY = in.readInt();
        crop = in.readByte() != 0;
        cropScale = in.readByte() != 0;
        cropScaleUp = in.readByte() != 0;
        circleCrop = in.readByte() != 0;
        hideCamera = in.readByte() != 0;
    }

    public static final Creator<Configuration> CREATOR = new Creator<Configuration>() {
        @Override
        public Configuration createFromParcel(Parcel in) {
            return new Configuration(in);
        }

        @Override
        public Configuration[] newArray(int size) {
            return new Configuration[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(maxCount);
        dest.writeInt(aspectX);
        dest.writeInt(aspectY);
        dest.writeInt(cropOutputX);
        dest.writeInt(cropOutputY);
        dest.writeByte((byte) (crop ? 1 : 0));
        dest.writeByte((byte) (cropScale ? 1 : 0));
        dest.writeByte((byte) (cropScaleUp ? 1 : 0));
        dest.writeByte((byte) (circleCrop ? 1 : 0));
        dest.writeByte((byte) (hideCamera ? 1 : 0));
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getAspectX() {
        return aspectX;
    }

    public void setAspectX(int aspectX) {
        this.aspectX = aspectX;
    }

    public int getAspectY() {
        return aspectY;
    }

    public void setAspectY(int aspectY) {
        this.aspectY = aspectY;
    }

    public int getCropOutputX() {
        return cropOutputX;
    }

    public void setCropOutputX(int cropOutputX) {
        this.cropOutputX = cropOutputX;
    }

    public int getCropOutputY() {
        return cropOutputY;
    }

    public void setCropOutputY(int cropOutputY) {
        this.cropOutputY = cropOutputY;
    }

    public boolean isCrop() {
        return crop;
    }

    public void setCrop(boolean crop) {
        this.crop = crop;
    }

    public boolean isCropScale() {
        return cropScale;
    }

    public void setCropScale(boolean cropScale) {
        this.cropScale = cropScale;
    }

    public boolean isCropScaleUp() {
        return cropScaleUp;
    }

    public void setCropScaleUp(boolean cropScaleUp) {
        this.cropScaleUp = cropScaleUp;
    }

    public boolean isCircleCrop() {
        return circleCrop;
    }

    public void setCircleCrop(boolean circleCrop) {
        this.circleCrop = circleCrop;
    }

    public boolean isHideCamera() {
        return hideCamera;
    }

    public void setHideCamera(boolean hideCamera) {
        this.hideCamera = hideCamera;
    }
}
