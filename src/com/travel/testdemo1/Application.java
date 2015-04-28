package com.travel.testdemo1;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android_serialport_api.SerialPort;
import android.util.Log;
/**
 * åŠŸèƒ½ï¼šæ‰“å�°Demoä¸»ç¨‹åº�
 * åˆ›å»ºäººï¼šzzy
 * æ—¥æœŸï¼š2014-4-30
 */
public class Application extends android.app.Application {

    private SerialPort mSerialPort = null;
    final public int PT488ABaud = 9600;//æ‰“å�°æœºæ³¢ç‰¹çŽ‡
    final public int PT488ABaud_NEW = 115200;//TPS586,æ™®ç‘žç‰¹æ‰“å�°æœºæ³¢ç‰¹çŽ‡
    final public int HdxBaud = 115200;//æ‰“å�°æœºæ³¢ç‰¹çŽ‡
    public Boolean CheckVersionEnd=false;//è¿›å…¥å¼€å§‹è¯»ç‰ˆæœ¬å�·ï¼ŒæŒ‰æ‰“å�°å�¥å�Žæ— æ•ˆ
    public StringBuffer DataRece;//ä¸²å�£æµ‹è¯•æŽ¥æ”¶æ•°æ�®

    public SerialPort getSerialPort(int PrintBaudrate) throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
			/* Read serial port parameters */
            SharedPreferences sp = getSharedPreferences("com.travel.testdemo1.sample_preferences", MODE_PRIVATE);

            //åˆ¤æ–­ç³»ç»Ÿç‰ˆæœ¬ï¼ŒA9,4.2çš„æ‰“å�°æœºä¸²å�£ä¸ºttyS1ï¼ŒA8ä¸ºttyS2
            int currentapiVersion=android.os.Build.VERSION.SDK_INT;
            String path = "/dev/ttyS2";
            if(currentapiVersion>=17)
            {
                path = "/dev/ttyS1";
            }
Log.e("applicaton", "f1==="+path+" baudrate:" + PrintBaudrate);
            int baudrate = PrintBaudrate;//Integer.decode(sp.getString("BAUDRATE", "-1"));

//			/* Check parameters */
//            if ( (path.length() == 0) || (baudrate == -1)) {
//                //throw new InvalidParameterException();
//				/*use default value.    Nirvana 0710*/
//                path = "/dev/ttyS2";
//                baudrate = PrintBaudrate;
//            }

			/* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
}
