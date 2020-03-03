package xsg.lychee.richalpha.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationUtil {
    final static public String LANGUAGE_FILE_NAME = "language_local";
    final static public String LANGUAGE_TAG = "language";
    static private Context _mainContext = null;


    /**
     * 支持语言配置表
     */
    private static Map<String, Locale> mSupportLanguages = new HashMap<String, Locale>(7) {{
        put(LanguageConstants.ENGLISH, Locale.ENGLISH);
        put(LanguageConstants.TRADITIONAL_CHINESE, Locale.TRADITIONAL_CHINESE);
    }};

    public static void init(Context context) {
        _mainContext = context;
    }

    public static void setLanguage(String lan) {
        SharedPreferences preferences = _mainContext.getSharedPreferences(LANGUAGE_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LANGUAGE_TAG, lan);
        editor.commit();
    }

    /**
     * 获取系统首选语言
     *
     * @return Locale
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Locale getSystemPreferredLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * 是否支持此语言
     *
     * @param language language
     * @return true:支持 false:不支持
     */
    public static boolean isSupportLanguage(String language) {
        return mSupportLanguages.containsKey(language);
    }

    /**
     * 获取支持语言
     *
     * @param language language
     * @return 支持返回支持语言，不支持返回系统首选语言
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getSupportLanguage(String language) {
        if (isSupportLanguage(language)) {
            return mSupportLanguages.get(language);
        }
        return getSystemPreferredLanguage();
    }

    /**
     * 应用语言
     * @param context
     * @param newLanguage
     */
    public static void applyLanguage(Context context, String newLanguage) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getSupportLanguage(newLanguage);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // apply locale
            configuration.setLocale(locale);

        } else {
            // updateConfiguration
            configuration.locale = locale;
            DisplayMetrics dm = resources.getDisplayMetrics();
            resources.updateConfiguration(configuration, dm);
        }
    }

    public static Context attachBaseContext(Context context) {
        String language = "";

        // 获取本地记录语言  禁用，原生统一以系统为准
//        SharedPreferences preferences = context.getSharedPreferences(LocalizationUtil.LANGUAGE_FILE_NAME, Context.MODE_PRIVATE);
//        language = preferences.getString(LocalizationUtil.LANGUAGE_TAG, "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return createConfigurationResources(context, language);
        } else {
            applyLanguage(context, language);
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context createConfigurationResources(Context context, String language) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getSupportLanguage(language);
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }
}
