package com.skyland.jsinterface;

import android.webkit.JavascriptInterface;

public interface JSBridge {

    @JavascriptInterface
    public abstract void album(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void takephoto(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void takevideo(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void takerecord(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void takecode(JSFunction callback);

    @JavascriptInterface
    public abstract void login(String parameter);

    @JavascriptInterface
    public abstract void signOut();

    @JavascriptInterface
    public abstract void showSetting(String parameter);

    @JavascriptInterface
    public abstract void filedown(String parameter);

    @JavascriptInterface
    public abstract void fixOrientation(String parameter);

    @JavascriptInterface
    public abstract void openTab(String parameter);

    @JavascriptInterface
    public abstract void navigateByLocation(String parameter);

    @JavascriptInterface
    public abstract void navigateByAddress(String parameter);

    @JavascriptInterface
    public abstract void getLocation(JSFunction callback);

    @JavascriptInterface
    public abstract void saveData(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void getData(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void openVideo(String parameter);

    @JavascriptInterface
    public abstract void printImage(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void printImageByJC(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void printTextByJC(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void phoneEdit(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void sqliteExecuteSql(String parameter, JSFunction callback);

    @JavascriptInterface
    public abstract void sqliteSaveToLocal(String parameter, JSFunction callback);
}
