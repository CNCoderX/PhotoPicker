package com.cncoderx.photopicker.ui;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.cncoderx.photopicker.Configuration;
import com.cncoderx.photopicker.PhotoPicker;
import com.cncoderx.photopicker.R;
import com.cncoderx.photopicker.adapter.AlbumListAdapter;
import com.cncoderx.photopicker.anim.Animation;
import com.cncoderx.photopicker.anim.AnimationListener;
import com.cncoderx.photopicker.anim.SlideInUnderneathAnimation;
import com.cncoderx.photopicker.anim.SlideOutUnderneathAnimation;
import com.cncoderx.photopicker.bean.Album;
import com.cncoderx.photopicker.core.IImage;
import com.cncoderx.photopicker.io.BytesBufferPool;
import com.cncoderx.photopicker.io.GalleryBitmapPool;
import com.cncoderx.photopicker.utils.BucketHelper;
import com.cncoderx.photopicker.utils.Logger;
import com.cncoderx.photopicker.widget.ToolbarActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by wujie on 2017/2/15.
 */
public class GalleryActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener, MediaScannerConnection.MediaScannerConnectionClient {
    Toolbar mToolbar;
    MenuItem mPositiveItem;
    ToolbarActionButton mPositiveButton;
    ListView lvAlbum;
    View mAlbumOverview;

    private AlbumListAdapter mAlbumAdapter;
    private Configuration configuration;
    private int curAlbumIndex;
    private File mStorageDir;
    private File mStorageFile;
    MediaScannerConnection mMediaScanner;

    private Fragment mActivedFragment;
    private PhotoGridFragment mPhotoGridFragment;
    private SinglePreviewFragment mSinglePreviewFragment;
    private MultiPreviewFragment mMultiPreviewFragment;

    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_CROP_PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhotoPicker.initialize(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gallery);

        Bundle bundle = getIntent().getExtras();
        configuration = bundle.getParcelable(PhotoPicker.EXTRA_CONFIGURATION);

        mMediaScanner = new MediaScannerConnection(getApplicationContext(), this);
        mStorageDir = new File(Environment.getExternalStorageDirectory(), "/DCIM/PhotoPicker/");
        if (!mStorageDir.exists()) mStorageDir.mkdirs();

        lvAlbum = (ListView) findViewById(R.id.lv_album);
        mAlbumOverview = findViewById(R.id.fl_album_overview);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        initAlbumList();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        showPhotoGridFragment("");
    }

    private void initAlbumList() {
        mAlbumAdapter = new AlbumListAdapter(this);
        lvAlbum.setAdapter(mAlbumAdapter);
        lvAlbum.setOnItemClickListener(this);
        lvAlbum.post(new Runnable() {
            @Override
            public void run() {
                mAlbumOverview.setVisibility(View.INVISIBLE);
                new SlideOutUnderneathAnimation(lvAlbum)
                        .setDirection(Animation.DIRECTION_UP)
                        .setListener(new AnimationListener() {
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mAlbumOverview.setVisibility(View.GONE);
                            }
                        })
                        .animate();
            }
        });
        List<Album> albumList = BucketHelper.getAlbumList(getApplicationContext());
        mAlbumAdapter.addAll(albumList);
        mAlbumAdapter.notifyDataSetChanged();
    }

    public void showPhotoGridFragment(String albumId) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mPhotoGridFragment == null) {
            Bundle bundle = new Bundle();
            bundle.putInt("maxCount", configuration.getMaxCount());
            bundle.putBoolean("showCamera", !configuration.isHideCamera());
            mPhotoGridFragment = new PhotoGridFragment();
            mPhotoGridFragment.setArguments(bundle);
            ft.add(R.id.container, mPhotoGridFragment);
        }
        if (mActivedFragment != null)
            ft.hide(mActivedFragment);
        ft.show(mPhotoGridFragment);
        ft.commit();

        mActivedFragment = mPhotoGridFragment;
        mPhotoGridFragment.showPhotos(albumId);
    }

    public void showSinglePreviewFragment(IImage image) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mSinglePreviewFragment == null) {
            mSinglePreviewFragment = new SinglePreviewFragment();
            ft.add(R.id.container, mSinglePreviewFragment);
        }
        if (mActivedFragment != null)
            ft.hide(mActivedFragment);
        ft.show(mSinglePreviewFragment);
        ft.commit();

        mActivedFragment = mSinglePreviewFragment;
        mSinglePreviewFragment.setImage(image);
    }

    public void showMultiPreviewFragment(IImage... images) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mMultiPreviewFragment == null) {
            mMultiPreviewFragment = new MultiPreviewFragment();
            ft.add(R.id.container, mMultiPreviewFragment);
        }
        if (mActivedFragment != null)
            ft.hide(mActivedFragment);
        ft.show(mMultiPreviewFragment);
        ft.commit();

        mActivedFragment = mMultiPreviewFragment;
        mMultiPreviewFragment.setImages(images);
    }

    public void slideInAlbumList() {
        if (!albumListIsShowing()) {
            lvAlbum.setEnabled(false);
            mPositiveButton.setEnabled(false);
            mAlbumOverview.setVisibility(View.VISIBLE);
            new SlideInUnderneathAnimation(lvAlbum)
                    .setDirection(Animation.DIRECTION_UP)
                    .setDuration(Animation.DURATION_DEFAULT)
                    .setListener(new AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            lvAlbum.setEnabled(true);
                            mPositiveButton.setEnabled(true);
                        }
                    })
                    .animate();
        }
    }

    public void slideOutAlbumList() {
        if (albumListIsShowing()) {
            lvAlbum.setEnabled(false);
            mPositiveButton.setEnabled(false);
            new SlideOutUnderneathAnimation(lvAlbum)
                    .setDirection(Animation.DIRECTION_UP)
                    .setDuration(Animation.DURATION_DEFAULT)
                    .setListener(new AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            lvAlbum.setEnabled(true);
                            mPositiveButton.setEnabled(true);
                            mAlbumOverview.setVisibility(View.GONE);
                        }
                    })
                    .animate();
        }
    }

    public boolean albumListIsShowing() {
        return mAlbumOverview.getVisibility() == View.VISIBLE;
    }

    public void takePhoto() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            String dateToken = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
            String filename = "IMG_" + dateToken + ".jpg";
            mStorageFile = new File(mStorageDir, filename);
            Uri uri;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                uri = Uri.fromFile(mStorageFile);
            } else {
                uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", mStorageFile);
            }
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(captureIntent, REQUEST_TAKE_PHOTO);
        } else {
            Toast.makeText(getApplicationContext(), R.string.open_camera_failure, Toast.LENGTH_SHORT).show();
        }
    }

    public void cropPhoto(String path) {
        Intent intent = new Intent(this, CropPhotoActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("aspectX", configuration.getAspectX());
        intent.putExtra("aspectY", configuration.getAspectY());
        intent.putExtra("circleCrop", configuration.isCircleCrop());
        startActivityForResult(intent, REQUEST_CROP_PHOTO);
    }

    public void setResultAndFinish(ArrayList<IImage> images) {
        Intent data = new Intent();
        data.putParcelableArrayListExtra("data", images);
        setResult(RESULT_OK, data);
        finish();
    }

    public void previewPhotos(IImage... images) {
        if (images.length == 1) {
            showSinglePreviewFragment(images[0]);
        } else if (images.length > 1) {
            showMultiPreviewFragment(images);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mPositiveItem = menu.findItem(R.id.action_positive);
        mPositiveButton = (ToolbarActionButton) MenuItemCompat.getActionProvider(mPositiveItem);
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (albumListIsShowing()) {
                    slideOutAlbumList();
                } else {
                    slideInAlbumList();
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mActivedFragment == mSinglePreviewFragment ||
                    mActivedFragment == mMultiPreviewFragment) {
                showPhotoGridFragment(mAlbumAdapter.get(curAlbumIndex).getId());
                return true;
            }
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        slideOutAlbumList();
        if (curAlbumIndex != position) {
            Album album = mAlbumAdapter.get(position);
            mPositiveButton.setText(album.getName());
            showPhotoGridFragment(album.getId());
        }
        curAlbumIndex = position;
    }

    @Override
    public void onBackPressed() {
        if (mActivedFragment == mSinglePreviewFragment ||
                mActivedFragment == mMultiPreviewFragment) {
            showPhotoGridFragment(mAlbumAdapter.get(curAlbumIndex).getId());
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GalleryBitmapPool.getInstance().clear();
        BytesBufferPool.getInstance().clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaScanner.isConnected())
            mMediaScanner.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                Logger.i(String.format("take photo success, saved as:%s", mStorageFile.getAbsolutePath()));

                //刷新相册数据库
                mMediaScanner.connect();
            } else if (requestCode == REQUEST_CROP_PHOTO) {
                setResult(resultCode, data);
                finish();
            }
        }
    }

    @Override
    public void onMediaScannerConnected() {
        mMediaScanner.scanFile(mStorageFile.getAbsolutePath(), "image/jpeg");
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mMediaScanner.disconnect();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showPhotoGridFragment(mAlbumAdapter.get(curAlbumIndex).getId());
            }
        });
    }
}
