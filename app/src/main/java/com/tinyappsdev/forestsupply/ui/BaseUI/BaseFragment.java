package com.tinyappsdev.forestsupply.ui.BaseUI;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;


public class BaseFragment<AI extends ActivityInterface> extends Fragment {
    protected AI mActivity;
    protected Handler mMsgHandler;

    protected void onMessage(Message msg) {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMsgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                onMessage(msg);
            }

            @Override
            public String toString() {
                return "Handler(" + BaseFragment.this.toString() + ")";
            }
        };
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
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
