package xsg.lychee.richalpha.voiceSDK;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.cocos2dx.lib.Cocos2dxHelper;

public class VoiceTools {
    /**
     * 录音权限检查
     * @return
     */
    public static boolean checkPermission() {
        final int PERMISSION_REQUEST_CODE = 9001;

        // android 6.0开始需要检查
        if (Build.VERSION.SDK_INT >= 23) {
            int permisson = ContextCompat.checkSelfPermission(Cocos2dxHelper.getActivity(), Manifest.permission.RECORD_AUDIO);

            if (permisson != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Cocos2dxHelper.getActivity(),
                        new String[] {Manifest.permission.RECORD_AUDIO},
                        PERMISSION_REQUEST_CODE);
            }

            return permisson == PackageManager.PERMISSION_GRANTED ? true : false;
        }

        return true;
    }
    /**
     * 音量计算
     */
    public static long getVolume(byte[] audiodata,long length){
        long v = 0;
        for (int i = 0; i < audiodata.length; i++) {
            v += audiodata[i] * audiodata[i];
        }
        double mean = v / (double) length;
        //long volume = Math.round(mean);
        //long volume=(long) Math.sqrt(mean);
        long volume = Math.round(10 * Math.log10(mean));
        return volume;
    }
}
