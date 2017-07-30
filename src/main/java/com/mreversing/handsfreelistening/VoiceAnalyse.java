package com.mreversing.handsfreelistening;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.mreversing.handsfreelistening.calc.VoiceFeatures;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mreversing on 2017/7/22.
 */
public class VoiceAnalyse extends Thread {

    public static int BufferLength=8; //对判断有较大影响，为6适中
    public static int spaceLength=20; //中间隔多少帧不算第二次，是两次识别的间隔帧数，事实间隔帧数为spaceLength+BufferLength
    int RevCount = 0;
    private AtomicBoolean mQuit = new AtomicBoolean(false);

    public VoiceAnalyse(Handler MainHandler) {
        this.handler = MainHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        vaHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 125701: //录音线程发来1024个数据
                        short[] data = (short[]) msg.obj;
//                    Log.e("VoiceAnalyse", "Data Recevied!!!");
//                      Log.e("VoiceAnalyse", "currentThread: "+Thread.currentThread().getName());
                        RevCount++;
                        Analyse(data);
                        break;
                    default:
                        break;
                }
            }
        };
        Looper.loop();
        //从这句话后就不再执行
        while (!mQuit.get()) {}
//        Log.e("VoiceAnalyse", "VoiceAnalyse stopped!!!");
    }

    private Handler handler;//发送识别成功信息到此Handler
    public Handler vaHandler;


    int realTriggerCount = 0;
    int tempReCount = 1-spaceLength;
    private void Analyse(short[] data) {
        if (RevCount - tempReCount > spaceLength) {
            Boolean Bingo = AnalyseTrigger(data);
            if (Bingo) {
                realTriggerCount++;
                tempReCount = RevCount;
                TriggerCount = 0;

                //以下为发消息给MainActivity
                Message msg;
                msg = Message.obtain();
                msg.obj = Integer.toString(RevCount);
                msg.what = 125805;
                handler.sendMessage(msg);
            }
        } else {
            //间隔期，不计算
        }
//        Log.e("VoiceAnalyse", "count: "+RevCount);
    }

    int TriggerCount = 0;
    VoiceFeatures[] vf;
    private Boolean AnalyseTrigger(short[] data) {
        //检测语音帧的触发器，缓存20个分析后的数据，也就是20*1024/44100=0.4643990929705215s的数据作为整体分析

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

        if (TriggerCount == BufferLength-1) {

            for (int i = 0; i < BufferLength; i++) {
                if (vf[i].Energy < 10) {
                    return false;
                }
            }
            for (int i = 0; i < BufferLength; i++) {
                if (vf[i].Calc_Zero() < 290 | vf[i].Zero > 600) {
                    return false;
                }
            }
            Log.e("VoiceAnalyse", "Ready!!" + RevCount);

            vf[BufferLength-1].Calc_VoicePeaks();
            int nums=vf[BufferLength-1].peakstofound*BufferLength;
            int[] numbers=new int[nums];
            for (int i = 0; i < BufferLength; i++) {
                if(vf[i].peaks==null){
                    break;
                }
                //综合缓存的几帧数据看，在置信区间外的峰太多就置否
                for(int j=0;j<vf[BufferLength-1].peakstofound;j++){
                    numbers[i*vf[BufferLength-1].peakstofound+j]=vf[i].peaks[j];
                }
            }
            if(Calc_PercentInFreqs(numbers)<90){
                //强烈的嘶声这里大于95过不去，估计低频太多
                return false;
            }
            //TODO: 再加一个方法，对嘶声频率作进一步群体判断


            Log.e("VoiceAnalyse", "Analyse Bingo!!" + RevCount);

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

    /**
     * stop task
     */
    public void quit() {
        mQuit.set(true);
//        Log.e("VoiceAnalyse", "mQuit was set true!!");
    }

}
