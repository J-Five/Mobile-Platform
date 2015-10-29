package com.seojunkyo.soma.controlhome.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.seojunkyo.soma.controlhome.R;
import com.seojunkyo.soma.controlhome.ui.CONTROLHOMEActivity;

/**
 * Created by seojunkyo on 15. 10. 27..
 */
public class LoginActivity extends CONTROLHOMEActivity {

    public static Activity sActivityReference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initAcitivy();
        setLayout();
    }

    private EditText mEditEmail;
    private EditText mEditPW;
    private ImageButton mBtnLogin;

    @Override
    public void initAcitivy() {
        mEditEmail = (EditText) findViewById(R.id.login_email);
        mEditPW = (EditText) findViewById(R.id.login_password);
        mBtnLogin = (ImageButton) findViewById(R.id.login_connect);
    }

    @Override
    public void setLayout() {
        mEditEmail.setPadding(150,0,0,70);
        mEditPW.setPadding(150,0,0,70);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, AddressListActivity.class));
            }
        });
    }
}
