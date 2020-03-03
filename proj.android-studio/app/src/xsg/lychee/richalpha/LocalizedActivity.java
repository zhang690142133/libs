package xsg.lychee.richalpha;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import xsg.lychee.richalpha.utils.LocalizationUtil;

//本地化界面基类
public class LocalizedActivity extends Activity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalizationUtil.attachBaseContext(newBase));
    }
}
