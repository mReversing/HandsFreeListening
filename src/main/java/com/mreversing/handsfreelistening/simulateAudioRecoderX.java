package com.mreversing.handsfreelistening;

import android.provider.MediaStore;
import android.util.Log;

import com.mreversing.handsfreelistening.Utils.myPcmReader;
import com.mreversing.handsfreelistening.calc.OptFFT;
import com.mreversing.handsfreelistening.calc.VoiceFeatures;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by mreversing on 2017/7/8.
 */
public class simulateAudioRecoderX {
    String Path = "";
    String Dir = "";

    public simulateAudioRecoderX(String fPath, String fDir) {
        Path = fPath;
        Dir = fDir;
    }

    public int startSimulate() {
        myPcmReader pr = new myPcmReader();
        short[] data = pr.readPcm(Path);
        initOutputStream();
        int lencircle = data.length / 1792;
        int count = 0;

        short[] bdata = new short[1024];
        int flag = 0;//记录bdata已填充的位置序号
//        int indicator =0;
        for (int i = 0; i < lencircle; i++) {

            short[] adata = new short[1792];
            for (int j = 0; j < 1792; j++) {
                adata[j] = data[j + 1792 * i];
            }

//            for (int j = 0; j < adata.length; j++) {
            for (int j = 0; j < adata.length; j+=8) {
                    bdata[flag] = adata[j];
                if (flag == 1024 - 1) {
                    //已集齐1024个数据
                    count = simulateAnalyse(bdata);
                    bdata = new short[1024];
                    flag = -1;
                }
                flag++;
            }

        }
        if (osw != null) {
            try {
                osw.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

	public static int BufferLength=6;
    int AnalyseCount = 0;//分析次数计数

    private int simulateAnalyse(short[] data) {
        AnalyseCount++;

        Boolean Bingo= AnalyseTrigger(data);
        String str="",str0="",str1="";



        //str+=AnalyseCount+": ";
        str+=vf[BufferLength-1].maxFreq + " | " + vf[BufferLength-1].Zero + " | " + vf[BufferLength-1].Energy +" _ ";
//        int[] peaks=vf[BufferLength-1].peaks;
//        for(int i=0;i<VoiceFeatures.peakstofound;i++){
//            str+=peaks[i]+" | ";
//        }
        OptFFT op = new OptFFT(vf[BufferLength-1].data, AudioRecoderX.sampleRateInHz/8);
        op.Calc_FFT();
        double[] mA =new double[op.Calc_FFT_Size / 2];
        for (int i = 0; i < op.Calc_FFT_Size / 2; i++) {
            mA[i]=op.getModelfromN(i);
        }
        for (int i = 0; i < op.Calc_FFT_Size / 2; i++) {
            str1 += " | " + (int) mA[i];
        }
        str1+="_ ";
//        short[] cdata=new short[128];
//        for(int i=0;i<128;i++){
//            for(int j=0;j<8;j++){
//                cdata[i]+=vf[BufferLength-1].data[i*8+j];
//            }
//            cdata[i]+=vf[BufferLength-1].data[i*8];
//        }
//        VoiceFeatures vf2=new VoiceFeatures(cdata,AudioRecoderX.sampleRateInHz/8/8);
//        vf2.Calc_VoicePeaks();
//        int[] peaks2=vf2.peaks;
//        for(int i=0;i<VoiceFeatures.peakstofound;i++){
//            str1+=peaks2[i]+" | ";
//        }

//        OptFFT op = new OptFFT(cdata, AudioRecoderX.sampleRateInHz/8);
//        op.Calc_FFT();
//        double[] mA =new double[op.Calc_FFT_Size / 2];
//        for (int i = 0; i < op.Calc_FFT_Size / 2; i++) {
//            mA[i]=op.getModelfromN(i);
//        }
//        for (int i = 0; i < op.Calc_FFT_Size / 2; i++) {
//            str1+=" | " +(int)mA[i];
//        }

            if(Bingo){
            str0="√";
        }else {
            str0="×";
        }

        try {
            osw.write(str+ str1 + str0 + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }


    int TriggerCount = 0;
    VoiceFeatures[] vf;
    private Boolean AnalyseTrigger(short[] data) {
        //检测语音帧的触发器，缓存BufferLength个分析后的数据，也就是20*1024/44100=0.4643990929705215s的数据作为整体分析

        if (TriggerCount == 0) {
            vf = new VoiceFeatures[BufferLength];
        }
		
		//向前移一位
		for (int i = 0; i < BufferLength-1; i++) {
			vf[i] = vf[i + 1];
		}
		vf[BufferLength-1] = new VoiceFeatures(data,AudioRecoderX.sampleRateInHz/8);
		vf[BufferLength-1].Calc_Energy();
		vf[BufferLength-1].Calc_Zero();
		vf[BufferLength-1].Calc_MaxFrequency();
        vf[BufferLength-1].Calc_VoicePeaks();

        if (TriggerCount == BufferLength-1) {

            for (int i = 0; i < BufferLength; i++) {
                if (vf[i].Energy < 10) {
                    return false;
                }
            }
            for (int i = 0; i < BufferLength; i++) {
                if (vf[i].Zero < 320 | vf[i].Zero > 560) {
                    return false;
                }
            }

            int nums=VoiceFeatures.peakstofound*BufferLength;
            int[] numbers=new int[nums];
            int flag=0;
            for (int i = 0; i < BufferLength; i++) {
                //综合缓存的几帧数据看，在置信区间外的峰太多就置否
                for(int j=0;j<VoiceFeatures.peakstofound;j++){
                   numbers[i*VoiceFeatures.peakstofound+j]=vf[i].peaks[j];
                }
            }
            if(Calc_CountInFreqs(numbers)*100/nums<80){
                return false;
            }
//            Log.e("VoiceAnalyse", "Analyse Bingo" + RevCount);
            return true;
        } else {
            TriggerCount++;
        }
        return false;
    }

    private int Calc_CountInFreqs(int[] data){
        int counts=0;
        for(int i=0;i<data.length;i++){
            if (data[i]>4600 & data[i]<13510){
                counts++;
                continue;
            }
            else if (data[i]>13500 & data[i]<15500){
                counts++;
                continue;
            }
        }
        return counts;
    }

    OutputStreamWriter osw;
    FileOutputStream fos;
    private void initOutputStream() {
        try {
            File file = new File(Dir + "simulate_result.txt");
            if (file.exists()) {
                file.delete();
//                file.createNewFile();
            } else {
                //file.mkdir();注意要先创建文件夹
                file.createNewFile();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
            osw = new OutputStreamWriter(fos);
//            fos.close();
        } catch (Exception e) {
            Log.e("simulateAudioRecoderX", "File Open Failed!!!!");
            e.printStackTrace();
        }
    }

}
