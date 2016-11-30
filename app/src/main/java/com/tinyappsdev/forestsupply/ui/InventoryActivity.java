package com.tinyappsdev.forestsupply.ui;

import android.Manifest;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.Surface;
import android.view.View;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.tinyappsdev.forestsupply.AppGlobal;
import com.tinyappsdev.forestsupply.R;
import com.tinyappsdev.forestsupply.data.CountRecord;
import com.tinyappsdev.forestsupply.data.ModelHelper;
import com.tinyappsdev.forestsupply.data.Product;
import com.tinyappsdev.forestsupply.helper.TinyMap;
import com.tinyappsdev.forestsupply.helper.TinyUtils;
import com.tinyappsdev.forestsupply.rest.ApiCallClient;
import com.tinyappsdev.forestsupply.ui.BaseUI.InventoryActivityInterface;
import com.tinyappsdev.forestsupply.ui.BaseUI.SimpleMasterDetailActivity;
import com.tinyappsdev.forestsupply.ui.InventoryFragment.BarcodeReaderFragment;
import com.tinyappsdev.forestsupply.ui.InventoryFragment.InventoryItemInfoFragment;
import com.tinyappsdev.forestsupply.ui.InventoryFragment.InventorySearchFragment;
import com.tinyappsdev.forestsupply.Scanner.FrameProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class InventoryActivity extends SimpleMasterDetailActivity implements
        InventoryActivityInterface {

    private long mSessionId;
    private int mTeamId;
    private Handler mBarcodeHandler;

    public static final int DEFAULT_BARCODE_FORMATS = Barcode.UPC_A | Barcode.UPC_E
            | Barcode.EAN_13 | Barcode.EAN_8
            | Barcode.ISBN | Barcode.ITF
            | Barcode.CODE_39 | Barcode.CODE_93 | Barcode.CODE_128;

    private Camera mCamera;
    private FrameProcessor mFrameProcessor;
    private ToneGenerator mToneGenerator;
    private boolean mIsRotated;
    private final int mCameraId = 0;
    private BarcodeOnResultListener mBarcodeOnResultListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            mSessionId = bundle.getLong("SessionId");
            mTeamId = bundle.getInt("TeamId");
            getSupportActionBar().setTitle(bundle.getString("SessionName") + " - Team #" + mTeamId);
        }


        mTabLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mFrameProcessor != null) {
            mFrameProcessor.setOnResultListener(null);
            mFrameProcessor.release();
        }
        if(mToneGenerator != null) mToneGenerator.release();
        if(mBarcodeHandler != null) mBarcodeHandler.removeCallbacksAndMessages(null);
    }

    protected void openCamera() {
        if(mCamera != null) return;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            TinyUtils.showMsgBox(this, "Please Enable Camera Permission For This App");
            return;
        }

        mCamera = Camera.open(mCameraId);
        if(mCamera == null) return;

        if(mToneGenerator == null) mToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        if(mFrameProcessor == null) {
            mBarcodeHandler = new Handler();
            BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                    .setBarcodeFormats(DEFAULT_BARCODE_FORMATS).build();
            mBarcodeOnResultListener = new BarcodeOnResultListener();
            mFrameProcessor = new FrameProcessor(detector);
            mFrameProcessor.setOnResultListener(mBarcodeOnResultListener);
            mFrameProcessor.start();
        }

        setupCamera();
    }

    protected void closeCamera() {
        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void showScanner(View view) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("scanner");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == null) fragment = new BarcodeReaderFragment();

        fragmentTransaction.add(R.id.mainContainer, fragment, "scanner");
        fragmentTransaction.addToBackStack("scanner");
        fragmentTransaction.commit();
    }

    public void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        mIsRotated = degrees % 180 != info.orientation % 180;
        mCamera.setDisplayOrientation(result);
    }

    protected void setupCamera() {
        //orientation
        setCameraDisplayOrientation();

        //focus
        Camera.Parameters parameters = mCamera.getParameters();
        if(parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> areaList = new ArrayList<Camera.Area>();
            areaList.add(new Camera.Area(new Rect(-100, -100, 100, 100), 1000));
            parameters.setFocusAreas(areaList);
        }
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        //fps
        int[] fpsRange = selectPreviewFpsRange(mCamera, 30);
        parameters.setPreviewFpsRange(fpsRange[0], fpsRange[1]);

        mCamera.setParameters(parameters);
    }

    private int[] selectPreviewFpsRange(Camera camera, float desiredPreviewFps) {
        int desiredPreviewFpsScaled = (int) (desiredPreviewFps * 1000.0f);

        int[] selectedFpsRange = null;
        int minDiff = Integer.MAX_VALUE;
        List<int[]> previewFpsRangeList = camera.getParameters().getSupportedPreviewFpsRange();
        for (int[] range : previewFpsRangeList) {
            int deltaMin = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
            int deltaMax = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            int diff = Math.abs(deltaMin) + Math.abs(deltaMax);
            if (diff < minDiff) {
                selectedFpsRange = range;
                minDiff = diff;
            }
        }
        return selectedFpsRange;
    }

    @Override
    protected void loadItem(long _id) {
        if(mItemLoader != null) mItemLoader.cancel();
        mItemLoader = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                "/InventoryCountProduct/getDoc?_id=" + _id,
                null,
                Product.class,
                new ApiCallClient.OnResultListener<Product>() {
                    @Override
                    public void onResult(ApiCallClient.Result<Product> result) {
                        mItemLoader = null;
                        if(result.error != null || result.data == null) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                            onLoadedItem(null);
                        } else {
                            onLoadedItem(result.data);
                        }
                    }
                }
        );
    }

    @Override
    protected Fragment createSearchFragment() {
        return InventorySearchFragment.newInstance();
    }

    @Override
    protected Object loadItemFromJson(String json) {
        return ModelHelper.fromJson(json, Product.class);
    }

    @Override
    protected String dumpItemToJson(Object item) {
        return ModelHelper.toJson(item);
    }

    @Override
    protected FragmentPagerAdapter createFragmentPagerAdapter(FragmentManager fragmentManager) {
        return new SectionsPagerAdapter(getSupportFragmentManager());
    }

    @Override
    public long getSessionId() {
        return mSessionId;
    }

    @Override
    public int getTeamId() {
        return mTeamId;
    }

    @Override
    public void addCountRecord(CountRecord countRecord) {
        if(mResult != null) mResult.cancel();
        mResult = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                "/InventoryCount/addCountRecord",
                countRecord,
                Map.class,
                new ApiCallClient.OnResultListener<Map>() {
                    @Override
                    public void onResult(ApiCallClient.Result<Map> result) {
                        TinyMap map = TinyMap.AsTinyMap(result.data);
                        if(result.error != null || map == null || !map.getBoolean("success")) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                        } else if(map.getLong("_id") <= 0) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                        } else {
                            sendMessage(R.id.SimpleMasterDetailOnItemUpdate);
                        }
                    }
                }
        );
    }

    @Override
    public void editCountRecord(CountRecord countRecord) {
        if(mResult != null) mResult.cancel();
        mResult = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                "/InventoryCount/editCountRecord",
                countRecord,
                Map.class,
                new ApiCallClient.OnResultListener<Map>() {
                    @Override
                    public void onResult(ApiCallClient.Result<Map> result) {
                        TinyMap map = TinyMap.AsTinyMap(result.data);
                        if(result.error != null || map == null || !map.getBoolean("success")) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                        } else if(map.getLong("_id") <= 0) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                        } else {
                            sendMessage(R.id.SimpleMasterDetailOnItemUpdate);
                        }
                    }
                }
        );
    }

    @Override
    public void deleteCountRecord(CountRecord countRecord) {
        if(mResult != null) mResult.cancel();
        mResult = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                "/InventoryCount/deleteCountRecord",
                countRecord,
                Map.class,
                new ApiCallClient.OnResultListener<Map>() {
                    @Override
                    public void onResult(ApiCallClient.Result<Map> result) {
                        TinyMap map = TinyMap.AsTinyMap(result.data);
                        if(result.error != null || map == null || !map.getBoolean("success")) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                        } else if(map.getLong("_id") <= 0) {
                            TinyUtils.showMsgBox(getApplicationContext(), R.string.error_occurred);
                        } else {
                            sendMessage(R.id.SimpleMasterDetailOnItemUpdate);
                        }
                    }
                }
        );
    }

    @Override
    public Camera getCamera() {
        openCamera();
        return mCamera;
    }

    @Override
    public boolean isCameraRotated() {
        return mIsRotated;
    }

    @Override
    public void addFrameData(FrameProcessor.FrameData frameData) {
        mFrameProcessor.addFrame(frameData);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0)
                return InventoryItemInfoFragment.newInstance();
            return null;
        }

        @Override
        public int getCount() {
            return 1;
        }

        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return getString(R.string.inventory_count_item_info);
            return null;
        }

    }


    protected void getItemByBarcode(final long UPC) {
        if(mItemLoader != null) mItemLoader.cancel();
        mItemLoader = AppGlobal.getInstance().getUiApiCallClient().makeCall(
                String.format(
                        "/InventoryCountProduct/getDocByUPC?inventoryCountId=%d&UPC=%d",
                        mSessionId,
                        UPC
                        ),
                null,
                Product.class,
                new ApiCallClient.OnResultListener<Product>() {
                    @Override
                    public void onResult(ApiCallClient.Result<Product> result) {
                        mItemLoader = null;
                        if(result.error != null || result.data == null) {
                            selectItem(0, null);
                            TinyUtils.showMsgBox(
                                    getApplicationContext(),
                                    String.format("%s not found", UPC)
                            );
                        } else {
                            selectItem(result.data.getId(), result.data);
                        }
                    }
                }
        );
    }

    protected void onBarcode(long barcode) {
        if(barcode != 0) {
            mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            getSupportFragmentManager().popBackStack("scanner", POP_BACK_STACK_INCLUSIVE);
            getItemByBarcode(barcode);
        }

        sendMessage(R.id.FrameDataProcessed);
    }

    class BarcodeOnResultListener implements FrameProcessor.OnResultListener {
        @Override
        public void onResult(final FrameProcessor.FrameData frameData, final SparseArray barcodes) {
            String targetBarcode = null;
            if(barcodes != null && barcodes.size() > 0) {
                for(int i = 0; i < barcodes.size(); i++) {
                    Barcode barcode = (Barcode)barcodes.valueAt(i);
                    Rect rect = barcode.getBoundingBox();

                    if(frameData.rect.contains(rect)) {
                        targetBarcode = barcode.displayValue;
                        break;
                    }
                }
            }

            long UPC = 0;
            if(targetBarcode != null) {
                try {
                    UPC = Long.parseLong(targetBarcode);
                } catch(NumberFormatException e) {
                }
            }

            final long _UPC = UPC;
            mBarcodeHandler.post(new Runnable() {
                @Override
                public void run() {
                    onBarcode(_UPC);
                }
            });
        }
    }
}
