package com.mreversing.handsfreelistening;

import android.util.Log;

import com.mreversing.handsfreelistening.Utils.myPcmReader;
import com.mreversing.handsfreelistening.calc.OptFFT;

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

            for (int j = 0; j < adata.length; j++) {
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

    int AnalyseCount = 0;//分析次数计数

    private int simulateAnalyse(short[] data) {
        AnalyseCount++;
        OptFFT op = new OptFFT(data, 44100);
        op.Calc_FFT();
//            op.Calc_Filter2();
//            op.Calc_iFFT();
//
//            if (new mySimilarityAlgorithm().xzifftcheck(op.getifftResult()) > 80) {
//                count++;
//            }
        double max = 0;
        int maxIndex = 0;
        for (int i = 1; i < op.Calc_FFT_Size/2; i++) { //这里除以Calc_FFT_Size/2是因为20kHz以上的声音分析不出来（44100/2）
            if (max < op.getModelfromN(i)) {
                max = op.getModelfromN(i);
                maxIndex = i;
            }
        }
        int maxF = op.getFfromN(maxIndex);

        int ZeroCount = 0;
        for (int i = 1; i < data.length; i++) {
            if (data[i] > 0 & data[i - 1] < 0 | data[i] < 0 & data[i - 1] > 0) {
                ZeroCount++;
            }
        }

        int EnergyCount = 0;
        long temp = 0;
        for (int i = 0; i < data.length; i++) {
            temp += Math.abs(data[i]);
            EnergyCount = (int) temp / 1024;
        }
        Boolean Bingo= AnalyseTrigger(maxF,ZeroCount,EnergyCount);
        try {
            if(Bingo){
                osw.write(AnalyseCount + ": " + maxF + " | " + ZeroCount + " | " + EnergyCount + " √\n");
            }else {
                osw.write(AnalyseCount + ": " + maxF + " | " + ZeroCount + " | " + EnergyCount + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }


    int[][] Trigger;//储存20帧数据
    int TriggerCount=0;
    private Boolean AnalyseTrigger(int maxF,int ZeroCount,int EnergyCount) {
        //检测语音帧的触发器，缓存20个分析后的数据，也就是20*1024/44100=0.4643990929705215s的数据作为整体分析
        if(Trigger==null){
            Trigger=new int[20][3];
        }
         if(TriggerCount==20){
             //20个储存满了
             //先算Energy>30的连续超过12个
             //再算其中12个ZeroCount>300且<500
             //12个maxF>4500

             //向前移一位

             for (int i = 0; i < 19; i++) {
                 Trigger[i][0]=Trigger[i+1][0];
                 Trigger[i][1]=Trigger[i+1][1];
                 Trigger[i][2]=Trigger[i+1][2];
             }
             Trigger[19][0]=maxF;
             Trigger[19][1]=ZeroCount;
             Trigger[19][2]=EnergyCount;

             int BeginIndex=-1;

             for (int i = 0; i < 8; i++) {
                 for (int j = 0; j < 12; j++) {
                     if (Trigger[i + j][2] < 30) {
                         return false;
                     }
                     if(j==12){
                         BeginIndex=i;
                     }
                 }
                 if(BeginIndex>=0){
                    break;
                 }
             }
             if(BeginIndex>=0){
                 for (int j = BeginIndex; j < BeginIndex+12; j++) {
                     if (Trigger[j][1] < 300 & Trigger[j][1]>500) {
                         return false;
                     }
                 }
             }
             for (int j = BeginIndex; j < BeginIndex+12; j++) {
                 if (Trigger[j][0] < 4500) {
                     return false;
                 }
             }

             return true;
         }else{
             Trigger[TriggerCount][0]=maxF;
             Trigger[TriggerCount][1]=ZeroCount;
             Trigger[TriggerCount][2]=EnergyCount;
             TriggerCount++;}
        return false;
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