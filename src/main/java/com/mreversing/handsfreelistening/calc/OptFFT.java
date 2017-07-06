package com.mreversing.handsfreelistening.calc;

import static com.mreversing.handsfreelistening.calc.FFT.fft;

/**
 * Created by mreversing on 2017/7/5.
 */
public class OptFFT {

    /**
     * 显示频谱时进行FFT计算
     *
     * @param buf        不定长的源数据，方法中自动整合到2的n次方以对齐FFT算法
     * @param samplerate 采样率
     */
    public static double OptFFTf(short[] buf, double samplerate) {
        // 八分频(相当于降低了8倍采样率)，这样1024缓存区中的fft频率密度就越大，有利于取低频。但修改后先不管分频的事

        int FFT_SIZE=0;

        if (buf.length == 0) {
            //没有数据，报错
        }
        int n2Pow = 1, n = 0;
        while (buf.length > n2Pow) {
            n++;
            n2Pow = (int) Math.pow(2, n);
        }
        FFT_SIZE=n2Pow;//传递给FFT方法的数组长度
        Complex[] cpFFT=new Complex[FFT_SIZE];
        for (int i = 0; i < FFT_SIZE; i++) {
             if(i>=buf.length){
                //多余空间，置0
//                 cpFFT[i].setre(0);
                 cpFFT[i]=new Complex(0,0);
             }else{
//                 cpFFT[i].setre(buf[i]);
                 cpFFT[i]=new Complex(buf[i],0);
             }
        }
        Complex[] cpFFTresult=new Complex[FFT_SIZE];
        cpFFTresult=fft(cpFFT);//返回FFT结果

        double F = 1204;//需要求的频率
        int Nf=(int)(F*FFT_SIZE/samplerate);//频率对应的n值

        double model;//幅度
        model = 2 * cpFFTresult[Nf].abs() / FFT_SIZE;// 计算电频最大值，Math.hypot(x,y)返回sqrt(x2+y2)，最高电频
        //该点的模值除以N/2就是对应该频率下的信号的幅度（对于直流信号是除以N)

        double[] m2=new double[200];
        for(int j=800;j<1000;j++){
            m2[j-800] = 2 * cpFFTresult[Nf].abs() / FFT_SIZE;
        }

        return model;




    }

    public static String OptFFTftest(short[] buf, double samplerate) {
        // 八分频(相当于降低了8倍采样率)，这样1024缓存区中的fft频率密度就越大，有利于取低频。但修改后先不管分频的事

        int FFT_SIZE=0;

        if (buf.length == 0) {
            //没有数据，报错
        }
        int n2Pow = 1, n = 0;
        while (buf.length > n2Pow) {
            n++;
            n2Pow = (int) Math.pow(2, n);
        }
        FFT_SIZE=n2Pow;//传递给FFT方法的数组长度
        Complex[] cpFFT=new Complex[FFT_SIZE];
        for (int i = 0; i < FFT_SIZE; i++) {
            if(i>=buf.length){
                //多余空间，置0
//                 cpFFT[i].setre(0);
                cpFFT[i]=new Complex(0,0);
            }else{
//                 cpFFT[i].setre(buf[i]);
                cpFFT[i]=new Complex(buf[i],0);
            }
        }
        Complex[] cpFFTresult=new Complex[FFT_SIZE];
        cpFFTresult=fft(cpFFT);//返回FFT结果

//        double F = 1204;//需要求的频率
//        int Nf=(int)(F*FFT_SIZE/samplerate);//频率对应的n值
//
//        double model;//幅度
//        model = 2 * cpFFTresult[Nf].abs() / FFT_SIZE;// 计算电频最大值，Math.hypot(x,y)返回sqrt(x2+y2)，最高电频
        //该点的模值除以N/2就是对应该频率下的信号的幅度（对于直流信号是除以N)

//        double[] m2=new double[200];
//        for(int j=800;j<1000;j++){
//            m2[j-800] = 2 * cpFFTresult[Nf].abs() / FFT_SIZE;
//        }

//        return model;
        double[] fn =new double[]{0,20,30,40,50,75,100,150,200,350,500,1000,2000,5000,10000,15000,20000};
        String str="";
        for (double ff:
                fn) {
            double F = ff;//需要求的频率
            int Nf=(int)(F*FFT_SIZE/samplerate);//频率对应的n值
            double model;//幅度
            model = 2 * cpFFTresult[Nf].abs() / FFT_SIZE;// 计算电频最大值，Math.hypot(x,y)返回sqrt(x2+y2)，最高电频
            str+=" "+(int)F+":"+model+"/";
        }
        return str;



    }


}
