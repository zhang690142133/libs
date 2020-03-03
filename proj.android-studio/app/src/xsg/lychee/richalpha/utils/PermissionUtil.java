package xsg.lychee.richalpha.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxHelper;


import java.util.HashMap;
import java.util.Map;

import xsg.lychee.richalpha.AppActivity;
import xsg.lychee.richalpha.R;

public class PermissionUtil {
    private static PermissionUtil _instance = null;
    private PermissionListener mPermissionListener = null;
    private Map<String, Boolean> requestRejectStatus = new HashMap<String, Boolean>();

    public interface PermissionListener {
        void onPermissionAccess(boolean result);
    }

    public static PermissionUtil getInstance() {
        if (_instance==null) {
            _instance = new PermissionUtil();
        }
        return _instance;
    }

    /**
     * 检查通知权限
     */
    public void checkNotificationPermission() {
       boolean isEnabled = NotificationManagerCompat.from(Cocos2dxActivity.getContext()).areNotificationsEnabled();
       mPermissionListener = null;
       if (!isEnabled) {
           openSetting(R.string.need_notify_permission);
       }
    }

    /**
     * 检查短信读取权限
     * @param listener 权限检查监听
     */
    public void checkSMSPermission(PermissionListener listener) {
        mPermissionListener = null;
        checkPermissions(new String[]{ Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS }, Constant.REQ_PERM_SMS, listener);
    }


    /**
     * 检查相机权限
     * @param listener 权限检查监听
     */
    public void checkCameraPermission(PermissionListener listener) {
        mPermissionListener = null;
        checkPermissions(new String[]{ Manifest.permission.CAMERA }, Constant.REQ_PERM_CAMERA, listener);

    }

    /**
     * 检查相册权限
     * @param listener 权限检查监听
     */
    public void checkAlbumPermission(PermissionListener listener) {
        mPermissionListener = null;
        checkPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE }, Constant.REQ_PERM_PHOTO, listener);
    }

    /**
     * 打开设置界面
     * @param textId id
     */
    private void openSetting(int textId) {
        final Context ctx = Cocos2dxActivity.getContext();

        new AlertDialog.Builder(ctx, R.style.PermissionDialog)
            .setMessage(textId)
            .setPositiveButton(R.string.to_open, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + Cocos2dxHelper.getPackageName()));
                    ctx.startActivity(intent);
                    dialogInterface.dismiss();;
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mPermissionListener != null) {
                        mPermissionListener.onPermissionAccess(false);
                    }
                }
            })
            .show();
    }

    /**
     * 判断是否已经取得权限
     * @param permissions 权限列表
     * @return true为已获取，false为未获取
     */
    private boolean hasPermissions(final String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(AppActivity.getContext(), perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查是否取得权限，未取得则申请
     * @param requestCode 用于跟踪请求的编码
     * @param permissions 需申请的权限列表
     * @param listener 权限检查监听
     */
    private void checkPermissions(final String[] permissions, final int requestCode, PermissionListener listener) {
        if (hasPermissions(permissions)) {
            listener.onPermissionAccess(true);
            return;
        }

        requestPermissions(permissions, requestCode, listener);
    }

    /**
     * 申请权限
     * @param permissions 权限列表
     * @param requestCode 用于跟踪请求的编码
     * @param listener 权限检查监听
     */
    private void requestPermissions(final String[] permissions, int requestCode, PermissionListener listener) {
        for (String perm : permissions) {
            boolean enabled = ActivityCompat.shouldShowRequestPermissionRationale((Activity) AppActivity.getContext(), perm);
            requestRejectStatus.put(perm, enabled);
        }

        mPermissionListener = listener;
        ActivityCompat.requestPermissions((Activity) AppActivity.getContext(), permissions, requestCode);
    }

    /**
     * 检查请求结果
     * @param permissions 权限列表
     * @param grantResults 获取结果
     */
    public boolean checkRequestPermissionResult(String[] permissions, int[] grantResults) {
        for (int ret : grantResults) {
            if (ret != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * 是否显示设置弹框
     * @param permissions 权限列表
     * @return 是否显示
     */
    private boolean shouldShowRequestRejectDialog(String[] permissions) {
        boolean enabled = false;

        for (String perm : permissions) {
            Boolean showEnabled = requestRejectStatus.get(perm);
            if (!showEnabled && !ActivityCompat.shouldShowRequestPermissionRationale((Activity) AppActivity.getContext(), perm)) {
                enabled = true;
                break;
            }
        }

        return enabled;
    }

    /**
     * 如果勾选了不再提醒，需要提示玩家前往设置界面设置
     * @param permissions 权限列表
     * @param textId 文字ID
     */
    public void showRequestRejectTip(String[] permissions, int textId) {
        if (shouldShowRequestRejectDialog(permissions)) {
            openSetting(textId);
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isGranted = checkRequestPermissionResult(permissions, grantResults);
        if (isGranted) {
            if (mPermissionListener != null) {
                mPermissionListener.onPermissionAccess(true);
            }
        } else {
            switch (requestCode) {
                case Constant.REQ_PERM_CAMERA:
                    showRequestRejectTip(permissions, R.string.need_camera_permission);
                    break;
                case Constant.REQ_PERM_PHOTO:
                    showRequestRejectTip(permissions, R.string.need_photo_permission);
                    break;
                    default:
                        break;
            }

        }
    }
}
