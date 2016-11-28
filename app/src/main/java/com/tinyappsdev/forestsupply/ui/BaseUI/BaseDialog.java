package com.tinyappsdev.forestsupply.ui.BaseUI;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;


public class BaseDialog<AI extends ActivityInterface> extends DialogFragment {
    protected AI mActivity;
    protected Handler mMsgHandler;

    protected void onMessage(Message msg) {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMsgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                onMessage(msg);
            }

            @Override
            public String toString() {
                return "Handler(" + BaseDialog.this.toString() + ")";
            }
        };
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity.registerMsgHandler(mMsgHandler);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.unregisterMsgHandler(mMsgHandler);
        mMsgHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ActivityInterface)
            mActivity = (AI)context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity = null;
    }
}
