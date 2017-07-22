package com.mreversing.handsfreelistening;


import com.mreversing.handsfreelistening.calc.Complex;
import com.mreversing.handsfreelistening.calc.FFT;

public class mySimilarityAlgorithm {
    //定义响度上限下限，一音频帧中平均值在这个区间内才开始识别是否有命令。16位为2字节为short。这应该通过不同设备预录音得到
    //实测16bit最大值为32767
    public short _volumeTop=30000;
    public short _volumeBottom=10000;

    public int _samplerate=44100;

    //根据位宽和采样率计算。采样率乘以声道数乘以位宽为一秒的数据量
    //一帧“音频帧”（Frame）的大小 = 采样率 x 位宽 x 采样时间 x 通道数
    //int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat);
    public static double getsampletime( int MinBufferSize,int bits,int samplerate){
        //zukz1 采样时间为0.0406349206349206s   44100Hz下每帧1792个2字节数据 MinBufferSize为3584
        //zukz1 8bit录制时为unsigned？以0x80(-128)及0x7f(127)为基准(静音)，排除掉这两个就好了
        //bit是1的话就是8位，2就16位
        return MinBufferSize/bits/samplerate;
    }

//    public mySimilarityAlgorithm(byte[] Targetdata,byte[] Origindata,int bufferSizeInBytes){
//
//    }



    public int xzifftcheck(short[] data){
        int checklist[] = {-203, -74, -30, 30, 75, 117, 174, 246};//相对最高点位置
        int checkpointabs[] = {61, 159, 452, 581, 308, 70, 191, 108};//幅值
        int checkmax = 594;
        int len = data.length;
        int lencheck = checklist.length;
        int temp = 0;
        int maxpoint = 0;
        short max = 0;
//findMaxad
        for (int i = 0; i < len; i++) {
            if (temp < data[i]) {
                temp = data[i];
                maxpoint = i;
            }
        }
        if (maxpoint < -checklist[0]+2 | maxpoint > checklist[lencheck - 1]+2) {
            return 0;
            //保证能取到
        }
        max = data[maxpoint];
        double times = max / checkmax;//倍数检查
        double times0 = 0;
        int count0 = 0;
        for (int i = 0; i < lencheck; i++) {
            times0 = changemax(data, checklist[i] + maxpoint) / checkpointabs[i];
            times0 = times0 / times;
            if (times0 > 1.02 | times0 < 0.98) {

            } else {
                count0++;
            }
        }
        return (int) (100 * count0 / lencheck);
    }

    private short changemax(short[] data, int ad) {
        //5个点取最大值
        short temp = 0;
        int i0 = 0;
        for (int i = -2; i < 3; i++) {
            if (temp < data[ad + i]) {
                temp = data[ad + i];
            }
        }
        return temp;
    }



    /**
     * 查找最大值
     *
     * @param data
     * @return
     */
    public static short findMax(short[] data) {
        short max = data[0];
        for (int i = 0; i < data.length; i++) {
            if (max < Math.abs(data[i])) {
                max = (short) Math.abs(data[i]);
            }
        }
        System.out.println("max :  " + max);
        return max;
    }

    public static short findMin(short[] data) {
        short min = data[0];
        for (int i = 0; i < data.length; i++) {
            if (min > data[i]) {
                min = data[i];
            }
        }
        System.out.println("min :  " + min);
        return min;
    }
    public static short findMaxNoabs(short[] data) {
        short max = data[0];
        for (int i = 0; i < data.length; i++) {
            if (max < data[i]) {
                max = data[i];
            }
        }
        return max;
    }

    /**
     * 查找最大值
     *
     * @param data
     * @return
     */
    public static double findMax(double[] data,Boolean returnIndex) {
        double max = data[0];
        int index=0;

        for (int i = 0; i < data.length; i++) {
            if (max < Math.abs(data[i])) {
                max = Math.abs(data[i]);
                index=i;
            }
        }
        System.out.println("max :  " + max);

        return  returnIndex? max:index;
    }
}
