/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.travel.testdemo1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.travel.testdemo1.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.md5.MD5Util;
import com.travel.testdemo1.PRTUtil;
import hdx.HdxUtil;
import hdx.pwm.PWMControl;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * åŠŸèƒ½ï¼šæ‰“å�°å¤„ç�†Demo
 * åˆ›å»ºäººï¼šzzy
 * æ—¥æœŸï¼š2014-4-30
 */

public class ConsoleActivity extends SerialPortActivity {
    public static String PRINT_VERSION_CHANGE="PRINT_VERSION_CHANGE";//æ‰“å�°ç‰ˆæœ¬ä¿®æ”¹å¹¿æ’­
    public static String PRINT_VERSION_CHANGE_MSG="PRINT_VERSION_CHANGE_MSG";
    private static boolean printVersionFlag=false;//æ‰“å�°æœºç‰ˆæœ¬ falseä¸ºæ™®ç‘žç‰¹ï¼Œtrueä¸ºå¥½å¾·èŠ¯
    private static String printVersion;
    private final int PRINTIT = 1;
    private final int ENABLE_BUTTON = 2;
    private final int NOPAPER = 3;
    private final int LOWBATTERY=4;
    private final int PRINTVERSION=5;//
    private final int RECHECK_PRINT_VERSION=6;//å†�æ¬¡æŸ¥è¯¢ç‰ˆæœ¬å�·

    private boolean stop = false;
    private  boolean WriteRunning =false;//æ‰“å�°è¿›è¡Œä¸­
    private static final String TAG = "ConsoleTestActivity";

    final String logoWidthStore ="logoWidthStore";
    final String logoHeightStore ="logoHeightStore";
    final String logoMD5Store ="logoMD5Store";
    private int lastLogoWidth=0;//ä¿�å­˜åœ¨æ‰“å�°æœºé‡Œçš„å›¾ç‰‡å®½
    private int lastLogoHeight=0;//ä¿�å­˜åœ¨æ‰“å�°æœºé‡Œçš„å›¾ç‰‡é•¿
    private String lastMD5Str=null;//ä¿�å­˜åœ¨æ‰“å�°æœºé‡Œçš„å›¾ç‰‡çš„MD5è½¬æ�¢å€¼
    SharedPreferences preferences ;
    SharedPreferences.Editor editor;;
    private ProgressDialog progressDialog;
    
    ArrayList<String> print_arrstring = new ArrayList<String>();
    
    //æ™®ç‘žç‰¹å�‚æ•°
    private boolean checkPrinterState=false;//å¼€å§‹æ£€æŸ¥æ‰“å�°çŠ¶æ€�
    static boolean isOReceive=false;//æ™®ç‘žç‰¹æ‰“å�°æœºè¿”å›žOKçŠ¶æ€�ï¼Œå·²ç»�æ”¶åˆ°Oæ ‡å¿—
    boolean PTIsReady=false;//æ‰“å�°æœºå°±ç»ª

    static String versionStore="";

    private TimerTask timerTask;
    private Timer timer=new Timer();//æ£€æŸ¥ç‰ˆæœ¬å�·å®šæ—¶

    public TextView TVSerialRx;
    MyHandler handler;
    Button ButtonCodeDemo;
    Button ButtonImageDemo;
    Button ButtonCharacterDemo;
    Button ButtonQuit;
    ExecutorService pool = Executors.newSingleThreadExecutor();
    WakeLock lock;
    private Boolean nopaper=false;
    private boolean LowBattery = false;
    private Spinner mySpinner;// æ‰“å�°èŠ¯ç‰‡ç±»åž‹é€‰æ‹©æ¡† luyq 20140430
    ArrayAdapter<String> adapter_app;
    private int pos;// é€‰æ‹©çš„æ‰“å�°æœºç±»åž‹ luyq 20140430
    //   private Boolean CheckVersionEnd=false;//è¿›å…¥å¼€å§‹è¯»ç‰ˆæœ¬å�·ï¼ŒæŒ‰æ‰“å�°å�¥å�Žæ— æ•ˆ
    private boolean isClose=false;//å…³é—­ç¨‹åº�

    public  static int PrintDriveVer=0 ;//TPS550/TPS580 ä¸€å…±ä¸‰ç§�é©±åŠ¨ 0:æ™®ç‘žç‰¹9600æ³¢ç‰¹çŽ‡é©±åŠ¨ï¼›1ï¼šæ™®ç‘žç‰¹115200æ³¢ç‰¹çŽ‡é©±åŠ¨ï¼›2ï¼šå¥½å¾·èŠ¯é©±åŠ¨ï¼ˆA8ï¼‰

    private class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            if (stop == true)
                return;
            switch (msg.what) {
                case PRINTIT:
                    final ArrayList<String> rInfoList = new ArrayList<String>();
                    ButtonCodeDemo.setEnabled(false);
                    ButtonImageDemo.setEnabled(false);
                    ButtonCharacterDemo.setEnabled(false);
                    ButtonQuit.setEnabled(false);
//                    pos= mySpinner.getSelectedItemPosition();
//                    if (pos == 0) {
//                        Toast.makeText(ConsoleActivity.this,
//                                getString(R.string.selectprinter),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                    else if (pos == 2) {
////					Toast.makeText(ConsoleActivity.this,
////							getString(R.string.not_support), Toast.LENGTH_SHORT)
////							.show();
//                    }

                    // å¼€å§‹æ‰“å�°
                    new WriteThread(rInfoList).start();
                    break;
                case ENABLE_BUTTON:
                    ButtonCodeDemo.setEnabled(true);
                    ButtonImageDemo.setEnabled(true);
                    ButtonCharacterDemo.setEnabled(true);
                    ButtonQuit.setEnabled(true);
                    break;
                case NOPAPER:
                    noPaperDlg();
                    break;
                case LOWBATTERY:
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConsoleActivity.this);
                    alertDialog.setTitle(R.string.operation_result);
                    alertDialog.setMessage(getString(R.string.LowBattery));
                    alertDialog.setPositiveButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alertDialog.show();
                    break;
                case PRINTVERSION:
                    break;
                case RECHECK_PRINT_VERSION:
                    versionStore="";
                    printVersion="";
                    CheckPrintVersion();//è¯»å�–æ‰“å�°æœºç‰ˆæœ¬

                    if(timer!=null){
                        timer.cancel();
                        timer=null;
                    }
                    if(timerTask!=null){
                        timerTask.cancel();
                        timerTask=null;
                    }

                    timer=new Timer();
                    timerTask=new MyTimeTask();
                    timer.schedule(timerTask, 500);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "ConsoleActivity====onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);
        setTitle("Serail Port Console");

        int currentapiVersion=android.os.Build.VERSION.SDK_INT;
        if(currentapiVersion>=17)
        {
            HdxUtil.SwitchSerialFunction(HdxUtil.SERIAL_FUNCTION_PRINTER);
        }
        else
        {
            Log.v(TAG,"No switch serial port");
        }


        handler = new MyHandler();
        PowerManager pm = (PowerManager) getApplicationContext()
                .getSystemService(Context.POWER_SERVICE);
        lock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);

        ButtonCodeDemo = (Button) findViewById(R.id.ButtonCodeDemo);
        ButtonImageDemo = (Button) findViewById(R.id.ButtonImageDemo);
        ButtonCharacterDemo = (Button) findViewById(R.id.ButtonCharacterDemo);
        ButtonQuit = (Button) findViewById(R.id.quit);
        TVSerialRx = (TextView) findViewById(R.id.TextViewSerialRx);

//        mySpinner = (Spinner) findViewById((R.id.printer_select));// luyq
    //    String[] arr ={"",getString(R.string.CommonVer),getString(R.string.MultilingualVer)};
     //   adapter_app = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,arr);
     //    mySpinner.setAdapter(adapter_app);
//        mySpinner.setSelection(1,true);
//        pos= 1;
        preferences =getSharedPreferences("logoStorePreferences",Context.MODE_PRIVATE);
        editor=preferences.edit();

        IntentFilter pIntentFilter=new IntentFilter();
        pIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
//        pIntentFilter.addAction(PRINT_VERSION_CHANGE);
        registerReceiver(printReceive, pIntentFilter);

        ButtonCharacterDemo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(LowBattery == true){
                    handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
                }else{
              //      progressDialog.setMessage(error_msg + "  æ‰“å�°ä¸­......");
              //      new MainActivity.WriteThread(data).start();
                    if(!nopaper) {
                        setTitle("print character");
                        mApplication.CheckVersionEnd = true;
                        handler.sendMessage(handler.obtainMessage(PRINTIT, 1, 0, null));
                     // test();
                    }
                    else {
                        Toast.makeText(ConsoleActivity.this,getString(R.string.ptintInit),Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
        ButtonQuit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mApplication.CheckVersionEnd=false;
                finish();
            }
        });

        versionStore="";
        printVersion="";
        PrintDriveVer=0;
        CheckPrintVersion();//è¯»å�–æ‰“å�°æœºç‰ˆæœ¬
        timerTask = new MyTimeTask();
        timer.schedule(timerTask, 500);
    }

    private class  MyTimeTask  extends TimerTask{

        @Override
        public void run() {

            if (printVersion.indexOf("PRT")<0) {
                if(PrintDriveVer==0){
                    //åˆ‡æ�¢ä¸ºæ™®ç‘žç‰¹æ–°115200é©±åŠ¨
                    SerialProtReset(mApplication.PT488ABaud_NEW);
                    PrintDriveVer=1;
                    handler.sendMessage(handler.obtainMessage(RECHECK_PRINT_VERSION, 1, 0, null));
                }
                else if(PrintDriveVer==1){
                    //åˆ‡æ�¢ä¸ºå¥½å¾·èŠ¯é©±åŠ¨
                    PrintDriveVer=2;
                    printVersionFlag = true;
                    //  SerialProtReset(mApplication.HdxBaud);
                }

            }
        }
    }

    /* Called when the application resumes */
    @Override
    protected void onResume() {
        super.onResume();
        int currentapiVersion=android.os.Build.VERSION.SDK_INT;
        if(currentapiVersion>=17)
        {
            HdxUtil.SwitchSerialFunction(HdxUtil.SERIAL_FUNCTION_PRINTER);
        }
        else
        {
            Log.v(TAG,"No switch serial port");
        }
    }

    private BroadcastReceiver  printReceive=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(Intent.ACTION_BATTERY_CHANGED))
            {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,BatteryManager.BATTERY_STATUS_NOT_CHARGING);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,0);
                if(status!=BatteryManager.BATTERY_STATUS_CHARGING)
                {
                    if(level*5<=scale)
                    {
                       LowBattery=true;
                    }
                    else{
                        LowBattery=false;
                    }
                }
                else
                {
                    LowBattery=false;
                }
            }
//            else if(action.equals(PRINT_VERSION_CHANGE))
//            {
//                mySpinner.setSelection(intent.getIntExtra(PRINT_VERSION_CHANGE_MSG,0));
//            }
        }
    };

    @Override
    protected void onDataReceived(final byte[] buffer, final int size,
                                  final int n) {
        runOnUiThread(new Runnable() {

            public void run() {
                Log.e(TAG, "onDataReceived==============");
                if (nopaper) {
                    return;
                }
                StringBuilder sn = new StringBuilder();

                for (int i = 0; i < size; i++) {
                    Log.e(TAG, Integer.toHexString(buffer[i]));

                    if (printVersionFlag) {//æ£€æµ‹ç¼ºçº¸;
                        if (((buffer[i] & 0x01) == 1) && ((buffer[i] & 0x0ff) != 0xff)) {
                            nopaper = true;
                        }
                    } else {
                        //æ£€æµ‹ç¼ºçº¸å’Œè¿‡çƒ­
                        if (buffer[i] < 0x20 && buffer[i] > 0) {
                            if ((buffer[i] & 0x04) != 0) {
                                //ç¼ºçº¸
                                nopaper = true;
                            } else if ((buffer[i] & 0x10) != 0) {
                                //è¿‡çƒ­
                                Toast.makeText(ConsoleActivity.this, getString(R.string.overTemp), Toast.LENGTH_LONG).show();
                            }
                        }
                        if (versionStore.indexOf("PRT") >= 0) {
                            if (buffer[i] != 0) {
                                if (i == size - 1) {
                                    versionStore += new String(buffer);
                                }

                            } else {
                                printVersion = versionStore;
                                versionStore = "";
                                handler.sendMessage(handler.obtainMessage(PRINTVERSION, 1, 0, null));
                            }
                        }
                        //æ£€æµ‹ç‰ˆæœ¬
                        if (buffer[i] == 'P') {
                            versionStore = "P";
                        } else if (buffer[i] == 'R') {
                            if (versionStore.equals("P")) {
                                versionStore = "PR";
                            }
                        } else if (buffer[i] == 'T') {
                            if (versionStore.equals("PR")) {
                                versionStore = "PRT";
                            }
                        }

                        //æ£€æµ‹æ‰“å�°æœºçŠ¶æ€�
                        if (checkPrinterState) {
                            //æ‰“å�°æœºå°±ç»ªæ£€æµ‹
                            if (buffer[i] == 'O') {
                                isOReceive = true;
                            } else if (buffer[i] == 'K') {
                                if (isOReceive) {
                                    PTIsReady = true;
                                }
                                isOReceive = false;
                            } else {
                                isOReceive = false;
                            }
                        }
                    }
                    sn.append(String.format("%02x", buffer[i]));
                }
                TVSerialRx.setText("*recive:" + sn.toString() + "\n");
                if (nopaper) {
                    Log.e(TAG, "No PAPER !!!!!!!");
                }
            }
        });
    }

    private void noPaperDlg()
    {
        AlertDialog.Builder dlg=new AlertDialog.Builder(ConsoleActivity.this);
        dlg.setTitle(getString(R.string.noPaper));
        dlg.setMessage(getString(R.string.noPaperNotice));
        dlg.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(!nopaper) {
                    handler.sendMessage(handler.obtainMessage(PRINTIT, 1, 0, null));
                }else{
                    Toast.makeText(ConsoleActivity.this,getString(R.string.ptintInit),Toast.LENGTH_LONG).show();
                    handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1, 0, null));
                }
            }
        });
        dlg.setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handler.sendMessage(handler.obtainMessage(ENABLE_BUTTON, 1, 0, null));
            }
        });
        dlg.show();
    }

    private void sendLineFeed() {
        sendCommand(0x0a);
//        sleep(300);
    }

    private  void CheckPrintVersion()
    {
        lock.acquire();
        try {
            PWMControl.PrinterEnable(1);
            ConsoleActivity.this.sleep(100);
            sendCommand(0x1b,0x03);//æŸ¥è¯¢æ‰“å�°æœºç‰ˆæœ¬
        } finally {
            lock.release();
            PWMControl.PrinterEnable(0);
        }
    }

    private void setDefaultFormat() {
        sendCommand(0x1b, 0x6d, 15);
        // sendCommand(0x1d,0x4f,0x01,0x0a,0x1b,0x21,0x00);
    }

    private String atMiddleLarge(String str) {
        return str;
    }

    private String atMiddleNormal(String str) {
        return str;
    }

    private class WriteThread extends Thread {
        ArrayList<String> arr;

        public WriteThread(ArrayList<String> str) {
            arr = str;
        }

        public void run() {
            super.run();
            setName("Print WriteThread");
            WriteRunning=true;
           try {
               PWMControl.PrinterEnable(1);
               ConsoleActivity.this.sleep(1000);
               lock.acquire();
             
              printPingZheng();
              if (nopaper||isClose)
            	  return;
             

//               printObliqueLineDemo();
//             if (nopaper||isClose)
//                 return;();
//               if (nopaper||isClose)
//                  return;
               
                //æ‰“å�°åˆ�å§‹åŒ–
               // printInt();
                //if (nopaper||isClose)
                //    return;

                //å¼€å¤´å­—ç¬¦æµ‹è¯•
                //PrintHeadCharDemo();
                //if (nopaper||isClose)
                //    return;
//
//                //ç›´çº¿æ‰“å�°
//                printlineDemo();
//                if (nopaper||isClose)
//                    return;

//               ConsoleTestActivity.this.sleep(1000);

  //              barcode Print
//                printBarcodeDemo();
//                if (nopaper||isClose)
//                    return;
//                sleep(500);

//                   Mosaic Print
//                printMosaicDemo();
//                if (nopaper||isClose)
//                    return;


 ////            Oblique line print
//                printObliqueLineDemo();
//               if (nopaper||isClose)
//                   return;

//            Qrcode print
            //   printBarcode();

   //           æ‰“å�°å�Œå›¾ç‰‡æµ‹è¯• 1223ç‰ˆ
    //           printDoubleBitmap();

//               ondaonsoleTestActivity.this.sleep(1000);
//


//               ConsoleTestActivity.this.sleep(500);

                //ä¸�å�Œå­—ä½“å¤§å°�æ‰“å�°
//                FontSizeTest();
//                if (nopaper||isClose)
//                    return;
//               ConsoleTestActivity.this.sleep(500);

                //å­—ç¬¦æ‰“å�°æµ‹è¯•
  //              printCharactersTest();
   //              if (nopaper||isClose)
    //                return;
             //  ConsoleTestActivity.this.sleep(500);

                //æ‰“å�°è®¾å¤‡åŸºç¡€ä¿¡æ�¯
            //    printDeviceBaseInfo();
             //   if (nopaper||isClose)
             //       return;
//               ConsoleTestActivity.this.sleep(500);
                //æ‰“å�°ç»“æ�Ÿ
            //    printTestEnd();
          //      if (nopaper||isClose)
            //        return;

                 ConsoleActivity.this.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
//                   sendCommand(0x1B, 0x40);//åˆ�å§‹åŒ–
                  mOutputStreamClear();
                if(nopaper)
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
               // printInt();
               lock.release();
               PWMControl.PrinterEnable(0);
               ConsoleActivity.this.sleep(500);
//               if(printVersionFlag) {
//                   SerialProtReset(mApplication.HdxBaud);
//               }else
//               {
//                   SerialProtReset(mApplication.PT488ABaud);
//               }

                nopaper=false;
                WriteRunning=false;
                Log.v(TAG,"The Print Progress End !!!");
                if(isClose) {
                }
            }

            handler.sendMessage(handler
                    .obtainMessage(ENABLE_BUTTON, 1, 0, null));
        }
    }

    private class printStateThead extends Thread{
        @Override
        public void run() {
            super.run();

            while (!PTIsReady)
            {
//                if()
            }
        }
    }

    private void mOutputStreamClear() {
        try {
            mOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sleep(300);
    }
    //****************å…¼å®¹æ™®ç‘žç‰¹å’Œå¥½å¾·èŠ¯æ‰“å�°æœºæŽ¥å�£******start**********/


    /**
     * Function printBarcode
     * @return None
     * @author  zhouzy
     * @date 20141223
     * @note
     */
    private void printBarcode()throws Exception
    {
    //       Bitmap bitmap= BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/time1.bmp"));

        Bitmap bitmap=CreateCode("12191126511",BarcodeFormat.QR_CODE,256,256);
        if(bitmap!=null){
            Log.v(TAG,"Find the Bmp");
            if(printVersionFlag){
                printGraphDemo(bitmap);
            }else {
                if(PrintDriveVer==1){
                    sleep(300);
                    printLogoNew(bitmap);
                }else{
                    printLogo(bitmap);
                }

            }
        }
    }

    /**
     * Function printBarcode
     * @return None
     * @author  zhouzy
     * @date 20141223
     * @note
     */
    private void printDoubleBitmap()throws Exception{
               if(!printVersionFlag) {
                   Bitmap bitmap0 = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/blackblock.bmp"));
                   Bitmap bitmap1 = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/line.bmp"));
                  // Looper.prepare();
                   //   progressDialog.show(this, "å›¾ç‰‡åŠ è½½", "å›¾ç‰‡åŠ è½½ä¸­,è¯·ç¨�å€™â€¦â€¦");
                   storeBitmap(bitmap0, 2);
                   storeBitmap(bitmap1, 3);
                   //   progressDialog.dismiss();
                  // Looper.loop();
                   sendBitmapPrintCmd();
               }
    }

    /**
     * Function check printer version
     * @return None
     * @author  zhouzy
     * @date 20140702
     * @note
     */
    private void  printerVersionCheck(int nstep)
    {
        try {
            PWMControl.PrinterEnable(1);
            ConsoleActivity.this.sleep(100);
            lock.acquire();
            sendCommand(0x1b,0x03);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConsoleActivity.this.sleep(100);
            lock.release();
            PWMControl.PrinterEnable(0);
        }

    }

    /**
     * Function  Print initial
     * @return None
     * @author  zhouzy
     * @date 20140702
     * @note
     */
    private void  printInt()
    {
        if(printVersionFlag)
        {
            sendCommand(0x1b, 0x40);
            ConsoleActivity.this.sleep(1000);
        }
        else
        {
            //åˆ�å§‹åŒ–
            sendCommand(0x12,0x20,0x32);
            ConsoleActivity.this.sleep(1000);
//
//            sendCommand(0x1b,0x40);
            //å·¦é—´è·�
            sendCommand(0x1d,0x4c,0x08,0x00);
            //æ‰“å�°æµ“åº¦
            sendCommand(0x12,0x23,0x03);

    //        sendCommand(0x1d,0x4d,(byte)0xC8,0x00);


        }
    }

    /**
     * Function  Paper skip a line
     * @return None
     * @author  zhouzy
     * @date 20140702
     * @note
     */
    private void  step(int line)
    {
//        if (nstep>=256)
//            return;
        int i=0;
        if(line<=0)
            return;
        if(printVersionFlag)
        {

        }else
        {
            for(i=0;i<line;i++) {
                //èµ°çº¸
                sendCommand(0x1b, 0x4a, 0x80);
                sendCommand(0x0a);
            }
        }
    }

    /**
     * Function  Print char code
     * @return None
     * @author  zhouzy
     * @date 20140702
     * @note
     */
    private void PrintHeadCharDemo() {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("-----------------------------");
        arr.add("TP PRINT TEST");
        if(printVersionFlag)
        {
            sendCommand(0x1b, 0x55, 0x01); // å€�é«˜å‘½ä»¤
            sendString(arr.get(0));// ç ´æŠ˜å�·

            sendCommand( 0x1b, 0x55, 0x02); // å€�é«˜å‘½ä»¤
            sendCommand(0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
            sendString(arr.get(1));// TP PRINT TEST
            sendCommand(0x1b, 0x56, 0x01); // å�–æ¶ˆå€�å®½å‘½ä»¤
            sendCommand(0x1b, 0x55, 0x01); // å�–æ¶ˆå€�é«˜å‘½ä»¤

        }else
        {
            sendCommand(0x0a);
            ConsoleActivity.this.sleep(200);

            sendCommand(0x1d, 0x21, 0x00);// å€�é«˜å‘½ä»¤
            sendString(arr.get(0));// ç ´æŠ˜å�·
            sendCommand(0x0a);

            sendCommand(0x1d,0x21,0x11);// å€�é«˜å‘½ä»¤ å€�å®½å‘½ä»¤
            sendString(arr.get(1));// TP PRINT TEST
            sendCommand(0x0a);

            sendCommand(0x1d,0x21,0x00); // å�–æ¶ˆå€�å®½å€�é«˜å‘½ä»¤
            sendCommand(0x0a);
        }
    }

    /**
     * Function  Print barcode demo
     * @return None
     * @author  zhouzy
     * @date 20140702
     * @note
     */
    private void printBarcodeDemo()
    {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("Bar Code Test:");
        arr.add("-----------------------------");

        sendString(arr.get(0));
        sendCommand(0x0a);

        sendString(arr.get(1));
        sendCommand(0x0a);
        if (printVersionFlag)
        {
            sendCommand(0x1d, 0x6b, 0x00, 0x01, 0x03, 0x03, 0x04, 0x05, 0x06, 0x07,
                    0x08, 0x09, 0x03, 0x03);
//            sendCommand(29, 107, 0, //UPC-A
//                    4,1,2,1,9,1,0,5,8,5,9);
            ConsoleActivity.this.sleep(1000);
        } else
        {
            sendCommand(0x1d, 0x48, 0x00);
            sendCommand(0x1d, 0x77, 0x04);
            sendCommand(0x1d, 0x68, 0x64);
            sendCommand(0x1d, 0x6b, 0x46, 0x0A, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39);
//            sendCommand(0x1d, 0x6b, 0x46, 0x0A, 0x34, 0x31, 0x32, 0x31, 0x39, 0x31, 0x30, 0x35, 0x38,0x35, 0x39, 0x31);
        }
    }

    /**
     * Function  Print char on different font size
     * @return None
     * @author  zhouzy
     * @date 20140702
     * @note
     */
    private void FontSizeTest()
    {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("Font Size Test");
        arr.add("-----------------------------");
        arr.add("PRINT TEST");
        arr.add("PRINT TEST");
        arr.add("PRINT TEST");
        arr.add("PRINT TEST");

        if(printVersionFlag)
        {
            ConsoleActivity.this.sleep(200);
//        sendCommand(0x0a);
            sendString(arr.get(0));// Font size test
            sendCommand(0x0a);

            sendString(arr.get(1));// ç ´æŠ˜å�·
            sendCommand(0x0a);// æ�¢è¡Œ

            sendString(arr.get(2));// æ­£å¸¸é«˜åº¦çš„print test

            // 2å€�é«˜åº¦çš„print test
            sendCommand(0x0a,0x1b, 0x55, 0x02); // å€�é«˜å‘½ä»¤
            sendCommand(0x0a,0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
            sendString(arr.get(3));

            // 3å€�é«˜åº¦çš„print test
            sendCommand(0x0a, 0x1b, 0x55, 0x03); // å€�é«˜å‘½ä»¤
            sendCommand(0x0a, 0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
            sendString(arr.get(4));

            // 4å€�é«˜åº¦çš„print test
            sendCommand(0x0a, 0x1b, 0x55, 0x04); // å€�é«˜å‘½ä»¤
            sendCommand(0x0a, 0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
            sendString(arr.get(5));
            sendCommand(0x0a,0x1b, 0x56, 0x01); // å�–æ¶ˆå€�å®½å‘½ä»¤
            sendCommand(0x0a, 0x1b, 0x55, 0x01);// å�–æ¶ˆå€�é«˜å‘½ä»¤

        }else {
            sendString(arr.get(0));
            sendCommand(0x0a);

            sendString(arr.get(1));
            sendCommand(0x0a);

//            sendString(arr.get(2));//æ­£å¸¸å­—ä½“
//            sendCommand(0x0a);
//
//            sendCommand( 0x1d, 0x21, 0x01); // å€�å®½å‘½ä»¤
//            sendCommand(0x0a);
//            sendString(arr.get(3));
//            sendCommand(0x0a);
//
//            sendCommand( 0x1d, 0x21, 0x10); // å€�é«˜å‘½ä»¤
//            sendCommand(0x0a);
//            sendString(arr.get(4));
//            sendCommand(0x0a);
//
//            sendCommand( 0x1d, 0x21, 0x11); // å€�é«˜å€�å®½å‘½ä»¤
//            sendCommand(0x0a);
//            sendString(arr.get(5));
//            sendCommand(0x0a);

            sendCommand( 0x1b, 0x21, 0x01); // å€�é«˜å€�å®½å‘½ä»¤
            sendString(arr.get(2));
            sendCommand(0x0a);

            sendCommand( 0x1b, 0x21, 0x00); // å€�é«˜å€�å®½å‘½ä»¤
            sendString(arr.get(3));
            sendCommand(0x0a);

            sendCommand( 0x1b, 0x21, 0x31); // å€�é«˜å€�å®½å‘½ä»¤
            sendString(arr.get(4));
            sendCommand(0x0a);

            sendCommand( 0x1b, 0x21, 0x30); // å€�é«˜å€�å®½å‘½ä»¤
            sendString(arr.get(5));
            sendCommand(0x0a);

            sendCommand(0x1d,0x21,0x00); // å�–æ¶ˆå€�å®½å€�é«˜å‘½ä»¤
            sendCommand(0x0a);
            sleep(300);
        }

    }
  
    //打印凭证
    private void printPingZheng()
    {
    	//打印log
        /* <center>$$<img src="logo"/>$$</center>$$<br />$$ */
    	// printLogo("printlineHex.bin", 384);
       //  sleep(300);
    	//arr
    /*     int count = print_arrstring.size();
         for(int i=0; i<count; i++)
         {
        	 //print fontsiz8x16
        	if(i==0)
        	{
        		 sendCommand(0x0a,0x1b,0x56,0x02); // 倍宽
        		 sendString(print_arrstring.get(i));
        		 sendCommand(0x0a,0x1b,0x56,0x01); // 正常
        		 sendCommand(0x0a);//换行        		     
        	}else {
        		sendString(print_arrstring.get(i));
        		sendCommand(0x0a);//换行
        	}
        	 sleep(300);
         }
         */
 //打印结束
         sendCommand(0x0a,0x1b,0x56,0x02); // 倍宽
		 sendString("客户确认:");
		 sendCommand(0x0a,0x1b,0x56,0x01); // 正常
		 sendCommand(0x0a);//换行 
		 sendString("********************************");
		 //sendCommand(0x0a);//换行
		 sendString("爱电影网,方便您的电影生活");
		 sendCommand(0x0a);//换行
		 sendString("www.imovie.cn 4006 88 9777");
		 sendCommand(0x0a);//换行
		 sendString("********************************");
		 sendCommand(0x0a);//换行
  		 sendCommand(0x0a);//换行
  		 sendCommand(0x0a);//换行
  		 sendCommand(0x0a);//换行
    }
    /**
     * Function  Print characters test
     * @return None
     * @author  zhouzy
     * @date 20140702
     * @note
     */
    private void printCharactersTest()
    {
        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Characters Test:");// æ‰“å�°å�„ç±»ç‰¹æ®Šå­—ç¬¦
        arr.add("爱电影网电影兑换凭证");// æ‰“å�°å�„ç±»ç‰¹æ®Šå­—ç¬¦
       // arr.add(getString(R.string.CharTest));// æ‰“å�°å�„ç±»ç‰¹æ®Šå­—ç¬¦ã€�æ±‰å­—

        if(printVersionFlag)
        {
            arr.add(getString(R.string.CharTest));// æ‰“å�°å�„ç±»ç‰¹æ®Šå­—ç¬¦ã€�æ±‰å­—
            sendString(arr.get(0));// å­—ç¬¦æµ‹è¯•
            sendCommand(0x0a);// æ�¢è¡Œ

            sendString(arr.get(1));// ç ´æŠ˜å�·
            sendCommand(0x0a);// æ�¢è¡Œ

            sendString(arr.get(2));// å­—ç¬¦
            sleep(1000);
        }else
        {
            sendString(arr.get(0));
            sendCommand(0x0a);
            sendString(arr.get(1));
            sendCommand(0x0a);
            /*
            try {
                mOutputStream.write(PRTUtil.getPrintBytes(getString(R.string.CharTest)));
                sendCommand(0x0a);
                sleep(1000);
            }catch (IOException e)
            {
                e.printStackTrace();
            }
            */
        }
    }
    /**
     * Function  Print  test end
     * @return None
     * @author  zhouzy
     * @date 20140702
     * @note
     */

    private void printTestEnd() {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("-----------------------------");
        arr.add("              Print Test End");
        arr.add("-----------------------------");

        sendString(arr.get(0));//ç ´æŠ˜å�·
        sendCommand(0x0a);
        sendString(arr.get(1));// Line Test
        sendCommand(0x0a);
        sendString(arr.get(2));// ç ´æŠ˜å�·
        sendCommand(0x0a);
        if(printVersionFlag)
        {
            sendCommand(0x1b, 0x64, 0x8);// æµ‹è¯•ç»“æ�Ÿå¾€ä¸‹èµ°çº¸50ç‚¹è¡Œ
        }
        else {
            step(1);
        }
    }

    /**
     * Function  Print device base information
     * @return None
     * @author  zhouzy
     * @date 20140702
     * @note
     */
    private void printDeviceBaseInfo()
    {
        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Device Base Information");
        arr.add("-----------------------------");
        arr.add("Printer Version:");
        arr.add("  V05.2.0.3");
        arr.add("Printer Gray: 3");
        arr.add("Soft Version:");
        arr.add("  TPDemo.G50.0.Build140313");
        arr.add("Battery Level: 100%");
        arr.add("CSQ Value: 24");
        arr.add("IMEI:" + getDeviceIMEI());
        arr.add(getString(R.string.PrintTemp1));
        arr.add(getString(R.string.PrintTemp2));

        sendString(arr.get(0));// Device Base Information
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(1));// ç ´æŠ˜å�·
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(2));// Printer Version
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(3));// Printer Version Value
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(4));// Printer Gray
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(5));// Soft Version
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(6));// Soft Version Value
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(7));// Battery Level
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(8));// CSQ Value
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(9));// IMEI
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(10));// THP Temp:before print
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(11));// THP Temp:after print
        sendCommand(0x0a);// æ�¢è¡Œ
    }

    /**
     * Function  Print mosaic logo
     * @return None
     * @author  zhouzy
     * @date 20140704
     * @note
     */
    private  void printlineDemo() throws Exception
    {
        if(printVersionFlag) {
            printGraphDemo("printlineHex.bin", 384);
        }else {
            if(PrintDriveVer==1){
                sleep(300);
                printLogoNew("printlineHex.bin", 384);
            }else{
                printLogo("printlineHex.bin", 384);
            }

        }
    }

    /**
     * Function  Print mosaic logo
     * @return None
     * @author  zhouzy
     * @date 20140704
     * @note
     */
    private  void printMosaicDemo() throws Exception
    {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("Black block Test:");
        arr.add("-----------------------------");
        sendString(arr.get(0));// Black block Test
        sendCommand(0x0a);
        sendString(arr.get(1));// ç ´æŠ˜å�·
        sendCommand(0x0a);

        if(printVersionFlag)
        {
            printGraphDemo("mosaicHex.bin", 384);
            sleep(1000);
        }else {

            if(PrintDriveVer==1){
                sleep(300);
                printLogoNew("mosaicHex.bin", 384);
            }else{
                printLogo("mosaicHex.bin", 384);
                sleep(2000);
            }
        }
    }

    /**
     * Function  Print Oblique Line
     * @return None
     * @author  zhouzy
     * @date 20140704
     * @note
     */
    private  void printObliqueLineDemo() throws Exception
    {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("Line Test:");
        arr.add("-----------------------------");
        sendString(arr.get(0));// Black block Test
        sendCommand(0x0a);
        sendString(arr.get(1));// ç ´æŠ˜å�·
        sendCommand(0x0a);
        if(printVersionFlag)
        {
            printGraphDemo("lineHex.bin", 384);
        }
        else {

            if(PrintDriveVer==1){
                sleep(300);
                printLogoNew("lineHex.bin", 384);
            }else{
                printLogo("lineHex.bin", 384);
            }
//            sleep(1000);
        }
    }

    private final static int ARGB_MASK_RED = 0x00ff0000;
    private final static int ARGB_MASK_GREEN = 0x0000ff00;
    private final static int ARGB_MASK_BLUE = 0x000000ff;
    private final static int RGB565_MASK_RED = 0xF800;
    private final static int RGB565_MASK_GREEN = 0x07E0;
    private final static int RGB565_MASK_BLUE = 0x001F;
    private final static int color = 128;
    //  å›¾ç‰‡ç‚¹é˜µå¤„ç�†
    private byte[] bitmapFormat(Bitmap bitmap)
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if(width>384)
        {
            return null;
        }
        // è°ƒæ•´åˆ°é€‚åº”æ‰“å�°æœºçš„æ•´å€�æ•°
        int adjustedHeight = (height + 7) / 8 * 8; // 8çš„å€�æ•°ï¼Œä¸€æ¬¡æ‰“å�°é«˜åº¦æ˜¯8
        Log.v(TAG, "bitmap.getWidth():"+bitmap.getWidth()+",bitmap.getHeight():" +bitmap.getHeight()+",adjustedHeight : "+ adjustedHeight);
        // å¾ªçŽ¯å›¾ç‰‡åƒ�ç´ æ‰“å�°å›¾ç‰‡
        // å¾ªçŽ¯é«˜,ä¸€æ¬¡æ‰“å�°8åƒ�ç´ é«˜åº¦
        byte[] bmpByte=new byte[adjustedHeight/8*width];

        for (int i = 0; i < (adjustedHeight / 8); i++) {
        byte[] data = new byte[width];

        byte xL = (byte) (width % 256);
        byte xH = (byte) (width / 256);

        for (int k = 0; k < 8; k++) {
            // å¾ªçŽ¯å®½
            for (int x = 0; x < width; x++) {
                int y = (i * 8) + k;
                // ç¡®ä¿�åœ¨å›¾ç‰‡èŒƒå›´å†…
                if (y < height) {
                    int pixel = bitmap.getPixel(x, y);
                    if (Color.red(pixel) == 0
                            || Color.green(pixel) == 0
                            || Color.blue(pixel) == 0) {
                        // é«˜ä½�åœ¨å·¦ï¼Œæ‰€ä»¥ä½¿ç”¨128 å�³ç§»
                        data[x] += (byte) (128 >> (y % 8));
                    }
                }
            }
        }
        Log.v(TAG, "bmpData : " + data.toString());
        System.arraycopy(data,0,bmpByte,width*i,width);
    }
        return bmpByte;
    }

    /**
     * Function  Print logo
     * @return None
     * @author  zhouzy
     * @date 20141218
     * @note
     */
    private void printLogo(Bitmap bitmap) throws Exception {
        boolean isNewLogo=false;
        try {
            byte[] bmpByte=bitmapFormat(bitmap);
            if(bmpByte==null)
            {
                return;
            }
            int imageWidth = bitmap.getWidth();
            InputStream in = new ByteArrayInputStream(bmpByte);
            int leng =in.available();
            int height = leng*8/imageWidth;
            String MD5Str="";
            byte[] buffer=new byte[leng];
            in.read(buffer);
            MD5Str=MD5Util.getMD5String(buffer);

            //å�–ä¸Šä¸€æ¬¡å­˜å‚¨å›¾ç‰‡æ•°æ�®
            if (null != preferences) {
                try {
                    lastMD5Str=preferences.getString(logoMD5Store, "0");
                    lastLogoWidth = Integer.parseInt(preferences.getString(logoWidthStore, "384")) ;
                    lastLogoHeight= Integer.parseInt(preferences.getString(logoHeightStore, "0")) ;

                }catch (NumberFormatException e)
                {
                    lastLogoWidth = 0 ;
                    lastLogoHeight=0;
                    lastMD5Str="";
                    e.printStackTrace();
                }
            }

            //åˆ¤æ–­å›¾ç‰‡çš„å®½å’Œé«˜
            if(imageWidth!=lastLogoWidth||height!=lastLogoHeight)
            {
                isNewLogo=true;
            }
            else{
                //åˆ¤æ–­MD5æ˜¯å�¦ä¸€æ ·
                if(!lastMD5Str.equals(MD5Str))
                {
                    isNewLogo=true;
                }
            }
            //æ–°å›¾ç‰‡ä¿�å­˜æ•°æ�®ï¼Œç„¶å�Žå�‘é€�åˆ°æ‰“å�°æœºå†…å­˜
            isNewLogo=true;
            if(isNewLogo)
            {
                //å�‘é€�ç‚¹é˜µæ•°æ�®åˆ°æ‰“å�°æœº

                /*åˆ¤å®šå®½åº¦æ˜¯å�¦ä¸º8çš„æ•´æ•°å€�ï¼Œå�¦åˆ™éœ€è¦�è¡¥é½�*/
                boolean isAligned = (imageWidth % 8 ==0) ? true:false;
                /*å¤„ç�†å›¾ç‰‡å‘½ä»¤ä¸­å®½å’Œé«˜å�•å­—èŠ‚*/
                int LogoWidthLowByte,LogoWidthHighByte,LogoHeightLowByte,LogoHeightHighByte;
                LogoWidthHighByte= (int)((imageWidth/8)>> 8);
                if (isAligned)
                {
                    LogoWidthLowByte = (int)(((imageWidth/8) << 8) >> 8);
                }
                else
                {
                    LogoWidthLowByte = (int)(((imageWidth/8) << 8) >> 8) + 1;
                }
                LogoHeightHighByte= (int)((height/8)>> 8);
                LogoHeightLowByte = (int)(((height/8) << 8) >> 8);
                //å�‘é€�å†™å›¾ç‰‡å‘½ä»¤
                sendCommand(0x1C,0x71,0x01,LogoWidthLowByte,LogoWidthHighByte,LogoHeightLowByte,LogoHeightHighByte);
                Log.d(TAG,"LogoWidthLowByte="+LogoWidthLowByte+";LogoWidthHighByte="+LogoWidthHighByte
                        +";LogoHeightLowByte="+LogoHeightLowByte+";LogoHeightHighByte="+LogoHeightHighByte);
                int  widthIndex,heightIndex;
                byte[]  verticalBuffer=new byte[1024] ;
                int sendSize=0;
                System.out.print("Graphy:");
                for (widthIndex = 0; widthIndex < (imageWidth); widthIndex++)
                {
//                    verticalBuffer= Arrays.copyOf(buffer, height/8);
                    for(heightIndex = 0; heightIndex < height/8; heightIndex++)
                    {
                        verticalBuffer[heightIndex] = reverseByte(buffer[widthIndex + heightIndex*imageWidth]);
                    }
                    //ä¸‹è½½å›¾ç‰‡æ•°æ�®
                    mOutputStream.write(verticalBuffer,0,height/8);

                    sendSize += (height / 8);

                    if (sendSize >= 1536)
                    {
                        sleep(300);
                        sendSize = 0;
                    }
                }
                System.out.println("");

                int i=0;
                checkPrinterState=true;
                PTIsReady=false;
                while ((!PTIsReady)&&(i<60))
                {
                    sleep(100);
                    i++;
                }
                PTIsReady=false;
                checkPrinterState=false;
//                printStateThead()
                //ä¿�å­˜å›¾ç‰‡è§„æ ¼æ•°æ�®
                String storeStr;
                storeStr=Integer.toString(imageWidth);
                editor.putString(logoWidthStore,storeStr);
                editor.commit();
                storeStr=Integer.toString(height);
                editor.putString(logoHeightStore,storeStr);
                editor.commit();
                editor.putString(logoMD5Store,MD5Str);
                editor.commit();
            }
            in.close();
            //æ‰“å�°å›¾ç‰‡
            if (nopaper)
                return;
            sleep(300);
            sendCommand(0x1C,0x70,0x01,0x30);
//            sleep(2000);
            sendLineFeed();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Function  Print logo
     *
     * @return None
     * @author zhouzy
     * @date 20140702
     * @note
     */
    private void printLogoNew(String assetsPath, int imageWidth) throws Exception {
        boolean isNewLogo = false;
        try {
            // å¾—åˆ°èµ„æº�ä¸­çš„assetæ•°æ�®æµ�
            InputStream in = getResources().getAssets().open(assetsPath);
            int leng = in.available();
            int height = leng * 8 / imageWidth;
            String MD5Str = "";
            byte[] buffer = new byte[leng];
            in.read(buffer);

            //æ–°å›¾ç‰‡ä¿�å­˜æ•°æ�®ï¼Œç„¶å�Žå�‘é€�åˆ°æ‰“å�°æœºå†…å­˜
            isNewLogo=true;
            if (isNewLogo) {

                //å�‘é€�ç‚¹é˜µæ•°æ�®åˆ°æ‰“å�°æœº
                /*åˆ¤å®šå®½åº¦æ˜¯å�¦ä¸º8çš„æ•´æ•°å€�ï¼Œå�¦åˆ™éœ€è¦�è¡¥é½�*/
                boolean isAligned = (imageWidth % 8 == 0) ? true : false;
                /*å¤„ç�†å›¾ç‰‡å‘½ä»¤ä¸­å®½å’Œé«˜å�•å­—èŠ‚*/
                int LogoWidthLowByte, LogoWidthHighByte, LogoHeightLowByte, LogoHeightHighByte;
                LogoWidthHighByte = (int) ((imageWidth / 8) >> 8);
                if (isAligned) {
                    LogoWidthLowByte = (int) (((imageWidth / 8) << 8) >> 8);
                } else {
                    LogoWidthLowByte = (int) (((imageWidth / 8) << 8) >> 8) + 1;
                }
                LogoHeightHighByte = (int) ((height / 8) >> 8);
                LogoHeightLowByte = (int) (((height / 8) << 8) >> 8);
                //   sendCommand(0x1C, 0x71, 0x01, LogoWidthLowByte, LogoWidthHighByte, LogoHeightLowByte, LogoHeightHighByte);
                sendCommand(0x1C, 0x72, LogoWidthLowByte, LogoWidthHighByte, LogoHeightLowByte, LogoHeightHighByte);

                int widthIndex, heightIndex;
                byte[] verticalBuffer = new byte[1024];
                for (heightIndex = 0; heightIndex < height / 8; heightIndex++)
                {
                    for (widthIndex = 0; widthIndex < imageWidth; widthIndex++) {
                        verticalBuffer[widthIndex] = buffer[widthIndex + heightIndex * imageWidth];
                    }
                    mOutputStream.write(verticalBuffer, 0, imageWidth);
                    //   sleep(35);
                }

                int i = 0;
                checkPrinterState = true;
                PTIsReady = false;
                while ((!PTIsReady) && (i < 60)) {
                    sleep(100);
                    i++;
                }
                PTIsReady = false;
                checkPrinterState = false;

            }
            in.close();
            //æ‰“å�°å›¾ç‰‡
            if (nopaper)
                return;
            sleep(300);
            //  sendCommand(0x1C, 0x70, 0x01, 0x30);
//            sleep(2000);
            sendLineFeed();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Function  Print logo
     * @return None
     * @author  zhouzy
     * @date 20141218
     * @note
     */
    private void printLogoNew(Bitmap bitmap) throws Exception {
        boolean isNewLogo=false;
        try {
            byte[] bmpByte=bitmapFormat(bitmap);
            if(bmpByte==null)
            {
                return;
            }
            int imageWidth = bitmap.getWidth();
            InputStream in = new ByteArrayInputStream(bmpByte);
            int leng =in.available();
            int height = leng*8/imageWidth;
            String MD5Str="";
            byte[] buffer=new byte[leng];
            in.read(buffer);
            //     MD5Str=MD5Util.getMD5String(buffer);


            //æ–°å›¾ç‰‡ä¿�å­˜æ•°æ�®ï¼Œç„¶å�Žå�‘é€�åˆ°æ‰“å�°æœºå†…å­˜
            isNewLogo=true;
            if(isNewLogo)
            {
                //å�‘é€�ç‚¹é˜µæ•°æ�®åˆ°æ‰“å�°æœº

                /*åˆ¤å®šå®½åº¦æ˜¯å�¦ä¸º8çš„æ•´æ•°å€�ï¼Œå�¦åˆ™éœ€è¦�è¡¥é½�*/
                boolean isAligned = (imageWidth % 8 ==0) ? true:false;
                /*å¤„ç�†å›¾ç‰‡å‘½ä»¤ä¸­å®½å’Œé«˜å�•å­—èŠ‚*/
                int LogoWidthLowByte,LogoWidthHighByte,LogoHeightLowByte,LogoHeightHighByte;
                LogoWidthHighByte= (int)((imageWidth/8)>> 8);
                if (isAligned)
                {
                    LogoWidthLowByte = (int)(((imageWidth/8) << 8) >> 8);
                }
                else
                {
                    LogoWidthLowByte = (int)(((imageWidth/8) << 8) >> 8) + 1;
                }
                LogoHeightHighByte= (int)((height/8)>> 8);
                LogoHeightLowByte = (int)(((height/8) << 8) >> 8);
                //å�‘é€�å†™å›¾ç‰‡å‘½ä»¤
//                sendCommand(0x1C,0x71,0x01,LogoWidthLowByte,LogoWidthHighByte,LogoHeightLowByte,LogoHeightHighByte);
                sendCommand(0x1C,0x72,LogoWidthLowByte,LogoWidthHighByte,LogoHeightLowByte,LogoHeightHighByte);

                Log.d(TAG,"LogoWidthLowByte="+LogoWidthLowByte+";LogoWidthHighByte="+LogoWidthHighByte
                        +";LogoHeightLowByte="+LogoHeightLowByte+";LogoHeightHighByte="+LogoHeightHighByte);

                int widthIndex, heightIndex;
                byte[] verticalBuffer = new byte[1024];
                checkPrinterState=true;
                PTIsReady=false;
                for (heightIndex = 0; heightIndex < height / 8; heightIndex++)
                {
                    for (widthIndex = 0; widthIndex < imageWidth; widthIndex++) {
                        verticalBuffer[widthIndex] = buffer[widthIndex + heightIndex * imageWidth];
                    }
                    mOutputStream.write(verticalBuffer, 0, imageWidth);
                    sleep(35);
                }

                int i=0;


                while ((!PTIsReady)&&(i<60))
                {
                    sleep(100);
                    i++;
                }
                PTIsReady=false;
                checkPrinterState=false;

            }
            in.close();
            //æ‰“å�°å›¾ç‰‡
            if (nopaper)
                return;

            sendLineFeed();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
         * Function  Print logo
         * @return None
         * @author  zhouzy
         * @date 20140702
         * @note
         */
    private void printLogo(String assetsPath, int imageWidth) throws Exception {
        boolean isNewLogo=false;
        try {
            // å¾—åˆ°èµ„æº�ä¸­çš„assetæ•°æ�®æµ�
            InputStream in = getResources().getAssets().open(assetsPath);
            int leng =in.available();
            int height = leng*8/imageWidth;
            String MD5Str="";
            byte[] buffer=new byte[leng];
            in.read(buffer);
            MD5Str=MD5Util.getMD5String(buffer);

            //å�–ä¸Šä¸€æ¬¡å­˜å‚¨å›¾ç‰‡æ•°æ�®
            if (null != preferences) {
                try {
                    lastMD5Str=preferences.getString(logoMD5Store, "0");
                    lastLogoWidth = Integer.parseInt(preferences.getString(logoWidthStore, "384")) ;
                    lastLogoHeight= Integer.parseInt(preferences.getString(logoHeightStore, "0")) ;

                }catch (NumberFormatException e)
                {
                    lastLogoWidth = 0 ;
                    lastLogoHeight=0;
                    lastMD5Str="";
                    e.printStackTrace();
                }
            }

            //åˆ¤æ–­å›¾ç‰‡çš„å®½å’Œé«˜
            if(imageWidth!=lastLogoWidth||height!=lastLogoHeight)
            {
                isNewLogo=true;
            }
            else{
                //åˆ¤æ–­MD5æ˜¯å�¦ä¸€æ ·
                if(!lastMD5Str.equals(MD5Str))
                {
                    isNewLogo=true;
                }
            }
            //æ–°å›¾ç‰‡ä¿�å­˜æ•°æ�®ï¼Œç„¶å�Žå�‘é€�åˆ°æ‰“å�°æœºå†…å­˜
           //isNewLogo=true;
            if(isNewLogo)
            {
                //å�‘é€�ç‚¹é˜µæ•°æ�®åˆ°æ‰“å�°æœº

                /*åˆ¤å®šå®½åº¦æ˜¯å�¦ä¸º8çš„æ•´æ•°å€�ï¼Œå�¦åˆ™éœ€è¦�è¡¥é½�*/
                boolean isAligned = (imageWidth % 8 ==0) ? true:false;
                /*å¤„ç�†å›¾ç‰‡å‘½ä»¤ä¸­å®½å’Œé«˜å�•å­—èŠ‚*/
                int LogoWidthLowByte,LogoWidthHighByte,LogoHeightLowByte,LogoHeightHighByte;
                LogoWidthHighByte= (int)((imageWidth/8)>> 8);
                if (isAligned)
                {
                    LogoWidthLowByte = (int)(((imageWidth/8) << 8) >> 8);
                }
                else
                {
                    LogoWidthLowByte = (int)(((imageWidth/8) << 8) >> 8) + 1;
                }
                LogoHeightHighByte= (int)((height/8)>> 8);
                LogoHeightLowByte = (int)(((height/8) << 8) >> 8);
                //å�‘é€�å†™å›¾ç‰‡å‘½ä»¤
                sendCommand(0x1C,0x71,0x01,LogoWidthLowByte,LogoWidthHighByte,LogoHeightLowByte,LogoHeightHighByte);
                Log.d(TAG,"LogoWidthLowByte="+LogoWidthLowByte+";LogoWidthHighByte="+LogoWidthHighByte
                    +";LogoHeightLowByte="+LogoHeightLowByte+";LogoHeightHighByte="+LogoHeightHighByte);
                int  widthIndex,heightIndex;
                byte[]  verticalBuffer=new byte[1024] ;
                int sendSize=0;
                System.out.print("Graphy:");
                for (widthIndex = 0; widthIndex < (imageWidth); widthIndex++)
                {
//                    verticalBuffer= Arrays.copyOf(buffer, height/8);
                    for(heightIndex = 0; heightIndex < height/8; heightIndex++)
                    {
                        verticalBuffer[heightIndex] = reverseByte(buffer[widthIndex + heightIndex*imageWidth]);
                    }
                    //ä¸‹è½½å›¾ç‰‡æ•°æ�®
                    mOutputStream.write(verticalBuffer,0,height/8);

                    sendSize += (height / 8);

                    if (sendSize >= 1536)
                    {
                        sleep(300);
                        sendSize = 0;
                    }
                }
                System.out.println("");

                int i=0;
                checkPrinterState=true;
                PTIsReady=false;
                while ((!PTIsReady)&&(i<60))
                {
                    sleep(100);
                    i++;
                }
                PTIsReady=false;
                checkPrinterState=false;
//                printStateThead()
                //ä¿�å­˜å›¾ç‰‡è§„æ ¼æ•°æ�®
                String storeStr;
                storeStr=Integer.toString(imageWidth);
                editor.putString(logoWidthStore,storeStr);
                editor.commit();
                storeStr=Integer.toString(height);
                editor.putString(logoHeightStore,storeStr);
                editor.commit();
                editor.putString(logoMD5Store,MD5Str);
                editor.commit();
            }
            in.close();
            //æ‰“å�°å›¾ç‰‡
            if (nopaper)
                return;
            sleep(300);
            sendCommand(0x1C,0x70,0x01,0x30);
//            sleep(2000);
            sendLineFeed();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }





    /**
     * Function  send bitmap to printer flash
     * @param bitmap bitmap;
     * @param index bitmap index ,max number is 4;
     * @return false : fail;true : success
     * @author  zhouzy
     * @date 20141224
     * @note
     */
    private boolean storeBitmap(Bitmap bitmap,int index) throws Exception {
        if(bitmap==null)
        {
            return false;
        }
        if(index>4||index<0)
        {
            index=4;
        }
        byte[] bmpByte=bitmapFormat(bitmap);
        if(bmpByte==null)
        {
            return false;
        }
        int imageWidth = bitmap.getWidth();
        InputStream in = new ByteArrayInputStream(bmpByte);
        try {

            int leng =in.available();
            int height = leng*8/imageWidth;
            byte[] buffer=new byte[leng];
            in.read(buffer);

            //å�‘é€�ç‚¹é˜µæ•°æ�®åˆ°æ‰“å�°æœº
            /*åˆ¤å®šå®½åº¦æ˜¯å�¦ä¸º8çš„æ•´æ•°å€�ï¼Œå�¦åˆ™éœ€è¦�è¡¥é½�*/
            boolean isAligned = (imageWidth % 8 ==0) ? true:false;
            /*å¤„ç�†å›¾ç‰‡å‘½ä»¤ä¸­å®½å’Œé«˜å�•å­—èŠ‚*/
            int LogoWidthLowByte,LogoWidthHighByte,LogoHeightLowByte,LogoHeightHighByte;
            LogoWidthHighByte= (int)((imageWidth/8)>> 8);
            if (isAligned)
            {
                LogoWidthLowByte = (int)(((imageWidth/8) << 8) >> 8);
            }
            else
            {
                LogoWidthLowByte = (int)(((imageWidth/8) << 8) >> 8) + 1;
            }
            LogoHeightHighByte= (int)((height/8)>> 8);
            LogoHeightLowByte = (int)(((height/8) << 8) >> 8);
            //å�‘é€�å†™å›¾ç‰‡å‘½ä»¤
            sendCommand(0x1C,0x71,index,LogoWidthLowByte,LogoWidthHighByte,LogoHeightLowByte,LogoHeightHighByte);
            Log.d(TAG,"LogoWidthLowByte="+LogoWidthLowByte+";LogoWidthHighByte="+LogoWidthHighByte
                    +";LogoHeightLowByte="+LogoHeightLowByte+";LogoHeightHighByte="+LogoHeightHighByte);
            int  widthIndex,heightIndex;
            byte[]  verticalBuffer=new byte[1024] ;
            int sendSize=0;
            for (widthIndex = 0; widthIndex < (imageWidth); widthIndex++)
            {
                for(heightIndex = 0; heightIndex < height/8; heightIndex++)
                {
                    verticalBuffer[heightIndex] = reverseByte(buffer[widthIndex + heightIndex*imageWidth]);
                }
                //ä¸‹è½½å›¾ç‰‡æ•°æ�®
                mOutputStream.write(verticalBuffer,0,height/8);

                sendSize += (height / 8);

                if (sendSize >= 1536)
                {
                    sleep(300);
                    sendSize = 0;
                }
            }
            System.out.println("");

            int i=0;
            checkPrinterState=true;
            PTIsReady=false;
            while ((!PTIsReady)&&(i<60))
            {
                sleep(100);
                i++;
            }
            checkPrinterState=false;
            in.close();
            in=null;
            if(!PTIsReady){
                return false;
            }
            PTIsReady=false;
            Log.v(TAG,"Store the bitmap succsee:"+index);
            return true;
        } catch (Exception e) {
            if(in!=null){
                in.close();
                in=null;
            }
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Function  send bitmap print cmd
     * @return false : fail;true : success
     * @author  zhouzy
     * @date 20141224
     * @note
     */
    private void sendBitmapPrintCmd()
    {
        sleep(200);

        sendCommand(0x1C,0x70,0x02,0x30);
        sendLineFeed();

        Log.v(TAG,"print the bitmap succsee:2");

        sleep(2000);

        sendCommand(0x1C,0x70,0x03,0x30);
        sendLineFeed();

        Log.v(TAG,"Store the bitmap succsee:3");
    }


    static byte reverseByte( byte srcByte)
    {
        byte destByte;
        int iBit;

        destByte = 0;
        iBit = 0;
//        for (iBit = 0; iBit < 8; iBit++) {
//            if ((srcByte & (0x80 >> iBit)) != 0) {
//                destByte |= (0x01 << iBit);
//            }
//        }
        destByte=srcByte;
        return destByte;
    }

    //****************å…¼å®¹æ™®ç‘žç‰¹å’Œå¥½å¾·èŠ¯æ‰“å�°æœºæŽ¥å�£*******end*********/






    /**
     * æ‰“å�°å­—ç¬¦æ•°æ�®ï¼ˆè¡¨å¤´éƒ¨åˆ†ï¼‰
     *
     * @param //arr
     */

    private void PrintCharDemo1() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("-----------------------------");
        arr.add("TP PRINT TEST");

//        ConsoleActivity.this.sleep(200);

        sendCommand(0x1b, 0x55, 0x01); // å€�é«˜å‘½ä»¤
        sendString(arr.get(0));// ç ´æŠ˜å�·
//        sendCommand(0x0a);//

        sendCommand( 0x1b, 0x55, 0x02); // å€�é«˜å‘½ä»¤
        sendCommand( 0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
        sendString(arr.get(1));// TP PRINT TEST
        sendCommand(0x1b, 0x56, 0x01); // å�–æ¶ˆå€�å®½å‘½ä»¤
        sendCommand( 0x1b, 0x55, 0x01); // å�–æ¶ˆå€�é«˜å‘½ä»¤

//        ConsoleActivity.this.sleep(200);

    }


    private void PrintQRCodeText() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("QRCode Test:");
        arr.add("-----------------------------");

//        ConsoleActivity.this.sleep(200);
        sendString(arr.get(0));// Black block Test
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(1));// ç ´æŠ˜å�·
//        sendCommand(0x0a);// æ�¢è¡Œ
//
//        ConsoleActivity.this.sleep(200);

    }

    private void PrintLogoCodeText() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Logo Test:");
        arr.add("-----------------------------");

//        ConsoleActivity.this.sleep(200);
        sendString(arr.get(0));// Black block Test
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(1));// ç ´æŠ˜å�·
//        sendCommand(0x0a);// æ�¢è¡Œ
//
//        ConsoleActivity.this.sleep(200);

    }


    /**
     * æ‰“å�°æ�¡ç �å­—ç¬¦
     *
     */

    private void PrintBarCodeText() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Bar Code Test:");
        arr.add("-----------------------------");

//        ConsoleActivity.this.sleep(200);
        sendString(arr.get(0));// Black block Test
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(1));// ç ´æŠ˜å�·
//        sendCommand(0x0a);// æ�¢è¡Œ
//
//        ConsoleActivity.this.sleep(200);

    }

    /**
     * æ‰“å�°é»‘å�—å­—ç¬¦
     *
     */

    private void PrintBlackBlockText() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Black block Test:");
        arr.add("-----------------------------");

//        ConsoleActivity.this.sleep(200);
//       sendCommand(0x0a);// æ�¢è¡Œ
        sendString(arr.get(0));// Black block Test
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(1));// ç ´æŠ˜å�·
//        sendCommand(0x0a);// æ�¢è¡Œ

//        ConsoleActivity.this.sleep(200);

    }

    /**
     * æ‰“å�°å­—ç¬¦æ•°æ�®ï¼ˆè¡¨å¤´éƒ¨åˆ†ï¼‰
     *
     * @param //arr
     */

    private void PrintLineTitle() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Line Test:");
        arr.add("-----------------------------");


        // sendCommand(0x0a, 0x1b, 0x55, 0x01); // å€�é«˜å‘½ä»¤
//        sendCommand(0x0a);// æ�¢è¡Œ
        sendString(arr.get(0));// Line Test

        sendCommand(0x0a);// æ�¢è¡Œ
        sendString(arr.get(1));// ç ´æŠ˜å�·
//        sendCommand(0x0a);// æ�¢è¡Œ

    }

    /**
     * æ‰“å�°ç»“æ�Ÿ
     *
     * @param //arr
     */

    private void PrintTestEnd() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("-----------------------------");
        arr.add("              Print Test End");
        arr.add("-----------------------------");
        sendCommand( 0x1b, 0x55, 0x01); // å€�é«˜å‘½ä»¤
        sendString(arr.get(0));// Line Test

        sendCommand(0x0a);// æ�¢è¡Œ
        sendString(arr.get(1));// ç ´æŠ˜å�·
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(2));// ç ´æŠ˜å�·
         sendCommand(0x0a);// æ�¢è¡Œ
        sendCommand(0x1b, 0x64, 0x8);// æµ‹è¯•ç»“æ�Ÿå¾€ä¸‹èµ°çº¸50ç‚¹è¡Œ


    }

    /**
     * æ‰“å�°è®¾å¤‡åŸºç¡€ä¿¡æ�¯
     *
     * @param //arr
     */

    private void PrintDeviceBaseInfo() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Device Base Information");
        arr.add("-----------------------------");
        arr.add("Printer Version:");
        arr.add("  V05.2.0.3");
        arr.add("Printer Gray: 3");
        arr.add("Soft Version:");
        arr.add("  TPDemo.G50.0.Build140313");
        arr.add("Battery Level: 100%");
        arr.add("CSQ Value: 24");
        arr.add("IMEI:" + getDeviceIMEI());
        arr.add(getString(R.string.PrintTemp1));
        arr.add(getString(R.string.PrintTemp2));

        sendCommand( 0x1b, 0x55, 0x01); // å€�é«˜å‘½ä»¤
        sendString(arr.get(0));// Device Base Information
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(1));// ç ´æŠ˜å�·
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(2));// Printer Version
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(3));// Printer Version Value
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(4));// Printer Gray
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(5));// Soft Version
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(6));// Soft Version Value
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(7));// Battery Level
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(8));// CSQ Value
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(9));// IMEI
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(10));// THP Temp:before print
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(11));// THP Temp:after print
        sendCommand(0x0a);// æ�¢è¡Œ

    }

    /**
     * æ‰“å�°å­—ç¬¦æ•°æ�®Font Size Test å¼€å§‹éƒ¨åˆ†
     *
     * @param //arr
     */

    private void PrintCharDemoLast() {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("Font Size Test");
        arr.add("-----------------------------");
        arr.add("PRINT TEST");
        arr.add("PRINT TEST");
        arr.add("PRINT TEST");
        arr.add("PRINT TEST");

        arr.add("Characters Test:");// æ‰“å�°å�„ç±»ç‰¹æ®Šå­—ç¬¦
        arr.add("-----------------------------");// æ‰“å�°å�„ç±»ç‰¹æ®Šå­—ç¬¦
        arr.add(getString(R.string.CharTest));// æ‰“å�°å�„ç±»ç‰¹æ®Šå­—ç¬¦ã€�æ±‰å­—

        ConsoleActivity.this.sleep(200);
//        sendCommand(0x0a);
        sendString(arr.get(0));// Font size test
        sendCommand(0x0a);

        sendString(arr.get(1));// ç ´æŠ˜å�·
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(2));// æ­£å¸¸é«˜åº¦çš„print test

        // 2å€�é«˜åº¦çš„print test
        sendCommand(0x0a,0x1b, 0x55, 0x02); // å€�é«˜å‘½ä»¤
        sendCommand(0x0a,0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
        sendString(arr.get(3));

        // 3å€�é«˜åº¦çš„print test
        sendCommand(0x0a, 0x1b, 0x55, 0x03); // å€�é«˜å‘½ä»¤
        sendCommand(0x0a, 0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
        sendString(arr.get(4));

        // 4å€�é«˜åº¦çš„print test
        sendCommand(0x0a, 0x1b, 0x55, 0x04); // å€�é«˜å‘½ä»¤
        sendCommand(0x0a, 0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
        sendString(arr.get(5));
        sendCommand(0x0a,0x1b, 0x56, 0x01); // å�–æ¶ˆå€�å®½å‘½ä»¤
        sendCommand(0x0a, 0x1b, 0x55, 0x01);// å�–æ¶ˆå€�é«˜å‘½ä»¤

        sendString(arr.get(6));// å­—ç¬¦æµ‹è¯•
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(7));// ç ´æŠ˜å�·
        sendCommand(0x0a);// æ�¢è¡Œ

        sendString(arr.get(8));// å­—ç¬¦
//        sendCommand(0x0a);// æ�¢è¡Œ

    }

    /**
     * æ‰“å�°å›¾åƒ�æ�¡ç �Demo
     *
     */

    private void PrintBarCodeDemo() {
        // æ‰“å�°æ�¡ç �
//        ConsoleActivity.this.sleep(200);
        sendCodeDemo();
        ConsoleActivity.this.sleep(1000);
    }

    /**
     * æ‰“å�°å›¾åƒ�Demo(æ–¹æ³•3)
     *
     */
    private void printGraphDemo(int resourceID) throws Exception {
        try {
            sendCommand(0x1B, 0x40);// æ‰“å�°æœºåˆ�å§‹åŒ–

            ConsoleActivity.this.sleep(200);

            Resources r = getResources();
            // ä»¥æ•°æ�®æµ�çš„æ–¹å¼�è¯»å�–èµ„æº�
            InputStream is = r.openRawResource(resourceID);
            BitmapDrawable bmpDraw = new BitmapDrawable(is);
            Bitmap bmp = bmpDraw.getBitmap();

            if (mOutputStream != null)
                PrintBmp(bmp, mOutputStream);

            sendLineFeed();
            ConsoleActivity.this.sleep(1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     * æ‰“å�°é©¬èµ›å…‹å›¾åƒ�Demo(æ–¹æ³•1)
//     *
//     */
//    private void printMasaikeGraphDemo() throws Exception {
//
//        try {
//            ConsoleActivity.this.sleep(200);
//            int tempbyte;
//            int len = GraphArray.masaike.length;
//            for (int i = 0; i < len; i++) {
//
//                if ((i % (0x80)) == 0) { // 384:å›¾ç‰‡çš„å®½
//                    Log.e(TAG, "#########sendCommand");
//                    sendCommand(0x1b, 0x4b, 0x80, 0x01);
//                }
//
//                try {
//                    mOutputStream.write((char) GraphArray.masaike[i]);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            sendLineFeed();
//            ConsoleActivity.this.sleep(1000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * æ‰“å�°å›¾åƒ�Demo(æ–¹æ³•1:ç›´æŽ¥æ‰“å�°ä½�å›¾)
     *
     * @param bitmap å®½ä¸�å°�äºŽ256
     *            å‡½æ•°é‡�è½½åŒºåˆ†æ ‡è®°
     * @throws Exception
     */
    private void printGraphDemo(Bitmap bitmap)  throws Exception {
        try {
            ConsoleActivity.this.sleep(200);
            byte[] bmpByte=bitmapFormat(bitmap);
            if(bmpByte==null)
            {
                return;
            }
            int imageWidth = bitmap.getWidth();
            InputStream in = new ByteArrayInputStream(bmpByte);
            int tempbyte;
            int i = 0;
            while ((tempbyte = in.read()) != -1) {

                if ((i % (imageWidth)) == 0) { // å›¾ç‰‡çš„å®½
                    Log.e(TAG, "#########sendCommand");
                    sendCommand(0x1b, 0x4b, imageWidth, 0x01);
                }

                try {
                    mOutputStream.write((char) tempbyte);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                i++;
            }
            in.close();
            sendLineFeed();
            ConsoleActivity.this.sleep(2000);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * æ‰“å�°å›¾åƒ�Demo(æ–¹æ³•2:æŸ¥æ‰¾ç³»ç»Ÿç»�å¯¹è·¯å¾„ä¸­çš„æ–‡ä»¶)
     *
     * @param imagePath
     * @param imageWidth
     * @param Tag
     *            å‡½æ•°é‡�è½½åŒºåˆ†æ ‡è®°
     * @throws Exception
     */
    private void printGraphDemo(String imagePath, int imageWidth, int Tag)
            throws Exception {
        try {
            ConsoleActivity.this.sleep(200);

            File file = new File(imagePath);
            if (file.exists()) {
                FileInputStream in = new FileInputStream(file);
                int tempbyte;
                int i = 0;
                while ((tempbyte = in.read()) != -1) {

                    if ((i % (imageWidth)) == 0) { // å›¾ç‰‡çš„å®½
                        Log.e(TAG, "#########sendCommand");
                        sendCommand(0x1b, 0x4b, imageWidth, 0x01);
                    }

                    try {
                        mOutputStream.write((char) tempbyte);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                in.close();
                sendLineFeed();
                ConsoleActivity.this.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * æ‰“å�°å›¾åƒ�Demo(æ–¹æ³•3ï¼Œä»Žrawç›®å½•ä¸­èŽ·å�–)
     *
     */
    private void printGraphDemo(String assetsPath, int imageWidth)
            throws Exception {
        try {
            // å¾—åˆ°èµ„æº�ä¸­çš„assetæ•°æ�®æµ�
            InputStream in = getResources().getAssets().open(assetsPath);

            int tempbyte;
            int i = 0;
            while ((tempbyte = in.read()) != -1) {

                if ((i % (imageWidth)) == 0) { // å›¾ç‰‡çš„å®½
                    Log.e(TAG, "#########sendCommand");
                    sendCommand(0x1b, 0x4b, imageWidth, 0x01);
                }

                try {
                    mOutputStream.write((char) tempbyte);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                i++;
            }
            in.close();
            sleep(100);
            sendLineFeed();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendCodeDemo() {
        sendCommand(0x1d, 0x6b, 0x00, 0x01, 0x03, 0x03, 0x04, 0x05, 0x06, 0x07,
                0x08, 0x09, 0x03, 0x03);
    }

    private void sendCommand(int... command) {
        //luyq add 20140730
        System.out.print("Command:");
        try {
            for (int i = 0; i < command.length; i++) {
                mOutputStream.write(command[i]);

                //luyq add 20140730
                System.out.print(command[i]);
            }
            //luyq add 20140730
            System.out.println("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        sleep(100);
    }

    private void sendString(String tString) {

        String string = null;
        string = UnicodeToGBK(tString);
        System.out.print("sendString:");
        int j=0;
        try {
            for (int i = 0; i < string.length(); i++) {
                mOutputStream.write((int) string.charAt(i));
                j++;
                if(j>=120)
                {
                    sleep(200);
                    j=0;
                }
                //luyq add 20140730
                System.out.print((int) string.charAt(i));
            }
            //luyq add 20140730
            System.out.println("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // //// sleep(500);
    }

    private void sendCharacterDemo(ArrayList<String> arr) {

        Log.e(TAG, "#########sendCharacterDemo##########");
        Log.e(TAG, "print end");
    }

    private void sleep(int ms) {

        try {
            java.lang.Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* GBK TO UNICODE??? */
    public static String UnicodeToGBK(String s) {
        try {
            String newstring = null;
            newstring = new String(s.getBytes("GBK"), "ISO-8859-1");

            return newstring;
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        stop = true;
        unregisterReceiver(printReceive);
        PWMControl.PrinterEnable(0);
    }

    /**
     * æ‰“å�°å›¾åƒ�æ–¹æ³•4ï¼ˆåŽ‚å®¶æ��ä¾›çš„æº�ç �ï¼Œä½†æ‰“å�°é©¬èµ›å…‹ä¸�æ¸…æ™°ï¼‰
     *
     * @param bitmap
     * @param o
     */
    public void PrintBmp(Bitmap bitmap, OutputStream o) {
        byte[] start1 = { 0x0d, 0x0a };
        byte[] start2 = { 0x1B, 0x4b, 0x1B, 0x33 };

        try {

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // è°ƒæ•´åˆ°é€‚åº”æ‰“å�°æœºçš„æ•´å€�æ•°
            int adjustedHeight = (height + 7) / 8 * 8; // 8çš„å€�æ•°ï¼Œä¸€æ¬¡æ‰“å�°é«˜åº¦æ˜¯8
            // å¾ªçŽ¯å›¾ç‰‡åƒ�ç´ æ‰“å�°å›¾ç‰‡
            // å¾ªçŽ¯é«˜,ä¸€æ¬¡æ‰“å�°8åƒ�ç´ é«˜åº¦

            for (int i = 0; i < (adjustedHeight / 8); i++) {
                byte[] data = new byte[width];

                byte xL = (byte) (width % 256);
                byte xH = (byte) (width / 256);
                start2[2] = xL;
                start2[3] = xH;

                for (int k = 0; k < 8; k++) {

                    // å¾ªçŽ¯å®½
                    for (int x = 0; x < width; x++) {
                        int y = (i * 8) + k;
                        // ç¡®ä¿�åœ¨å›¾ç‰‡èŒƒå›´å†…
                        if (y < height) {
                            int pixel = bitmap.getPixel(x, y);
                            if (Color.red(pixel) == 0
                                    || Color.green(pixel) == 0
                                    || Color.blue(pixel) == 0) {
                                // é«˜ä½�åœ¨å·¦ï¼Œæ‰€ä»¥ä½¿ç”¨128 å�³ç§»
                                data[x] += (byte) (128 >> (y % 8));
                            }
                        }
                    }
                }
                Log.e(TAG, "bmpData : " + data.toString());
                o.write(start2);

                o.write(data);
                o.flush();
          //      Thread.sleep(200);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
//        catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        try {
            o.write(start1);
            o.write(start1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * èŽ·å�–IMEI
     */
    public String getDeviceIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        return imei;
    }

    /******************************************* UincodeèŠ¯ç‰‡éƒ¨åˆ†æµ‹è¯• å¼€å§‹ *************************************************/

    /**
     * æ‰“å�°å­—ç¬¦æ•°æ�®ï¼ˆè¡¨å¤´éƒ¨åˆ†ï¼‰
     *
     * @param //arr
     */

    private void PrintUnicodeDemo() {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add(getString(R.string.Multilingual));
        arr.add("-----------------------------\n");
        arr.add(getString(R.string.Language0));
        arr.add(getString(R.string.Language1));
        arr.add(getString(R.string.Language2));
        arr.add(getString(R.string.Language3));
        arr.add(getString(R.string.Language4));
        arr.add(getString(R.string.Language5));
        arr.add(getString(R.string.Language6));
        arr.add(getString(R.string.Language7));
        arr.add(getString(R.string.Language8));
        arr.add(getString(R.string.Language9));
        arr.add(getString(R.string.Language10));
        arr.add(getString(R.string.Language11));
        arr.add(getString(R.string.Language12));
        arr.add(getString(R.string.Language13));
//        arr.add(getString(R.string.Language14));
//        arr.add(getString(R.string.Language15));
//        arr.add(getString(R.string.Language16));
//        arr.add(getString(R.string.Language17));
//        arr.add(getString(R.string.Language18));
//        arr.add(getString(R.string.Language19));
        // å�‘é€�å’Œæ‰“å�°æ•°æ�®
//        ConsoleActivity.this.sleep(200);
        sendUnicodeListData(arr);
    }

    /**
     * å�‘é€�unicodeæ–‡æœ¬æ•°æ�®
     *
     * @param localArrayList
     */
    private void sendUnicodeListData(ArrayList<String> localArrayList) {
        for (int i = 0; i < localArrayList.size(); i++) {
            sendStringUnicode(localArrayList.get(i));
        }
    }

    /**
     * å�‘é€�Unicodeæ•°æ�®
     *
     * @param paramString
     */
    private void sendStringUnicode(String paramString) {

        int i = 0;
        int j = 0;
        byte ch = 0;

        byte[] b_unicode = new byte[0];

        try {
            b_unicode = paramString.getBytes("unicode");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] a = new byte[b_unicode.length * 2];

        for (i = 2; i < b_unicode.length ; i++) {
            if (i % 2 == 0) {
                ch = b_unicode[i];
            } else {
                a[j++] = 0x0B;
                a[j++] = b_unicode[i];
                a[j++] = ch;
            }
        }

        a[j++] = 0x0a;

        try {
            this.mOutputStream.write(a, 0, j);
        } catch (IOException localIOException) {
            localIOException.printStackTrace();
        }
    }

    /**
     * æ‰“å�°å­—ç¬¦æ•°æ�®ï¼ˆè¡¨å¤´éƒ¨åˆ†ï¼‰
     *
     * @param //arr
     */

    private void PrintCharDemoUnicode() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("-----------------------------");
        arr.add("TP PRINT TEST");



//        ConsoleActivity.this.sleep(200);

        sendCommand( 0x1b, 0x55, 0x01); // å€�é«˜å‘½ä»¤
        sendStringUnicode(arr.get(0));// ç ´æŠ˜å�·
        //   sendCommand(0x0a);//

        sendCommand(0x1b, 0x55, 0x02); // å€�é«˜å‘½ä»¤
        sendCommand( 0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
        sendStringUnicode(arr.get(1));// TP PRINT TEST
        sendCommand( 0x1b, 0x56, 0x01); // å�–æ¶ˆå€�å®½å‘½ä»¤
        sendCommand( 0x1b, 0x55, 0x01); // å�–æ¶ˆå€�é«˜å‘½ä»¤
        // sendCommand(0x0a);
//        ConsoleActivity.this.sleep(200);

    }


    /**
     * æ‰“å�°æ�¡ç �å­—ç¬¦
     *
     */

    private void PrintBarCodeTextUnicode() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Bar Code Test:");
        arr.add("-----------------------------");

//        ConsoleActivity.this.sleep(200);
        sendStringUnicode(arr.get(0));// Black block Test
        //      sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(1));// ç ´æŠ˜å�·
        //     sendCommand(0x0a);// æ�¢è¡Œ

//        ConsoleActivity.this.sleep(200);

    }


    /**
     * æ‰“å�°é»‘å�—å­—ç¬¦
     *
     */

    private void PrintBlackBlockTextUnicode() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Black block Test:");
        arr.add("-----------------------------");

//        ConsoleActivity.this.sleep(200);
//        sendCommand(0x0a);// æ�¢è¡Œ
        sendStringUnicode(arr.get(0));// Black block Test
        //    sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(1));// ç ´æŠ˜å�·
        //   sendCommand(0x0a);// æ�¢è¡Œ

//        ConsoleActivity.this.sleep(200);

    }


    /**
     * æ‰“å�°å­—ç¬¦æ•°æ�®ï¼ˆè¡¨å¤´éƒ¨åˆ†ï¼‰
     *
     * @param //arr
     */

    private void PrintLineTitleUniCode() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Line Test:");
        arr.add("-----------------------------");

//        ConsoleActivity.this.sleep(200);

        // sendCommand(0x0a, 0x1b, 0x55, 0x01); // å€�é«˜å‘½ä»¤
        //   sendCommand(0x0a);// æ�¢è¡Œ
        sendStringUnicode(arr.get(0));// Line Test

        //  sendCommand(0x0a);// æ�¢è¡Œ
        sendStringUnicode(arr.get(1));// ç ´æŠ˜å�·
        //sendCommand(0x0a);// æ�¢è¡Œ

//        ConsoleActivity.this.sleep(200);


    }


    /**
     * æ‰“å�°å­—ç¬¦æ•°æ�®Font Size Test å¼€å§‹éƒ¨åˆ†
     *
     * @param //arr
     */

    private void PrintCharDemoLastUnicode() {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("Font Size Test");
        arr.add("-----------------------------");
        arr.add("PRINT TEST");
        arr.add("PRINT TEST");
        arr.add("PRINT TEST");
        arr.add("PRINT TEST");

        arr.add("Characters Test:");// æ‰“å�°å�„ç±»ç‰¹æ®Šå­—ç¬¦
        arr.add("-----------------------------");// æ‰“å�°å�„ç±»ç‰¹æ®Šå­—ç¬¦
        arr.add(getString(R.string.CharTest));// æ‰“å�°å�„ç±»ç‰¹æ®Šå­—ç¬¦ã€�æ±‰å­—

        //   ConsoleActivity.this.sleep(200);
        // ConsoleActivity.this.sleep(200);
//     	sendCommand(0x0a);// æ�¢è¡Œ
        sendStringUnicode(arr.get(0));// Font size test
        //	sendCommand(0x0a);

        sendStringUnicode(arr.get(1));// ç ´æŠ˜å�·
        //	sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(2));// æ­£å¸¸é«˜åº¦çš„print test

        // 2å€�é«˜åº¦çš„print test
        sendCommand(0x1b, 0x55, 0x02); // å€�é«˜å‘½ä»¤
        sendCommand(0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
        sendStringUnicode(arr.get(3));

        // 3å€�é«˜åº¦çš„print test
        sendCommand(0x1b, 0x55, 0x03); // å€�é«˜å‘½ä»¤
        sendCommand( 0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
        sendStringUnicode(arr.get(4));

        // 4å€�é«˜åº¦çš„print test
        sendCommand(0x1b, 0x55, 0x04); // å€�é«˜å‘½ä»¤
        sendCommand( 0x1b, 0x56, 0x02); // å€�å®½å‘½ä»¤
        sendStringUnicode(arr.get(5));
        sendCommand( 0x1b, 0x56, 0x01); // å�–æ¶ˆå€�å®½å‘½ä»¤
        sendCommand( 0x1b, 0x55, 0x01);// å�–æ¶ˆå€�é«˜å‘½ä»¤
        ConsoleActivity.this.sleep(200);

        sendStringUnicode(arr.get(6));// å­—ç¬¦æµ‹è¯•
        //   sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(7));// ç ´æŠ˜å�·
        //   sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(8));// å­—ç¬¦
        //    sendCommand(0x0a);// æ�¢è¡Œ
        ConsoleActivity.this.sleep(1000);

    }

    /**
     * æ‰“å�°è®¾å¤‡åŸºç¡€ä¿¡æ�¯
     *
     * @param //arr
     */

    private void PrintDeviceBaseInfoUnicode() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("Device Base Information");
        arr.add("-----------------------------");
        arr.add("Printer Version:");
        arr.add("  V05.2.0.3");
        arr.add("Printer Gray: 3");
        arr.add("Soft Version:");
        arr.add("  TPDemo.G50.0.Build140313");
        arr.add("Battery Level: 100%");
        arr.add("CSQ Value: 24");
        arr.add("IMEI:" + getDeviceIMEI());
        arr.add(getString(R.string.PrintTemp1));
        arr.add(getString(R.string.PrintTemp2));

//        ConsoleActivity.this.sleep(200);

        sendCommand(0x1b, 0x55, 0x01); // å€�é«˜å‘½ä»¤
        sendStringUnicode(arr.get(0));// Device Base Information
        //  sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(1));// ç ´æŠ˜å�·
        //     sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(2));// Printer Version
        //    sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(3));// Printer Version Value
        //    sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(4));// Printer Gray
        //   sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(5));// Soft Version
        //    sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(6));// Soft Version Value
        //    sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(7));// Battery Level
        //     sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(8));// CSQ Value
        //    sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(9));// IMEI
        //    sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(10));// THP Temp:before print
        //    sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(11));// THP Temp:after print
        //    sendCommand(0x0a);// æ�¢è¡Œ

        //   sendCommand(0x1b, 0x64, 0x8);// æµ‹è¯•ç»“æ�Ÿå¾€ä¸‹èµ°çº¸50ç‚¹è¡Œ

        ConsoleActivity.this.sleep(1000);

    }


    /**
     * æ‰“å�°ç»“æ�Ÿ
     *
     * @param //arr
     */

    private void PrintTestEndUnicode() {

        ArrayList<String> arr = new ArrayList<String>();

        arr.add("-----------------------------");
        arr.add("              Print Test End");
        arr.add("-----------------------------");

//        ConsoleActivity.this.sleep(200);

//        sendCommand(0x0a, 0x1b, 0x55, 0x01); // å€�é«˜å‘½ä»¤
        sendStringUnicode(arr.get(0));// Line Test

//        sendCommand(0x0a);// æ�¢è¡Œ
        sendStringUnicode(arr.get(1));// ç ´æŠ˜å�·
//        sendCommand(0x0a);// æ�¢è¡Œ

        sendStringUnicode(arr.get(2));// ç ´æŠ˜å�·
        sendCommand(0x1b, 0x64, 0x8);// æµ‹è¯•ç»“æ�Ÿå¾€ä¸‹èµ°çº¸50ç‚¹è¡Œ

//        ConsoleActivity.this.sleep(200);



    }

    /******************************************* UincodeèŠ¯ç‰‡éƒ¨åˆ†æµ‹è¯• ç»“æ�Ÿ *************************************************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            mApplication.CheckVersionEnd=false;
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * ç”Ÿæˆ�æ�¡ç �
     *
     * @param str æ�¡ç �å†…å®¹
     * @param type æ�¡ç �ç±»åž‹ï¼š AZTEC, CODABAR, CODE_39, CODE_93, CODE_128, DATA_MATRIX, EAN_8, EAN_13, ITF, MAXICODE, PDF_417, QR_CODE, RSS_14, RSS_EXPANDED, UPC_A, UPC_E, UPC_EAN_EXTENSION;
     *  @param bmpWidth ç”Ÿæˆ�ä½�å›¾å®½,å®½ä¸�èƒ½å¤§äºŽ384ï¼Œä¸�ç„¶å¤§äºŽæ‰“å�°çº¸å®½åº¦
     *  @param bmpHeight ç”Ÿæˆ�ä½�å›¾é«˜ï¼Œ8çš„å€�æ•°
     */

    public Bitmap CreateCode(String str,com.google.zxing.BarcodeFormat type,int bmpWidth,int bmpHeight) throws WriterException {
        //ç”Ÿæˆ�äºŒç»´çŸ©é˜µ,ç¼–ç �æ—¶è¦�æŒ‡å®šå¤§å°�,ä¸�è¦�ç”Ÿæˆ�äº†å›¾ç‰‡ä»¥å�Žå†�è¿›è¡Œç¼©æ”¾,ä»¥é˜²æ¨¡ç³Šå¯¼è‡´è¯†åˆ«å¤±è´¥
        BitMatrix matrix = new MultiFormatWriter().encode(str, type,bmpWidth,bmpHeight);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // äºŒç»´çŸ©é˜µè½¬ä¸ºä¸€ç»´åƒ�ç´ æ•°ç»„ï¼ˆä¸€ç›´æ¨ªç�€æŽ’ï¼‰
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(matrix.get(x, y)){
                    pixels[y * width + x] = 0xff000000;
                }else{
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // é€šè¿‡åƒ�ç´ æ•°ç»„ç”Ÿæˆ�bitmap,å…·ä½“å�‚è€ƒapi
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    void test(){

        try{
//              Bitmap bitmap=CreateCode("123456789",BarcodeFormat.AZTEC.QR_CODE,200,200);
//              saveBitmap(bitmap);
              Bitmap bitmap= BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/bmp111.bmp"));
                if(bitmap!=null){
                   byte[] byteBmp= bitmapFormat(bitmap);

                    FileOutputStream fout = new FileOutputStream("/sdcard/111.bin");
                    fout.write(byteBmp);
                    fout.flush();
                    fout.close();
                    Toast.makeText(this,"æ–‡ä»¶å·²å†™å…¥",Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e){
                e.printStackTrace();
            }



    }
    /** ä¿�å­˜æ–¹æ³• */
    public void saveBitmap(Bitmap bitmap) {
        Log.e(TAG, "ä¿�å­˜å›¾ç‰‡");
        File f = new File("/sdcard/123.bmp");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "å·²ç»�ä¿�å­˜");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
