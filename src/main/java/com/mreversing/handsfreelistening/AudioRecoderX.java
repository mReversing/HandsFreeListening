package com.mreversing.handsfreelistening;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mreversing.handsfreelistening.Utils.myPcmWriter;

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
    private String mDstPath="";
    //蓝牙耳机 mreversing
    private AudioManager mAudioManager = null;

    Boolean isBlueToothModel = false;
    private AtomicBoolean mQuit = new AtomicBoolean(false);

    private Handler handler;
    private Handler vahandler;

    int maxVol = 0, minVol = 0; //最大最小响度

    //    public void setmDstPath(String filePath){
//        mDstPath = filePath;
//    }
    public AudioRecoderX(String filePath, Handler handler, Boolean isBlueToothModel, AudioManager nAudioManager, Handler voiceanalysehandler) {
        mDstPath = filePath;
        this.handler = handler;
        this.isBlueToothModel = isBlueToothModel;
        this.mAudioManager = nAudioManager;
        this.vahandler=voiceanalysehandler;
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
    public AudioRecoderX(String filePath, Handler handler, Boolean isBlueToothModel, AudioManager nAudioManager) {
        mDstPath = filePath;
        this.handler = handler;
        this.isBlueToothModel = isBlueToothModel;
        this.mAudioManager = nAudioManager;
    }

    public AudioRecoderX(Handler handler, Boolean isBlueToothModel, AudioManager nAudioManager,Handler voiceanalysehandler) {
        this.handler = handler;
        this.isBlueToothModel = isBlueToothModel;
        this.mAudioManager = nAudioManager;
        this.vahandler=voiceanalysehandler;
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

    private void prepareAudioRecord() {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);

        //if(isBlueToothModel){ audioSource= MediaRecorder.AudioSource.DEFAULT;}

        // 创建AudioRecord对象
        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        Log.e(TAG, "AudioRecorder prepareAudioRecord!!!!!!!!");
    }

    private void writeDataTOFile() {
        Log.e(TAG, "writeDataTOFile!!!!!!!!");
        //new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];
        //zuk z1 bufferSizeInBytes值为3584
        myPcmWriter pw= new myPcmWriter(MainActivity.recordPath);
        if(mDstPath!=""){
            pw.initOutputStream();
        }

        int readsize = 0;
        short[] bdata=new short[1024];
        int flag=0;//记录bdata已填充的位置序号

        //录音线程开始，发msg给MainActivity
        Message msg = Message.obtain();
        msg.obj = "Thread start";
        msg.what=125801;
        handler.sendMessage(msg);

        while (!mQuit.get()) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            //zuk z1 readsize为 1792*2
            if(readsize==0){
                //这里容易报错，程序意外终止，activity重刷了
            }
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                if(mDstPath!=""){
                    pw.writeData(audiodata);//写入文件
                }

                int i;
				short[] adata=new short[readsize/2];
        		short temp=0;
                long total=0;
                for(i=0;i<readsize/2;i++){
                        // 每16位读取一个音频数据
                    temp= (short) (((audiodata[2*i+1] & 0xff) << 8) | (audiodata[2*i] & 0xff));
                   // temp= (short) (((audiodata[2*i] & 0xff) << 8) | (audiodata[2*i+1] & 0xff));
                    //这里将高低位弄反了，version 0.0.1.3才发现
                    adata[i]=temp;
                    total+=Math.abs(temp);
                }

                total=(int)(total*2/readsize);//算出平均值，峰值是65536/4
                //Log.e(TAG, "|"+p+"|");
                msg = Message.obtain();
                msg.obj = "average: "+total+" ";
                msg.what=125803;
                handler.sendMessage(msg);

                //todo 需要加一个线程做对齐输出，每次输出1024个数据(2048字节)？事实上这是以时间来衡量的，大约0.02s
                //todo 不过默认所有设备都用44100Hz的采样率和16bits的样本，也就是换算MinBufferSize和字节数的区别
                //todo 实际操作就是MinBufferSize/2向1024对齐，算法如下：
                for(i=0;i<adata.length;i++){
                    bdata[flag]=adata[i];
                    if(flag==1024-1){
                        //已集齐1024个数据
                        msg = Message.obtain();
                        msg.obj = bdata;
                        msg.what = 125701;
                        if(vahandler!=null){
                            vahandler.sendMessage(msg);
                        }

                        bdata=new short[1024];
                        flag=-1;
                    }
                    flag++;
                }

                msg = Message.obtain();
                msg.obj = adata;
                msg.what = 125806;
                handler.sendMessage(msg);


                //为Spectrogram凑2的n次方，多余的归零
                int n2=8192;
                short[] normaldata=new short[n2];
                for(i=0;i<readsize/2;i++) {
                    normaldata[i]=adata[i];
                }
                for(;i<n2;i++) {
                    normaldata[i]=0;
                }
                //然后发送给FftActivity
                msg = Message.obtain();
                msg.obj = normaldata;
                msg.what=125904;
                handler.sendMessage(msg);
            }
        }
        if(mDstPath!=""){pw.close();}
    }

    private void release() {
        if (audioRecord != null) {
            System.out.println("stopRecord");
            audioRecord.stop();
            audioRecord.release();//释放资源
            audioRecord = null;

            //录制结束
            Message msg = Message.obtain();
            msg.obj = "Max: "+maxVol +" Min: "+minVol;
            msg.what=125802;
            handler.sendMessage(msg);
            Log.e(TAG, "already release!!!!!!!!");
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
