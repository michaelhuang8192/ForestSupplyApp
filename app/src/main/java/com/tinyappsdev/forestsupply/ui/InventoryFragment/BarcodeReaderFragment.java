package com.tinyappsdev.forestsupply.ui.InventoryFragment;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tinyappsdev.forestsupply.R;
import com.tinyappsdev.forestsupply.ui.BaseUI.BaseActivity;
import com.tinyappsdev.forestsupply.ui.BaseUI.BaseFragment;
import com.tinyappsdev.forestsupply.ui.BaseUI.InventoryActivityInterface;
import com.tinyappsdev.forestsupply.Scanner.FrameProcessor;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class BarcodeReaderFragment extends BaseFragment<InventoryActivityInterface> implements
        SurfaceHolder.Callback,
        Camera.AutoFocusCallback {

    public static final String TAG = BarcodeReaderFragment.class.getSimpleName();

    @BindView(R.id.cameraPreview) FrameLayout mCameraPreview;
    @BindView(R.id.cameraScanline) View  mCameraScanline;
    private Unbinder mUnbinder;

    private SurfaceView mCameraSurfaceView;
    private boolean mSurfaceReady;
    private boolean mPreviewActive;
    private FrameProcessor.FrameData mFrameData;
    private byte[] mBuffer;
    private int mFrameCount;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.barcode_reader, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mCameraSurfaceView = new SurfaceView(this.getActivity());
        mCameraSurfaceView.getHolder().addCallback(this);
        mCameraPreview.addView(mCameraSurfaceView);

        ((BaseActivity)mActivity).getSupportActionBar().hide();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();

        ((BaseActivity)mActivity).getSupportActionBar().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        startPreview();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPreview();
    }

    @Override
    protected void onMessage(Message msg) {
        switch(msg.what) {
            case R.id.FrameDataProcessed: {
                Camera camera = mActivity.getCamera();
                if(mPreviewActive)
                    camera.autoFocus(this);
                break;
            }
        }
    }

    protected void startPreview() {
        Camera camera = mActivity.getCamera();
        if(camera != null && !mPreviewActive && mSurfaceReady) {
            mPreviewActive = true;

            try {
                camera.setPreviewDisplay(mCameraSurfaceView.getHolder());
            } catch (IOException e) {
                Log.d(TAG, "Camera:startPreview -> " + e.getMessage());
                return;
            }
            setupBuffer(camera);
            camera.addCallbackBuffer(mBuffer);
            camera.startPreview();
            mFrameCount = 0;
            //camera.autoFocus(this);
        }
    }

    protected void stopPreview() {
        if(mPreviewActive) {
            mPreviewActive = false;
            Camera camera = mActivity.getCamera();
            camera.stopPreview();
            camera.setPreviewCallbackWithBuffer(null);
        }
    }

    @Override
    public void onAutoFocus(boolean b, Camera camera) {
        if(!mPreviewActive) return;
        mActivity.getCamera().addCallbackBuffer(mBuffer);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceReady = true;
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceReady = false;
        stopPreview();
    }


    protected void setupBuffer(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        //buffer
        Camera.Size previewSize = parameters.getPreviewSize();
        int numBitsPerPixel= ImageFormat.getBitsPerPixel(parameters.getPreviewFormat());
        int numBits = previewSize.width * previewSize.height * numBitsPerPixel;
        mBuffer = new byte[(numBits + 7) >> 3];

        int left, top, right, bottom;
        int scanLineTop = mCameraScanline.getTop();
        if(mActivity.isCameraRotated()) {
            double ratio = (double)previewSize.width / mCameraPreview.getHeight();
            left = (int)(scanLineTop * ratio);
            top = 0;
            right = (int)((scanLineTop + mCameraScanline.getHeight()) * ratio);
            bottom = previewSize.width;
        } else {
            double ratio = (double)previewSize.height / mCameraPreview.getHeight();
            left = 0;
            top = (int)(scanLineTop * ratio);
            right = previewSize.width;
            bottom = (int)((scanLineTop + mCameraScanline.getHeight()) * ratio);
        }

        mFrameData = new FrameProcessor.FrameData(
                mBuffer, previewSize.width, previewSize.height, parameters.getPreviewFormat(),
                new Rect(left, top, right, bottom)
        );

        camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                if(mFrameCount++ == 0)
                    camera.autoFocus(BarcodeReaderFragment.this);
                else
                    mActivity.addFrameData(mFrameData);
            }
        });
    }

}
