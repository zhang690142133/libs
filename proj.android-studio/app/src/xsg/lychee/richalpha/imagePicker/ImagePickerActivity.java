/****************************************************************************
 Author: Luma (stubma@gmail.com)
 
 https://github.com/stubma/cocos2dx-classical
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
package xsg.lychee.richalpha.imagePicker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import xsg.lychee.richalpha.R;
import xsg.lychee.richalpha.imagePicker.cropimage.CropImageIntentBuilder;
import xsg.lychee.richalpha.imagePicker.cropimage.Util;
import xsg.lychee.richalpha.utils.Constant;
import xsg.lychee.richalpha.utils.PermissionUtil;

import xsg.lychee.richalpha.BuildConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class ImagePickerActivity extends Activity {

	private Uri _imageSaveUri;
	private CompressFormat _outputFormat = CompressFormat.JPEG;

	private final String TAG = "ImagePickerActivity";

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		_imageSaveUri = FileProvider.getUriForFile(ImagePickerActivity.this, BuildConfig.APPLICATION_ID + ".provider", ImagePickerSDK.sDestFile);
		_outputFormat = isPNG() ? CompressFormat.PNG : CompressFormat.JPEG;

		if(ImagePickerSDK.sFromAlbum) {
			PermissionUtil.getInstance().checkAlbumPermission((boolean result)-> {
				if (result) {
					selectPhoto();
				}
			});
		} else if (ImagePickerSDK.sDestFile != null) {	// Fix :: android crash when choose photo from camera
			PermissionUtil.getInstance().checkCameraPermission((boolean result)-> {
				if (result) {
					takePhoto();
				}
			});
		}
	}

	private void selectPhoto() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, Constant.REQ_SELECT_PHOTO);
	}

	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageSaveUri);

//		List<ResolveInfo> resolvedIntentActivities = ImagePickerActivity.this.getPackageManager()
//				.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//		for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
//			String packageName = resolvedIntentInfo.activityInfo.packageName;
//
//			ImagePickerActivity.this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//		}

		if(ImagePickerSDK.sFront) {
			intent.putExtra("android.intent.extras.CAMERA_FACING", CameraInfo.CAMERA_FACING_FRONT);
		}
		startActivityForResult(intent, Constant.REQ_CAPTURE_IMAGE);
	}
	
	private boolean isPNG() {
		String path = ImagePickerSDK.sDestFile.getAbsolutePath();
		int lastDot = path.lastIndexOf('.');
		if(lastDot == -1) {
			return false;
		} else {
			return path.substring(lastDot + 1).equalsIgnoreCase("png");
		}
	}

	private void showCropImageView(Uri sourceImage) {
		CropImageIntentBuilder cropImage = new CropImageIntentBuilder(ImagePickerSDK.sExpectedWidth, ImagePickerSDK.sExpectedHeight, _imageSaveUri);
		cropImage.setSourceImage(sourceImage);
		if(isPNG())
			cropImage.setOutputFormat(CompressFormat.PNG.toString());

		Intent intent = cropImage.getIntent(this);
		if (ImagePickerSDK.sCropMode == ImagePickerSDK.CropMode.CIRCLE) {
			intent.putExtra("circleCrop", "true");
		}
		startActivityForResult(intent, Constant.REQ_CROP_IMAGE);
	}

	private void saveImageWithOutCrop(final Uri sourceImage)  {
		OutputStream outputStream = null;
		try {
			long startTime = System.currentTimeMillis();

			Bitmap saveBitmap = ImageUtil.getBitmapFormUri(
					ImagePickerActivity.this, sourceImage,
					(float) ImagePickerSDK.sExpectedWidth,
					(float) ImagePickerSDK.sExpectedHeight,
					_outputFormat
			);

			// 三星手机的图片90度旋转，需要摆正
			int degreee = ImageUtil.getBitmapDegree(ImagePickerSDK.sDestFile.getAbsolutePath());
			Log.d(TAG, "saveImageWithCrop: degreee " + degreee);
			saveBitmap = ImageUtil.rotateBitmapByDegree(saveBitmap, degreee);

			Log.d(TAG, "saveImageWithCrop: setp1 " + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();

			outputStream = getContentResolver().openOutputStream(_imageSaveUri);
			if (outputStream != null) {
				saveBitmap.compress(_outputFormat, 50, outputStream);

				ImagePickerSDK.onImagePicked();
				finish();
			}

			Log.d(TAG, "saveImageWithCrop: setp2 " + (System.currentTimeMillis() - startTime));
		} catch (IOException ex) {
			Log.e(TAG, "Cannot save file: " + sourceImage, ex);

			ImagePickerSDK.onImagePickingCancelled();
			finish();
		} finally {
			if (outputStream != null)
				Util.closeSilently(outputStream);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(ImagePickerSDK.sDestFile != null && resultCode == RESULT_OK)
		{
			try
			{
				switch(requestCode) {
					case Constant.REQ_SELECT_PHOTO:
						if (ImagePickerSDK.sCropMode == ImagePickerSDK.CropMode.NONE) {
							// 保存图片

							saveImageWithOutCrop(data.getData());
						}
						else {
							showCropImageView(data.getData());
						}

						break;
					case Constant.REQ_CAPTURE_IMAGE:
						if (ImagePickerSDK.sCropMode == ImagePickerSDK.CropMode.NONE) {
							// 保存图片
							saveImageWithOutCrop(_imageSaveUri);
						}
						else {
							showCropImageView(_imageSaveUri);
						}
						break;
					case Constant.REQ_CROP_IMAGE:
						ImagePickerSDK.onImagePicked();
						finish();
						break;
					default:
						break;
				}
			}
			catch(Exception e)
			{
				ImagePickerSDK.onImagePickingCancelled();
				finish();
			}
		}
		else
		{
			ImagePickerSDK.onImagePickingCancelled();
			finish();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}
