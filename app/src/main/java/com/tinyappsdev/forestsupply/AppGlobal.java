package com.tinyappsdev.forestsupply;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;


import com.tinyappsdev.forestsupply.data.ModelHelper;
import com.tinyappsdev.forestsupply.rest.ApiCallClient;
import com.tinyappsdev.forestsupply.rest.HttpClient;
import com.tinyappsdev.forestsupply.ui.LoginActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AppGlobal {
    public static final String TAG = AppGlobal.class.getSimpleName();
    private static AppGlobal sAppGlobal;

    public static AppGlobal createInstance(Context context) {
        Context appContext = context.getApplicationContext();
        synchronized (AppGlobal.class) {
            if(sAppGlobal != null) {
                sAppGlobal.mMsgHandlers.clear();
            }

            Log.i(TAG, "AppGlobal Created - " + appContext.toString());
            sAppGlobal = new AppGlobal(appContext);
        }

        return sAppGlobal;
    }

    public static AppGlobal getInstance() {
        return sAppGlobal;
    }

    private Context mContext;
    private ApiCallClient mUiApiCallClient;
    private SharedPreferences mSharedPreferences;
    private final Set<Handler> mMsgHandlers = new HashSet();


    public AppGlobal(Context context) {
        mContext = context.getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mUiApiCallClient = new ApiCallClient() {
            @Override
            public String toJson(Object obj) {
                return ModelHelper.toJson(obj);
            }

            @Override
            public Object fromJson(String str, Class resultType) {
                return ModelHelper.fromJson(str, resultType);
            }
        };
        mUiApiCallClient.setServerAddress(context.getString(R.string.serverAddress));
        mUiApiCallClient.setOnCookieListener(new HttpClient.OnCookieListener() {
            @Override
            public Map<String, String> load(String uri) {
                Map<String, String> map = new HashMap();
                map.put("serverAuth", mSharedPreferences.getString("serverAuth", ""));
                return map;
            }

            @Override
            public void save(String uri, Map<String, String> cookies) {
                String serverAuth = cookies.get("serverAuth");
                if(serverAuth == null || serverAuth.isEmpty())
                    mSharedPreferences.edit().remove("serverAuth").apply();
                else
                    mSharedPreferences.edit().putString("serverAuth", serverAuth).apply();
            }
        });

    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public ApiCallClient getUiApiCallClient() { return mUiApiCallClient; }

    public void showLogin(Context context) {
        showLogin(context, false);
    }

    public void showLogin(Context context, boolean clearAll) {
        Intent intent = new Intent(context, LoginActivity.class);
        if(clearAll) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            Bundle bundle = new Bundle();
            bundle.putBoolean("popup", true);
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }



    public void registerMsgHandler(Handler handler) {
        synchronized (mMsgHandlers) {
            mMsgHandlers.add(handler);
        }
    }

    public void unregisterMsgHandler(Handler handler) {
        synchronized (mMsgHandlers) {
            mMsgHandlers.remove(handler);
        }
    }

    public void sendMessage(int msgId) {
        synchronized (mMsgHandlers) {
            for(Handler handler : mMsgHandlers)
                handler.sendEmptyMessage(msgId);
        }
    }
}
