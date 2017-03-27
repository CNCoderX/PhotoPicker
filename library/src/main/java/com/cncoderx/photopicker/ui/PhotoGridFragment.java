package com.cncoderx.photopicker.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.cncoderx.photopicker.R;
import com.cncoderx.photopicker.adapter.PhotoGridAdapter;
import com.cncoderx.photopicker.anim.Animation;
import com.cncoderx.photopicker.anim.AnimationListener;
import com.cncoderx.photopicker.anim.SlideInUnderneathAnimation;
import com.cncoderx.photopicker.anim.SlideOutUnderneathAnimation;
import com.cncoderx.photopicker.bean.Photo;
import com.cncoderx.photopicker.core.IImage;
import com.cncoderx.photopicker.utils.BucketHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wujie on 2017/2/15.
 */
public class PhotoGridFragment extends Fragment implements
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        PhotoGridAdapter.OnSelectedChangeListener {
    GridView mGridView;
    GalleryActivity mActivity;
    View mBottomBar;
    View mBottomBarOverview;
    TextView tvPreview;
    TextView tvCrop;
    TextView tvCommit;

    private String albumId;
    private boolean showCamera;
    private int maxCount;
    private PhotoGridAdapter mPhotoGridAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (GalleryActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_grid, container, false);
        mBottomBar = view.findViewById(R.id.rl_bottom_bar);
        mBottomBarOverview = view.findViewById(R.id.fl_bottom_bar_overview);
        tvPreview = (TextView) view.findViewById(R.id.tv_bottom_bar_pre);
        tvCrop = (TextView) view.findViewById(R.id.tv_bottom_bar_next);
        tvCommit = (TextView) view.findViewById(R.id.tv_bottom_bar_commit);
        mGridView = (GridView) view.findViewById(R.id.grid);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mActivity.mToolbar != null) {
            mActivity.mToolbar.setTitle(R.string.image);
        }
        tvPreview.setText(R.string.preview);
        tvCrop.setText(R.string.crop);
        tvCommit.setText(R.string.commit);
        tvPreview.setOnClickListener(this);
        tvCrop.setOnClickListener(this);
        tvCommit.setOnClickListener(this);

        Bundle bundle = getArguments();
        showCamera = bundle.getBoolean("showCamera", true);
        maxCount = bundle.getInt("maxCount", 1);

        mPhotoGridAdapter = new PhotoGridAdapter(getContext());
        mPhotoGridAdapter.setMaxCount(maxCount);
        mPhotoGridAdapter.setShowCamera(showCamera);
        mPhotoGridAdapter.setOnSelectedChangeListener(this);
        mGridView.setOnItemClickListener(this);
        mGridView.setAdapter(mPhotoGridAdapter);

        tvCommit.setText(getString(R.string.commit_count, 0, maxCount));
        mBottomBarOverview.setVisibility(View.INVISIBLE);
        new SlideOutUnderneathAnimation(mBottomBar)
                .setDirection(Animation.DIRECTION_DOWN)
                .setListener(new AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mBottomBarOverview.setVisibility(View.GONE);
                    }
                })
                .animate();

        showPhotos(albumId);
    }

    public void showPhotos(String albumId) {
        this.albumId = albumId;

        if (mPhotoGridAdapter != null) {
            List<Photo> photos;
            if (TextUtils.isEmpty(albumId)) {
                photos = BucketHelper.getPhotoList(mActivity.getApplicationContext());
            } else {
                photos = BucketHelper.getPhotoListByBucketId(mActivity.getApplicationContext(), albumId);
            }

            mPhotoGridAdapter.clear();
            mPhotoGridAdapter.addAll(new ArrayList<IImage>(photos));
            mPhotoGridAdapter.notifyDataSetChanged();
        }
    }

    public void slideInBottomBar() {
        if (!bottomBarIsShowing()) {
            mPhotoGridAdapter.setSelectable(false);
            mBottomBarOverview.setVisibility(View.VISIBLE);
            new SlideInUnderneathAnimation(mBottomBar)
                    .setDirection(Animation.DIRECTION_DOWN)
                    .setDuration(Animation.DURATION_DEFAULT)
                    .setListener(new AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mPhotoGridAdapter.setSelectable(true);
                        }
                    })
                    .animate();
        }
    }

    public void slideOutBottomBar() {
        if (bottomBarIsShowing()) {
            mPhotoGridAdapter.setSelectable(false);
            new SlideOutUnderneathAnimation(mBottomBar)
                    .setDirection(Animation.DIRECTION_DOWN)
                    .setDuration(Animation.DURATION_DEFAULT)
                    .setListener(new AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mPhotoGridAdapter.setSelectable(true);
                            mBottomBarOverview.setVisibility(View.GONE);
                        }
                    })
                    .animate();
        }
    }

    public boolean bottomBarIsShowing() {
        return mBottomBarOverview.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (showCamera) {
            if (position == 0) {
                mActivity.takePhoto();
            } else {
                IImage image = mPhotoGridAdapter.get(position - 1);
                mActivity.previewPhotos(image);
            }
        } else {
            IImage image = mPhotoGridAdapter.get(position);
            mActivity.previewPhotos(image);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void select(int index) {
        int count = mPhotoGridAdapter.getSelectedCount();
        if (1 == count) {
            slideInBottomBar();
            tvCrop.setVisibility(View.VISIBLE);
        } else {
            tvCrop.setVisibility(View.GONE);
        }
        tvCommit.setText(getString(R.string.commit_count, count, maxCount));
    }

    @Override
    public void unselect(int index) {
        int count = mPhotoGridAdapter.getSelectedCount();
        if (0 == count) {
            slideOutBottomBar();
            tvCrop.setVisibility(View.GONE);
        } else if (1 == count) {
            tvCrop.setVisibility(View.VISIBLE);
        } else {
            tvCrop.setVisibility(View.GONE);
        }
        tvCommit.setText(getString(R.string.commit_count, count, maxCount));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_bottom_bar_pre) {
            List<IImage> images = mPhotoGridAdapter.getSelectedData();
            mActivity.previewPhotos(images.toArray(new IImage[images.size()]));
        } else if (id == R.id.tv_bottom_bar_next) {
            List<IImage> images = mPhotoGridAdapter.getSelectedData();
            mActivity.cropPhoto(images.get(0).getPath());
        } else if (id == R.id.tv_bottom_bar_commit) {
            mActivity.setResultAndFinish(new ArrayList<>(mPhotoGridAdapter.getSelectedData()));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (mActivity.mPositiveItem != null)
            mActivity.mPositiveItem.setVisible(!hidden);
    }
}
