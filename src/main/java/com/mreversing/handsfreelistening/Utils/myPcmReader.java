package com.mreversing.handsfreelistening.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mreversing on 2017/7/6.
 */
public class myPcmReader {
    private String filename = "文件";
    private long len = 0;
    //默认16bit，单声道，44100HZ

    private InputStream bis = null;

    public short[] readPcm(String Path){
        File file=new File(Path);
        len=file.length()/2;//有多少个short数据
        try {
            bis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        short[] data = new short[(int) len];
        for (int i = 0; i < len; i++) {
            data[i] = readShort();
        }
        return data;
    }

    private byte[] readBytes(int len)
    {
        byte[] buf = new byte[len];
        try {
            if (bis.read(buf) != len)
            {
                throw new IOException("no more data!!!");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buf;
    }

    private short readShort()
    {
        byte[] buf = new byte[2];
        short res = 0;
        try
        {
            if (bis.read(buf) != 2)
            {
                throw new IOException("no more data!!!");
            }
            res = (short)((buf[0] & 0xFF)| ((( buf[1])& 0xFF)<< 8 ));
            //PCM格式储存是每两字节为一段，前后反过来，故buf[1]要左移8位
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return res;
    }

}
