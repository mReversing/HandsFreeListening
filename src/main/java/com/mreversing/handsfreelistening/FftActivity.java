package com.mreversing.handsfreelistening;

import android.app.ActivityManager;
import android.content.Context;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mreversing.handsfreelistening.R;
import com.mreversing.handsfreelistening.Utils.PcmReader;
import com.mreversing.handsfreelistening.Utils.WaveFileReader;
//import com.mreversing.handsfreelistening.utils.WaveFileReader;
//import com.mreversing.handsfreelistening.view.Spectrogram;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Author:pdm on 2016/3/15
 * Email:aiyh0202@163.com
 * CSDN:http://blog.csdn.net/aiyh0202
 * GitHub:https://github.com/flyingfishes
 */

public class FftActivity extends BaseActivity {

    Toolbar toolbar;
    private static final int HANDLER_SPECTROGRAM = 0;
//    private WaveFileReader reader = null;
    private PcmReader reader = null;
    private short[] data = null;
    private boolean isOpenThisActivity = false;
    //采样率
    private double samplerate = 0;
    //频谱
    private Spectrogram mSpectrogram;
    private Thread thread = null;
    private TextView mTitle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fft);
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle("");
//        setSupportActionBar(toolbar);
//        已经没有toolbar，无需再设置
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText("频谱图");
        mSpectrogram = (Spectrogram) findViewById(R.id.spectrogram);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fft, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                mSpectrogram.changeShowType();
                if (mTitle.getText().toString().equals("频谱图")) {
                    mTitle.setText("波形图");
                } else {
                    mTitle.setText("频谱图");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initWaveData() {
            if (data == null) {
//                AssetManager am = getAssets();
//                InputStream inputStream = am.open("default.wav");
//                reader = new WaveFileReader();
                reader = new PcmReader();
                //data = reader.initReader(inputStream)[0]; // 获取第一声道
                //获取采样率
                samplerate = reader.getSampleRate();
                mSpectrogram.setBitspersample(reader.getBitPerSample());//设置采样点的编码长度
            }
    }

    @Override
    protected void onStart() {
        initWaveData();
        startFftRecord();

        if (!isOpenThisActivity && thread == null) {
            thread = new Thread(specRun);
            isOpenThisActivity = true;
            thread.start();
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        isOpenThisActivity = false;
//        thread.interrupt();
//        thread = null;
        super.onStop();
    }



private Runnable specRun = new Runnable() {
    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (isOpenThisActivity) {
            long a;
            long T;
            int[] buf;
            int offset = 0;
            if (data != null && reader.isSuccess()) {
                    while (offset <= (data.length - Spectrogram.SAMPLING_TOTAL)) {
                        T = System.nanoTime() / 1000000;
                        buf = new int[Spectrogram.SAMPLING_TOTAL];
                        for (int i = 0; i < Spectrogram.SAMPLING_TOTAL; i++) {
                            buf[i] = data[offset + i];
                        }
                        handler.sendMessage(handler.obtainMessage(
                                HANDLER_SPECTROGRAM, buf));
                        offset += (Spectrogram.SAMPLING_TOTAL * 10) / 17;
                        while (true) {
                            a = System.nanoTime() / 1000000;
                            if ((a - T) >= 100)
                                break;
                        }
                        if (!isOpenThisActivity) {
                            return;
                        }
                    }
                    //reader.issuccess=false;

            }
        }
    }
};


    AudioRecoderX mAudioRecoderX;
    private void startFftRecord(){
        mAudioRecoderX=new AudioRecoderX(MainActivity.mkPCMname(),handler,true,(AudioManager)getSystemService(Context.AUDIO_SERVICE));
        mAudioRecoderX.start();
    }

    Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {   //录音线程返回
                switch (msg.what) {
                    case HANDLER_SPECTROGRAM: //原代码，不修改。
                        mSpectrogram.ShowSpectrogram((int[]) msg.obj, false, samplerate);
                        break;
                    case 4:
                        data = (short[]) msg.obj;
//                        for (int i = 0; i < msg.arg1; i++) {
//                            InputStream InputStream;
//                            ObjectInputStream ois = new ObjectInputStream(InputStream0);
//                            msg.obj.
//                        }
                        reader.issuccess=true;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK && event.getAction()== KeyEvent.ACTION_DOWN){
            onStop();
            mAudioRecoderX.quit();
            this.finish();
            //return true;
        }
        return super.onKeyDown(keyCode, event);
    }



}
