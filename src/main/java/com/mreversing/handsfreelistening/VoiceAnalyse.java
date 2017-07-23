package com.mreversing.handsfreelistening;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.mreversing.handsfreelistening.calc.OptFFT;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mreversing on 2017/7/22.
 */
public class VoiceAnalyse extends Thread {

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
        while (!mQuit.get()) {
        }

    }

    private Handler handler;//发送识别成功信息到此Handler
    public Handler vaHandler;


    private void Analyse(short[] data) {
        Boolean Bingo = AnalyseTrigger(data);
        if (Bingo) {
            if (RevCount - tempReCount > 50) { //50是两次识别的间隔帧
                realTriggerCount++;
                tempReCount = RevCount;

                //以下为发消息给MainActivity
                Message msg;
                msg = Message.obtain();
                msg.obj = Integer.toString(RevCount);
                msg.what = 125805;
                handler.sendMessage(msg);
            }
        }
    }

    int realTriggerCount = 0;
    int tempReCount = 0;

    int TriggerCount = 0;
    VoiceFeatures[] vf;
    private Boolean AnalyseTrigger(short[] data) {
        //检测语音帧的触发器，缓存20个分析后的数据，也就是20*1024/44100=0.4643990929705215s的数据作为整体分析

        if (TriggerCount == 0) {
            vf = new VoiceFeatures[10];
        }

        if (TriggerCount == 10) {
            //向前移一位

            for (int i = 0; i < 9; i++) {
                vf[i] = vf[i + 1];
            }
            vf[9] = new VoiceFeatures(data);
            vf[9].Calc_Energy();

            for (int i = 0; i < 10; i++) {
                if (vf[i].Energy < 10) {
                    return false;
                }
            }
            for (int i = 0; i < 10; i++) {
                if (vf[i].Calc_Zero() < 320 | vf[i].Zero > 560) {
                    return false;
                }
            }
            for (int i = 0; i < 10; i++) {
                vf[i].Calc_MaxFrequency();
                if (vf[i].freq < 4600 | vf[i].freq > 11800) {
                    Log.e("VoiceAnalyse", "F wrong!" + RevCount + "|" + vf[i].freq);
                    return false;
                } else if (vf[i].freq > 6200 & vf[i].freq < 7800) {
                    return false;
                } else if (vf[i].freq > 9800 & vf[i].freq < 10000) {
                    return false;
                }
            }
            Log.e("VoiceAnalyse", "Analyse Bingo" + RevCount);

            return true;
        } else {
            vf[TriggerCount] = new VoiceFeatures(data);
            vf[TriggerCount].Calc_Energy();
            TriggerCount++;
        }
        return false;
    }

    /**
     * stop task
     */
    public void quit() {
        mQuit.set(true);
    }

    class VoiceFeatures {
        //一帧所具有的音频特性
        public short[] data;
        int Energy;
        int Zero;
        int freq;

        public VoiceFeatures(short[] mdata) {
            data = mdata;
        }

        public int Calc_Energy() {
            int EnergyCount = 0;
            long temp = 0;
            for (int i = 0; i < data.length; i++) {
                temp += Math.abs(data[i]);
                EnergyCount = (int) temp / 1024;
            }
            this.Energy = EnergyCount;
            return Energy;
        }

        public int Calc_Zero() {
            int ZeroCount = 0;
            for (int i = 1; i < data.length; i++) {
                if (data[i] > 0 & data[i - 1] < 0 | data[i] < 0 & data[i - 1] > 0) {
                    ZeroCount++;
                }
            }
            Zero = ZeroCount;
            return Zero;
        }

        public int Calc_MaxFrequency() {
            OptFFT op = new OptFFT(data, 44100);
            op.Calc_FFT();
            double max = 0;
            int maxIndex = 0;
            for (int i = 1; i < op.Calc_FFT_Size / 2; i++) { //这里除以Calc_FFT_Size/2是因为20kHz以上的声音分析不出来（44100/2）
                if (max < op.getModelfromN(i)) {
                    max = op.getModelfromN(i);
                    maxIndex = i;
                }
            }
            freq = op.getFfromN(maxIndex);
            return freq;
        }
    }
}
