package com.cncoderx.photopicker.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;

import com.cncoderx.photopicker.io.GalleryBitmapPool;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;

/**
 * Created by wujie on 2017/2/15.
 */
public class BitmapUtils {
    public static final int DEFAULT_JPEG_QUALITY = 90;

    public static Bitmap decodeThumbnail(AsyncTask task,
                                         String filePath,
                                         BitmapFactory.Options options,
                                         int targetSize,
                                         Thumbnail type) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(filePath);
            FileDescriptor fd = stream.getFD();
            return decodeThumbnail(task, fd, options, targetSize, type);
        } catch (Exception ex) {
            Logger.e(ex);
            return null;
        } finally {
            IOUtils.close(stream);
        }
    }

    public static Bitmap decodeThumbnail(AsyncTask task,
                                         FileDescriptor fd,
                                         BitmapFactory.Options options,
                                         int targetSize,
                                         Thumbnail type) {
        if (options == null) options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (task.isCancelled()) return null;

        int w = options.outWidth;
        int h = options.outHeight;

        if (type == Thumbnail.micro) {
            float scale = (float) targetSize / Math.min(w, h);
            options.inSampleSize = computeSampleSizeLarger(scale);

            final int MAX_PIXEL_COUNT = 640000;
            if ((w / options.inSampleSize) * (h / options.inSampleSize) > MAX_PIXEL_COUNT) {
                options.inSampleSize = computeSampleSize(
                        (float) Math.sqrt((float) MAX_PIXEL_COUNT / (w * h)));
            }
        } else {
            float scale = (float) targetSize / Math.max(w, h);
            options.inSampleSize = computeSampleSizeLarger(scale);
        }

        options.inJustDecodeBounds = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            options.inMutable = true;

        Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (result == null) return null;

        // We need to resize down if the decoder does not support inSampleSize
        // (For example, GIF images)
        float scale2 = (float) targetSize / (type == Thumbnail.micro
                ? Math.min(result.getWidth(), result.getHeight())
                : Math.max(result.getWidth(), result.getHeight()));
        if (scale2 <= 0.5) result = resizeBitmapByScale(result, scale2, true);

        return result;
    }


    private static int computeSampleSizeLarger(int w, int h, int minSideLength) {
        int initialSize = Math.max(w / minSideLength, h / minSideLength);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? MathUtils.prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    private static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) Math.floor(1f / scale);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? MathUtils.prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    private static int computeSampleSize(float scale) {
        if (scale <= 0) throw new IllegalArgumentException();
        int initialSize = Math.max(1, (int) Math.ceil(1 / scale));
        return initialSize <= 8
                ? MathUtils.nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    private static Bitmap resizeBitmapByScale(
            Bitmap bitmap, float scale, boolean recycle) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth()
                && height == bitmap.getHeight()) return bitmap;
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    public static Bitmap resizeDownBySideLength(Bitmap bitmap, int maxLength, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.min(
                (float) maxLength / srcWidth, (float) maxLength / srcHeight);
        if (scale >= 1.0f) return bitmap;
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size) return bitmap;

        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale = (float) size / Math.min(w,  h);

        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();

        return target;
    }

    public static byte[] compressToBytes(Bitmap bitmap) {
        return compressToBytes(bitmap, DEFAULT_JPEG_QUALITY);
    }

    public static byte[] compressToBytes(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    public static Bitmap decodeIfBigEnough(AsyncTask task, byte[] data, BitmapFactory.Options options, int targetSize) {
        if (options == null)
            options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (task.isCancelled()) return null;
        if (options.outWidth < targetSize || options.outHeight < targetSize) {
            return null;
        }
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(
                options.outWidth, options.outHeight, targetSize);
        options.inJustDecodeBounds = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            options.inMutable = true;

        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static Bitmap decodeUsingPool(AsyncTask task, byte[] data, int offset,
                                         int length, BitmapFactory.Options options) {
        if (options == null) options = new BitmapFactory.Options();
        if (options.inSampleSize < 1) options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inBitmap = (options.inSampleSize == 1)
                ? findCachedBitmap(data, offset, length, options) : null;
        try {
            Bitmap bitmap = decode(data, offset, length, options);
            if (options.inBitmap != null && options.inBitmap != bitmap) {
                GalleryBitmapPool.getInstance().put(options.inBitmap);
                options.inBitmap = null;
            }
            return bitmap;
        } catch (IllegalArgumentException e) {
            if (options.inBitmap == null) throw e;

            Logger.w("decode fail with a given bitmap, try decode to a new bitmap");
            GalleryBitmapPool.getInstance().put(options.inBitmap);
            options.inBitmap = null;
            return decode(data, offset, length, options);
        }
    }

    private static Bitmap findCachedBitmap(byte[] data,
                                           int offset, int length, BitmapFactory.Options options) {
        decodeBounds(data, offset, length, options);
        return GalleryBitmapPool.getInstance().get(options.outWidth, options.outHeight);
    }

    public static Bitmap decode(byte[] bytes, BitmapFactory.Options options) {
        return decode(bytes, 0, bytes.length, options);
    }

    public static Bitmap decode(byte[] bytes, int offset,
                                int length, BitmapFactory.Options options) {
        if (options == null) options = new BitmapFactory.Options();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            options.inMutable = true;
        return BitmapFactory.decodeByteArray(bytes, offset, length, options);
    }

    public static void decodeBounds(byte[] bytes, int offset,
                                    int length, BitmapFactory.Options options) {
        if (options == null)
            throw new IllegalArgumentException();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, offset, length, options);
        options.inJustDecodeBounds = false;
    }

    // Whether we should recycle the input (unless the output is the input).
    public static final boolean RECYCLE_INPUT = true;
    public static final boolean NO_RECYCLE_INPUT = false;

    public static Bitmap transform(Matrix scaler,
                                   Bitmap source,
                                   int targetWidth,
                                   int targetHeight,
                                   boolean scaleUp,
                                   boolean recycle) {
        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
            /*
             * In this case the bitmap is smaller, at least in one dimension,
             * than the target.  Transform it by placing as much of the image
             * as possible into the target and leaving the top/bottom or
             * left/right (or both) black.
             */
            Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b2);

            int deltaXHalf = Math.max(0, deltaX / 2);
            int deltaYHalf = Math.max(0, deltaY / 2);
            Rect src = new Rect(
                    deltaXHalf,
                    deltaYHalf,
                    deltaXHalf + Math.min(targetWidth, source.getWidth()),
                    deltaYHalf + Math.min(targetHeight, source.getHeight()));
            int dstX = (targetWidth  - src.width())  / 2;
            int dstY = (targetHeight - src.height()) / 2;
            Rect dst = new Rect(
                    dstX,
                    dstY,
                    targetWidth - dstX,
                    targetHeight - dstY);
            c.drawBitmap(source, src, dst, null);
            if (recycle) {
                source.recycle();
            }
            return b2;
        }
        float bitmapWidthF = source.getWidth();
        float bitmapHeightF = source.getHeight();

        float bitmapAspect = bitmapWidthF / bitmapHeightF;
        float viewAspect   = (float) targetWidth / targetHeight;

        if (bitmapAspect > viewAspect) {
            float scale = targetHeight / bitmapHeightF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        } else {
            float scale = targetWidth / bitmapWidthF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        }

        Bitmap b1;
        if (scaler != null) {
            // this is used for minithumb and crop, so we want to filter here.
            b1 = Bitmap.createBitmap(source, 0, 0,
                    source.getWidth(), source.getHeight(), scaler, true);
        } else {
            b1 = source;
        }

        if (recycle && b1 != source) {
            source.recycle();
        }

        int dx1 = Math.max(0, b1.getWidth() - targetWidth);
        int dy1 = Math.max(0, b1.getHeight() - targetHeight);

        Bitmap b2 = Bitmap.createBitmap(
                b1,
                dx1 / 2,
                dy1 / 2,
                targetWidth,
                targetHeight);

        if (b2 != b1) {
            if (recycle || b1 != source) {
                b1.recycle();
            }
        }

        return b2;
    }

}
