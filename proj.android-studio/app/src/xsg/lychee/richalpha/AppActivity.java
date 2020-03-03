/****************************************************************************
 Copyright (c) 2015-2016 Chukong Technologies Inc.
 Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

 http://www.cocos2d-x.org

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
package xsg.lychee.richalpha;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.appsflyer.AppsFlyerLib;

import xsg.lychee.appsflyer.Appsflyer;
import xsg.lychee.appsflyer.IReferrerListener;

import xsg.lychee.nativebridge.JavaJsBridge;
import xsg.lychee.payment.Payment;
import xsg.lychee.richalpha.line.WZ3LineSDK;
import xsg.lychee.richalpha.utils.AdaptiveUtil;
import xsg.lychee.richalpha.utils.LocalizationUtil;
import xsg.lychee.richalpha.qrcode.QRcodeTool;

import xsg.lychee.richalpha.utils.AppUtils;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;

import xsg.lychee.richalpha.utils.PermissionUtil;
import xsg.lychee.richalpha.utils.SMSCodeUtil;
import xsg.lychee.richalpha.utils.firebase.WZ3Analytics;


public class AppActivity extends com.sdkbox.plugin.SDKBoxActivity implements IReferrerListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first
		// -time/16447508
		if( !isTaskRoot() )
		{
			// Android launched another instance of the root activity into an existing task
			//  so just quietly finish and go away, dropping the user back into the activity
			//  at the top of the stack (ie: the last state of this task)
			// Don't need to finish it again since it's finished in super.onCreate .
			return;
		}
		AdaptiveUtil.init(this);
		// DO OTHER INITIALIZATION BELOW
		SDKWrapper.getInstance().init(this);

		JavaJsBridge.init(this);
		AppUtils.reqAppToken();
		handleIntent(getIntent());

		Appsflyer.setListener(this);
		Appsflyer.Init(getString(R.string.appsflyer_id), getString(R.string.fcm_sender_id), this.getApplication());
		AppsFlyerLib.getInstance().sendDeepLinkData(this);

		WZ3Analytics.init(this);
		WZ3LineSDK.init(this);

		//QRcode
		QRcodeTool.init(this);

		LocalizationUtil.init(this);

		Payment.getInstance().init(this);
		SMSCodeUtil.getInstance().init(this);
		PermissionUtil.getInstance().checkNotificationPermission();
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(LocalizationUtil.attachBaseContext(newBase));
	}

	private void handleIntent(Intent intent)
	{
		String appLinkAction = intent.getAction();

		if( Intent.ACTION_MAIN.equals(appLinkAction) )
		{
			Uri appLinkData = intent.getData();
			if (appLinkData != null) {
				String info = appLinkData.getQuery();
				AppUtils.setAppLaunchInfo(info);
			}

			String fbNotificationInfo = intent.getStringExtra("data");
			if (fbNotificationInfo != null) {
				AppUtils.setAppLaunchInfo(fbNotificationInfo);
			}
		}
	}

	@Override
	public Cocos2dxGLSurfaceView onCreateView()
	{
		Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
		// TestCpp should create stencil buffer
		glSurfaceView.setEGLConfigChooser(5, 6, 5, 0, 16, 8);
		SDKWrapper.getInstance().setGLSurfaceView(glSurfaceView, this);

		return glSurfaceView;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		SDKWrapper.getInstance().onResume();

	}

	@Override
	protected void onPause()
	{
		super.onPause();
		SDKWrapper.getInstance().onPause();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		SDKWrapper.getInstance().onDestroy();
		SMSCodeUtil.getInstance().destroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		SDKWrapper.getInstance().onActivityResult(requestCode, resultCode, data);
		QRcodeTool.onActivityResult(requestCode, resultCode, data);
		WZ3LineSDK.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		SDKWrapper.getInstance().onNewIntent(intent);
		handleIntent(intent);
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		SDKWrapper.getInstance().onRestart();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		SDKWrapper.getInstance().onStop();
	}

	@Override
	public void onBackPressed()
	{
		SDKWrapper.getInstance().onBackPressed();
		super.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		SDKWrapper.getInstance().onConfigurationChanged(newConfig);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		SDKWrapper.getInstance().onRestoreInstanceState(savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		SDKWrapper.getInstance().onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart()
	{
		SDKWrapper.getInstance().onStart();
		super.onStart();
	}

	@Override
	public void onReferrer(String referrer)
	{
		AppUtils.setReferrer(referrer);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		PermissionUtil.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}
