package com.mreversing.handsfreelistening;

import com.mreversing.handsfreelistening.Utils.myPcmReader;
import com.mreversing.handsfreelistening.calc.OptFFT;

/**
 * Created by mreversing on 2017/7/8.
 */
public class simulateAudioRecoderX {
    String Path="";
public simulateAudioRecoderX(String fPath){
Path=fPath;
}
    public int startSimulate(){
        myPcmReader pr = new myPcmReader();
        short[] data = pr.readPcm(Path);
        int lencircle = data.length / 1792;
        int count = 0;
//        int indicator =0;
        for (int i = 0; i < lencircle; i++) {

            short[] buffer = new short[1792];
            for (int j = 0; j < 1792; j++) {
                buffer[j] = data[j + 1792 * i];
            }

            OptFFT op = new OptFFT(buffer, 44100);
            op.Calc_FFT();
            op.Calc_Filter2();
            op.Calc_iFFT();

            if (new mySimilarityAlgorithm().xzifftcheck(op.getifftResult()) > 80) {
                count++;
            }
        }
        return count;
    }

}
