package com.skyland.zht;

import java.io.File;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;
import com.loopj.android.http.PersistentCookieStore;

import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.ImageLoader;
import cn.finalteam.galleryfinal.ThemeConfig;
import cn.jpush.android.api.JPushInterface;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.tencent.smtt.sdk.QbSdk;
import com.videogo.openapi.EZOpenSDK;

public class App extends Application {

    public static App shared;
    private String host = "";
    private String deviceId = "";
    private UserInfo userInfo;
    private boolean connected = true;
    public boolean tagServiceRuning;
    private PersistentCookieStore cookieStore;
    private int orientation = 1;
    private boolean firstload = true;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = new AMapLocationClientOption();
    private ILocation iLocation = null;

    public ILocation getiLocation() {
        return iLocation;
    }

    public void setiLocation(ILocation iLocation) {
        this.iLocation = iLocation;
    }

    public boolean isFirstload() {
        return firstload;
    }

    public void setFirstload(boolean firstload) {
        this.firstload = firstload;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        shared = this;
        initApp();
        new DeviceInfo(this);
        setDeviceId(DeviceInfo.uuid.toString());
        JPushInterface.setDebugMode(false);
        JPushInterface.init(this);
        userInfo = new UserInfo();
        userInfo.init(getApplicationContext());
        tagServiceRuning = false;
        cookieStore = new PersistentCookieStore(getApplicationContext());
        QbSdk.initX5Environment(getApplicationContext(), null);
        initLocation();
        initGalleryFinal();
    }

    private void initGalleryFinal() {
        ThemeConfig theme = new ThemeConfig.Builder()
                .setTitleBarTextColor(Color.parseColor("#FFFFFF"))
                .setTitleBarBgColor(Color.parseColor("#59B5AE"))
                .build();
        FunctionConfig functionConfig = new FunctionConfig.Builder()
                .build();
        ImageLoader imageloader = new XUtils2ImageLoader(this);
        CoreConfig coreConfig = new CoreConfig.Builder(this, imageloader, theme)
                .setFunctionConfig(functionConfig)
                .build();
        GalleryFinal.init(coreConfig);
    }

    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        //设置定位参数
        locationClient.setLocationOption(getDefaultOption());
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                if (loc.getErrorCode() == 0){
                //解析定位结果
                //String result = Utils.getLocationStr(loc);
                double latitude = loc.getLatitude();
                double longitude = loc.getLongitude();
                if (iLocation != null) {
                    iLocation.OnLocationResult(latitude, longitude);
                }
                }else{
                    Log.e("AmapError","location Error, ErrCode: "+ loc.getErrorCode() + ", errInfo:" + loc.getErrorInfo());
                }
            } else {

            }
        }
    };

    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        return mOption;
    }

    public void startLocation() {
        // 启动定位
        locationClient.startLocation();
    }

    public void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public PersistentCookieStore getCookieStore() {
        return cookieStore;
    }

    public void startTagService() {
        if (tagServiceRuning) {
            return;
        }
        if (userInfo.isLogin()) {
            // if (!App.shared.getTag().equals(
            // App.shared.getUserInfo().getUserId())) {
            Intent service = new Intent(this, TagService.class);
            startService(service);
            tagServiceRuning = true;
            // }
        }
    }

    public void stopTagService() {
        Intent service = new Intent(this, TagService.class);
        stopService(service);
        tagServiceRuning = false;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void initApp() {
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                MainActivity.verifyStoragePermissions(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        File appPath = Utility.getAppStoreDir(this);
        if (!appPath.exists()) {
            appPath.mkdir();
        }

        File imagesPath = Utility.getImageStoreDir(this);
        if (!imagesPath.exists()) {
            imagesPath.mkdir();
        }

        File videosPath = Utility.getVideoStoreDir(this);
        if (!videosPath.exists()) {
            videosPath.mkdir();
        }

        File audiosPath = Utility.getAudioStoreDir(this);
        if (!audiosPath.exists()) {
            audiosPath.mkdir();
        }

        File downloadFilePath = Utility.getDownloadStoreDir(this);
        if (!downloadFilePath.exists()) {
            downloadFilePath.mkdir();
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl(String type) {
        if (this.host.equals("")) {
            return "";
        }
        return this.host + type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTag() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        return sp.getString("bind_Tag", "");
    }

    public void setTag(String tag) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        Editor editor = sp.edit();
        editor.putString("bind_Tag", tag);
        editor.commit();
        tagServiceRuning = false;
    }

    public void clearTag() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        Editor editor = sp.edit();
        editor.remove("bind_Tag");
        editor.commit();
    }
}
