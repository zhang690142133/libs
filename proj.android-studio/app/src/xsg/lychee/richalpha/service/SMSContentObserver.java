package xsg.lychee.richalpha.service;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xsg.lychee.nativebridge.BridgeEnum;
import xsg.lychee.nativebridge.JavaJsBridge;
import xsg.lychee.richalpha.AppActivity;

public class SMSContentObserver extends ContentObserver {
    public final String SMS_URI_INBOX = "content://sms/inbox";//收信箱
    private String smsContent = "";//验证码
    private String smsID = "";

    public SMSContentObserver(Handler handler) {
        super(handler);
    }

    /**
     * 短信观察者 收到一条短信时 onchange方法会执行两次，所以比较短信id，如果一致则不处理
     * https://yq.aliyun.com/articles/595895
     */
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Cursor cursor = null;// 光标
        // 读取收件箱中指定号码的短信
        cursor = AppActivity.getContext().getContentResolver().query(Uri.parse(SMS_URI_INBOX),
                null,
                null,
                null,
                "date desc");//排序

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                //比较和上次接收到短信的ID是否相等
                if (smsID.equals(cursor.getString(cursor.getColumnIndex("_id")))) {
                    return;
                }
                smsID = cursor.getString(cursor.getColumnIndex("_id"));

                String smsbody = cursor.getString(cursor.getColumnIndex("body"));

                //用正则表达式匹配验证码
                Pattern pattern = Pattern.compile("[0-9]{4}");
                Matcher matcher = pattern.matcher(smsbody);
                if (matcher.find()) {//匹配到4位的验证码
                    smsContent = matcher.group();
                    JavaJsBridge.notifyEventToJs(BridgeEnum.SMS_CODE, smsContent);
                }

            }
        }

    }
}
