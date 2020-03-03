package xsg.lychee.richalpha.utils.firebase;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

public class WZ3CrashlyticsKit {
    public static void setString(String key, String value) {
        Crashlytics.setString(key, value);
    }

    public static void log(String tag, String message) {
        Crashlytics.log(Log.DEBUG, tag, message);
    }
}
