package com.cncoderx.photopicker.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.cncoderx.photopicker.R;
import com.cncoderx.photopicker.bean.Album;
import com.cncoderx.photopicker.bean.Photo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wujie on 2017/2/15.
 */
public class BucketHelper {
    static final String[] PROJECTION_GET_PHOTO =  {
            MediaStore.Images.ImageColumns._ID,           // 0
            MediaStore.Images.ImageColumns.TITLE,         // 1
            MediaStore.Images.ImageColumns.MIME_TYPE,     // 2
            MediaStore.Images.ImageColumns.LATITUDE,      // 3
            MediaStore.Images.ImageColumns.LONGITUDE,     // 4
            MediaStore.Images.ImageColumns.DATE_TAKEN,    // 5
            MediaStore.Images.ImageColumns.DATE_ADDED,    // 6
            MediaStore.Images.ImageColumns.DATE_MODIFIED, // 7
            MediaStore.Images.ImageColumns.DATA,          // 8
            MediaStore.Images.ImageColumns.ORIENTATION,   // 9
            MediaStore.Images.ImageColumns.BUCKET_ID,     // 10
            MediaStore.Images.ImageColumns.SIZE,          // 11
            MediaStore$Images$ImageColumns$WIDTH(),       // 12
            MediaStore$Images$ImageColumns$HEIGHT(),      // 13
    };

    static final String ORDER_GET_PHOTO = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC";

    static final String[] PROJECTION_GET_ALBUM = {
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.ORIENTATION,
            "COUNT(1)"
    };

    static final String SELECTION_GET_ALBUM = "1) GROUP BY (1";

    static String MediaStore$Images$ImageColumns$WIDTH() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return "0";
        } else {
            return MediaStore.Images.ImageColumns.WIDTH;
        }
    }

    static String MediaStore$Images$ImageColumns$HEIGHT() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return "0";
        } else {
            return MediaStore.Images.ImageColumns.HEIGHT;
        }
    }

    public static List<Album> getAlbumList(Context context) {
        List<Album> albumList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_GET_ALBUM, SELECTION_GET_ALBUM,
                null, null);
        if (cursor == null) {
            Logger.w("cannot open media database: " + MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            return albumList;
        }

        int photoCount = 0;
        try {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String cover = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                int count = cursor.getInt(4);
                photoCount += count;
                Album album = new Album();
                album.setId(id);
                album.setName(name);
                album.setCoverPath(cover);
                album.setPhotoCount(count);
                albumList.add(album);
            }
        } finally {
            cursor.close();
        }

        if (photoCount > 0) {
            Album album = new Album();
            album.setId("");
            album.setName(context.getString(R.string.all_image));
            album.setCoverPath(albumList.get(0).getCoverPath());
            album.setPhotoCount(photoCount);
            albumList.add(0, album);
        }

        return albumList;
    }

    public static List<Photo> getPhotoList(Context context) {
        List<Photo> photoList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_GET_PHOTO,
                null,
                null,
                ORDER_GET_PHOTO);
        if (cursor == null) {
            Logger.w("cannot open media database: " + MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            return photoList;
        }
        try {
            while (cursor.moveToNext()) {
                Photo photo = new Photo(cursor);
                photoList.add(photo);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    String filePath = photo.getFilePath();
                    if (!TextUtils.isEmpty(filePath)) {
                        try {
                            ExifInterface exifInterface = new ExifInterface(filePath);
                            int width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                            int height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
                            photo.setWidth(width);
                            photo.setHeight(height);
                        } catch (IOException e) {
                            Logger.e(e);
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return photoList;
    }

    public static List<Photo> getPhotoListByBucketId(Context context, String bucketId) {
        List<Photo> photoList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_GET_PHOTO,
                MediaStore.Images.ImageColumns.BUCKET_ID + "=?",
                new String[]{bucketId},
                ORDER_GET_PHOTO);
        if (cursor == null) {
            Logger.w("cannot open media database: " + MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            return photoList;
        }
        try {
            while (cursor.moveToNext()) {
                Photo photo = new Photo(cursor);
                photoList.add(photo);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    String filePath = photo.getFilePath();
                    if (!TextUtils.isEmpty(filePath)) {
                        try {
                            ExifInterface exifInterface = new ExifInterface(filePath);
                            int width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                            int height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
                            photo.setWidth(width);
                            photo.setHeight(height);
                        } catch (IOException e) {
                            Logger.e(e);
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return photoList;
    }
}
