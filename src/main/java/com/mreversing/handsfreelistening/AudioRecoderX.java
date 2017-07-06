package com.mreversing.handsfreelistening;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by liming on 15/9/9.
 */
public class AudioRecoderX extends Thread {
    private static final String TAG = "AudioRecorder";
    // 音频获取源
    public int audioSource = MediaRecorder.AudioSource.MIC;

    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public static int sampleRateInHz = 44100;

    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    public static int channelConfig = AudioFormat.CHANNEL_IN_MONO;

    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    public static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;



    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    private AudioRecord audioRecord;
    private String mDstPath;
    //蓝牙耳机 mreversing
    private AudioManager mAudioManager = null;

    Boolean isBlueToothModel=false;
    private AtomicBoolean mQuit = new AtomicBoolean(false);

    private Handler handler;
    int maxVol=0,minVol=0; //最大最小响度

//    public void setmDstPath(String filePath){
//        mDstPath = filePath;
//    }
public AudioRecoderX(String filePath,Handler handler,Boolean isBlueToothModel,AudioManager nAudioManager){
    mDstPath = filePath;
    this.handler = handler;
    this.isBlueToothModel=isBlueToothModel;
    this.mAudioManager = nAudioManager;
//    if (isBlueToothModel){
//        if (!mAudioManager.isBluetoothScoAvailableOffCall()){
//            Log.d(TAG, "系统不支持蓝牙录音");
//        }
//
//        audioSource = MediaRecorder.AudioSource.DEFAULT;
//
//        //蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
//        mAudioManager.startBluetoothSco();
//        //蓝牙SCO连接建立需要时间，连接建立后会发出ACTION_SCO_AUDIO_STATE_CHANGED消息，通过接收该消息而进入后续逻辑。
//        //也有可能此时SCO已经建立，则不会收到上述消息，可以startBluetoothSco()前先stopBluetoothSco()
//        Context.registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
//
//                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
//                    mAudioManager.setBluetoothScoOn(true);  //打开SCO
//                    mRecorder.start();//开始录音
//                    unregisterReceiver(this);  //别遗漏
//                } else {//等待一秒后再尝试启动SCO
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    mAudioManager.startBluetoothSco();
//                }
//            }
//        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
//    }
}



    private void prepareAudioRecord() {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);

       if(isBlueToothModel){ audioSource= MediaRecorder.AudioSource.DEFAULT;}

        // 创建AudioRecord对象
        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        Log.e(TAG, "AudioRecorder prepareAudioRecord!!!!!!!!");
    }



    @Override
    public void run() {
        try {
            Log.e(TAG, "AudioRecorder run!!!!!!!!");
            prepareAudioRecord();
            audioRecord.startRecording();
            writeDataTOFile();
            Log.e(TAG, "release!!!!!!!!");
        } finally {
            release();
        }
    }

    private void writeDataTOFile() {
        Log.e(TAG, "writeDataTOFile!!!!!!!!");
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];

        FileOutputStream fos = null;
        int readsize = 0;
        try {
            File file = new File(MainActivity.recordPath);
            if (file.exists()) {
                file.delete();
            }else{
                //file.mkdir();注意要先创建文件夹
                file.createNewFile();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
        } catch (Exception e) {
            Log.e(TAG, "File Open Failed!!!!");
            e.printStackTrace();
        }

        Message msg = Message.obtain();
        msg.obj = "Thread start";
        msg.what=1;
        handler.sendMessage(msg);

        int m=0,n=0;//单帧最大最小响度
        int flag=0;

        while (!mQuit.get()) {
            m=0;n=0;
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            //zuk z1 readsize为 1792*2
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                try {
                    fos.write(audiodata);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int i;
//                for(i=0;i<readsize;i++){
//                    if(audiodata[i]!=127 && audiodata[i]!=-128){
//                        if(audiodata[i]> m){
//                            m=audiodata[i];
//                        }
//                        if(audiodata[i]< n){
//                            n=audiodata[i];
//                        }
//                    }
//                }
//                maxVol=(m>maxVol)?m:maxVol;
//                minVol=(n<minVol) ? n:minVol;

				short[] adata=new short[readsize/2];
				
        		short q=0;
                long p=0;
                for(i=0;i<readsize/2;i++){
                        // 每16位读取一个音频数据
                    q= (short) (((audiodata[2*i] & 0xff) << 8) | (audiodata[2*i+1] & 0xff));
                    adata[i]=q;
                    p+=Math.abs(q);
                }
                p=(int)p/readsize*2;//算出平均值，峰值是65536/4
                //Log.e(TAG, "|"+p+"|");
                msg = Message.obtain();
                msg.obj = "|"+p+"|";
                msg.what=3;
                handler.sendMessage(msg);

//                int n2=4096;
//                double[] normaldata=new double[n2];
//                for(i=0;i<readsize/2;i++) {
//                    normaldata[i]=adata[i]/25565;
//                }
//                for(;i<n2;i++) {
//                    normaldata[i]=0;
//                }
//                    mySimilarityAlgorithm.ffft(n2,normaldata);


                int n2=8192;
                short[] normaldata=new short[n2];
                for(i=0;i<readsize/2;i++) {
                    normaldata[i]=adata[i];
                }
                for(;i<n2;i++) {
                    normaldata[i]=0;
                }

                msg = Message.obtain();
                msg.obj = normaldata;
                msg.what=4;
                msg.arg1=n2;
                handler.sendMessage(msg);



                msg = Message.obtain();
                msg.obj = adata;
                msg.what=5;
                handler.sendMessage(msg);

            }
        }
        try {
            fos.close();// 关闭写入流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void release() {
        if (audioRecord != null) {
            System.out.println("stopRecord");
            audioRecord.stop();
            audioRecord.release();//释放资源
            audioRecord = null;

            Message msg = Message.obtain();
            msg.obj = "Max: "+maxVol +" Min: "+minVol;
            msg.what=2;
            handler.sendMessage(msg);
        }
    }

    /**
     * stop task
     */
    public void quit() {
        mQuit.set(true);
        Log.e(TAG, "mQuit was set true!!!!!!!!");
    }
}
