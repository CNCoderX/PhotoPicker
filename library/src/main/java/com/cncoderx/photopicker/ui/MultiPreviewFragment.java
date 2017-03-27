package com.cncoderx.photopicker.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cncoderx.photopicker.R;
import com.cncoderx.photopicker.adapter.PhotoPreviewAdapter;
import com.cncoderx.photopicker.core.IImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by wujie on 2017/2/17.
 */
public class MultiPreviewFragment extends Fragment implements View.OnClickListener, ViewPager.OnPageChangeListener {
    GalleryActivity mActivity;
    ViewPager mViewPager;
    TextView tvPreview;
    TextView tvCrop;
    TextView tvCommit;

    private List<IImage> mImages;
    private PhotoPreviewAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (GalleryActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_photo_preview, container, false);
        tvPreview = (TextView) view.findViewById(R.id.tv_bottom_bar_pre);
        tvCrop = (TextView) view.findViewById(R.id.tv_bottom_bar_next);
        tvCommit = (TextView) view.findViewById(R.id.tv_bottom_bar_commit);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCommit.setText(R.string.commit);
        tvPreview.setVisibility(View.GONE);
        tvCrop.setVisibility(View.GONE);
        tvCommit.setOnClickListener(this);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.addOnPageChangeListener(this);
        mAdapter = new PhotoPreviewAdapter(getContext());
        if (mImages != null) {
            mAdapter.addAll(mImages);
        }
        mViewPager.setAdapter(mAdapter);
    }

    public void setImages(IImage... images) {
        mImages = new ArrayList<>();
        if (images != null) {
            for (int i = 0; i < images.length; i++) {
                mImages.add(images[i]);
            }
        }
        if (mAdapter != null) {
            mAdapter.clear();
            mAdapter.addAll(mImages);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewPager.clearOnPageChangeListeners();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        int count = mAdapter.getSize();
        if (mActivity.mToolbar != null) {
            mActivity.mToolbar.setTitle(String.format(Locale.US, "%d/%d", position+1, count));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_bottom_bar_commit) {
            mActivity.setResultAndFinish(new ArrayList<>(mImages));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            if (mActivity.mToolbar != null) {
                mActivity.mToolbar.setTitle(R.string.image);
            }
        } else {
            int count = mAdapter.getSize();
            int index = mViewPager.getCurrentItem();
            if (mActivity.mToolbar != null) {
                mActivity.mToolbar.setTitle(String.format(Locale.US, "%d/%d", index+1, count));
            }
        }
    }
}
