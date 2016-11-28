package com.tinyappsdev.forestsupply.helper;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by pk on 11/20/2016.
 */

public class TinyUtils {

    public static double toPrecision(double v, int precision) {
        double factor = Math.pow(10, precision);
        return Math.round(v * factor) / factor;
    }

    public static void showMsgBox(Context context, int resId) {
        showMsgBox(context, context.getString(resId));
    }

    public static void showMsgBox(Context context, String str) {
        Toast.makeText(
                context,
                str,
                Toast.LENGTH_LONG
        ).show();
    }
}
