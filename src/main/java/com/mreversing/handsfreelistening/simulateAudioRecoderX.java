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

           for (int j = 0; j < adata.length; j++) {//44100Hz
           // for (int j = 0; j < adata.length; j+=16) { //44100/8Hz
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

	public static int BufferLength=8;
    int AnalyseCount = 0;//分析次数计数

    private int simulateAnalyse(short[] data) {
        AnalyseCount++;

        Boolean Bingo = AnalyseTrigger(data);
        String str = "", str0 = "", str1 = "";

        //str+=AnalyseCount+": ";
        str += vf[BufferLength - 1].maxFreq + " | " + vf[BufferLength - 1].Zero + " | " + vf[BufferLength - 1].Energy + " _ ";
        //正常输出44100Hz的峰
        int[] peaks=vf[BufferLength-1].peaks;
        for(int i=0;i<vf[BufferLength-1].peakstofound;i++){
            str+=" | " + peaks[i];
        }

//        double[] mA = vf[BufferLength - 1].Calc_AllFreq();
//        for (int i = 0; i < mA.length; i++) {
//            str1 += " | " + (int) mA[i];
//        }
        str+=" | ";
        str+=vf[BufferLength-1].modelsPercent+"%";

        str1 += " | _ ";
        if (Bingo) {
            str0 = "√";
        } else {
            str0 = "×";
        }

        try {
            osw.write(str + str1 + str0 + "\n");
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
		vf[BufferLength-1] = new VoiceFeatures(data,AudioRecoderX.sampleRateInHz);
//        vf[BufferLength-1] = new VoiceFeatures(data,AudioRecoderX.sampleRateInHz/8);
        vf[BufferLength-1].Calc_Energy();
		vf[BufferLength-1].Calc_Zero();
		vf[BufferLength-1].Calc_MaxFrequency();
        vf[BufferLength-1].Calc_modelsumfromFs(7500, 11800);
        vf[BufferLength-1].Calc_modelsPercentfromFs(7500, 11800);
        vf[BufferLength-1].Calc_VoicePeaks();

        if (TriggerCount == BufferLength-1) {

            for (int i = 0; i < BufferLength; i++) {
                if (vf[i].Energy < 9) {
                    return false;
                }
            }
            for (int i = 0; i < BufferLength; i++) {
                if (vf[i].Zero < 220 | vf[i].Zero > 600) {
                    return false;
                }
            }

            for (int i = 0; i < BufferLength; i++) {
                if (vf[i].modelsum < 30) {
                    return false;
                }
            }
            for (int i = 0; i < BufferLength; i++) {
                if (vf[i].modelsPercent < 37 | vf[i].modelsPercent > 90) {
                    return false;
                }
            }

            int nums=vf[BufferLength-1].peakstofound*BufferLength;
            int[] numbers=new int[nums];
            for (int i = 0; i < BufferLength; i++) {
                //综合缓存的几帧数据看，在置信区间外的峰太多就置否
                for(int j=0;j<vf[BufferLength-1].peakstofound;j++){
                   numbers[i*vf[BufferLength-1].peakstofound+j]=vf[i].peaks[j];
                }
            }
            if(Calc_PercentInFreqs(numbers)<80){
                return false;
            }

//            Log.e("VoiceAnalyse", "Analyse Bingo" + RevCount);
            return true;
        } else {
            TriggerCount++;
        }
        return false;
    }

    private int Calc_PercentInFreqs(int[] data){
        int counts=0;
        int flag=0;
        for(int i=0;i<data.length;i++){
            if (data[i]>4600 & data[i]<13510){
                counts++;
                continue;
            }
            else if (data[i]>13500 & data[i]<15500){
                counts++;
                continue;
            }else if (data[i]<220) {
                flag++;
            }
        }
        if(flag==data.length){
            return 0;
        }
        return counts*100/(data.length-flag);
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
