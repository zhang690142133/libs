package xsg.lychee.richalpha.voiceSDK;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import xsg.lychee.nativebridge.JavaJsBridge;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class VoiceRecorderThread extends Thread{
    static{
        System.loadLibrary("lameSDK");
    }
    // 采样位数, 8, 16, Web仅支持16位,android仅支持16位
    private  int bit = 16;
    // 采样率, 8,000 Hz - 电话所用采样率
    private int sampleRate = 8000;
    // 通道数(1-2)
    private int numChannels = 2;
    private String file;
    private String AudioFileName;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    private AudioRecord audioRecord=null;
    private byte[] mp3data=null;
    private byte[] rawdata=null;
    private boolean needWrite = true;
    /**
     * 线程通讯的handler
     */
    public  VoiceRecorderThread(int bit,int sampleRate,int numChannels,String file){
        this.bit = bit;
        this.sampleRate = sampleRate;
        this.numChannels = numChannels;
        audioRecord = createAudioRecord(file);
    }

    /**
     * 创建 createAudioRecord
     * @param file
     * @return
     */
    private AudioRecord createAudioRecord(String file){
        this.file = file;
        // 获取音频文件路径
        AudioFileName = file.substring(0, file.lastIndexOf("/")) + "/test.raw";
        int pcmBit = bit == 8 ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;
        int channel = numChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate, channel, pcmBit);
        LameSDK.init(sampleRate,numChannels,sampleRate,bit);
        return new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, pcmBit, bufferSizeInBytes);
    }
    @Override
    public void run() {
        super.run();
        Log.d("VoiceRecorder","开始录音");
        audioRecord.startRecording();
        dealAudioData();
        if(needWrite){
            writeDataToFile();
        }
    }
    /**
     * 录音数据处理
     */
    private void dealAudioData(){
        rawdata = new byte[bufferSizeInBytes];
        mp3data = new byte[sampleRate/20+7200];
        long dataLength = 0;
        while(VoiceManage.isRecording){
            try{
                dataLength  = audioRecord.read(rawdata, 0, bufferSizeInBytes);
                if(AudioRecord.ERROR_INVALID_OPERATION != dataLength){
                    if(VoiceManage.needCollectVolume){
                        long volume = VoiceTools.getVolume(rawdata,dataLength);
                        VoiceManage.getInstance().sendVolume(volume);
                    }
                    if(VoiceManage.isEncodeWhenRecording){//边录边转
                        LameSDK.encode();
                    }
                }
                //Thread.sleep(100);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        needWrite = false;
    }


    /**
     * 将声音数据写入文件
     */
    private void writeDataToFile(){

    }
    /**
     * 结束
     */
    public void close(){
        needWrite = true;
        releseAudioRecord();
    }

    /**
     * 取消
     */
    public void cancel(){
        needWrite = false;
        releseAudioRecord();
    }
    /**
     * 释放audioRecord
     */
    private void releseAudioRecord(){
        if(audioRecord!=null){
            audioRecord.stop();
            audioRecord.release();//释放资源
            audioRecord = null;
        }
        VoiceManage.getInstance().setRecordState(false);
    }

}

