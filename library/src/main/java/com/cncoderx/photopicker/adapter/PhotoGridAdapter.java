package com.cncoderx.photopicker.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.cncoderx.photopicker.R;
import com.cncoderx.photopicker.core.IImage;
import com.cncoderx.photopicker.graphics.BitmapDrawable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wujie on 2017/2/15.
 */
public class PhotoGridAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mLayoutInflater;
    private List<IImage> mData = new ArrayList<>();
    private Set<Integer> mSelectedIdSet = new HashSet<>();
    private OnSelectedChangeListener onSelectedChangeListener;
    private boolean isSelectable = true;
    private boolean showCamera = true;
    private int maxCount = 1;

    public static final int ITEM_TYPE_CAMERA = 0;
    public static final int ITEM_TYPE_PHOTO = 1;

    public PhotoGridAdapter(Context context) {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setOnSelectedChangeListener(OnSelectedChangeListener listener) {
        this.onSelectedChangeListener = listener;
    }

    void notifySelectedChangeListener(int index, boolean isSelected) {
        if (onSelectedChangeListener != null) {
            if (isSelected) {
                onSelectedChangeListener.select(index);
            } else {
                onSelectedChangeListener.unselect(index);
            }
        }
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
        return showCamera ? getSize() + 1 : getSize();
    }

    @Override
    public IImage getItem(int position) {
        return get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public void setSelectable(boolean selectable) {
        isSelectable = selectable;
    }

    public int getSelectedCount() {
        return mSelectedIdSet.size();
    }

    public List<IImage> getSelectedData() {
        List<IImage> result = new ArrayList<>();
        for(int index : mSelectedIdSet) {
            IImage image = get(index);
            result.add(image);
        }
        return result;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (getItemViewType(position) == ITEM_TYPE_CAMERA) {
            if (convertView == null) {
                v = mLayoutInflater.inflate(R.layout.item_camera_grid, parent, false);
            } else {
                v = convertView;
            }
        } else {
            ViewHolder vh;
            if (convertView == null) {
                v = mLayoutInflater.inflate(R.layout.item_photo_grid, parent, false);
                v.setTag(vh = new ViewHolder(v));
            } else {
                v = convertView;
                vh = (ViewHolder) v.getTag();
            }
            if (showCamera) {
                vh.onBind(get(position - 1), position - 1);
            } else {
                vh.onBind(get(position), position);
            }
        }
        return v;
    }

    @Override
    public int getViewTypeCount() {
        return showCamera ? 2 : 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (showCamera && position == 0) ? ITEM_TYPE_CAMERA : ITEM_TYPE_PHOTO;
    }

    class ViewHolder implements View.OnClickListener {
        int position;
        View itemView;
        ImageView ivPhoto;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
            ivPhoto = (ImageView) itemView.findViewById(R.id.photo);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            checkBox.setOnClickListener(this);
        }

        public void onBind(IImage image, int position) {
            this.position = position;
            Drawable recycle = ivPhoto.getDrawable();
            Drawable drawable = drawableForItem(image, recycle);
            if (recycle != drawable) {
                ivPhoto.setImageDrawable(drawable);
            }
            if (maxCount > 1) {
                checkBox.setVisibility(View.VISIBLE);
                if (mSelectedIdSet.contains(position)) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
            } else {
                checkBox.setVisibility(View.GONE);
            }
        }

        private Drawable drawableForItem(IImage image, Drawable recycle) {
            final BitmapDrawable drawable;
            if (recycle == null || !(recycle instanceof BitmapDrawable)) {
                drawable = new BitmapDrawable();
            } else {
                drawable = (BitmapDrawable) recycle;
            }
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int size = metrics.widthPixels / 3;
            drawable.setImage(image, size, size);
            return drawable;
        }

        @Override
        public void onClick(View v) {
            if (maxCount <= 1)
                return;

            if (!isSelectable) {
                checkBox.toggle();
                return;
            }
            boolean isChecked = checkBox.isChecked();
            if (isChecked) {
                if (getSelectedCount() >= maxCount) {
                    checkBox.toggle();
                    Toast.makeText(context.getApplicationContext(),
                            context.getString(R.string.image_max_count_tip, maxCount),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mSelectedIdSet.add(position);
            } else {
                mSelectedIdSet.remove(position);
            }
            notifySelectedChangeListener(position, isChecked);
        }
    }

    public static interface OnSelectedChangeListener {
        void select(int index);
        void unselect(int index);
    }
}
