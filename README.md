# PhotoPicker
### 添加依赖关系
```gradle
compile 'com.github.CNCoderX:PhotoPicker:1.0.0'
```
### 使用方法
##### 添加权限
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
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
