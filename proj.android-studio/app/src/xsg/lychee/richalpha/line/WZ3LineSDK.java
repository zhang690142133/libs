package xsg.lychee.richalpha.line;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.linecorp.linesdk.LineProfile;
import com.linecorp.linesdk.Scope;
import com.linecorp.linesdk.api.LineApiClient;
import com.linecorp.linesdk.api.LineApiClientBuilder;
import com.linecorp.linesdk.auth.LineAuthenticationParams;
import com.linecorp.linesdk.auth.LineLoginApi;
import com.linecorp.linesdk.auth.LineLoginResult;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;

import xsg.lychee.nativebridge.BridgeEnum;
import xsg.lychee.nativebridge.JavaJsBridge;
import xsg.lychee.richalpha.BuildConfig;
import xsg.lychee.richalpha.utils.Constant;

public class WZ3LineSDK {
    static private Activity mActivity;
    private static LineApiClient mLineApiClient;

    static public void init(Activity activty) {
        mActivity = activty;
        LineApiClientBuilder apiClientBuilder = new LineApiClientBuilder(activty, Constant.LINE_CHANNEL_ID);
        mLineApiClient = apiClientBuilder.build();
    }

    static public void login() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            try {
                // 检测token有效性
                if (mLineApiClient.verifyToken().isSuccess()) {
                    String accessToken = mLineApiClient.getCurrentAccessToken().getResponseData().getTokenString();
                    LineProfile profile = mLineApiClient.getProfile().getResponseData();

                    loginSuccess(accessToken, profile.getPictureUrl());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ((Activity) Cocos2dxActivity.getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{
                        // 调起line app授权登录
                        Intent loginIntent = LineLoginApi.getLoginIntent(
                                mActivity,
                                Constant.LINE_CHANNEL_ID,
                                new LineAuthenticationParams.Builder()
                                        .scopes(Arrays.asList(Scope.PROFILE))
                                        .build());
                        mActivity.startActivityForResult(loginIntent, Constant.REQ_LINE_LOGIN);

                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    static public void logout() {
        mLineApiClient.logout();
    }

    static public void sharePicture(String fullPathFile) {
        ComponentName cn = new ComponentName("jp.naver.line.android"
                , "jp.naver.line.android.activity.selectchat.SelectChatActivityLaunchActivity");
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        Uri uri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID + ".provider", new File(fullPathFile));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/jpeg"); //图片分享
        //intent.setType("text/plain"); // 纯文本
        //intent.putExtra(Intent.EXTRA_SUBJECT, "分享的标题");
        //intent.putExtra(Intent.EXTRA_TEXT, "分享的内容");
        shareIntent.setComponent(cn);//跳到指定APP的Activity
        mActivity.startActivity(Intent.createChooser(shareIntent,""));
    }

    static public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != Constant.REQ_LINE_LOGIN) {
            return;
        }

        LineLoginResult result = LineLoginApi.getLoginResultFromIntent(data);
        switch (result.getResponseCode()) {

            case SUCCESS:
                String accessToken = result.getLineCredential().getAccessToken().getTokenString();
                LineProfile profile = result.getLineProfile();

                loginSuccess(accessToken, profile.getPictureUrl());
                break;
            default:
                loginFailed(result.getErrorData().toString());
        }
    }

    static private void loginSuccess(String accessToken, Uri uri) {
        JSONObject postMessage = new JSONObject();
        try {
            postMessage.put("token", accessToken);
            postMessage.put("pic", uri==null?"":uri.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JavaJsBridge.notifyEventToJs(BridgeEnum.LINE_LOGIN, postMessage);
    }

    static private void loginFailed(String err) {
        JSONObject postMessage = new JSONObject();
        try {
            postMessage.put("err", err);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JavaJsBridge.notifyEventToJs(BridgeEnum.LINE_LOGIN, postMessage);
    }
}
