package xsg.lychee.richalpha.utils;

import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.WindowInsets;
import android.view.WindowManager;

import xsg.lychee.richalpha.AppActivity;

import java.lang.reflect.Method;
import java.util.List;

public class AdaptiveUtil {
    private final static String TAG = "AppUtils";
    // 是否有刘海
    private static boolean hasNotch = false;
    // 需不需要刘海适配,全面屏使用刘海区域 false:无刘海或有刘海不需要使用
    private static boolean useNotch = false;
    private static int notchHeight = 0;
    private static AppActivity That = null;

    public static void init(AppActivity app) {
        That = app;
        initNotch();
    }

    // 判断是否有刘海并设置刘海信息
    public static void initNotch() {
        if (That == null) {
            Log.d(TAG, "APP为空");
        } else {
            Log.d(TAG, "APP不为空");
        }
        Log.d(TAG, "sdk 版本" + Build.VERSION.SDK_INT);
        notchHeight = initOtherNotch();
        if (notchHeight > 0) {
            hasNotch = true;
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            WindowInsets windowInsets = That.getWindow().getDecorView().getRootWindowInsets();
            if(windowInsets!=null){
                DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                if (displayCutout != null) {
                    List<Rect> rects = displayCutout.getBoundingRects();
                    // 通过判断是否存在rects来确定是否刘海屏手机
                    if (rects != null && rects.size() > 0) {
                        // 有刘海屏
                        hasNotch = true;
                        // 设置刘海高度
                        notchHeight = displayCutout.getSafeInsetTop();
                    }
                }
            }

        }
        // 开启刘海
        if (hasNotch) {
            Log.d(TAG, "开启刘海");
            onNotch();
        }
        onNotch();

    }

    // 其他厂商刘海
    public static int initOtherNotch() {
        int h = 0;
        String manufacturer = Build.MANUFACTURER;
        switch (manufacturer) {
        case "HUAWEI":
            h = getNotchHeightForHUAWEI();
            break;
        case "xiaomi":
            h = getNotchHeightForXIAOMI();
            break;
        case "oppo":
            h = getNotchHeightForOPPO();
            break;
        case "vivo":
            h = getNotchHeightForVIVO();
            break;
        default:
            return 0;
        }
        return h;
    }

    // 华为
    public static int getNotchHeightForHUAWEI() {
        if (hasNotchtForHUAWEI()) {
            int[] ret = new int[] { 0, 0 };
            try {
                ClassLoader cl = That.getClassLoader();
                Class<?> HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
                Method get = HwNotchSizeUtil.getMethod("getNotchSize");
                ret = (int[]) get.invoke(HwNotchSizeUtil);

            } catch (ClassNotFoundException e) {
                Log.e("NotchScreenUtil", "getNotchSize ClassNotFoundException");
            } catch (NoSuchMethodException e) {
                Log.e("NotchScreenUtil", "getNotchSize NoSuchMethodException");
            } catch (Exception e) {
                Log.e("NotchScreenUtil", "getNotchSize Exception");
            }
            return ret[1];
        } else {
            return 0;
        }
    }

    public static boolean hasNotchtForHUAWEI() {
        try {
            ClassLoader cl = That.getClassLoader();
            Class NotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = NotchSizeUtil.getMethod("hasNotchInScreen");
            return (boolean) get.invoke(NotchSizeUtil);
        } catch (Exception e) {
            return false;
        }
    }

    // 小米
    public static int getNotchHeightForXIAOMI() {
        int h = 0;
        if (hasNotchtForXIAOMI()) {
            // 隐藏屏幕刘海是否开启
            boolean fb = false;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                fb = Settings.System.getInt(That.getContentResolver(), "force_black", 0) == 1;
            } else {
                fb = Settings.Global.getInt(That.getContentResolver(), "force_black", 0) == 1;
            }
            if (!fb) {
                // 获取高度
                int resourceId = That.getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    h = That.getResources().getDimensionPixelSize(resourceId);
                }
            }
        }
        return h;

    }

    public static boolean hasNotchtForXIAOMI() {
        try {
            ClassLoader cl = AppActivity.getContext().getClassLoader();
            Class NotchSizeUtil = cl.loadClass("android.os.SystemProperties");
            Method get = NotchSizeUtil.getMethod("getInt", String.class, int.class);
            return (int) (get.invoke(NotchSizeUtil, "ro.miui.notch", 0)) == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // oppo
    public static int getNotchHeightForOPPO() {
        if (hasNotchtForOPPO()) {
            return 80;
        } else {
            return 0;
        }

    }

    public static boolean hasNotchtForOPPO() {
        return AppActivity.getContext().getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    // vivo
    public static int getNotchHeightForVIVO() {
        if (hasNotchtForVIVO()) {
            return 27;
        } else {
            return 0;
        }
    }

    public static boolean hasNotchtForVIVO() {
        try {
            ClassLoader cl = AppActivity.getContext().getClassLoader();
            Class NotchSizeUtil = cl.loadClass("android.util.FtFeature");
            Method get = NotchSizeUtil.getMethod("isFeatureSupport");
            return (boolean) (get.invoke(NotchSizeUtil, 0x20));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 是否使用刘海区域
    public static void onNotch() {
        useNotch = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {// Android P使用刘海区域
            WindowManager.LayoutParams lp = That.getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            That.getWindow().setAttributes(lp);
        } else {
        }
    }

    // 获取刘海高度
    public static int getNotchHeight() {
        final float scale = That.getResources().getDisplayMetrics().density;
        Log.d(TAG, "刘海高度" + notchHeight + ":" + (int) (notchHeight / scale + 0.5f));
        // return (int) (notchHeight * scale + 0.5f);
        return (int) (notchHeight / scale + 0.5f);
        // return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        // notchHeight,That.getResources().getDisplayMetrics() );
    }

}
