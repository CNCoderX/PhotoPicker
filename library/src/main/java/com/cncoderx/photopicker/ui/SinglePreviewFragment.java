package com.cncoderx.photopicker.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cncoderx.photopicker.R;
import com.cncoderx.photopicker.core.BitmapLoader;
import com.cncoderx.photopicker.core.IImage;
import com.cncoderx.photopicker.utils.Thumbnail;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by wujie on 2017/2/17.
 */
public class SinglePreviewFragment extends Fragment implements View.OnClickListener {
    GalleryActivity mActivity;
    PhotoView mPhotoView;
    TextView tvPreview;
    TextView tvCrop;
    TextView tvCommit;
    private IImage mImage;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (GalleryActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_photo_preview, container, false);
        mPhotoView = (PhotoView) view.findViewById(R.id.preview);
        tvPreview = (TextView) view.findViewById(R.id.tv_bottom_bar_pre);
        tvCrop = (TextView) view.findViewById(R.id.tv_bottom_bar_next);
        tvCommit = (TextView) view.findViewById(R.id.tv_bottom_bar_commit);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tvCrop.setText(R.string.crop);
        tvCommit.setText(R.string.commit);
        tvPreview.setVisibility(View.GONE);
        tvCrop.setOnClickListener(this);
        tvCommit.setOnClickListener(this);

        setImage(mImage);
    }

    public void setImage(IImage image) {
        mImage = image;
        if (mPhotoView != null) {
            if (image == null) {
                mPhotoView.setImageBitmap(null);
                return;
            }
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int targetSize = Math.min(metrics.heightPixels, metrics.widthPixels);
            new BitmapLoader(Thumbnail.normal, targetSize) {
                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    mPhotoView.setImageBitmap(bitmap);
                }
            }.execute(image);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_bottom_bar_next) {
            mActivity.cropPhoto(mImage.getPath());
        } else if (id == R.id.tv_bottom_bar_commit) {
            ArrayList<IImage> images = new ArrayList<>();
            images.add(mImage);
            mActivity.setResultAndFinish(images);
        }
    }
}
