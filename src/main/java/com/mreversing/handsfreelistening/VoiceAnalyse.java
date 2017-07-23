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

    int Recount=0;
    private AtomicBoolean mQuit = new AtomicBoolean(false);

    public VoiceAnalyse(Handler mhandler){
        this.handler = mhandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        Looper.loop();
        while (!mQuit.get()) {}

    }

    private Handler handler;//发送识别成功信息到此Handler
    public Handler vaHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 125701: //录音线程发来1024个数据
                    short[] data =(short[])msg.obj;
//                    Log.e("VoiceAnalyse", "Data Recevied!!!");
                    Recount++;
                    Analyse(data);
                    break;
                default:
                    break;
            }
        }
    };

    private void Analyse(short[] data){
        OptFFT op=new OptFFT(data,44100);
        op.Calc_FFT();
        double max=0;
        int maxIndex=0;
        for (int i=1;i<op.Calc_FFT_Size;i++){
            if(max<op.getModelfromN(i)){
                max=op.getModelfromN(i);
                maxIndex=i;
            }
        }
        int maxF=op.getFfromN(maxIndex);

            Message msg;

            //以下为发消息给MainActivity
            msg = Message.obtain();
            msg.obj = Integer.toString(maxF);
            msg.what=125805;
            handler.sendMessage(msg);


    }




    /**
     * stop task
     */
    public void quit() {
        mQuit.set(true);
    }
}
