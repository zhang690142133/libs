package xsg.lychee.richalpha.utils;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import xsg.lychee.richalpha.AppActivity;
import xsg.lychee.richalpha.service.SMSContentObserver;

public class SMSCodeUtil {
    private static SMSCodeUtil mInstace = null;

    private SMSContentObserver mObserver = null;
    private Context mContext = null;
    private Handler mHandler = null;

    /**
     * 打开自动填入验证码功能
     */
    public static void openSMSCodeAutoInput() {
        getInstance()._openSMSCodeAutlInput();
    }


    public static SMSCodeUtil getInstance() {
        if (null == mInstace) {
            mInstace = new SMSCodeUtil();
        }
        return mInstace;
    }

    //实例化短信监听
    public void init(Context context) {
        mContext = context;
        mHandler = new Handler();
    }

    public void _openSMSCodeAutlInput() {
        PermissionUtil.getInstance().checkSMSPermission((boolean result)->{
            if (result) {
                if (mObserver == null) {
                    mObserver = new SMSContentObserver(mHandler);
                    mContext.getContentResolver().registerContentObserver(Uri.parse("content://sms/inbox"), true, mObserver);
                }
            }
            else {
                this.destroy();
            }
        });
    }

    public void destroy() {
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
        }
    }

    public SMSContentObserver getObserver() {
        return mObserver;
    }
}
