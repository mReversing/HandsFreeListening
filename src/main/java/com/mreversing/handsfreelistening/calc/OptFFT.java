package com.mreversing.handsfreelistening.calc;

import static com.mreversing.handsfreelistening.calc.FFT.fft;

/**
 * Created by mreversing on 2017/7/5.
 */
public class OptFFT {

    short[] data_buffer;//源数据
    double data_samplerate;//源数据采样率

    int Calc_FFT_Size;//调整的FFT_Size，2的n次方
    int Calc_FFT_N;//上面那个n
    Complex[] Calc_cpFFT;
    Complex[] Calc_cpFFT_Result;

    public OptFFT(short[] buf, double samplerate){
        data_buffer=buf;
        data_samplerate=samplerate;
    }

    public void startCalc(){
        if (data_buffer.length == 0) {
            //没有数据，报错
        }
        int n2Pow = 1, n = 0;
        while (data_buffer.length > n2Pow) {
            n++;
            n2Pow = (int) Math.pow(2, n);
        }
        Calc_FFT_Size=n2Pow;//传递给FFT方法的数组长度
        Calc_FFT_N=n;

        Calc_cpFFT=new Complex[Calc_FFT_Size];
        for (int i = 0; i < Calc_FFT_Size; i++) {
            if(i>=data_buffer.length){
                //多余空间，置0
                Calc_cpFFT[i]=new Complex(0,0);
            }else{
                Calc_cpFFT[i]=new Complex(data_buffer[i],0);
            }
        }
        Calc_cpFFT_Result=fft(Calc_cpFFT);//返回FFT结果
    }

    public int getNfromF(double F){
        return (int)(F*Calc_FFT_Size/data_samplerate);//频率对应的n值
    }

    public double getModelfromN(int N){
        return  2 * Calc_cpFFT_Result[N].abs() / Calc_FFT_Size;// 计算电频最大值，Math.hypot(x,y)返回sqrt(x2+y2)，最高电频

    }


    /**
     * 显示频谱时进行FFT计算
     *
     * @param buf        不定长的源数据，方法中自动整合到2的n次方以对齐FFT算法
     * @param samplerate 采样率
     */
    public static double OptFFTf(short[] buf, double samplerate) {
        // 八分频(相当于降低了8倍采样率)，这样1024缓存区中的fft频率密度就越大，有利于取低频。但修改后先不管分频的事
        //经测试，采样数据多的情况下fft算法比较慢

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

        double[] m2=new double[20];
       int count=0;
        for(int j=2850;j<3050;j+=10){
            m2[(j-2850)/10] = 2 * cpFFTresult[Nf].abs() / FFT_SIZE;
            if(m2[(j-2850)/20]>100){
                count++;
            }
        }
//        for(int j=120;j<140;j++){
//            m2[j-120] = 2 * cpFFTresult[Nf].abs() / FFT_SIZE;
//            if(m2[j-120]>2000){
//                count++;
//            }
//        }

        return count;




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
//        double[] fn =new double[]{0,20,30,40,50,75,100,150,200,350,500,1000,2000,5000,10000,15000,20000};
        double[] fn =new double[]{50,100,200,350,500,1000,1100,1200,1250,1300,1350,1400,1450,1500,1750,2000,2300,2400,2500,2550,2600,2650,2700,2750,2800,2850,2900,2950,3000,4000,5000,10000,15000,20000,
        2750,2760,2770,2780,2790,2800,2810,2820,2830,2840,2850,2860,2870,3000,3250,3500,3750,4000,4250,4500 };
        String str="";
        for (double ff:
                fn) {
            double F = ff;//需要求的频率
            int Nf=(int)(F*FFT_SIZE/samplerate);//频率对应的n值
            double model;//幅度
            model = 2 * cpFFTresult[Nf].abs() / FFT_SIZE;// 计算电频最大值，Math.hypot(x,y)返回sqrt(x2+y2)，最高电频
//            model =cpFFTresult[Nf].abs();// 计算电频最大值，Math.hypot(x,y)返回sqrt(x2+y2)，最高电频
            //经多组数据测试，为了进行FFT置零的值影响到了cpFFTresult[Nf].abs()但对除以FFT_SIZE后的影响未知
            //数据越少模值越大
            str+=" "+(int)F+":"+model+"/";
        }
        return str;



    }


}
