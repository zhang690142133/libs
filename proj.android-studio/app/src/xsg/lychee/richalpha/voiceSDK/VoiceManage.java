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
    private static VoiceRecorder voiceRecorder = null;
    private static VoiceManage mInstance;
    public synchronized static VoiceManage getInstance()
    {
        if(mInstance == null){
            mInstance = new VoiceManage();
        }
        return mInstance;
    }
    // 采样位数, 8, 16, Web仅支持16位,android仅支持16位
    private  int bit = 16;
    // 采样率, 8,000 Hz - 电话所用采样率
    private int sampleRate = 8000;
    // 通道数(1-2)
    private int numChannels = 2;
    // 是否需要采集音量
    private  boolean needCollectVolume = false;
    // 是否正在录音
    private  boolean isRecording = false;
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
                needCollectVolume = obj.getBoolean("needCollectVolume");
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置录音状态
     * @param state
     */
    public void setRecordState(boolean state){
        isRecording = state;
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
        if(isRecording){
            Log.d("VoiceManage","正在录音中");
            return false;
        }
        voiceRecorder = new VoiceRecorder(bit,sampleRate,numChannels,file);
        voiceRecorder.start();
        return true;
    }
    /**
     * 停止录音
     */
    public void stopRecord(){
        Log.d("VoiceManage","停止录音 "+isRecording);
        if(isRecording){
            voiceRecorder.close();
        }
        voiceRecorder = null;
    }

    /**
     * 取消录音
     */
    public void cancelRecord(){
        Log.d("VoiceManage","取消录音 "+isRecording);
        if(isRecording){
            voiceRecorder.cancel();
        }
        voiceRecorder = null;
    }


}
