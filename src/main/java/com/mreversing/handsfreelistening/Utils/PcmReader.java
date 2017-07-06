package com.mreversing.handsfreelistening.Utils;

import com.mreversing.handsfreelistening.AudioRecoderX;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mreversing on 2017/7/5.
 */
public class PcmReader {

    private String filename = "文件";


    private int audioformat = 0;


    private int numchannels = 1;

    private long samplerate = AudioRecoderX.sampleRateInHz;


    private int bitspersample = 16;
//    private int bitspersample = AudioRecoderX.audioFormat;
    static private int lenbitspersample = 2;


    public boolean issuccess = false;
    // 获取数据
    // 数据是一个二维数组，[n][m]代表第n个声道的第m个采样值

    // 判断是否创建wav读取器成功
    public boolean isSuccess() {
        return issuccess;
    }

    // 获取每个采样的编码长度，8bit或者16bit
    public int getBitPerSample() {
        return this.bitspersample;
    }

    // 获取采样率
    public long getSampleRate() {
        return this.samplerate;
    }

    // 获取声道个数，1代表单声道 2代表立体声
    public int getNumChannels() {
        return this.numchannels;
    }


    public void initReader(InputStream inputStream) {
        issuccess = true;

    }
}

