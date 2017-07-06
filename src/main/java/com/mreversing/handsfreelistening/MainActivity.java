package com.mreversing.handsfreelistening;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mreversing.handsfreelistening.calc.OptFFT;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    AudioRecoderX mAudioRecoderX;

    public static String recordDir;
    public static String recordPath;

    Button btnTest;
    TextView tvTest1;
    TextView tvTest2;

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

        tvTest1=(TextView)findViewById(R.id.tvTest1);
        tvTest2=(TextView)findViewById(R.id.tvTest2);
        btnTest=(Button)findViewById(R.id.btnTest);

        btnTest.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    Log.e(TAG, "mQuit was going to be set true!!!!!!!!");
                    mAudioRecoderX.quit();
                }
                else if(event.getAction() == MotionEvent.ACTION_DOWN){

                    //Context context=getBaseContext();//首先，在Activity里获取context
                    //String path=context.getFilesDir().getAbsolutePath();
                    //mAudioRecoderX=new AudioRecoderX(path+"/test.wav");

                    mkPCMname();
                    mAudioRecoderX=new AudioRecoderX(recordPath,handler,true,(AudioManager)getSystemService(Context.AUDIO_SERVICE));

                    mAudioRecoderX.start();
                }
                return false;
            }
        });
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        }
        return super.onOptionsItemSelected(item);
    }


    private Handler handler = new Handler() { //录音线程返回
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    btnTest.setText("Stop");
                    break;
                case 2:
                    btnTest.setText("Start");
                    Log.d(TAG, "handleMessage AudioRecoder stopped ");
                    //findpcmmax();
                    tvTest2.setText((String) msg.obj);
                    //findMyRate();
                    break;
                case 3:
                    tvTest2.setText((String)msg.obj);
                    break;
                case 5:
                    tvTest1.setText(OptFFT.OptFFTftest((short[]) msg.obj, (double) AudioRecoderX.sampleRateInHz));
                    break;
                default:
                    break;
            }
        }
    };
	

    public void findMyRate(){
        String str1="str1：",str2="str2：";
        short[] aa;
        aa= getAudioData(recordPath,1);
        for(int i=10;i<aa.length-10;i++){
            if(aa[i]>aa[i+1] & aa[i]>aa[i+2] & aa[i]>aa[i+3] & aa[i]>aa[i+4] & aa[i]>aa[i+5] &
            aa[i]>aa[i+6] & aa[i]>aa[i+7] & aa[i]>aa[i+8] & aa[i]>aa[i+9] & aa[i]>aa[i+10]
                    & aa[i]>aa[i-1] & aa[i]>aa[i-2] & aa[i]>aa[i-3] & aa[i]>aa[i-4] & aa[i]>aa[i-5]
                    & aa[i]>aa[i-6] & aa[i]>aa[i-7] & aa[i]>aa[i-8] & aa[i]>aa[i-9] & aa[i]>aa[i-10]){
                str1+=i+" ";
            }
            if(aa[i]<aa[i+1] & aa[i]<aa[i+2] & aa[i]<aa[i+3] & aa[i]<aa[i+4] & aa[i]<aa[i+5] &
                    aa[i]<aa[i+6] & aa[i]<aa[i+7] & aa[i]<aa[i+8] & aa[i]<aa[i+9] & aa[i]<aa[i+10]
                    & aa[i]<aa[i-1] & aa[i]<aa[i-2] & aa[i]<aa[i-3] & aa[i]<aa[i-4] & aa[i]<aa[i-5]
                    & aa[i]<aa[i-6] & aa[i]<aa[i-7] & aa[i]<aa[i-8] & aa[i]<aa[i-9] & aa[i]<aa[i-10]){
                str2+=i+" ";
            }
            tvTest1.setText(str1);
            tvTest2.setText(str2);
        }


    }

    public void findpcmmax(){
        short[] bb;
        bb= getAudioData(recordPath,1);
        tvTest1.setText("Max: "+mySimilarityAlgorithm.findMaxNoabs(bb)+"Min: "+mySimilarityAlgorithm.findMin(bb));
    }

    /**
     * 获取音频数据
     *
     * @param filePath 音频数据文件路径
     * @return
     */
    public short[] getAudioData(String filePath, int type) {
        File file = new File(filePath);
        System.out.println("File info   " + file.length());
        DataInputStream dis = null;
        short[] audioData = null;
        try {
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
if(type==1){
    // file.length() / 2 +1 : /2 : 两位byte数据保存为一位short数据; +1 : 保存文件结尾标志
    audioData = getAudioData(dis, (int) file.length() / 2, 1);
}else{
    //type=2 8bit
    audioData = getAudioData(dis, (int) file.length(), 2);
}

            dis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return audioData;
    }

    /**
     * 按编码类型提取音频数据
     *
     * @param dis
     * @param size
     * @param type
     * @return
     */
    public static short[] getAudioData(DataInputStream dis, int size, int type) {
        short[] audioData = new short[size];
        try {
            if(type==1){
                byte[] tempData = new byte[2];
                long audioDataSize = 0;
                while (dis.read(tempData) != -1) {
                    // 每16位读取一个音频数据
                    audioData[(int) audioDataSize] = (short) (((tempData[0] & 0xff) << 8) | (tempData[1] & 0xff));
                    audioDataSize++;
                    if (audioDataSize == size) {
                        break;
                    }
                }
            }else if(type==2) {
                byte tempData[] = new byte[1];
                long audioDataSize = 0;
                while (dis.read(tempData) != -1) {
                    // 每8位读取一个音频数据
                    audioData[(int) audioDataSize] = (short) (tempData[0]);
                    audioDataSize++;
                    if (audioDataSize == size) {
                        break;
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return audioData;
    }
}
