package com.android.king.intercom.input;

import android.media.AudioRecord;
import android.os.Handler;
import android.util.Log;

import com.android.king.intercom.data.AudioData;
import com.android.king.intercom.data.MessageQueue;
import com.android.king.intercom.job.JobHandler;
import com.android.king.intercom.util.Constants;

/**
 * 音频录制数据格式ENCODING_PCM_16BIT，返回数据类型为short[]
 *
 * @author yanghao1
 */
public class Recorder extends JobHandler {

    private AudioRecord audioRecord;
    // 音频大小
    private int inAudioBufferSize;
    // 录音标志
    private boolean isRecording = false;

    public Recorder(Handler handler) {
        super(handler);
        // 获取音频数据缓冲段大小
        inAudioBufferSize = AudioRecord.getMinBufferSize(
                Constants.sampleRateInHz, Constants.inputChannelConfig, Constants.audioFormat);
        // 初始化音频录制
        audioRecord = new AudioRecord(Constants.audioSource,
                Constants.sampleRateInHz, Constants.inputChannelConfig, Constants.audioFormat, inAudioBufferSize);
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    @Override
    public void run() {
        while (isRecording) {
            Log.d("Recorder", audioRecord.getRecordingState() + "");
            // 启动录音
            if (audioRecord.getRecordingState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.startRecording();
            }
            // 实例化音频数据缓冲
            short[] rawData = new short[inAudioBufferSize];
            audioRecord.read(rawData, 0, inAudioBufferSize);
            AudioData audioData = new AudioData(rawData);
            MessageQueue.getInstance(MessageQueue.ENCODER_DATA_QUEUE).put(audioData);
        }
    }

    @Override
    public void free() {
        // 释放音频录制资源
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
    }
}
