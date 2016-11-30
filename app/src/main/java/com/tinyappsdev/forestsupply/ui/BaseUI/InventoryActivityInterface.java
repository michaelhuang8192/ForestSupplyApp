package com.tinyappsdev.forestsupply.ui.BaseUI;

import android.hardware.Camera;

import com.tinyappsdev.forestsupply.data.CountRecord;
import com.tinyappsdev.forestsupply.Scanner.FrameProcessor;

/**
 * Created by pk on 11/25/2016.
 */

public interface InventoryActivityInterface extends SimpleMasterDetailInterface {
    long getSessionId();
    int getTeamId();
    void addCountRecord(CountRecord countRecord);
    void editCountRecord(CountRecord countRecord);
    void deleteCountRecord(CountRecord countRecord);
    Camera getCamera();
    boolean isCameraRotated();
    void addFrameData(FrameProcessor.FrameData frameData);
}
