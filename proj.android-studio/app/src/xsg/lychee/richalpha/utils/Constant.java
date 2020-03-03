package xsg.lychee.richalpha.utils;

/**
 * 常量
 */
public class Constant {
    // 操作码
    public static final int REQ_QR_CODE_CAPTURE = 11001; // // 打开扫描界面
    public static final int REQ_QR_CODE_DISPLAY = 11002; // 读写文件
    public static final int REQ_CAPTURE_IMAGE = 11003; //拍照
    public static final int REQ_CROP_IMAGE = 11004; //截图
    public static final int REQ_SELECT_PHOTO = 11005; //选择相片
    public static final int REQ_LINE_LOGIN = 11006; //line 登陆

    // 权限码
    public static final int REQ_PERM_CAMERA = 12001; // 摄像头权限
    public static final int REQ_PERM_PHOTO = 12002; // 相册权限
    public static final int REQ_PERM_SMS = 12003; // 相册权限

    // line channel id
    public static final String LINE_CHANNEL_ID = "1653391901";
    public static final String INFORM_CHANNEL_ID = "xsg.lychee.richalpha";


    public static final String INTENT_EXTRA_KEY_QR_SCAN = "qr_scan_result";
    public static final String INTENT_EXTRA_KEY_QR_HEAD = "qr_head_url";
    public static final String INTENT_EXTRA_KEY_QR_DESCRIPTION = "qr_description";

    // 網路類型
    public static final int NETWORK_TYPE_UNKNOWN = -1;
    public static final int NETWORK_TYPE_MOBILE = 0;
    public static final int NETWORK_TYPE_WIFI = 1;
}
