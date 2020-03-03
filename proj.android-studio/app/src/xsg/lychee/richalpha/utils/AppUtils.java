package xsg.lychee.richalpha.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import xsg.lychee.richalpha.AppActivity;
import xsg.lychee.nativebridge.BridgeEnum;
import xsg.lychee.nativebridge.JavaJsBridge;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xsg.lychee.richalpha.BuildConfig;
import xsg.lychee.richalpha.R;
import xsg.lychee.richalpha.imagePicker.ImagePickerActivity;
import xsg.lychee.richalpha.imagePicker.ImagePickerSDK;


public class AppUtils
{
    private final static String TAG = "AppUtils";
    private static String appToken = "";
    private static String appLaunchInfo = "";
    private static String appReferrer = "";


    public static void reqAppToken()
    {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
        {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task)
            {
                if( !task.isSuccessful() )
                {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                String token = task.getResult().getToken();

                setAppToken(token);
            }
        });
    }

    public static void receiveToken(final String token)
    {
        setAppToken(token);

        JSONObject postMessage = new JSONObject();
        try
        {
            postMessage.put("token", token);
        }
        catch( JSONException e )
        {
            e.printStackTrace();
        }

        String jsonData = postMessage.toString();
        JavaJsBridge.notifyEventToJs(BridgeEnum.TOKEN_REFRESH, jsonData);
    }

    private static void setAppToken(String token)
    {
        appToken = token;
        Log.d(TAG, "app token = " + token);
    }

    public static String getAppToken()
    {
        Log.d(TAG, "app token = " + appToken);
        return appToken;
    }

    public static void setAppLaunchInfo(String info)
    {
        appLaunchInfo = info;
        Log.d(TAG, "appLaunchInfo = " + info);
        JavaJsBridge.notifyEventToJs(BridgeEnum.NOTIFY_MESSAGE, appLaunchInfo);
    }

    public static String getAppLaunchInfo()
    {
        return appLaunchInfo;
    }

    public static String getUDID()
    {
        return Settings.Secure.getString(AppActivity.getContext().getContentResolver(), Settings.Secure.ANDROID_ID) + "_" +
                Build.SERIAL;
    }

    public static String getChannel()
    {
        return BuildConfig.CHANNEL;
    }

    public static String getVersion()
    {
        return BuildConfig.VERSION_NAME;
    }

    public static String getReferrer()
    {
        return appReferrer;
    }

    public static void setReferrer(String ref)
    {
        appReferrer = ref;
    }


    public static String getDeviceModel() throws IOException
    {
        String[] versions =
                { "ro.build.version.incremental", "ro.build.version.emui", "ro.vivo.os.version", "ro.build.version.opporom",
                        "ro.build.sense.version", "ro.rom.version", "ro.build.display.id", "ro.modversion" };
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        String property = "";
        String pattern = ".*[^.](\\d+\\.\\d+\\.\\d).*";
        Pattern r = Pattern.compile(pattern);
        for( String version : versions )
        {
            property = properties.getProperty(version);
            if( property != null )
            {
                Matcher m = r.matcher(property);
                if( m.find() )
                {
                    return properties.getProperty("ro.product.model") + "(" + m.group(1) + ")";
                }
            }
        }
        return properties.getProperty("ro.product.model") + "(" + properties.getProperty("ro.build.version.release") + ")";
    }


    private static NetworkInfo[] getNetworkInfo()
    {
        if(AppActivity.getContext() != null)
        {
            ConnectivityManager cm = (ConnectivityManager) AppActivity.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if( cm == null )
            {
                return null;
            }
            else
            {
                return cm.getAllNetworkInfo();
            }
        }

        return null;
    }

    public static boolean isNetworkAvailable() {
        NetworkInfo[] infos = AppUtils.getNetworkInfo();
        if(infos != null) {
            for( NetworkInfo networkInfo : infos )
            {
                // 只檢查 wifi、手機行動網路 (3G、4G)、藍芽分享網路
                int type = networkInfo.getType();
                if( (type == ConnectivityManager.TYPE_WIFI) || (type == ConnectivityManager.TYPE_MOBILE) ||
                        (type == ConnectivityManager.TYPE_BLUETOOTH) )
                {
                    if( networkInfo.isConnected() )
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static int getNetworkType()
    {
        NetworkInfo[] infos = AppUtils.getNetworkInfo();
        if( infos != null )
        {
            for( NetworkInfo networkInfo : infos )
            {
                // 只檢查 wifi、手機行動網路 (3G、4G)、藍芽分享網路
                int type = networkInfo.getType();
                if( (type == ConnectivityManager.TYPE_WIFI) || (type == ConnectivityManager.TYPE_MOBILE) ||
                        (type == ConnectivityManager.TYPE_BLUETOOTH) )
                {
                    if( networkInfo.isConnected() )
                    {
                        if(type == ConnectivityManager.TYPE_MOBILE) {
                            return Constant.NETWORK_TYPE_MOBILE;
                        }

                        return Constant.NETWORK_TYPE_WIFI;
                    }
                }
            }
        }

        return Constant.NETWORK_TYPE_UNKNOWN;
    }

    /**
     * 保存图片到相册
     * @param fullPathFile 图片全路径
     * @return success
     */
    public static void savePictureToPhotoAlbum(String fullPathFile)
    {
        PermissionUtil.getInstance().checkAlbumPermission((boolean result)-> {
            if (result) {
                saveImageToGallery(new File(fullPathFile));
            }
        });
    }

    private static void saveImageToGallery(File file) {
        Context context = AppActivity.getContext();

        //把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            saveImageResult(false);
        }

        // 通知图库更新
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            if (uri == null) {
                                uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                            }
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(uri);
                            context.sendBroadcast(mediaScanIntent);

                            saveImageResult(true);
                        }
                    });
        } else {
            String relationDir = file.getParent();
            File file1 = new File(relationDir);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file1.getAbsoluteFile())));

            saveImageResult(true);
        }
    }

    private static void saveImageResult(boolean success) {
        JSONObject postMessage = new JSONObject();
        try {
            postMessage.put("success", success);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JavaJsBridge.notifyEventToJs(BridgeEnum.SAVE_PHOTO, postMessage);
    }
}
