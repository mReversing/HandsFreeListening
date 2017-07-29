package com.mreversing.handsfreelistening.calc;

/**
 * Created by mreversing on 2017/7/26.
 */
public class VoiceFeatures {
    //一帧所具有的音频特性
    public double samplerate;
    public short[] data;
    public int Energy;
    public int Zero;
    public int maxFreq;
    public int peaks[];
    public static int peakspace=5;//前后十个不算在峰内
    public static int peakstofound=9;//寻找多少个峰
    Boolean isConformFreqFeatures=false;
    OptFFT op=null;

    public VoiceFeatures(short[] mdata,double msamplerate) {
        data = mdata;
        samplerate=msamplerate;
    }

    public int Calc_Energy() {
        int EnergyCount = 0;
        long temp = 0;
        for (int i = 0; i < data.length; i++) {
            temp += Math.abs(data[i]);
        }
        EnergyCount = (int) (temp / data.length);
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
        initFftOp();
        double max = 0;
        int maxIndex = 0;
        if (op==null){
            op.Calc_FFT_Size++;
        }
        for (int i = 1; i < op.Calc_FFT_Size / 2; i++) { //这里除以Calc_FFT_Size/2是因为20kHz以上的声音分析不出来（44100/2）
            if (max < op.getModelfromN(i)) {
                max = op.getModelfromN(i);
                maxIndex = i;
            }
        }
        maxFreq = op.getFfromN(maxIndex);
        return maxFreq;
    }

    public int[] Calc_VoicePeaks(){
        //返回几个峰的Freq值
        initFftOp();
        peaks= new int[peakstofound];
        double[] mA =new double[op.Calc_FFT_Size / 2];
        int[] mB =new int[op.Calc_FFT_Size / 2];
        for (int i = 0; i < op.Calc_FFT_Size / 2; i++) { //这里除以Calc_FFT_Size/2是因为20kHz以上的声音分析不出来（44100/2）
            mA[i]=op.getModelfromN(i);
        }
        mB=bubbleSort(mA);

        int[] peaksindexarry=new int[peakstofound];
        int peaksalreadyfound=0;
        if(mB[0]==0){
            peaksindexarry[0]=mB[1];//自动忽略0Hz直流分量幅值
        }else{
            peaksindexarry[0]=mB[0];
        }
        peaksalreadyfound++;
        for (int i = 1; i < op.Calc_FFT_Size / 2; i++) { //这里除以Calc_FFT_Size/2是因为20kHz以上的声音分析不出来（44100/2）
            int flag=0;
            if(mB[i]!=0){
                for(int j=0;j<peaksalreadyfound;j++){
                    if(peaksindexarry[j]-peakspace>mB[i] |
                            peaksindexarry[j]+peakspace<mB[i]){
                        flag++;
                    }
//					for(int k=-peakspace;k<peakspace;k++){
//						if(peaksindexarry[j]+k
//						peaksindexarry[peaksalreadyfound]=mB[peaksalreadyfound];
//						peaksalreadyfound++;
//					}
                }
                if(flag==peaksalreadyfound){
                    peaksindexarry[peaksalreadyfound]=mB[i];
                    peaksalreadyfound++;
                    if(peakstofound==peaksalreadyfound){
                        break;
                    }
                }
            }
        }
        for(int i=0;i<peakstofound;i++){
            peaks[i]=op.getFfromN(peaksindexarry[i]);
        }
        return peaks;
    }

    private void initFftOp(){
        if(op==null){
            op = new OptFFT(data, samplerate);
            op.Calc_FFT();
        }
    }

    /**

     * 冒泡排序
     * 比较相邻的元素。如果第一个比第二个大，就交换他们两个。
     * 对每一对相邻元素作同样的工作，从开始第一对到结尾的最后一对。在这一点，最后的元素应该会是最大的数。
     * 针对所有的元素重复以上的步骤，除了最后一个。
     * 持续每次对越来越少的元素重复上面的步骤，直到没有任何一对数字需要比较。
     *
     @param
     numbers 需要排序的整型数组

     */

    public static int[] bubbleSort(double[] numbers)
    {
        double	temp = 0;
        int temp2;
        int	size = numbers.length;
        int[] indexArry=new int[size];
        for(int i = 0 ; i < size; i ++)
        {
            indexArry[i]=i;
        }
        for(int i = 0 ; i < size-1; i ++)
        {
            for(int j = 0 ;j < size-1-i ; j++)
            {
                if(numbers[j] < numbers[j+1])  //交换两数位置
                {
                    temp = numbers[j];
                    numbers[j] = numbers[j+1];
                    numbers[j+1] = temp;
                    temp2=indexArry[j];
                    indexArry[j]=indexArry[j+1];
                    indexArry[j+1]=temp2;
                }
            }
        }
        return indexArry;
    }

    public double[] Calc_AllFreq() {
        initFftOp();
        double[] mA =new double[op.Calc_FFT_Size / 2];
        for (int i = 0; i < op.Calc_FFT_Size / 2; i++) { //这里除以Calc_FFT_Size/2是因为20kHz以上的声音分析不出来（44100/2）
            mA[i]=op.getModelfromN(i);
        }
        return mA;
    }
}
