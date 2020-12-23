package com.example.bike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {
    private static WifiManager mWifiManager;

    private WifiInfo wifiInfo;
    private Button bt1, bt2, bt3, bt4,btstart, btpause,btreset;
    private TextView tv1,time_clock;
    private Socket socket;
    private String time;
    private String SSID;
    private int i, j, x, t, csec, cmin, cmsec;
    private long tmsec,tStart,tBuff,tUpdate = 0L;

    private  static  final  String TAG = "MainActivity";

    private Handler mainHandler = new Handler();
    private Handler conHandler = new Handler();
    private Handler timeHandler = new Handler();
    private Handler timeHandler2 = new Handler();
    private ExecutorService wifiThreadPool;

    private volatile boolean stpthred = false;
    private volatile boolean nobike = false;
    private volatile boolean IsConnt = false;
    private volatile boolean IsConnBuDis = false;
    private boolean running;
    private boolean wasRunning;
    private boolean teed;

    private Thread thread1, thread2;
    private Runnable runnable;

    //private Chronometer mChronometer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getGPSpermission();
        setMain();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        wifiThreadPool = Executors.newCachedThreadPool();
        wifiThreadPool.execute(new SocketConn());

        tv1 = (TextView) findViewById(R.id.Wifist);
        bt1 = findViewById(R.id.Bt1);
        bt2 = findViewById(R.id.Bt2);
        bt3 = findViewById(R.id.Bt3);
        bt4 = findViewById(R.id.Bt4);

        btstart = findViewById(R.id.start);
        btpause = findViewById(R.id.stop);
        btreset = findViewById(R.id.reset);
        time_clock = (TextView)findViewById(R.id.timeclock);

        //
        NumberPicker numberPicker = findViewById(R.id.number_picker);
        String[] values = {"Bike", "Low", "Mid", "High"};
        numberPicker.setDisplayedValues(values);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(3);
        numberPicker.setOnValueChangedListener(onValueChangeListener);
        //

    }

    //public void onStart(View view){
        //TextView time_clock = (TextView)findViewById(R.id.timeclock);
        //time_clock.setText("time");
        //runTimer();
        //super.onStart();
        //timeHandler = new Handler();
        //timeHandler.post(myRunnable);
        //startTimerThread();
        //running = true;
        //time_clock.setText("123");
        /*thread2 = new Thread(){
            @Override
            public void run() {


                    time = String.format(Locale.getDefault(),"%02d",csec);
                    timeHandler2.post(new Runnable() {
                        @Override
                        public void run() {
                            time_clock.setText("time");
                        }
                    });

            }
        };
        thread2.start();*/
        //timeHandler.postDelayed(myRunnable, 10);
    //}
   /* public void onStop(View view){

        running = false;
    }
    public void onReset(View view){
        csec = 0;
        running = false;
        //super.onStop();
        timeHandler.postDelayed(runnable,10);
    }*/


    NumberPicker.OnValueChangeListener onValueChangeListener =
            new NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    t = numberPicker.getValue();
                    Log.d(TAG, String.valueOf(t));
                    tv1.setText("selected number " + numberPicker.getValue());
                }
            };



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,new Home()).commit();  //切換fragment
                    return true;
                case R.id.Timer:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,new Timer()).commit();  //切換fragment
                    return true;

            }
            return false;
        }
    };

    private void setMain() {

        this.getSupportFragmentManager().beginTransaction().add(R.id.frameLayout,new Home()).commit();
    }


    public void getGPSpermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }



    public void onClic1(View view) {
        //mWifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //wifiInfo = mWifiManager.getConnectionInfo();
        int Fr = wifiInfo.getFrequency();
        String FREQUENCY_UNITS = wifiInfo.FREQUENCY_UNITS.toString();
        String SSID2 = wifiInfo.getSSID();

        tv1.setText(SSID2 + "\n" + Fr + FREQUENCY_UNITS);
        //bt1.setEnabled(false);
        setbtn(1);
    }

    public void onClic2(View view) {
        //setbtn(2);
        /*new Thread() {
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
        }.start();*/

    }

    public void onClic3(View view) throws IOException {
        setbtn(3);
        stpthred = true;
        teed = true;
        //socket.close();
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
        //setbtn(4);
        stpthred = false;
        if(teed){
            wifiThreadPool = Executors.newCachedThreadPool();
        }
        if(IsConnBuDis){

        }
        wifiThreadPool.execute(new SocketConn());


        /*ExRunnable runnable = new ExRunnable(4);
        new  Thread(runnable).start();*/
        /*if(teed){
            wifiThreadPool = Executors.newCachedThreadPool();
        }

        wifiThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                wifiThreadPool.execute(new ExRunnable(1));
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
                        Thread.sleep(800);
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
        });
*/
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
    public void SwichMode(int SetMod){
        switch (SetMod){
            case 0:
                String Setmode = "Mode1";
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

    public class  ExRunnable implements  Runnable{
        int sec;
        ExRunnable(int sec){
            this.sec = sec;
        }
        @Override
        public void run() {
            for ( i = 1; i <= sec; i++) {
                if (stpthred){
                    Log.d(TAG,"Close Thread");
                    teed = true;
                    return;
                }
                try {
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

    public class  SocketConn implements  Runnable{
        @Override
        public void run() {
            //add for
            String ss = '"'+"bike"+'"';
            wifiThreadPool.execute(new SocketConnTime());

            for(csec = 0; csec<=50;csec++){
                if (stpthred){
                    Log.d(TAG,"Close Thread");
                    wifiThreadPool.shutdown();
                    return;
                }
                try {
                    mWifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiInfo = mWifiManager.getConnectionInfo();
                    SSID = wifiInfo.getSSID();
                    Log.d(TAG, "connecting: " + csec +SSID);
                    //Log.i("MainActivity",SSID+" "+ss);
                    nobike = false;
                    if(ss.equals(ss)){
                        //IsConnt = socket.isConnected();
                        if(IsConnt == true){
                            Log.e(TAG,"OK");
                            Log.i("MainActivity",SSID+" "+ss);
                            nobike = true;
                        }else {
                            //sleep(15000);
                            IsConnt = true;
                            sleep(15000);
                            //socket = new Socket("192.168.4.1", 4567);
                            //IsConnt = socket.isConnected();
                            Log.d(TAG,"CCCCCCCCCCOOOOONNN");
                        }
                    }else {
                        //stpthred = true;
                        nobike = true;
                        wifiThreadPool.execute(new SocketConnTime());
                        conHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"Not Conneted bike",Toast.LENGTH_LONG).show();
                                tv1.setText("Not Conneted bike");
                            }
                        });
                    }
                    sleep(5000);
                }catch (Exception e){
                    Log.i("MainActivity", "ConnFaile");
                }
            }
        }
    }

    public class SocketConnTime implements Runnable{
        @Override
        public void run() {
            for (j = 0; j<100000; j++){
                if (nobike){
                    Log.d(TAG,"Close Thread SocketConnTime");
                    //wifiThreadPool.shutdown();
                    //socket.close();
                    return;
                }
                sleep(1000);
                x += 1;
                if (x == 4)
                    x = 0;
                conHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String [] strings = {"",".", ".." ,"..."};
                        tv1.setText("Connecting" + strings[x]);
                        //sleep(1000);
                        if (IsConnt){
                            tv1.setText("Connected");
                            Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_LONG).show();
                            return;
                        }else {
                            new SocketConn();
                        }
                    }
                });
                Log.d(TAG,t+" "+j +" "+x);

            }

            /*for (j = 0; j<1000;j++){
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
                    Thread.sleep(800);
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
            }*/
        }
    }


}

