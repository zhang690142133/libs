package xsg.lychee.richalpha.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.zxing.activity.CaptureActivity;

import xsg.lychee.nativebridge.BridgeEnum;
import xsg.lychee.nativebridge.JavaJsBridge;
import xsg.lychee.richalpha.R;
import xsg.lychee.richalpha.utils.Constant;
import xsg.lychee.richalpha.utils.PermissionUtil;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.json.JSONException;
import org.json.JSONObject;


public class QRcodeTool {
    private static final int REQ_CODE_PERMISSION = 0x1111;
    private static Activity _activity = null;
    private static String _headUrl = null;
    private static String _description = null;

    public static void init(Activity act) {
        _activity = act;
    }

    public static void openScan(String description, String path) {
        _description = description;
        _headUrl = path;

        PermissionUtil.getInstance().checkCameraPermission((boolean result)-> {
                if (result) {
                    QRcodeTool.startQrCode();
                }
        });
    }

    // 开始扫码
    private static void startQrCode() {
        ((Activity) Cocos2dxActivity.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    // 二维码扫码
                    Intent intent = new Intent(_activity, CaptureActivity.class);
                    intent.putExtra(Constant.INTENT_EXTRA_KEY_QR_HEAD, _headUrl);
                    intent.putExtra(Constant.INTENT_EXTRA_KEY_QR_DESCRIPTION, _description);
                    _activity.startActivityForResult(intent, Constant.REQ_QR_CODE_CAPTURE);

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);

            JSONObject postMessage = new JSONObject();
            try {
                postMessage.put("scanString", scanResult);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JavaJsBridge.notifyEventToJs(BridgeEnum.SCAN_QRCODE, postMessage);
        }
    }
}
