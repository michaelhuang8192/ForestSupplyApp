package com.tinyappsdev.forestsupply.ui.BaseUI;


public interface SimpleMasterDetailInterface extends ActivityInterface {
    String getSearchQuery();
    Object getItem();
    void selectItem(long _id);
    void setResult(Object item);
    boolean isResultNeeded();
}
