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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;

import xsg.lychee.nativebridge.BridgeEnum;
import xsg.lychee.nativebridge.JavaJsBridge;
import xsg.lychee.richalpha.AppActivity;
import xsg.lychee.richalpha.BuildConfig;
import xsg.lychee.richalpha.dailog.BottomDailog;
import org.cocos2dx.lib.Cocos2dxActivity;

import java.io.File;

import xsg.lychee.richalpha.R;
import xsg.lychee.richalpha.utils.Constant;
import xsg.lychee.richalpha.utils.PermissionUtil;


public class ImagePickerSDK {



	public static boolean sFromAlbum;
	public static String sPath;
	public static int sExpectedWidth;
	public static int sExpectedHeight;
	public static boolean sFront;
	public static boolean sKeepRatio;
	public static File sDestFile;
	private static boolean picking = false;
	public static int sCropMode = CropMode.RECTANGLE;

	public static class CropMode {
		public static int CIRCLE = 0;
		public static int RECTANGLE = 1;
		public static int NONE = 2;
	}

	static boolean hasCamera() {
		return Camera.getNumberOfCameras() > 0;
	}

	static boolean hasFrontCamera() {
		int c = Camera.getNumberOfCameras();
		for(int i = 0; i < c; i++) {
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(i, cameraInfo);
			if(cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT)
				return true;
		}
		return false;
	}

	public static boolean openImagePicker(final String path, final int w, final int h, final boolean front, final int cropMode) {
		((Activity)Cocos2dxActivity.getContext()).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final BottomDailog dialogView = new BottomDailog(Cocos2dxActivity.getContext(), R.layout.bottom_dailog, true, true);
				dialogView.show();

				Button camera = (Button) dialogView.findViewById(R.id.camera);
				camera.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialogView.dismiss();
						pickFromCameraJs(path, w, h, front, cropMode);
					}
				});

				Button photo = (Button) dialogView.findViewById(R.id.photo);
				photo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialogView.dismiss();
						pickFromAlbumJs(path, w, h, cropMode);
					}
				});
			}
		});

		return true;
	}

	public static boolean pickFromCameraJs(String path, int w, int h, boolean front, final int cropMode) {
		sCropMode = cropMode;
		sFromAlbum = false;
		sPath = path;
		sExpectedWidth = w;
		sExpectedHeight = h;
		sFront = front;
		sKeepRatio = true;

		if (picking) {
			return false;
		}

		picking = true;

		// the full path of image file
		sDestFile = new File(sPath);

		PermissionUtil.getInstance().checkCameraPermission((boolean result)-> {
			if (result) {
				Context context = AppActivity.getContext();
				Intent intent = new Intent(context, ImagePickerActivity.class);
				context.startActivity(intent);
			}
		});

		return true;
	}

	public static boolean pickFromAlbumJs(String path, int w, int h, final int cropMode) {
		sCropMode = cropMode;
		sFromAlbum = true;
		sPath = path;
		sExpectedWidth = w;
		sExpectedHeight = h;
		sKeepRatio = true;

		if (picking) {
			return false;
		}

		picking = true;
		
		// the full path of image file
		sDestFile = new File(sPath);

		PermissionUtil.getInstance().checkAlbumPermission((boolean result)-> {
			if (result) {
				Context context = AppActivity.getContext();
				Intent intent = new Intent(context, ImagePickerActivity.class);
				context.startActivity(intent);
			}
		});

		return true;
	}
	
	static void onImagePicked() {
		picking = false;
		JavaJsBridge.notifyEventToJs(BridgeEnum.IMAGE_PICKER, sDestFile.getAbsolutePath());
		sDestFile = null;
	}
	
	static void onImagePickingCancelled() {
		sDestFile = null;
		picking = false;
	}
}