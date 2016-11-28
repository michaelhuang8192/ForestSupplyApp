package com.tinyappsdev.forestsupply.ui.BaseUI;

import android.content.SharedPreferences;
import android.os.Handler;


public interface ActivityInterface {
    void registerMsgHandler(Handler handler);
    void unregisterMsgHandler(Handler handler);
    SharedPreferences getSharedPreferences();
}
