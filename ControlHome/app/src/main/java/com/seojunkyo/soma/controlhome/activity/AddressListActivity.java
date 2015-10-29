package com.seojunkyo.soma.controlhome.activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.databases.CustomCursorAdapter;
import com.seojunkyo.soma.controlhome.databases.DbOpenHelper;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;
import com.seojunkyo.soma.controlhome.util.AddressList;
import com.seojunkyo.soma.controlhome.util.AddressListAdapter;
import com.seojunkyo.soma.controlhome.util.MQTTUtils;

import java.util.ArrayList;

public class AddressListActivity extends CONTROLHOMEActivity {

    public ArrayList<AddressList> items;
    private AddressListAdapter adapter;
    private PopupWindow pwindo;
    private int mWidthPixels, mHeightPixels;

    private CustomCursorAdapter customAdapter;
    private DbOpenHelper databaseHelper;
    private static final int ENTER_DATA_REQUEST_CODE = 1;

    private String server;

    private static final String TAG = AddressListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        initAcitivy();
        setLayout();
    }

    private ListView mListView;
    private Button mFloatingButton;
    private Button btn_cancel;
    private Button btn_ok;
    private EditText hubSpace;
    private EditText hubAddress;

    @Override
    public void initAcitivy() {
        databaseHelper = new DbOpenHelper(this);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                customAdapter = new CustomCursorAdapter(AddressListActivity.this, databaseHelper.getAllData());
                mListView.setAdapter(customAdapter);
            }
        });

        mListView = (ListView) findViewById(R.id.mListView);
        mFloatingButton = (Button) findViewById(R.id.mFloatingActionButton);
        items = new ArrayList<AddressList>();


        items.add(new AddressList("MY HOME", "swhomegateway.dyndns.org"));
        items.add(new AddressList("TEST", "172.16.100.62"));
        adapter = new AddressListAdapter(getLayoutInflater(), items);
        mListView.setAdapter(adapter);

        WindowManager w = getWindowManager();
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        mWidthPixels = metrics.widthPixels;
        mHeightPixels = metrics.heightPixels;
        if (Build.VERSION.SDK_INT >= 17)
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
                mWidthPixels = realSize.x;
                mHeightPixels = realSize.y;
            } catch (Exception ignored) {
            }
    }

    @Override
    public void setLayout() {

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dialog(items.get(position).getAddress());

            }
        });

        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopupWindow(v);
            }
        });
    }

    public void initiatePopupWindow(View view) {
        try {
            //  LayoutInflater 객체와 시킴
            LayoutInflater inflater = (LayoutInflater) AddressListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View layout = inflater.inflate(R.layout.popup_space,
                    (ViewGroup) view.findViewById(R.id.popup_add_space));

            pwindo = new PopupWindow(layout, mWidthPixels - 100, mHeightPixels - 1200, true);
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);

            hubSpace = (EditText) layout.findViewById(R.id.popup_space);
            hubAddress = (EditText) layout.findViewById(R.id.popup_address);

            btn_cancel = (Button) layout.findViewById(R.id.popup_reminder_btn_close);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwindo.dismiss();
                }
            });

            btn_ok = (Button) layout.findViewById(R.id.popup_reminder_btn_check);
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        items.add(new AddressList(hubSpace.getText().toString(), hubAddress.getText().toString()));
                    } catch (Exception e) {
                        items.add(new AddressList("MY HOME", "swhomegateway.dyndns.org"));
                    }
                    adapter = new AddressListAdapter(getLayoutInflater(), items);
                    mListView.setAdapter(adapter);
                    pwindo.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void Dialog(final String address) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);


        alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, "clicked on item: " + address);
                if (MQTTUtils.connect(address)) {
                    Toast.makeText(getApplicationContext(), "연결성공", Toast.LENGTH_SHORT).show();
                    ControlActivity.server = address;
                    startActivity(new Intent(AddressListActivity.this, ControlActivity.class));
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
}