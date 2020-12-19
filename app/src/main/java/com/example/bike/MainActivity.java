package com.example.bike;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static WifiManager mWifiManager;

    private WifiInfo wifiInfo;
    private Button bt1, bt2, bt3, bt4;
    private TextView tv1;
    private Socket socket;
    private String t;
    private int i,j ,x;

    private  static  final  String TAG = "MainActivity";

    private Handler mainHandler = new Handler();
    private Handler conHandler = new Handler();

    private  volatile boolean stpthred = false;

    private Thread thread1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getGPSpermission();
        tv1 = (TextView) findViewById(R.id.Wifist);
        bt1 = findViewById(R.id.Bt1);
        bt2 = findViewById(R.id.Bt2);
        bt3 = findViewById(R.id.Bt3);
        bt4 = findViewById(R.id.Bt4);
        NumberPicker numberPicker = findViewById(R.id.number_picker);
        String[] values = {"Bike", "Low", "Mid", "High"};
        numberPicker.setDisplayedValues(values);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(3);
        numberPicker.setOnValueChangedListener(onValueChangeListener);

    }
    NumberPicker.OnValueChangeListener onValueChangeListener =
            new NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    tv1.setText("selected number " + numberPicker.getValue());
                }
            };


    public void getGPSpermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }


    public void onClic1(View view) {
        mWifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiInfo = mWifiManager.getConnectionInfo();
        int Fr = wifiInfo.getFrequency();
        String FREQUENCY_UNITS = wifiInfo.FREQUENCY_UNITS.toString();
        String SSID = wifiInfo.getSSID();

        tv1.setText(SSID + "\n" + Fr + FREQUENCY_UNITS);
        //bt1.setEnabled(false);
        setbtn(1);
    }

    public void onClic2(View view) {
        //setbtn(2);
        new Thread() {
            public void run() {
                try {
                    InetAddress destination = null;
                    destination = InetAddress.getByName("192.168.4.1");
                    socket = new Socket(destination, 4567);
                    socket.isConnected();
                    //Socket socket = new S
                    //socket.close();
                } catch (Exception e) {
                    Log.i("MainActivity", "EXITO");
                }
            }
        }.start();

    }

    public void onClic3(View view) {
        setbtn(3);

        stpthred = true;

        //thread1.interrupt();
        /*new Thread() {
            public void run() {
                try {
                    //Socket socket = new S
                    DataOutputStream DOS = new DataOutputStream(socket.getOutputStream());
                    byte[] ascii = "Java".getBytes(StandardCharsets.US_ASCII);
                    String asciiString = "Mode1";
                    DOS.write(asciiString.getBytes());
                    DOS.flush();
                    //socket.close();
                } catch (Exception e) {
                    Log.i("MainActivity", "EXITO");
                }
            }
        }.start();
        */
    }

    public void onClic4(View view) {
        setbtn(4);
        stpthred = false;
        ExRunnable runnable = new ExRunnable(4);
        new  Thread(runnable).start();

        thread1 = new Thread(){
            @Override
            public void run() {
                for (j = 0; j<1000;j++){

                    if (stpthred){
                        conHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tv1.setText("Connection Timeout");
                            }
                        });

                        Log.d(TAG,"Close Thread1");
                        return;
                    }

                    try {
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                        t =format.format(new Date());
                        sleep(1000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }

                    x+=1;
                    if (x==4)
                        x=0;

                    conHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String [] strings = {"",".", ".." ,"..."};
                            tv1.setText("Connecting" + strings[x] + j);

                        }
                    });

                    Log.d(TAG,t+" "+j +" "+x);
                }
            }
        };
        thread1.start();
        //ExThread thread = new ExThread(10);
        //thread.start();
    }

    public void setbtn(int ty) {
        switch (ty) {
            case 1:
                bt1.setEnabled(false);
                bt2.setEnabled(true);
                bt3.setEnabled(true);
                bt4.setEnabled(true);
                break;
            case 2:
                bt2.setEnabled(false);
                bt1.setEnabled(true);
                bt3.setEnabled(true);
                bt4.setEnabled(true);
                break;
            case 3:
                bt3.setEnabled(false);
                bt1.setEnabled(true);
                bt2.setEnabled(true);
                bt4.setEnabled(true);
                break;
            case 4:
                bt4.setEnabled(false);
                bt1.setEnabled(true);
                bt2.setEnabled(true);
                bt3.setEnabled(true);
                break;

        }
    }


    public String getWifiStateStr() {
        switch (mWifiManager.getWifiState()) {
            case WifiManager.WIFI_STATE_DISABLING:
                return "disabling";
            case WifiManager.WIFI_STATE_DISABLED:
                return "disabled";
            case WifiManager.WIFI_STATE_ENABLING:
                return "enabling";
            case WifiManager.WIFI_STATE_ENABLED:
                return "enabled";
            case WifiManager.WIFI_STATE_UNKNOWN:
                return "unknown";
            default:
                return null;  //or whatever you want for an error string
        }
    }

    class  ExRunnable implements  Runnable{
        int sec;

        ExRunnable(int sec){
            this.sec = sec;
        }

        @Override
        public void run() {
            for ( i = 1; i <= sec; i++) {
                if (stpthred){
                    Log.d(TAG,"Close Thread");
                    return;
                }

                try {
                    /*if (i == 5) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tv1.setText("5");
                            }
                        });
                    }*/
                    //SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    //String t =format.format(new Date());


                    Log.d(TAG, "connecting: " + i);
                    socket = new Socket("192.168.4.1", 4567);


                    if(socket.isConnected()){
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tv1.setText("Connected");
                            }
                        });
                    }


                    //Thread.sleep(50);
                } catch (Exception e) {
                    if(i==2){
                        stpthred = true;
                    }
                    //thread1.interrupt();
                    Log.i("MainActivity", "Faile");
                    //e.printStackTrace();
                }
            }
        }
    }



}

