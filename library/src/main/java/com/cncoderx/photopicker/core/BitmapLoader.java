package com.cncoderx.photopicker.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;

import com.cncoderx.photopicker.PhotoPicker;
import com.cncoderx.photopicker.io.BytesBufferPool;
import com.cncoderx.photopicker.utils.BitmapUtils;
import com.cncoderx.photopicker.utils.Logger;
import com.cncoderx.photopicker.utils.Thumbnail;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by wujie on 2017/2/17.
 */
public class BitmapLoader extends AsyncTask<IImage, Integer, Bitmap> {
    private Thumbnail thumbnail;
    private int targetSize;

    public BitmapLoader(Thumbnail thumbnail, int targetSize) {
        this.thumbnail = thumbnail;
        this.targetSize = targetSize;
    }

    @Override
    protected Bitmap doInBackground(IImage... IImages) {
        IImage IImage = IImages[0];
        ImageCacheService cacheService = PhotoPicker.getImageCacheService();
        BytesBufferPool bytesBufferPool = BytesBufferPool.getInstance();
        BytesBufferPool.BytesBuffer buffer = bytesBufferPool.get();
        try {
            boolean found = cacheService.getImageData(IImage.getPath(), IImage.getDateToken(), thumbnail, buffer);
            if (isCancelled()) return null;
            if (found) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap;
                if (thumbnail == Thumbnail.micro) {
                    bitmap = BitmapUtils.decodeUsingPool(this,
                            buffer.data, buffer.offset, buffer.length, options);
                } else {
                    bitmap = BitmapUtils.decodeUsingPool(this,
                            buffer.data, buffer.offset, buffer.length, options);
                }
                if (bitmap == null && !isCancelled()) {
                    Logger.w("decode cached failed");
                }
                return bitmap;
            }
        } finally {
            bytesBufferPool.recycle(buffer);
        }

        Bitmap bitmap = onDecodeOriginal(IImage.getPath(), targetSize, thumbnail);
        if (isCancelled()) return null;
        if (bitmap == null) {
            Logger.w("decode orig failed");
            return null;
        }
        if (thumbnail == Thumbnail.micro) {
            bitmap = BitmapUtils.resizeAndCropCenter(bitmap, targetSize, true);
        } else {
            bitmap = BitmapUtils.resizeDownBySideLength(bitmap, targetSize, true);
        }
        if (isCancelled()) return null;
        byte[] array = BitmapUtils.compressToBytes(bitmap);
        if (isCancelled()) return null;
        cacheService.putImageData(IImage.getPath(), IImage.getDateToken(), thumbnail, array);
        return bitmap;
    }

    private Bitmap onDecodeOriginal(String path, int targetSize, Thumbnail type) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if (type == Thumbnail.micro) {
            ExifInterface exif;
            byte[] thumbData = null;
            try {
                exif = new ExifInterface(path);
                thumbData = exif.getThumbnail();
            } catch (FileNotFoundException e) {
                Logger.w("failed to find file to read thumbnail");
            } catch (IOException e) {
                Logger.w("failed to get thumbnail");
            }
            if (thumbData != null) {
                Bitmap bitmap = BitmapUtils.decodeIfBigEnough(this, thumbData, options, targetSize);
                if (bitmap != null) return bitmap;
            }
        }
        return BitmapUtils.decodeThumbnail(this, path, options, targetSize, type);
    }
}
