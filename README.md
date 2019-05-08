# PhotoPicker
### 添加依赖关系
```gradle
compile 'com.github.CNCoderX:PhotoPicker:1.0.1'
```
### 使用方法
##### 添加权限
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
##### 在Android7.0上添加FileProvider
```xml
// 在AndroidManifest.xml中添加
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">

        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
</provider>

// 新建xml/file_paths.xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-path name="external" path="DCIM"/>
</paths>
```

##### 注册activity
```java
<activity
    android:name="com.cncoderx.photopicker.ui.GalleryActivity"
    android:theme="@style/PhotoPickerTheme"
    android:screenOrientation="portrait"/>

<activity
    android:name="com.cncoderx.photopicker.ui.CropPhotoActivity"
    android:theme="@style/PhotoPickerTheme"
    android:screenOrientation="portrait"/>
```
##### 在代码中添加
```java
new PhotoPicker.Builder(this)
	.setMaxCount(maxCount)
	.setAspect(1, 1)
	.hideCamera(true)
	.circleCrop(true)
	.create(1);
```
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (resultCode == RESULT_OK) {
	    ArrayList<IImage> images = data.getParcelableArrayListExtra(PhotoPicker.EXTRA_DATA);
	    ......
	}
}
```
