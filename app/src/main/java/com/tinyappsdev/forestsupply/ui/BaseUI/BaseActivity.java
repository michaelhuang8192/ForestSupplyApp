package com.tinyappsdev.forestsupply.ui.BaseUI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.tinyappsdev.forestsupply.AppGlobal;
import com.tinyappsdev.forestsupply.rest.ApiCallClient;
import com.tinyappsdev.forestsupply.ui.LoginActivity;

import java.util.HashSet;
import java.util.Set;

public class BaseActivity extends AppCompatActivity implements ActivityInterface {
    public final static String TAG = BaseActivity.class.getSimpleName();

    protected Set<Handler> mMsgHandlers;
    protected SharedPreferences mSharedPreferences;
    protected ApiCallClient.Result mResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mMsgHandlers = new HashSet<Handler>();
        mSharedPreferences = AppGlobal.getInstance().getSharedPreferences();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!(this instanceof LoginActivity)) {
            String serverAuth = mSharedPreferences.getString("serverAuth", "");
            if(serverAuth == null || serverAuth.isEmpty())
                AppGlobal.getInstance().showLogin(this, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMsgHandlers.clear();
        if(mResult != null) mResult.cancel();
    }

    @Override
    public void registerMsgHandler(Handler handler) {
        Log.d(TAG, String.format("%s.registerMsgHandler(%s)", this.toString(), handler.toString()));
        mMsgHandlers.add(handler);
    }

    @Override
    public void unregisterMsgHandler(Handler handler) {
        Log.d(TAG, String.format("%s.unregisterMsgHandler(%s)", this.toString(), handler.toString()));
        mMsgHandlers.remove(handler);
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public void sendMessage(int msgId) {
        for(Handler handler : mMsgHandlers)
            handler.sendEmptyMessage(msgId);
    }

    public void sendMessage(int msgId, int arg1, int arg2, Bundle data) {
        for(Handler handler : mMsgHandlers) {
            Message msg = handler.obtainMessage(msgId, arg1, arg2);
            if(data != null) msg.setData(data);
            handler.sendMessage(msg);
        }
    }

}
