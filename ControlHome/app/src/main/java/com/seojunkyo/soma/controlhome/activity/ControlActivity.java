package com.seojunkyo.soma.controlhome.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;
import com.seojunkyo.soma.controlhome.util.DeviceList;
import com.seojunkyo.soma.controlhome.util.MQTTUtils;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ControlActivity extends CONTROLHOMEActivity {

    public enum DEVICE{
        LED,
        HUMI,
        RADIO
    };
    public ConnectivityManager mConnMan;
    public static String server;
    private static final String TAG = "MQTTService";
    private volatile IMqttAsyncClient mqttClient;
    private boolean hasWifi = false;
    private boolean hasMmobile = false;
    private String deviceId;
    private Context anoactivity;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        IntentFilter intentf = new IntentFilter();
        intentf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        setClientID("HOME");
        registerReceiver(new MQTTBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        initAcitivy();
        setLayout();
    }

    private ToggleButton mImgTogContolLED;
    private ToggleButton mImgTogContolTV;
    //private String subTOPIC;
    private String[] subTOPIC=new String[4];
    private String pubTOPIC;
    ArrayList<DeviceList> deviceItem;

    @Override
    public void initAcitivy() {

        MQTTUtils.pub("WHOLESYNC", String.format("{\"CONTROLLER\":\"ANDROID\"}"));

        mImgTogContolTV = (ToggleButton) findViewById(R.id.toggle_tv);
        mImgTogContolLED = (ToggleButton) findViewById(R.id.toggle_LED);
        deviceItem.add(new DeviceList(mImgTogContolTV, "LED1", "R.drawable.btn_light_on", "R.drawable.btn_light_off"));
        deviceItem.add(new DeviceList(mImgTogContolLED, "HUMI", "R.drawable.btn_tv_on", "R.drawable.btn_tv_off"));
        subTOPIC = new String[]{"SYNCDEVICE","SYNCCONTROL","WHOLESYNC","CONTROL"};
        pubTOPIC = "CONTROL";
    }

    @Override
    public void setLayout() {
        mImgTogContolLED.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "LED";
                setClientID(deviceId);
                publish(mImgTogContolLED, pubTOPIC);

            }
        });

        mImgTogContolTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "TV";
                setClientID(deviceId);
                publish(mImgTogContolTV, pubTOPIC);
            }
        });
    }

    private void publish(ToggleButton BTN, String pubTOPIC) {
        if (mImgTogContolLED.isChecked()) {
            mImgTogContolLED.setBackgroundResource(R.drawable.btn_light_off);
            String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"LED1\", \"COMMAND\":\"OFF\"}");
            MQTTUtils.pub(pubTOPIC, PAYLOAD);
        } else if (!mImgTogContolLED.isChecked()) {
            mImgTogContolLED.setBackgroundResource(R.drawable.btn_light_on);
            String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"LED1\", \"COMMAND\":\"ON\"}");
            MQTTUtils.pub(pubTOPIC, PAYLOAD);
        }
        if (mImgTogContolTV.isChecked()) {
            mImgTogContolTV.setBackgroundResource(R.drawable.btn_tv_off);
            String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"HUMI\", \"COMMAND\":\"OFF\"}");
            MQTTUtils.pub(pubTOPIC, PAYLOAD);
        } else if (!mImgTogContolTV.isChecked()){
            mImgTogContolTV.setBackgroundResource(R.drawable.btn_tv_on);
            String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"HUMI\", \"COMMAND\":\"PUSH\"}");
            MQTTUtils.pub(pubTOPIC, PAYLOAD);
        }
    }

    public void changeStatus(String payload) throws Exception{
        JSONObject subObject = new JSONObject(payload);
        String DEVICE = subObject.getString("DEVICE");
        String STATUS = subObject.getString("STATUS");

        switch(DEVICE){
            case "LED1":
                if (STATUS.equals("OFF")) {
                        mImgTogContolLED.setBackgroundResource(R.drawable.btn_light_off);
                    } else if (STATUS.equals("ON")) {
                        mImgTogContolLED.setBackgroundResource(R.drawable.btn_light_on);
                }
                break;
            case "HUMI":
                if (STATUS.equals("OFF")) {
                    mImgTogContolTV.setBackgroundResource(R.drawable.btn_tv_off);
                } else if (STATUS.equals("ON")) {
                    mImgTogContolTV.setBackgroundResource(R.drawable.btn_tv_on);
                }
                break;
        }
    }

    public class MQTTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            IMqttToken token;
            /*Bundle bundle = intent.getExtras();
            String server = bundle.getString("server");
            String server = "192.168.56.1";*/
            Log.d("server: ",server);

            boolean hasConnectivity = false;
            boolean hasChanged = false;
            anoactivity = context;
            mConnMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo infos[] = mConnMan.getAllNetworkInfo();

            for (int i = 0; i < infos.length; i++) {
                if (infos[i].getTypeName().equalsIgnoreCase("MOBILE")) {
                    if ((infos[i].isConnected() != hasMmobile)) {
                        hasChanged = true;
                        hasMmobile = infos[i].isConnected();
                    }
                    Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
                } else if (infos[i].getTypeName().equalsIgnoreCase("WIFI")) {
                    if ((infos[i].isConnected() != hasWifi)) {
                        hasChanged = true;
                        hasWifi = infos[i].isConnected();
                    }
                    Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
                }
            }
            hasConnectivity = hasMmobile || hasWifi;
            Log.v(TAG, "hasConn: " + hasConnectivity + " hasChange: " + hasChanged + " - " + (mqttClient == null || !mqttClient.isConnected()));
            if (hasConnectivity && hasChanged && (mqttClient == null || !mqttClient.isConnected())) {
                doConnect(server, subTOPIC);
            } else if (!hasConnectivity && mqttClient != null && mqttClient.isConnected()) {
                Log.d(TAG, "doDisconnect()");
                try {
                    token = mqttClient.disconnect();
                    token.waitForCompletion(1000);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }

        private void doConnect(String server, String[] subTOPIC) {
            IMqttToken token;
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            try {
                Log.d(TAG, "doConnect()");
                mqttClient = new MqttAsyncClient("tcp://" + server + ":1883", deviceId, new MemoryPersistence());
                token = mqttClient.connect();
                token.waitForCompletion(3500);
                mqttClient.setCallback(new MqttEventCallback());
                for(int i=0; i<subTOPIC.length; i++)
                    token = mqttClient.subscribe(subTOPIC[i], 0);
                token.waitForCompletion(5000);
            } catch (MqttSecurityException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                switch (e.getReasonCode()) {
                    case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                    case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                    case MqttException.REASON_CODE_CONNECTION_LOST:
                    case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                        Log.v(TAG, "c" + e.getMessage());
                        e.printStackTrace();
                        break;
                    case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                        Intent i = new Intent("RAISEALLARM");
                        i.putExtra("ALLARM", e);
                        Log.e(TAG, "b" + e.getMessage());
                        break;
                    default:
                        Log.e(TAG, "a" + e.getMessage());
                }
            }
        }

        private class MqttEventCallback implements MqttCallback {
            @Override
            public void connectionLost(Throwable arg0) {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {

            }

            @Override
            @SuppressLint("NewApi")
            public void messageArrived(String subTOPIC, final MqttMessage msg) throws Exception {
                Log.i(TAG, "Message arrived from subTOPIC" + subTOPIC);

                Handler h = new Handler(getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String payload = new String(msg.getPayload());
                            //String payload = "{\"CONTROL\":\"ANDROID\"}";
                            changeStatus(payload);
                            Toast.makeText(getBaseContext(), "success", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.d(TAG, "예외 발생 =" + e);
                        }
                    }
                });
            }
        }
        public void onConfigurationChanged(Configuration newConfig) {
            Log.d(TAG, "onConfigurationChanged()");
            android.os.Debug.waitForDebugger();
            onConfigurationChanged(newConfig);
        }

        public String getThread() {
            return Long.valueOf(thread.getId()).toString();
        }

        public IBinder onBind(Intent intent) {
            Log.i(TAG, "onBind called");
            return null;
        }
    }

    private void setClientID(String id) {
        if (deviceId == null) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wInfo = wifiManager.getConnectionInfo();
            deviceId = wInfo.getMacAddress();
        } else {
            deviceId = id;
        }
    }

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

