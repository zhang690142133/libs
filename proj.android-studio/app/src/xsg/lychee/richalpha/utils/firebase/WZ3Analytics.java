package xsg.lychee.richalpha.utils.firebase;

import android.app.Activity;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class WZ3Analytics {
    private static FirebaseAnalytics mFirebaseAnalytics;
    private static Activity mActivity;

    public static void init(Activity activity) {
        mActivity = activity;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
    }

    public static void logEvent(String type, String event) {
        if (mFirebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT, event);
            mFirebaseAnalytics.logEvent(type, bundle);
        }
    }

    public static void setScreenName(String name) {
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setCurrentScreen(mActivity, name, null /* class override */);
        }
    }

    public static void setUserID(String userID) {
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setUserId(userID);
        }
    }
}
