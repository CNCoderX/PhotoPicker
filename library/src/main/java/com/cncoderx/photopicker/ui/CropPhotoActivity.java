package com.cncoderx.photopicker.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cncoderx.photopicker.R;
import com.cncoderx.photopicker.bean.Photo;
import com.cncoderx.photopicker.core.BitmapLoader;
import com.cncoderx.photopicker.core.IImage;
import com.cncoderx.photopicker.utils.BitmapUtils;
import com.cncoderx.photopicker.utils.IOUtils;
import com.cncoderx.photopicker.utils.Logger;
import com.cncoderx.photopicker.utils.Thumbnail;
import com.cncoderx.photopicker.widget.CropImageView;
import com.cncoderx.photopicker.widget.HighlightView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by wujie on 2017/2/23.
 */
public class CropPhotoActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar mToolbar;
    CropImageView mImageView;
    HighlightView mCropView;
    TextView tvZoomIn;
    TextView tvZoomOut;
    TextView tvCommit;

    private Bitmap mBitmap;
    private boolean mSaving;

    private String filePath;
    private int aspectX = 1;
    private int aspectY = 1;
    private int outputX;
    private int outputY;
    private boolean scale = true;
    private boolean scaleUp = true;
    private boolean circleCrop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_photo);
        setupView();
        loadExtras();
        if (filePath == null) {
            setResult(RESULT_CANCELED);
        } else {
            loadBitmap();
        }
    }

    private void setupView() {
        mImageView = (CropImageView) findViewById(R.id.crop_image);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        tvZoomIn = (TextView) findViewById(R.id.tv_bottom_bar_pre);
        tvZoomOut = (TextView) findViewById(R.id.tv_bottom_bar_next);
        tvCommit = (TextView) findViewById(R.id.tv_bottom_bar_commit);
        tvZoomIn.setText(R.string.zoom_in);
        tvZoomOut.setText(R.string.zoom_out);
        tvCommit.setText(R.string.ok);
        tvZoomIn.setOnClickListener(this);
        tvZoomOut.setOnClickListener(this);
        tvCommit.setOnClickListener(this);
        setSupportActionBar(mToolbar);
    }

    private void loadExtras() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            filePath = extras.getString("path");
            aspectX = extras.getInt("aspectX", 1);
            aspectY = extras.getInt("aspectY", 1);
            outputX = extras.getInt("outputX");
            outputY = extras.getInt("outputY");
            scale = extras.getBoolean("scale", true);
            scaleUp = extras.getBoolean("scaleUp", true);
            circleCrop = extras.getBoolean("circleCrop", true);
        }
    }

    private void loadBitmap() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int targetSize = Math.min(metrics.heightPixels, metrics.widthPixels);
        new BitmapLoader(Thumbnail.normal, targetSize) {
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                mBitmap = bitmap;
                if (!isFinishing()) {
                    mImageView.setImageBitmapResetBase(mBitmap, true);
                    makeHighlightView();
                }
            }
        }.execute(new IImage() {

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }

            @Override
            public String getPath() {
                return filePath;
            }

            @Override
            public int getWidth() {
                return 0;
            }

            @Override
            public int getHeight() {
                return 0;
            }

            @Override
            public int getRotation() {
                return 0;
            }

            @Override
            public long getDateToken() {
                return 0;
            }
        });
    }

    private void makeHighlightView() {
        HighlightView hv = new HighlightView(mImageView);

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();

        Rect imageRect = new Rect(0, 0, width, height);

        // make the default size about 4/5 of the width or height
        int cropWidth = Math.min(width, height) * 4 / 5;
        int cropHeight = cropWidth;

        if (aspectX != 0 && aspectY != 0) {
            if (aspectX > aspectY) {
                cropHeight = cropWidth * aspectY / aspectX;
            } else {
                cropWidth = cropHeight * aspectX / aspectY;
            }
        }

        int x = (width - cropWidth) / 2;
        int y = (height - cropHeight) / 2;

        RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
        hv.setup(mImageView.getImageMatrix(), imageRect, cropRect, circleCrop,
                aspectX != 0 && aspectY != 0);
        mImageView.add(hv);
        mImageView.invalidate();
        if(mImageView.getHighlightViews().size() == 1) {
            mCropView = mImageView.getHighlightViews().get(0);
            mCropView.setFocus(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
        return true;
    }

    public boolean isSaved() {
        return mSaving;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_bottom_bar_pre) {
            mImageView.zoomIn();
        } else if (id == R.id.tv_bottom_bar_next) {
            mImageView.zoomOut();
        } else if (id == R.id.tv_bottom_bar_commit) {
            onSaveClicked();
        }
    }

    private void onSaveClicked() {
        // TODO this code needs to change to use the decode/crop/encode single
        // step api so that we don't require that the whole (possibly large)
        // bitmap doesn't have to be read into memory
        if (mCropView == null) {
            return;
        }

        if (mSaving) return;
        mSaving = true;

        Bitmap croppedImage;

        // If the output is required to a specific size, create an new image
        // with the cropped image in the center and the extra space filled.
        if (outputX != 0 && outputY != 0 && !scale) {
            // Don't scale the image but instead fill it so it's the
            // required dimension
            croppedImage = Bitmap.createBitmap(outputX, outputY, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(croppedImage);

            Rect srcRect = mCropView.getCropRect();
            Rect dstRect = new Rect(0, 0, outputX, outputY);

            int dx = (srcRect.width() - dstRect.width()) / 2;
            int dy = (srcRect.height() - dstRect.height()) / 2;

            // If the srcRect is too big, use the center part of it.
            srcRect.inset(Math.max(0, dx), Math.max(0, dy));

            // If the dstRect is too big, use the center part of it.
            dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

            // Draw the cropped bitmap in the center
            canvas.drawBitmap(mBitmap, srcRect, dstRect, null);

            // Release bitmap memory as soon as possible
            mImageView.clear();
            mBitmap.recycle();
        } else {
            Rect r = mCropView.getCropRect();

            int width = r.width();
            int height = r.height();

            // If we are circle cropping, we want alpha channel, which is the
            // third param here.
            croppedImage = Bitmap.createBitmap(width, height,
                    circleCrop ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);

            Canvas canvas = new Canvas(croppedImage);

            if (circleCrop) {
                final int color = 0xffff0000;
                final Paint paint = new Paint();
                final Rect rect = new Rect(0, 0, croppedImage.getWidth(), croppedImage.getHeight());
                final RectF rectF = new RectF(rect);

                paint.setAntiAlias(true);
                paint.setDither(true);
                paint.setFilterBitmap(true);
                canvas.drawARGB(0, 0, 0, 0);
                paint.setColor(color);
                canvas.drawOval(rectF, paint);

                paint.setColor(Color.BLUE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth((float) 4);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(mBitmap, r, rect, paint);
            }
            else {
                Rect dstRect = new Rect(0, 0, width, height);
                canvas.drawBitmap(mBitmap, r, dstRect, null);
            }

            // Release bitmap memory as soon as possible
            mImageView.clear();
            mBitmap.recycle();

            // If the required dimension is specified, scale the image.
            if (outputX != 0 && outputY != 0 && scale) {
                croppedImage = BitmapUtils.transform(new Matrix(), croppedImage,
                        outputX, outputY, scaleUp, BitmapUtils.RECYCLE_INPUT);
            }
        }

        mImageView.setImageBitmapResetBase(croppedImage, true);
        mImageView.center(true, true);
        mImageView.getHighlightViews().clear();

        // save it to the specified URI.
        final Bitmap b = croppedImage;
        new AsyncTask<Void, Integer, IImage>() {
            ProgressDialog pd;

            @Override
            protected void onPreExecute() {
                pd = ProgressDialog.show(CropPhotoActivity.this, null,
                        getResources().getString(R.string.saving_image));
            }

            @Override
            protected IImage doInBackground(Void[] params) {
                return saveOutput(b);
            }

            @Override
            protected void onPostExecute(IImage image) {
                pd.dismiss();
                mImageView.clear();
                b.recycle();

                if (image != null) {
                    ArrayList<IImage> images = new ArrayList<>();
                    images.add(image);

                    Intent data = new Intent();
                    data.putParcelableArrayListExtra("data", images);
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }
            }
        }.execute();
    }

    private IImage saveOutput(Bitmap croppedImage) {
        File oldPath = new File(filePath);
        String fileName = oldPath.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf("."));

        long dataToken = System.currentTimeMillis() / 1000L;
        String suffix = circleCrop ? ".png" : ".jpg";
        File directory = getExternalFilesDir("crop");
        File file = new File(directory.getAbsolutePath(), fileName + suffix);
        int index = 0;
        while (file.exists()) {
            file = new File(directory.getAbsolutePath(), fileName + "_" + ++index + suffix);
        }

        // We should store image data earlier than insert it to ContentProvider, otherwise
        // we may not be able to generate thumbnail in time.
        OutputStream outputStream = null;
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) dir.mkdirs();
            outputStream = new FileOutputStream(file);
            if (croppedImage != null) {
                if (circleCrop) {
                    croppedImage.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
                } else {
                    croppedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                }
            }
        } catch (Exception ex) {
            Logger.e(ex);
        } finally {
            IOUtils.close(outputStream);
        }

        Photo photo = new Photo();
        photo.setMimeType(circleCrop ? "image/png" : "image/jpeg");
        photo.setFileSize(file.length());
        photo.setDateAddedInSec(dataToken);
        photo.setDateModifiedInSec(dataToken);
        photo.setFilePath(file.getAbsolutePath());
        if (croppedImage != null) {
            photo.setWidth(croppedImage.getWidth());
            photo.setHeight(croppedImage.getHeight());
        }
        return photo;
    }
}
