package com.cncoderx.test.photopicker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.cncoderx.photopicker.PhotoPicker;
import com.cncoderx.photopicker.core.IImage;
import com.cncoderx.photopicker.widget.SquareImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Spinner spCount, spAspect;
    CheckBox cbCamera, cbCrop, cbCircle;
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spCount = (Spinner) findViewById(R.id.sp_photo_picker_count);
        spAspect = (Spinner) findViewById(R.id.sp_photo_picker_aspect);
        cbCamera = (CheckBox) findViewById(R.id.cb_photo_picker_camera);
        cbCrop = (CheckBox) findViewById(R.id.cb_photo_picker_crop);
        cbCircle = (CheckBox) findViewById(R.id.cb_photo_picker_circle_crop);
        gridView = (GridView) findViewById(R.id.gv_photo_picker_photos);
    }

    public void onSelect(View view) {
        String[] ss = spAspect.getSelectedItem().toString().split(":");
        int aspectX = Integer.parseInt(ss[0].trim());
        int aspectY = Integer.parseInt(ss[1].trim());
        int maxCount = spCount.getSelectedItemPosition() + 1;
        new PhotoPicker.Builder(this)
                .setMaxCount(maxCount)
                .setAspect(aspectX, aspectY)
                .hideCamera(cbCamera.isChecked())
                .circleCrop(cbCircle.isChecked())
//                .setCrop(cbCrop.isChecked())
                .create(1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ArrayList<IImage> images = data.getParcelableArrayListExtra("data");
            if (images.size() > 0) {
                gridView.setAdapter(new PhotoAdapter(images));
            }
        }
    }

    class PhotoAdapter extends BaseAdapter {
        final List<IImage> images;

        PhotoAdapter(List<IImage> images) {
            this.images = images;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public IImage getItem(int position) {
            return images.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView view = new SquareImageView(MainActivity.this);
            Bitmap bitmap = BitmapFactory.decodeFile(images.get(position).getPath());
            view.setImageBitmap(bitmap);
            view.setBackgroundColor(0xfff0f0f0);
            return view;
        }
    }
}
