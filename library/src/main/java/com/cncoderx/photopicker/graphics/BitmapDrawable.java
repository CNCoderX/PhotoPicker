package com.cncoderx.photopicker.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.cncoderx.photopicker.core.BitmapLoader;
import com.cncoderx.photopicker.core.IImage;
import com.cncoderx.photopicker.io.GalleryBitmapPool;
import com.cncoderx.photopicker.utils.Thumbnail;

import java.lang.ref.WeakReference;

public class BitmapDrawable extends Drawable implements Runnable {
    private ThumbnailLoader mLoader;
    private IImage mImage;
    private Bitmap mBitmap;
    private Paint mPaint = new Paint();
    private Matrix mDrawMatrix = new Matrix();
    private int mMaxWidth = -1;
    private int mMaxHeight = -1;

    public void setImage(IImage image, int maxWidth, int maxHeight) {
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;

        if (mImage == image)
            return;

        if (mLoader != null) {
            mLoader.cancel(true);
        }
        mImage = image;
        if (mBitmap != null) {
            GalleryBitmapPool.getInstance().put(mBitmap);
            mBitmap = null;
        }
        if (mImage != null) {
            mLoader = new ThumbnailLoader(this, Math.max(mMaxWidth, mMaxHeight));
            mLoader.execute(mImage);
        }
        invalidateSelf();
    }

    @Override
    public void run() {
        Bitmap bitmap = mLoader.bitmap;
        if (bitmap != null) {
            mBitmap = bitmap;
            mLoader.bitmap = null;
            updateDrawMatrix();
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updateDrawMatrix();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (mBitmap != null) {
            canvas.save();
            canvas.clipRect(bounds);
            canvas.concat(mDrawMatrix);
            canvas.rotate(mImage.getRotation(), bounds.centerX(), bounds.centerY());
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            canvas.restore();
        } else {
            mPaint.setColor(0xFFCCCCCC);
            canvas.drawRect(bounds, mPaint);
        }
    }

    private void updateDrawMatrix() {
        Rect bounds = getBounds();
        if (mBitmap == null || bounds.isEmpty()) {
            mDrawMatrix.reset();
            return;
        }

        float scale;
        float dx = 0, dy = 0;

        int dwidth = mBitmap.getWidth();
        int dheight = mBitmap.getHeight();
        int vwidth = bounds.width();
        int vheight = bounds.height();

        if (dwidth * vheight > vwidth * dheight) {
            scale = (float) vheight / (float) dheight;
            dx = (vwidth - dwidth * scale) * 0.5f;
        } else {
            scale = (float) vwidth / (float) dwidth;
            dy = (vheight - dheight * scale) * 0.5f;
        }

        mDrawMatrix.setScale(scale, scale);
        mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        invalidateSelf();
    }

    @Override
    public int getIntrinsicWidth() {
        return mMaxWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mMaxHeight;
    }

    @Override
    public int getOpacity() {
        Bitmap bm = mBitmap;
        return (bm == null || bm.hasAlpha() || mPaint.getAlpha() < 255) ?
                PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {
        int oldAlpha = mPaint.getAlpha();
        if (alpha != oldAlpha) {
            mPaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    static class ThumbnailLoader extends BitmapLoader {
        WeakReference<BitmapDrawable> mParent;
        Bitmap bitmap;

        public ThumbnailLoader(BitmapDrawable drawable, int size) {
            super(Thumbnail.micro, size);
            mParent = new WeakReference<>(drawable);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            this.bitmap = bitmap;
            BitmapDrawable drawable = mParent.get();
            if (drawable != null) {
                drawable.scheduleSelf(drawable, 0);
            }
        }
    }
}
