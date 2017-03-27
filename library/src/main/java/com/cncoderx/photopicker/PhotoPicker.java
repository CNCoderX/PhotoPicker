package com.cncoderx.photopicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.cncoderx.photopicker.core.ImageCacheService;
import com.cncoderx.photopicker.ui.GalleryActivity;

/**
 * Created by admin on 2017/2/16.
 */
public class PhotoPicker {
    private static boolean initialized = false;
    private static ImageCacheService sImageCacheService;

    public static final String EXTRA_CONFIGURATION = "configuration";
    public static final String EXTRA_DATA = "data";

    public static void initialize(Context context) {
        if (!initialized) {
            initialized = true;
            createImageCacheService(context);
        }
    }

    private static void createImageCacheService(Context context) {
        if (sImageCacheService == null) {
            sImageCacheService = new ImageCacheService(context);
        }
    }

    public static ImageCacheService getImageCacheService() {
        return sImageCacheService;
    }

    public static void startGalleryActivity(Activity activity, int requestCode, Configuration configuration) {
        Intent intent = new Intent(activity, GalleryActivity.class);
        intent.putExtra(EXTRA_CONFIGURATION, configuration);
        activity.startActivityForResult(intent, requestCode);
    }

    public static class Builder {
        private Activity activity;
        private Configuration configuration;

        public Builder(Activity activity) {
            this.activity = activity;
            configuration = new Configuration();
            initialize(activity.getApplicationContext());
        }

        public Builder setMaxCount(int maxCount) {
            configuration.setMaxCount(maxCount);
            return this;
        }

        public Builder setAspect(int aspectX, int aspectY) {
            configuration.setAspectX(aspectX);
            configuration.setAspectY(aspectY);
            return this;
        }

//        public Builder setCrop(boolean crop) {
//            configuration.setCrop(crop);
//            return this;
//        }

        public Builder setCropScale(boolean cropScale) {
            configuration.setCropScale(cropScale);
            return this;
        }

        public Builder setCropScaleUp(boolean cropScaleUp) {
            configuration.setCropScaleUp(cropScaleUp);
            return this;
        }

        public Builder circleCrop(boolean circleCrop) {
            configuration.setCircleCrop(circleCrop);
            return this;
        }

        public Builder hideCamera(boolean hideCamera) {
            configuration.setHideCamera(hideCamera);
            return this;
        }

        public Builder setCropOutputSize(int outputX, int outputY) {
            configuration.setCropOutputX(outputX);
            configuration.setCropOutputY(outputY);
            return this;
        }

        public void create(int requestCode) {
            startGalleryActivity(activity, requestCode, configuration);
        }
    }
}
