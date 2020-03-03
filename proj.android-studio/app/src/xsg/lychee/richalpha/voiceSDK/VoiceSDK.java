package xsg.lychee.richalpha.voiceSDK;

public class VoiceSDK {
    // 录音配置 {bit:采样位数, sampleRate:采样率, numChannels:通道数}
    public static void setConfigJs(String config) {
        VoiceManage.getInstance().setRecordConfig(config);
    }

    // 开始录音
    public static boolean startRecordJs(String file) {
        return VoiceManage.getInstance().startRecord(file);
    }
    // 结束录音
    public static void stopRecordJs(){
        VoiceManage.getInstance().stopRecord();
    }

    // 取消录音
    public static void cancelRecordJs(){
        VoiceManage.getInstance().cancelRecord();
    }
}
