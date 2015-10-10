package com.seojunkyo.soma.controlhome.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;
import com.seojunkyo.soma.controlhome.util.MQTTUtils;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

public class MainActivity extends CONTROLHOMEActivity {

    public ConnectivityManager mConnMan;
    private static final String TAG = "MQTTService";
    private volatile IMqttAsyncClient mqttClient;
    private boolean hasWifi = false;
    private boolean hasMmobile = false;
    private String deviceId;
    private Context anoactivity;
    private Thread thread;
    MQTTBroadcastReceiver mMqttReceiver = new MQTTBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter intentf = new IntentFilter();
        intentf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        setClientID("HOME");
        registerReceiver(new MQTTBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        initAcitivy();
        setLayout();
    }

    private ToggleButton mImgTogContoldv;
    private String server;
    private String subTOPIC;
    private String pubTOPIC;
    private ImageButton mBtnSetting;
    private ImageButton mBtnConnect;

    @Override
    public void initAcitivy() {
        mImgTogContoldv = (ToggleButton) findViewById(R.id.toggle_tv);
        mBtnSetting = (ImageButton) findViewById(R.id.setting);
        mBtnConnect = (ImageButton) findViewById(R.id.connect);

        //server = "swhomegateway.dyndns.org";
        subTOPIC = "STATUS";
        //server = "172.16.100.62";
        server = "192.168.0.73";
        pubTOPIC = "CONTROL";

        mImgTogContoldv.setEnabled(false);
    }

    @Override
    public void setLayout() {
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MQTTUtils.connect(server)) {
                    Toast.makeText(getApplicationContext(), "연결성공", Toast.LENGTH_SHORT).show();
                    mImgTogContoldv.setEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "연결실패", Toast.LENGTH_SHORT).show();
                    mImgTogContoldv.setEnabled(false);
                }
            }
        });
        mImgTogContoldv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceId = "TV";
                setClientID(deviceId);
                publish(pubTOPIC);
            }
        });
        mBtnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog();
            }
        });
    }

    private void publish(String pubTOPIC) {
        if (mImgTogContoldv.isChecked()) {
            mImgTogContoldv.setBackgroundResource(R.drawable.btn_tv_off);
            String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"LED1\", \"COMMAND\":\"OFF\"}");
            MQTTUtils.pub(pubTOPIC, PAYLOAD);
        } else {
            mImgTogContoldv.setBackgroundResource(R.drawable.btn_tv_on);
            String PAYLOAD = String.format("{\"CONTROLLER\":\"ANDROID\", \"TARGET\":\"LED1\", \"COMMAND\":\"ON\"}");
            MQTTUtils.pub(pubTOPIC, PAYLOAD);
        }
    }

    public void changeStatus(String STATUS){
        if(STATUS.equals("OFF")){
            mImgTogContoldv.setBackgroundResource(R.drawable.btn_tv_off);
        }
        else if(STATUS.equals("ON")){
            mImgTogContoldv.setBackgroundResource(R.drawable.btn_tv_on);
        }
    }
    private void Dialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("주소를 입력하세요");

        final EditText name = new EditText(this);
        alert.setView(name);

        alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                server = name.getText().toString();
                if (MQTTUtils.connect(server)) {
                    Toast.makeText(getApplicationContext(), "재연결성공", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "연결실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alert.setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    public class MQTTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            IMqttToken token;
            /*Bundle bundle = intent.getExtras();
            String server = bundle.getString("server");
            String server = "192.168.56.1";*/
            Log.d(TAG, server);

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

        private void doConnect(String server, String subTOPIC) {
            Log.d(TAG, "doConnect()");
            IMqttToken token;
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            try {
                mqttClient = new MqttAsyncClient("tcp://" + server + ":1883", deviceId, new MemoryPersistence());
                token = mqttClient.connect();
                token.waitForCompletion(3500);
                mqttClient.setCallback(new MqttEventCallback());
                token = mqttClient.subscribe(subTOPIC, 0);
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
                            JSONObject subObject = new JSONObject(payload);
                            String devName = subObject.getString("LED1");
                            changeStatus(devName);
                            Toast.makeText(getApplicationContext(), devName, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.d(TAG, "예외 발생 =" + e);
                        }
                    }
                });
            }
        }
        /*@Override
        public void onConfigurationChanged(Configuration newConfig) {
            Log.d(TAG, "onConfigurationChanged()");
            android.os.Debug.waitForDebugger();
            super.onConfigurationChanged(newConfig);
        }

        public String getThread() {
            return Long.valueOf(thread.getId()).toString();
        }

        public IBinder onBind(Intent intent) {
            Log.i(TAG, "onBind called");
            return null;
        }*/
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

