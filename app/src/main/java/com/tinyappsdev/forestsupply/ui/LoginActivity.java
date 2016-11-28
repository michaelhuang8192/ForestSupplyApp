package com.tinyappsdev.forestsupply.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.tinyappsdev.forestsupply.AppGlobal;
import com.tinyappsdev.forestsupply.R;
import com.tinyappsdev.forestsupply.helper.TinyMap;
import com.tinyappsdev.forestsupply.helper.TinyUtils;
import com.tinyappsdev.forestsupply.rest.ApiCallClient;
import com.tinyappsdev.forestsupply.ui.BaseUI.BaseActivity;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.userName) EditText mUserName;
    @BindView(R.id.userPassword) EditText mUserPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUserName.setText(mSharedPreferences.getString("userName", ""));

        checkLogin();
    }

    public void checkLogin() {
        if(mResult != null) mResult.cancel();
        mResult = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                "/Auth/checkAuth",
                null,
                Map.class,
                new ApiCallClient.OnResultListener<Map>() {
                    @Override
                    public void onResult(ApiCallClient.Result<Map> result) {
                        mResult = null;
                        TinyMap map = TinyMap.AsTinyMap(result.data);
                        if(result.error != null || map == null || !map.getBoolean("success")) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                        } else if(map.getBoolean("authFailed")) {
                        } else {
                            onLoginSuccessful();
                        }
                    }
                }
        );
    }



    public void login(View view) {
        String userName = mUserName.getText().toString();
        String userPassword = mUserPassword.getText().toString();

        if(userName.length() <= 0) {
            TinyUtils.showMsgBox(this, R.string.msg_invalid_input);
            return;
        }

        mSharedPreferences.edit().putString("userName", userName).apply();
        if(mResult != null) mResult.cancel();

        Map<String, String> map = new HashMap();
        map.put("userName", userName);
        map.put("userPassword", userPassword);
        mResult = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                "/Auth/getAuth",
                map,
                Map.class,
                new ApiCallClient.OnResultListener<Map>() {
                    @Override
                    public void onResult(ApiCallClient.Result<Map> result) {
                        mResult = null;
                        TinyMap map = TinyMap.AsTinyMap(result.data);
                        if(result.error != null || map == null || !map.getBoolean("success")) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                        } else if(map.getBoolean("authFailed")) {
                            mUserPassword.setText("");
                        } else {
                            mUserPassword.setText("");
                            onLoginSuccessful();
                        }
                    }
                }
        );
    }

    protected void onLoginSuccessful() {
        Bundle bundle = getIntent().getExtras();
        finish();
        if(bundle == null || !bundle.getBoolean("popup")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
