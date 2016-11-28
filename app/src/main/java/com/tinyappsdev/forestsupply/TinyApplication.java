package com.tinyappsdev.forestsupply;


import android.app.Application;

import com.tinyappsdev.forestsupply.AppGlobal;

public class TinyApplication extends Application {
    private final Object mLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        AppGlobal.createInstance(getApplicationContext());
    }

}
