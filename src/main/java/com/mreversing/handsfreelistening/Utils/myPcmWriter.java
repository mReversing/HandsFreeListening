package com.mreversing.handsfreelistening.Utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by mreversing on 2017/7/7.
 */
public  class myPcmWriter {

    private FileOutputStream fos = null;
    private String fPath = null;

    public myPcmWriter(String myPath){
        fPath=myPath;
    }

    public void initOutputStream(){
        try {
            File file = new File(fPath);
            if (file.exists()) {
                file.delete();
            } else {
                //file.mkdir();注意要先创建文件夹
                file.createNewFile();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
        } catch (Exception e) {
            Log.e("PcmWriter", "File Open Failed!!!!");
            e.printStackTrace();
        }
    }

    public FileOutputStream getFOS(){
        return fos;
    }

    public void close() {
        try {
            fos.close();// 关闭写入流

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注释：short到字节数组的转换！
     *
     * @param number
     * @return
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
                    temp = temp >> 8; // 向右移8位
        }
        return b;//结果为b[0]为最后两位，b[1]为首两位
    }

    public static short  ByteToshort(byte number1,byte number2) {
        return (short) (((number1 & 0xff) << 8) | (number2 & 0xff));//上一个函数的反函数
    }

    public void writeData(short[] data){

        for (int i = 0; i < data.length; i++) {
            try {
              fos.write(myPcmWriter.shortToByte(data[i]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void writeData(byte[] data){
        try {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeData2(short[] data){
        //数字形式，非二进制形式数据
        OutputStreamWriter osw=new OutputStreamWriter(fos);
        for (int i = 0; i < data.length; i++) {
            try {
                osw.write(Short.toString(data[i])+"\n");
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeData2(byte[] data) {
        //默认16位
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        for (int i = 0; i < data.length / 2; i++) {
            try {
                osw.write(Short.toString(ByteToshort(data[2*i],data[2*i+1]))+"\n");
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}