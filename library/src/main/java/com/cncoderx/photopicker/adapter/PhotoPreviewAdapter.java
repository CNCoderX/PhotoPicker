package com.cncoderx.photopicker.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cncoderx.photopicker.R;
import com.cncoderx.photopicker.core.BitmapLoader;
import com.cncoderx.photopicker.core.IImage;
import com.cncoderx.photopicker.utils.Thumbnail;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by admin on 2017/2/17.
 */
public class PhotoPreviewAdapter extends PagerAdapter {
    private LayoutInflater mInflater;
    private List<IImage> mData = new ArrayList<>();
    private int targetSize;

    public PhotoPreviewAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        targetSize = Math.min(metrics.widthPixels, metrics.heightPixels);
    }

    public int getSize() {
        return mData.size();
    }

    public IImage get(int index) {
        return mData.get(index);
    }

    public void add(IImage image) {
        mData.add(image);
    }

    public void addAll(Collection<IImage> images) {
        mData.addAll(images);
    }

    public void remove(int index) {
        mData.remove(index);
    }

    public void clear() {
        mData.clear();
    }

    @Override
    public int getCount() {
        return getSize();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mInflater.inflate(R.layout.item_photo_preview, container, false);
        PhotoView photoView = (PhotoView) view.findViewById(R.id.preview);
        ThumbLoader thumbLoader = new ThumbLoader(photoView, targetSize);
        thumbLoader.execute(get(position));
        container.addView(view);
        return view;
    }

    static class ThumbLoader extends BitmapLoader {
        WeakReference<PhotoView> mParent;

        public ThumbLoader(PhotoView photoView, int size) {
            super(Thumbnail.normal, size);
            mParent = new WeakReference<>(photoView);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            PhotoView photoView = mParent.get();
            if (photoView != null) {
                photoView.setImageBitmap(bitmap);
            }
        }
    }
}
