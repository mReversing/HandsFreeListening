package com.mreversing.handsfreelistening.Utils;

import com.mreversing.handsfreelistening.AudioRecoderX;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mreversing on 2017/7/5.
 */
public class PcmReader {

    private String filename = "文件";

    private int len = 0;

    private String chunkdescriptor = null;
    static private int lenchunkdescriptor = 4;

    private long chunksize = 0;
    static private int lenchunksize = 4;

    private String waveflag = null;
    static private int lenwaveflag = 4;

    private String fmtubchunk = null;
    static private int lenfmtubchunk = 4;

    private long subchunk1size = 0;
    static private int lensubchunk1size = 4;

    private int audioformat = 0;
    static private int lenaudioformat = 2;

    private int numchannels = 1;
    static private int lennumchannels = 2;

    private long samplerate = AudioRecoderX.sampleRateInHz;
    static private int lensamplerate = 2;

    private long byterate = 0;
    static private int lenbyterate = 4;

    private int blockalign = 0;
    static private int lenblockling = 2;

    private int bitspersample = 16;
//    private int bitspersample = AudioRecoderX.audioFormat;
    static private int lenbitspersample = 2;

    private String datasubchunk = null;
    static private int lendatasubchunk = 4;

    private long subchunk2size = 0;

    private InputStream bis = null;

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

    // 获取数据长度，也就是一共采样多少个
    public int getDataLen() {
        return this.len;
    }

    public void initReader(InputStream inputStream) {
        issuccess = true;

    }
}

