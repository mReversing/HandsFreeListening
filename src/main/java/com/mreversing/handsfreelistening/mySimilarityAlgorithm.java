package com.mreversing.handsfreelistening;

/**
 * Created by zhxh on 17/4/9.
 */



public class mySimilarityAlgorithm {
    //定义响度上限下限，一音频帧中平均值在这个区间内才开始识别是否有命令。16位为2字节为short。这应该通过不同设备预录音得到
    //实测16bit最大值为32767
    public short volumeTop=30000;
    public short volumeBottom=10000;

    public int samplerate=44100;

    //根据位宽和采样率计算。采样率乘以声道数乘以位宽为一秒的数据量
    //一帧“音频帧”（Frame）的大小 = 采样率 x 位宽 x 采样时间 x 通道数
    //int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat);
    public int getsampletime(){
        //zukz1 采样时间为0.04s   44100Hz下每帧1764个2字节数据
        //zukz1 8bit录制时为unsigned,以0x80(-128)及0x7f(127)为基准(静音)，排除掉这两个就好了
        return 40;
    }

    public mySimilarityAlgorithm(byte[] Targetdata,byte[] Origindata,int bufferSizeInBytes){

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
    public static double findMax(double[] data) {
        double max = data[0];
        for (int i = 0; i < data.length; i++) {
            if (max < Math.abs(data[i])) {
                max = Math.abs(data[i]);
            }
        }
        System.out.println("max :  " + max);
        return max;
    }
}
