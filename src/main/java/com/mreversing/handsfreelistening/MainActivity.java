package com.mreversing.handsfreelistening;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mreversing.handsfreelistening.Utils.myPcmReader;
import com.mreversing.handsfreelistening.Utils.myPcmWriter;
import com.mreversing.handsfreelistening.calc.OptFFT;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    AudioRecoderX mAudioRecoderX;
    VoiceAnalyse mVoiceAnalyse;

    public static String recordDir;
    public static String recordPath;
    public Boolean isRunning=false;

    Button btnTest;
    TextView tvTest1;
    TextView tvTest2;
    Button btnFFTpcm;
    Button btnSimulate;
    Button btnRun;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //初始化SoundPool
        mSoundPool= new SoundPool(1,AudioManager.STREAM_SYSTEM,5);
        mSoundPool.load(this, R.raw.sound1_cut, 1);


        tvTest1=(TextView)findViewById(R.id.tvTest1);
        tvTest2=(TextView)findViewById(R.id.tvTest2);
        btnTest=(Button)findViewById(R.id.btnTest);
        btnFFTpcm=(Button)findViewById(R.id.btnFFTpcm);
        btnSimulate=(Button)findViewById(R.id.btnSimulate);
        btnRun=(Button)findViewById(R.id.btnRun);

        btnTest.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    Log.e(TAG, "mQuit was going to be set true!!!!!!!!");
                    mAudioRecoderX.quit();
                    mVoiceAnalyse.quit();
                }
                else if(event.getAction() == MotionEvent.ACTION_DOWN){

                    //Context context=getBaseContext();//首先，在Activity里获取context
                    //String path=context.getFilesDir().getAbsolutePath();
                    //mAudioRecoderX=new AudioRecoderX(path+"/test.wav");

                    mkPCMname();

                    mVoiceAnalyse=new VoiceAnalyse(handler);
                    mVoiceAnalyse.start();
                    while (mVoiceAnalyse.vaHandler==null){
                        //在此等待mVoiceAnalyse.vaHandler初始化完毕
                    }
                    mAudioRecoderX=new AudioRecoderX(recordPath,handler,true,(AudioManager)getSystemService(Context.AUDIO_SERVICE),mVoiceAnalyse.vaHandler);
                    mAudioRecoderX.start();
                }
                return false;
            }
        });
        btnFFTpcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mkPCMname();
                short[] buffer = new myPcmReader().readPcm(recordDir + "freqlb.pcm");

                //逆变换后写入文件
                OptFFT op = new OptFFT(buffer, 44100);
                op.Calc_FFT();
                op.Calc_Filter2();
                op.Calc_iFFT();
                myPcmWriter pw = new myPcmWriter(recordDir + "freqlb_result.pcm");
                pw.initOutputStream();
                pw.writeData(op.getifftResult());
                pw.close();
            }
        });

        btnSimulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mkPCMname();
                simulateAudioRecoderX sarx=new simulateAudioRecoderX(recordDir+"simulate.pcm",recordDir);
                tvTest1.setText("count:" + sarx.startSimulate());
            }
        });

        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRunning){
                    Log.e(TAG, "mQuit was going to be set true!!!!!!!!");
                    mAudioRecoderX.quit();
                    mVoiceAnalyse.quit();

                }else{

                    //mkPCMname();

                    mVoiceAnalyse=new VoiceAnalyse(handler);
                    mVoiceAnalyse.start();
                    while (mVoiceAnalyse.vaHandler==null){
                        //在此等待mVoiceAnalyse.vaHandler初始化完毕
                    }

                    mAudioRecoderX=new AudioRecoderX(handler,true,(AudioManager)getSystemService(Context.AUDIO_SERVICE),mVoiceAnalyse.vaHandler);
                    mAudioRecoderX.start();
                }
            }
        });
    }

    public static String mkPCMname(){
        recordDir=Environment.getExternalStorageDirectory().getAbsolutePath() + "/HandsFreeListening/";
        File file = new File(recordDir);
        try{  //提前创建好文件夹
            if (!file.exists()) {
                file.mkdir();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        recordPath = recordDir  + df.format(new Date()) + ".pcm";
        return recordPath;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.menu_main_action1:
            {
                //Intent intent = new Intent("com.mreversing.handsfreelistening.FftActivity");
                Intent intent = new Intent(MainActivity.this,FftActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.menu_main_action0:
            {
                finish();
                System.exit(0);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private Handler handler = new Handler() { //录音线程返回
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 125801: //录音线程开始
                    btnTest.setText("Stop");
                    isRunning=true;
                    btnRun.setText("Running...");
                    break;
                case 125802: //录音线程结束
                    btnTest.setText("Start");
                    isRunning=false;
                    btnRun.setText("Run");
                    Log.d(TAG, "handleMessage AudioRecoder stopped ");
                    //findpcmmax();
                    tvTest2.setText((String) msg.obj);
                    //findMyRate();
                    break;
                case 125803:
                    tvTest2.setText((String)msg.obj);
                    break;
                case 125804:
                    break;
                case 125805://识别成功
//                    Log.d(TAG, (String)msg.obj);
				    BingoCount++;
                    tvTest1.setText(Integer.toString(BingoCount));
					//tvTest1.setText((String)msg.obj);
                    //播放声效
                    mSoundPool.play(1, 1, 1, 0, 0, 2);
                    break;
                case 125806:

                    break;
                case 125807:
                    //tvTest1.setText(OptFFT.OptFFTftest((short[]) msg.obj, (double) AudioRecoderX.sampleRateInHz));
//                    if(OptFFT.OptFFTf((short[]) msg.obj, (double) AudioRecoderX.sampleRateInHz)>1000){
//                        count++;
//                    }
//                    计算在某个频率范围内的幅值大于多少（1000）就计数
//                    if((int)OptFFT.OptFFTf((short[]) msg.obj, (double) AudioRecoderX.sampleRateInHz)>0){
//                        count++;
//                    }
//                    tvTest1.setText("count:"+count);
                    OptFFT op=new OptFFT((short[]) msg.obj,44100);
                    op.Calc_FFT();
                    op.Calc_Filter2();
                    op.Calc_iFFT();
                    //warn:运算过慢
                    //提示：可以使用DTW，还要确保在整个音频帧内只有0.003s一个指定数据包(只是最有效的数据)
                    //可以利用fft求出频率为2700-3100所在范围的时域坐标，利用DTW计算是否符合，但还是不能抗噪
                    //还有一种方法是对ifft后的结果进行分析，可以抗噪，在特定频段考查，找出人声和响指的差别，难点是ifft后数据平滑，很难找特殊
                    //要比较多的模板数据

//                    short[] ifft=op.getifftResult();
//                    for(int i=0;i<op.Calc_FFT_Size;i++){
//                        if(ifft[i]>512){
//                            count++;
//                            break;
//                        }
//                    }
                    if(new mySimilarityAlgorithm(). xzifftcheck(op.getifftResult())>80){
                        count++;
                    }

                    tvTest1.setText("count:"+count);
                    break;
                default:
                    break;
            }
        }
    };
	int count=0;
	int BingoCount=0;


    private SoundPool mSoundPool;


    //记录用户首次点击返回键的时间
//    private long firstTime=0;
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN){
//            if (System.currentTimeMillis()-firstTime>2000){
//                Toast.makeText(MainActivity.this,"再按一次退出程序", Toast.LENGTH_SHORT).show();
//                firstTime=System.currentTimeMillis();
//            }else{
//                finish();
//                System.exit(0);
//            }
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

}
