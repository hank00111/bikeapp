package com.example.bike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {
    private static WifiManager mWifiManager;

    private WifiInfo wifiInfo;
    private Button bt1, bt2, bt3, bt4,btstart, btpause,btreset;
    private TextView tv1,time_clock;

    private Socket socket;
    private InetSocketAddress socketAdr;
    private OutputStream SetmodeStream;
    private String time, Setmode, hex;
    private String SSID;
    private int i, j, x, t, csec, cmin, cmsec, xz;
    private long tmsec,tStart,tBuff,tUpdate = 0L;


    private  static  final  String TAG = "MainActivity";

    private Handler mainHandler = new Handler();
    private Handler conHandler = new Handler();
    private Handler timeHandler = new Handler();
    private Handler timeHandler2 = new Handler();
    private ExecutorService wifiThreadPool ,RecvThreadPoll ;

    private volatile boolean stpthred = false;
    private volatile boolean nobike = false;
    private volatile boolean IsConnt = false;
    private volatile boolean IsConnBuDis = false;
    private boolean running;
    private boolean wasRunning;
    private boolean teed;
    private boolean ssz = true;

    private LineChart lineChart;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getGPSpermission();
        setMain();
        t = 1;
        //private Chronometer mChronometer;
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        wifiThreadPool = Executors.newCachedThreadPool();
        RecvThreadPoll = Executors.newCachedThreadPool();
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
        lineChart = findViewById(R.id.Recv_wave);

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
                    wifiThreadPool.submit(new Setmodefun(t));
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

    private void text_all(ArrayList<Entry> val1) {
        XAxis xAxis = lineChart.getXAxis();
        YAxis leftYAxis = lineChart.getAxisLeft();
        LineDataSet set1;

        //xAxis.setGranularity(10f);
        xAxis.setAxisMaximum(300f);
        xAxis.setLabelCount(10, true);

        leftYAxis.setAxisMinimum(-180f);
        leftYAxis.setAxisMaximum(180f);

        set1 = new LineDataSet(val1,"Date");
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setColor(Color.BLACK);
        set1.setLineWidth(2);
        set1.setDrawFilled(false);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);



        LineData datas = new LineData(set1);
        lineChart.setData(datas);
        lineChart.invalidate();
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setDrawLabels(false);


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
        ssz = false;
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

        RecvThreadPoll.execute(new Recv());
        ArrayList<Entry> val1 = new ArrayList<>();
        val1.add(new Entry(0,0));


        //try {
          //  while (ssz){
                /*BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //System.out.println("server get input from client socket..");
                String txt="Sever send:"+reader.readLine();
                Log.w(TAG,txt);*/
            //    PrintWriter out = new PrintWriter(new BufferedWriter(
              //          new OutputStreamWriter(socket.getOutputStream())), true);
                //System.out.println(out);
                /*DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                String response = dataInputStream.readUTF();
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                response = br.readLine();
                Log.w(TAG,response);*/
            //}
        //}catch (IOException e){
          //  e.printStackTrace();
        //}

        //setbtn(3);
        //stpthred = true;
        //teed = true;
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

    public class ExRunnable implements  Runnable{
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

    public class SocketConn implements  Runnable{
        @Override
        public void run() {
            //add for
            String ss = '"'+"ankleCuff_test"+'"';
            for(csec = 0; csec<=10000;csec++){
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
                    nobike = false;
                    if(SSID.equals(ss)){
                        //IsConnt = socket.isConnected();
                        if(IsConnt == true){
                            Log.e(TAG,"OK");
                            Log.i("MainActivity",SSID+" "+ss +" "+IsConnt);
                            nobike = true;
                        }else {
                            nobike = false;
                            //sleep(15000);
                            //IsConnt = true;
                            wifiThreadPool.submit(new SocketConnTime());
                            socketAdr = new InetSocketAddress("192.168.4.1",49152);
                            socket = new Socket();
                            socket.connect(socketAdr,30000);
                            nobike = true;
                            IsConnt = socket.isConnected();
                            Log.d(TAG,"CCCCCCCCCCOOOOONNN");
                        }
                    }else {
                        //stpthred = true;
                        nobike = true;
                        IsConnt = false;
                        wifiThreadPool.submit(new SocketConnTime());
                        conHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"Not Conneted bike",Toast.LENGTH_LONG).show();
                                tv1.setText("Not Conneted bike");
                            }
                        });
                    }
                }catch (Exception e){
                    Log.i("MainActivity", "ConnFaile" +" "+ t);
                    nobike = true;
                    sleep(500);
                }
                sleep(2000);
            }
        }
    }

    public class SocketConnTime implements Runnable{
        @Override
        public void run() {
            for (j = 0; j<100000; j++){
                if (nobike){
                    Log.d(TAG,"Close Thread SocketConnTime"+" "+IsConnt);
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
                        sleep(1000);
                        if (IsConnt){
                            tv1.setText("Connected");
                            Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                });
                Log.d(TAG,j +" "+x);

            }

        }
    }

    public class Setmodefun implements Runnable{
        int setmode;
        Setmodefun(int setmode){this.setmode = setmode;}
        @Override
        public void run() {
            switch (setmode){
                case 0:
                    try {
                        SetmodeStream = socket.getOutputStream();
                        Setmode = "X";
                        SetmodeStream.write((Setmode+"\n").getBytes("utf-8"));
                        SetmodeStream.flush();
                        Log.e(TAG,Setmode+SetmodeStream);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    try {
                        SetmodeStream = socket.getOutputStream();
                        Setmode = "C";
                        SetmodeStream.write((Setmode+"\n").getBytes("utf-8"));
                        SetmodeStream.flush();
                        Log.e(TAG,Setmode+SetmodeStream);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        SetmodeStream = socket.getOutputStream();
                        Setmode = "Z";
                        SetmodeStream.write((Setmode+"\n").getBytes("utf-8"));
                        SetmodeStream.flush();
                        Log.e(TAG,Setmode+SetmodeStream);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    try {
                        SetmodeStream = socket.getOutputStream();
                        Setmode = "D";
                        SetmodeStream.write((Setmode+"\n").getBytes("utf-8"));
                        SetmodeStream.flush();
                        Log.e(TAG,Setmode+SetmodeStream);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public class Recv implements Runnable{
        @Override
        public void run() {
            StringBuffer buffer = new StringBuffer();

            boolean end = false;
            String dataString ="";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            ArrayList<Entry> val1 = new ArrayList<>();
            int teim = 0;
            LineData datass = lineChart.getData();
            while (ssz){
                try {
                    /*val1.add(new Entry(2,10));
                    val1.add(new Entry(10,10));
                    val1.add(new Entry(50,100));
                    val1.add(new Entry(60,180));
                    text_all(val1);*/
                    //BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //String ascii = reader.readLine();
                    //String txt="Sever send:";
                    //InputStream stream = socket.getInputStream();
                    //byte[] data = new byte[30];
                    //StringBuffer buffer = new StringBuffer();
                    //InputStream stream = socket.getInputStream();
                    //InputStream input = null;
                    //input = new DataInputStream(socket.getInputStream());
                    InputStream sis = socket.getInputStream();
                    //BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                    int size = sis.available();
                    byte[] data = new byte[10];
                    while(!end)
                    {
                        //sis.read(data);
                        //byteArrayOutputStream.write(data, 0, size);

                        dataString += byteArrayOutputStream.toString("UTF-8");
                        byte[] bytes =new byte[4];
                        sis.read(bytes);
                        String value = new String(bytes, "UTF-8");
                        int ss= bytes[1]&bytes[2];


                        if (val1.isEmpty()) {
                            lineChart.clear();
                            teim+=1;
                            val1.add(new Entry(teim,ss));
                            System.out.println("エラー");
                        }else {
                            teim+=1;
                            val1.add(new Entry(teim,ss));
                            if (teim ==300){
                                val1 = new ArrayList<>();
                                teim = 1;
                            }
                        }
                        /*LineData datass = lineChart.getData();

                        if (val1.isEmpty()) {
                            lineChart.clear();
                        } else {
                            // set data
                            lineChart.setData(datass);

                            if (ss == ss){
                                teim+=1;
                                val1.add(new Entry(teim,ss));
                                if (teim ==300){
                                    val1 = new ArrayList<>();
                                    teim = 1;
                                }
                                text_all(val1);
                            }
                        }*/

                        /*if (ss == ss){
                            teim+=1;
                            val1.add(new Entry(teim,ss));
                            if (teim ==300){
                                val1 = new ArrayList<>();
                                teim = 1;
                            }

                        }*/
                        text_all(val1);

                        //value = Arrays.toString
                        //buffer.append((int)Integer.parseInt(String.valueOf(ss), 16));
                       // dataString = new String(String.valueOf(ss));

                        /*if (dataString.length() == 100)
                        {
                            end = true;
                        }*/
                        //System.out.println("MESSAGE: " + Arrays.toString(data));
                        System.out.println("Message: "+Arrays.toString(bytes) +" Message 2: "+ value +" Message 3: " + teim);
                    }



                    /*BufferedReader br = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    char[] m = new char[16];
                    br.read(m);
                    //byteToHex(br);
                    //String rec_msg = new String(m);
                    bytes[xz] = (byte)m[xz];
                    //BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //String clientInputStr = input.readUTF();
                    for (int xz = 0; xz < m.length; xz++) {
                        hex = String.format("%02X", (int) m[xz]);
                        bytes[xz] = (byte)m[xz];
                        //String tos = ;
                        buffer.append(hex);
                        System.out.println(hex);
                    }
                    for (int Z = 0; Z < hex.length(); Z+=2) {
                        String str = hex.substring(Z, i+2);
                        buffer.append((char)Integer.parseInt(str, 16));
                    }*/

                    //System.out.println(buffer);
                    //ystem.out.println("客戶端發過來的內容:" + clientInputStr);
                    //String receipt = new String(String.valueOf(stream.read()));
                    //buffer = new StringBuffer(String.valueOf(stream.read(bytes)));
                    //System.out.println("got receipt:" + bytes);

                    //String str;
                    /*while ((str = receipt) != null) {
                        buffer.append(str);
                        System.out.println(buffer.toString());
                    }*/
                    /*BufferedReader br = null;
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    StringBuffer buffer = new StringBuffer();

                    String str;
                    while ((str = br.readLine()) != null) {
                        buffer.append(str);
                        System.out.println(buffer.toString());
                    }*/

                    //InputStream stream = socket.getInputStream();
                    //byte[] bytes =new byte[1024];
                    //stream.read(bytes);
                    //String str = new String(bytes,0,bytes.length);
                    //int count = stream.read(data);
                    //Log.w(TAG, str);
                    sleep(100);
                    //InputStream in = socket.getInputStream();
                    //byte[] rebyte = new byte[1024];
                    //in.read(rebyte);
                    //String str2 = new String(new String(rebyte));
                    //Log.w(TAG,str2);
                    //int size = input.read(tmpData); // size 是讀取到的字節數，tmpData[]即為接收到之byte array
                    //Log.v("Socket-size", String.valueOf(size));
                }catch (IOException e) {
                    //System.out.println("close");
                    ssz=false;
                    e.printStackTrace();
                }
            }
        }
    }

}

