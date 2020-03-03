package xsg.lychee.richalpha.voiceSDK;

import org.json.JSONObject;

import xsg.lychee.nativebridge.BridgeEnum;
import xsg.lychee.nativebridge.JavaJsBridge;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 语音管理
 */
public class VoiceManage {
    static{
        System.loadLibrary("lameSDK");
    }
    private static VoiceRecorderThread voiceRecorder = null;
    private static VoiceManage mInstance;
    public synchronized static VoiceManage getInstance()
    {
        if(mInstance == null){
            mInstance = new VoiceManage();
        }
        return mInstance;
    }
    //是否边录边转码mp3
    public static boolean isEncodeWhenRecording = false;
    // 采样位数, 8, 16, Web仅支持16位,android仅支持16位
    private  int bit = 16;
    // 采样率, 8,000 Hz - 电话所用采样率
    private int sampleRate = 8000;
    // 通道数(1-2)
    private int numChannels = 2;
    // 是否需要采集音量
    public static boolean needCollectVolume = false;
    // 是否正在录音
    public static boolean isRecording = false;
    // 是否要保存
    public static boolean needSave = false;
    /**
     * 对外发送语音音量
     * @param volume
     */
    public void sendVolume(long volume){
        if(needCollectVolume){
            Log.d("VoiceManage","音量大小："+Long.toString(volume));
            JavaJsBridge.notifyEventToJs(BridgeEnum.RECORDER_VOLUME,Long.toString(volume));
        }
    }
    /**
     * 设置录音参数
     * @param config
     */
    public void setRecordConfig(String config) {
        try {
            JSONObject obj = new JSONObject(config);

            if (obj.has("sampleRate")){
                sampleRate = obj.getInt("sampleRate");
            }

            if (obj.has("numChannels")) {
                numChannels = obj.getInt("numChannels");
                if (numChannels != 1 && numChannels!= 2) {
                    numChannels = 2;
                }
            }
            if (obj.has("needCollectVolume")) {
                VoiceManage.needCollectVolume = obj.getBoolean("needCollectVolume");
            }
            if (obj.has("isEncodeWhenRecording")) {
                VoiceManage.isEncodeWhenRecording = obj.getBoolean("isEncodeWhenRecording");
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 开始录音
     * @param file
     */
    public boolean startRecord(String file){
        Log.d("VoiceManage","开始录音");
        if(!VoiceTools.checkPermission()){//未获取权限
            Log.d("VoiceManage","未获取录音权限");
            return false;
        }
        if(VoiceManage.isRecording){
            Log.d("VoiceManage","正在录音中");
            return false;
        }
        new VoiceRecorderThread(bit,sampleRate,numChannels,file).start();
        VoiceManage.isRecording = true;
        return true;
    }
    /**
     * 停止录音
     */
    public void stopRecord(){
        Log.d("VoiceManage","停止录音 "+isRecording);
        VoiceManage.isRecording=false;
        VoiceManage.needSave=true;
    }

    /**
     * 取消录音
     */
    public void cancelRecord(){
        Log.d("VoiceManage","取消录音 "+isRecording);
        VoiceManage.isRecording=false;
        VoiceManage.needSave=false;
    }


}
