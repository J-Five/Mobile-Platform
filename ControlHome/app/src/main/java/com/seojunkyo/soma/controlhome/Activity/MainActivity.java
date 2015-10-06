package com.seojunkyo.soma.controlhome.Activity;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;
import com.seojunkyo.soma.controlhome.util.MQTTUtils;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;

public class MainActivity extends CONTROLHOMEActivity {

    private MqttCallback callback;
    private MqttAsyncClient asyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAcitivy();
        setLayout();
    }
    private ToggleButton mImgTogContoldv;

    @Override
    public void initAcitivy() {
        mImgTogContoldv = (ToggleButton) findViewById(R.id.toggle_tv);
    }

    @Override
    public void setLayout() {
        mImgTogContoldv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String TOPIC = "CONTROL";
                String url = "192.168.123.114";
                String client="TV";
                changeStatus(url, TOPIC, client);
            }
        });
    }
    private void changeStatus(String url, String TOPIC, String mqttClient){
        if (mImgTogContoldv.isChecked()){
            mImgTogContoldv.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_tv_off));
            String PAYLOAD = String.format("{\"LED1\":\"OFF\"}");
            if (MQTTUtils.connect(url, mqttClient)) {
                MQTTUtils.pub(TOPIC, PAYLOAD);
                if(MQTTUtils.sub("OFF")){
                    mImgTogContoldv.setChecked(true);
                    mImgTogContoldv.setBackgroundResource(R.drawable.btn_tv_on);
                }
            }
            else {
                Toast.makeText(getApplication(), "Error connecting the server.", Toast.LENGTH_SHORT);
            }
        }
        else {
            mImgTogContoldv.setBackgroundResource(R.drawable.btn_tv_on);
        }
    }

    /*public void publish(String topic, String payload){
        Log.d("test","success2323");
        Toast.makeText(getApplicationContext(), "Connected to the server.", Toast.LENGTH_SHORT);
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            }

            @Override
            public void onProviderEnabled(String arg0) {
            }

            @Override
            public void onProviderDisabled(String arg0) {
            }

            @Override
            public void onLocationChanged(Location arg0) {
            }
        };
        String provider = LocationManager.GPS_PROVIDER;
        if (!lm.isProviderEnabled(provider)) {
            provider = LocationManager.NETWORK_PROVIDER;
        }
        if (!lm.isProviderEnabled(provider)) {
        } else {
                        *//*lm.requestSingleUpdate(provider, locationListener, Looper.getMainLooper());
                        Location location = lm.getLastKnownLocation(provider);*//*
            MQTTUtils.pub(topic, payload);
        }
        Toast.makeText(getApplicationContext(), "Connected to the server.", Toast.LENGTH_SHORT);
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

