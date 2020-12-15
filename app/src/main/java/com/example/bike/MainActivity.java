package com.example.bike;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.net.wifi.WifiManager;

import java.io.DataOutputStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    WifiManager mWifiManager;
    WifiInfo wifiInfo;
    Button bt1, bt2, bt3, bt4;
    TextView tv1;
    Socket socket;

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
    }

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
        new Thread() {
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
    }

    public void onClic4(View view) {
        setbtn(4);
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


}