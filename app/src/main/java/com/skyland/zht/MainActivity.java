package com.skyland.zht;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dothantech.lpapi.AtBitmap;
import com.dothantech.printer.IDzPrinter.PrintParamName;
import com.dothantech.lpapi.LPAPI;
import com.dothantech.printer.IDzPrinter;
import com.google.zxing.client.android.CaptureActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.skyland.jsinterface.JSBridge;
import com.skyland.jsinterface.JSFunction;
import com.skyland.jsinterface.JSWebView;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.videogo.openapi.EZOpenSDK;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.calvin.ActionSheet;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.jpush.android.api.JPushInterface;

public class MainActivity extends Activity implements JSBridge, ILocation {

    JSWebView mWebView;
    ProgressBar mProgressBar;
    JSFunction jsCallBack;
    String data;
    File path;
    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    Handler handler;
    FileReceiver fileReceiver = new FileReceiver();
    Bundle bundle = null;
    Button btnSetting;
    Button onlineBtn;//在线加载
    Button offlineBtn;//离线加载
    Button btnOrientation;
    RelativeLayout settingView;
    boolean isTab = false;
    ImageButton btnBack;
    TextView txtTitle;
    boolean isResume = false;
    Dialog dialog;
    LPAPI api;
    List<IDzPrinter.PrinterAddress> pairedPrinters = new ArrayList<IDzPrinter.PrinterAddress>();
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private Dao dao;
    // LPAPI 打印机操作相关的回调函数。
    private final LPAPI.Callback mCallback = new LPAPI.Callback() {

        /****************************************************************************************************************************************/
        // 所有回调函数都是在打印线程中被调用，因此如果需要刷新界面，需要发送消息给界面主线程，以避免互斥等繁琐操作。

        /****************************************************************************************************************************************/

        // 打印机连接状态发生变化时被调用
        @Override
        public void onStateChange(IDzPrinter.PrinterAddress arg0, IDzPrinter.PrinterState arg1) {
            final IDzPrinter.PrinterAddress printer = arg0;
            switch (arg1) {
                case Connected:
                case Connected2:
                    // 打印机连接成功，发送通知，刷新界面提示
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //	onPrinterConnected(printer);
                        }
                    });
                    break;

                case Disconnected:
                    // 打印机连接失败、断开连接，发送通知，刷新界面提示
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //	onPrinterDisconnected();
                        }
                    });
                    break;

                default:
                    break;
            }
        }

        // 蓝牙适配器状态发生变化时被调用
        @Override
        public void onProgressInfo(IDzPrinter.ProgressInfo arg0, Object arg1) {
        }

        @Override
        public void onPrinterDiscovery(IDzPrinter.PrinterAddress arg0, IDzPrinter.PrinterInfo arg1) {
        }

        // 打印标签的进度发生变化是被调用
        @Override
        public void onPrintProgress(IDzPrinter.PrinterAddress address, Object bitmapData, IDzPrinter.PrintProgress progress, Object addiInfo) {
            switch (progress) {
                case Success:
                    // 打印标签成功，发送通知，刷新界面提示
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //	onPrintSuccess();
                        }
                    });
                    break;

                case Failed:
                    // 打印标签失败，发送通知，刷新界面提示
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //		onPrintFailed();
                        }
                    });
                    break;

                default:
                    break;
            }
        }
    };


    //    @Override
    protected void onCreate1(Bundle savedInstanceState) {
        verifyStoragePermissions(this);
        super.onCreate(savedInstanceState);
        dao = new Dao(this);
        //初始化建基本表
        dao.initSysTable();

        setContentView(R.layout.activity_login_options);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        // TODO Auto-generated method stub
        Log.d("Msg", "SharedPreferences");
        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        //在线地址
        String addressString = settings.getString("address_preference", "");
        if (!addressString.equals("") && !addressString.endsWith("/"))
            addressString = addressString + "/";
        //离线地址
        final String offlineString = settings.getString("address_offline", "");

        btnSetting = (Button) findViewById(R.id.setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Config.customSetting) {
                    Intent intent = new Intent(MainActivity.this,
                            CustomSettingActivity.class);
                    startActivityForResult(intent, Config.SETTING_REQUEST_CODE);
                } else {
                    Intent intent = new Intent(MainActivity.this,
                            SettingActivity.class);
                    startActivityForResult(intent, Config.SETTING_REQUEST_CODE);
                }
            }
        });
        final LinearLayout settingView2 = (LinearLayout) findViewById(R.id.optionView);
        httpClient = new AsyncHttpClient();
        httpClient.setTimeout(120000);
        mWebView = (JSWebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebClient());
        mWebView.setWebChromeClient(new WebChrome());
        final WebSettings webSetting = mWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setAllowFileAccess(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);
        //lufei20180926
        webSetting.setAppCacheMaxSize(1024 * 1024 * 8);//设置缓冲大小，我设的是8M
        String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        webSetting.setAppCachePath(appCacheDir);

        //在线加载
        onlineBtn = (Button) findViewById(R.id.onlineBtn);
        final String finalAddressString = addressString;
        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.shared.setHost(finalAddressString);
                String loadUrl = App.shared.getUrl(Config.MainPage);
                if (!loadUrl.equals("")) {
                    webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
                    settingView2.setVisibility(View.GONE);
                    mWebView.loadUrl(loadUrl);
                }
            }
        });
        //离线加载
        offlineBtn = (Button) findViewById(R.id.offlineBtn);
        offlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loadUrl = offlineString;
                if (!loadUrl.equals("")) {
                    settingView2.setVisibility(View.GONE);
                    webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
                    mWebView.loadUrl(loadUrl);
                }
            }
        });
        settingView2.setVisibility(View.VISIBLE);
        App.shared.startTagService();
        App.shared.setFirstload(false);

        // 初始化 IDzPrinter 对象（简单起见，不处理结果通知）
        IDzPrinter.Factory.getInstance().init(this, null);

        // 调用LPAPI对象的init方法初始化对象
        this.api = LPAPI.Factory.createInstance(mCallback);
    }

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void initPhotoError(){
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        initPhotoError();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new Dialog.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                httpClient.cancelRequests(MainActivity.this, true);
            }

        });
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        settingView = (RelativeLayout) findViewById(R.id.settingView);
        btnOrientation = (Button) findViewById(R.id.btnOrientation);
        setOrientationButtonText();
        btnOrientation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                changeOrientation();
            }
        });

        btnSetting = (Button) findViewById(R.id.btnSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Config.customSetting) {
                    Intent intent = new Intent(MainActivity.this,
                            CustomSettingActivity.class);
                    startActivityForResult(intent, Config.SETTING_REQUEST_CODE);
                } else {
                    Intent intent = new Intent(MainActivity.this,
                            SettingActivity.class);
                    startActivityForResult(intent, Config.SETTING_REQUEST_CODE);
                }
            }
        });
        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                // TODO Auto-generated method stub
                progressDialog.setMax(msg.arg2);
                progressDialog.setProgress(msg.arg1);
                return true;
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.skyland.file");
        registerReceiver(fileReceiver, filter);

        httpClient = new AsyncHttpClient();
        httpClient.setTimeout(120000);
        mWebView = (JSWebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebClient());
        mWebView.setWebChromeClient(new WebChrome());
        WebSettings webSetting = mWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setAllowFileAccess(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);
//缓存设置

        mWebView.addJavascriptInterface(this);

        if (Config.disableCache) {
            webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        bundle = getIntent().getExtras();
        String url = null;
        String title = null;
        if (!App.shared.isFirstload()) {
            if (bundle != null) {
                url = bundle.getString("ZHT_URL");
                title = bundle.getString("ZHT_Title");
                if (title != null) {
                    txtTitle.setText(title);
                }
                showSetting("1");
                isTab = true;
                btnBack.setVisibility(View.VISIBLE);
                btnOrientation.setVisibility(View.GONE);
                btnSetting.setVisibility(View.GONE);
            }
        }

        btnSetting.setVisibility(View.VISIBLE);
        btnOrientation.setVisibility(View.VISIBLE);
        settingView.setVisibility(View.VISIBLE);
        if (url != null && url.length() > 0) {
            mWebView.loadUrl(url);
        } else {
            Log.d("Msg", "SharedPreferences");
            SharedPreferences settings = PreferenceManager
                    .getDefaultSharedPreferences(this);
            String addressString = settings.getString("address_preference", "");
            if (Config.developing) {
                //addressString = "http://172.16.38.59:8070";
                // addressString = "http://112.93.116.169:8080";
                // addressString = "http://192.168.1.193:8070";
                // addressString = "http://192.168.2.105/";
                //addressString="http://120.236.164.111:5200";
                addressString = "http://hjzl.nanhai.gov.cn/";

            }
            if (addressString.equals("")) {
                Editor editor = settings.edit();
                if (!Config.customSetting) {
                    editor.putString("address_preference",
                            "http://hjzl.nanhai.gov.cn/");
                    addressString = "http://hjzl.nanhai.gov.cn/";
                } else {
//					editor.putString("address_preference",
//							"http://hjzl.nanhai.gov.cn/");
//					addressString = "http://hjzl.nanhai.gov.cn/";
                }
                editor.commit();
                setResult(RESULT_OK);
            }
            if (!addressString.equals("") && !addressString.endsWith("/")) {
                addressString = addressString + "/";
            }
            App.shared.setHost(addressString);
            String loadUrl = App.shared.getUrl(Config.MainPage);
            if (!loadUrl.equals("")) {
                mWebView.loadUrl(loadUrl + "?appid="
                        + App.shared.getDeviceId());
            }
        }
        App.shared.startTagService();
        setOrientation();
        App.shared.setFirstload(false);

        // 初始化 IDzPrinter 对象（简单起见，不处理结果通知）
        IDzPrinter.Factory.getInstance().init(this, null);

        // 调用LPAPI对象的init方法初始化对象
        this.api = LPAPI.Factory.createInstance(mCallback);

//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//        builder.detectFileUriExposure();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d("Msg", "onNewIntent");
        String extra = intent.getStringExtra(JPushInterface.EXTRA_EXTRA);
        if (extra != null) {
            mWebView.loadUrl("javascript:AppMsgClick('" + extra + "')");
        }
    }

    private void setOrientationButtonText() {
        if (App.shared.getOrientation() == 1) {
            btnOrientation.setText("屏幕旋转");
        } else {
            btnOrientation.setText("锁定屏幕");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class WebChrome extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 JsResult result) {
            // TODO Auto-generated method stub
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub
                                    dialog.dismiss();
                                }
                            }).create().show();
            result.confirm();
            return true;
        }
    }

    private class WebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            mProgressBar.setVisibility(View.GONE);
            if (bundle != null) {
                String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
                if (extra != null) {
                    mWebView.loadUrl("javascript:AppMsgClick('" + extra + "')");
                    bundle = null;
                }
            }
        }
    }

    @Override
    public void album(String parameter, JSFunction callback) {
        // TODO Auto-generated method stub
        data = parameter;
        jsCallBack = callback;
        path = null;
        // jsCallBack.executeWithParams("1");
        chooseMutiImage();
    }

    private void chooseMutiImage() {
        FunctionConfig config = new FunctionConfig.Builder()
                .setMutiSelectMaxSize(8).setEnablePreview(true).build();
        GalleryFinal.openGalleryMuti(
                Config.ALBUM_MUTI_IMAGE_ACTIVITY_REQUEST_CODE, config,
                new GalleryFinal.OnHanlderResultCallback() {
                    @Override
                    public void onHanlderSuccess(int reqeustCode,
                                                 List<PhotoInfo> resultList) {
                        ArrayList<String> paths = new ArrayList<String>();
                        for (PhotoInfo photoInfo : resultList) {
                            String photoPath = photoInfo.getPhotoPath();
                            paths.add(photoPath);
                        }
                        if (paths.size() == 0)
                            return;
                        uploadFile(Config.PicPage, paths);
                    }

                    @Override
                    public void onHanlderFailure(int requestCode,
                                                 String errorMsg) {

                    }
                });
    }

    private void chooseOneImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, Config.ALBUM_IMAGE_ACTIVITY_REQUEST_CODE);
    }



    @Override
    public void takephoto(String parameter, JSFunction callback) {
        if(ContextCompat.checkSelfPermission(
                this,Manifest.permission.CAMERA)!=  PackageManager.PERMISSION_GRANTED)
        {ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                1);
        }
        data = parameter;
        jsCallBack = callback;
        path = Utility.getPath(this, MediaType.Photo);
        if (path.exists()) {
            path.delete();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = Uri.fromFile(path);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent,
                Config.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void takevideo(String parameter, JSFunction callback) {
        // TODO Auto-generated method stub
        data = parameter;
        jsCallBack = callback;
        path = Utility.getPath(this, MediaType.Video);
        if (path.exists()) {
            path.delete();
        }
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Uri uri = Uri.fromFile(path);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent,
                Config.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void takerecord(String parameter, JSFunction callback) {
        // TODO Auto-generated method stub
        data = parameter;
        jsCallBack = callback;
        path = Utility.getPath(this, MediaType.Audio);
        if (path.exists()) {
            path.delete();
        }
        Intent intent = new Intent(this, RecordActivity.class);
        intent.putExtra("Path", path.getPath());
        startActivityForResult(intent,
                Config.CAPTURE_RECORD_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void takecode(JSFunction callback) {
        // TODO Auto-generated method stub
        jsCallBack = callback;
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent,
                Config.CAPTURE_CODE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void login(String parameter) {
        if (parameter == null) {
            return;
        }
        if (parameter.trim().equals("")) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(parameter);
            String appid = (String) jsonObject.get("appid");
            String userid = (String) jsonObject.get("userid");
            App.shared.getUserInfo().setUserType(UserTypeEnum.Login);
            App.shared.getUserInfo().setAppId(appid);
            App.shared.getUserInfo().setUserId(userid);
            App.shared.getUserInfo().save(MainActivity.this);
            App.shared.startTagService();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void signOut() {
        App.shared.getUserInfo().signOut();
        App.shared.getUserInfo().save(MainActivity.this);
        App.shared.clearTag();
        App.shared.stopTagService();
        // jsCallBack.executeWithParams("1");
        PersistentCookieStore cookieStore = App.shared.getCookieStore();
        List<Cookie> cookies = cookieStore.getCookies();
        Cookie removeCookie = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("userid")) {
                removeCookie = cookie;
                break;
            }
        }
        if (removeCookie != null) {
            cookieStore.deleteCookie(removeCookie);
        }
        EZOpenSDK.getInstance().logout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == Config.SETTING_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                SharedPreferences settings = PreferenceManager
                        .getDefaultSharedPreferences(this);
                String addressString = settings.getString("address_preference",
                        "");
                if (!addressString.equals("")) {
                    if (!addressString.endsWith("/")) {
                        addressString = addressString + "/";
                    }
                    App.shared.setHost(addressString);
                    String url = App.shared.getUrl(Config.MainPage) + "?appid="
                            + App.shared.getDeviceId();
                    mWebView.loadUrl(url);
                }
            }
        } else if (requestCode == Config.ALBUM_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (null != data) {
                    Uri originalUri = data.getData();
                    if (originalUri.getScheme().equals("content")) {
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(originalUri,
                                filePathColumn, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int columnIndex = cursor
                                    .getColumnIndex(filePathColumn[0]);
                            String pathString = cursor.getString(columnIndex);
                            cursor.close();
                            path = new File(pathString);
                        }
                    } else {
                        String pathString = originalUri.getPath();
                        path = new File(pathString);
                    }
                    if (path != null) {
                        uploadFile(Config.PicPage);
                    }
                }
            } else {
                // jsCallBack.executeWithParams("", "0");
            }
        } else if (requestCode == Config.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                uploadFile(Config.PicPage);

            } else {
                if (path.exists()) {
                    path.delete();
                }
                // jsCallBack.executeWithParams("", "0");
            }
        } else if (requestCode == Config.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                uploadFile(Config.VideoPage);

            } else {
                if (path.exists()) {
                    path.delete();
                }
                // jsCallBack.executeWithParams("", "0");
            }
        } else if (requestCode == Config.CAPTURE_RECORD_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                uploadFile(Config.RecordPage);

            } else {
                if (path.exists()) {
                    path.delete();
                }
                // jsCallBack.executeWithParams("", "0");
            }
        } else if (requestCode == Config.CAPTURE_CODE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                jsCallBack
                        .executeWithParams(data.getExtras().getString("Code"));
            } else {
                // jsCallBack.executeWithParams("", "0");
            }
        }
    }

    private long exitTime = 0;
    private long waitTime = 3000;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isTab) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - exitTime >= waitTime) {
                    mWebView.loadUrl("javascript:goBack()");
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = currentTime;
                } else {
                    App.shared.setFirstload(true);
                    finish();
                    // android.os.Process.killProcess(android.os.Process.myPid());
                }
                return true;
            }
        } else {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void uploadFile(String type) {
        ArrayList<String> paths = new ArrayList<String>();
        paths.add(path.getPath());
        uploadFile(type, paths);
    }

    private void uploadFile(String type, ArrayList<String> paths) {

        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setTitle("正在上传文件,请稍后...");
        // progressDialog.setMessage("正在上传文件:"+path.getName());
        String url = App.shared.getUrl(type);
        RequestParams params = new RequestParams();
        try {
            params.put("data", data);
            for (int i = 0; i < paths.size(); i++) {
                params.put("file" + (i + 1), new File(paths.get(i)));
            }
        } catch (FileNotFoundException e) {
        }
        App app = (App) getApplicationContext();
        PersistentCookieStore cookieStore = app.getCookieStore();
        List<Cookie> cookies = cookieStore.getCookies();
        Cookie removeCookie = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("userid")) {
                removeCookie = cookie;
                break;
            }
        }
        if (removeCookie != null) {
            cookieStore.deleteCookie(removeCookie);
        }
        String userid = "";
        try {
            JSONObject jsonObject = new JSONObject(data);
            userid = jsonObject.getString("sessionid");
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        BasicClientCookie newCookie = new BasicClientCookie("userid", userid);
        newCookie.setVersion(1);
        newCookie.setDomain(Uri.parse(url).getHost());
        newCookie.setPath("/");
        cookieStore.addCookie(newCookie);
        httpClient.setCookieStore(cookieStore);
        httpClient.post(this, url, params, new TextHttpResponseHandler() {

            @Override
            public void onCancel() {
                // TODO Auto-generated method stub
                super.onCancel();
                progressDialog.hide();
            }

            @Override
            public void onFinish() {
                // TODO Auto-generated method stub
                super.onFinish();
                progressDialog.hide();
            }

            @Override
            public void onStart() {
                // TODO Auto-generated method stub
                progressDialog.show();
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                // TODO Auto-generated method stub
                super.onProgress(bytesWritten, totalSize);
                Message msg = new Message();
                msg.arg1 = (int) bytesWritten;
                if (totalSize == 0) {
                    msg.arg2 = 100;
                } else {
                    msg.arg2 = (int) totalSize;
                }
                handler.sendMessage(msg);
            }

            @Override
            public void onSuccess(int arg0, Header[] arg1, String arg2) {
                // TODO Auto-generated method stub

                Message msg = new Message();
                msg.what = 1;
                Bundle b = new Bundle();
                b.putString("EXTRA_EXTRA", arg2);
                msg.setData(b);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, String arg2,
                                  Throwable arg3) {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this, arg2, Toast.LENGTH_LONG)
                        .show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(fileReceiver);
        if (!isTab) {
            App.shared.setFirstload(true);
        }
        // 断开蓝牙连接，释放 IDzPrinter 对象
        IDzPrinter.Factory.getInstance().quit();
    }

    public class FileReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            // TODO Auto-generated method stub

            if (!isResume)
                return;
            try {
                String filePath = intent.getStringExtra("filePath");
                File f = new File(filePath);
                String fileName = f.getName();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("文件下载成功")
                        .setMessage("是否打开文件？\n\"" + fileName + "\"")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated // method stub
                                        dialog.dismiss();
                                        String filePath = intent
                                                .getStringExtra("filePath");
                                        try {
                                            Intent installApkIntent = UpdateService
                                                    .getFileIntent(new File(
                                                            filePath));
                                            startActivity(installApkIntent);
                                        } catch (Exception ex) {
                                            Toast.makeText(MainActivity.this,
                                                    "没有适合软件打开该文件！",
                                                    Toast.LENGTH_LONG).show();
                                        }

                                    }
                                })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated // method stub
                                        dialog.dismiss();
                                    }
                                }).create().show();
            } catch (Exception ex) {

            }

        }
    }

    @Override
    @JavascriptInterface
    public void showSetting(String parameter) {
        // TODO Auto-generated method stub
        if (parameter.equals("0")) {
            settingView.setVisibility(View.GONE);
        } else if (parameter.equals("1")) {
            settingView.setVisibility(View.VISIBLE);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            String arg2 = msg.getData().getString("EXTRA_EXTRA");
            switch (msg.what) {
                case 1: {
                    jsCallBack.executeWithParams(arg2);
                }
                break;
                default:
                    break;
            }

        }
    };

    @Override
    @JavascriptInterface
    public void filedown(String parameter) {

        if (parameter == null) {
            return;
        }
        Toast.makeText(MainActivity.this, "正在下载文件...", Toast.LENGTH_SHORT)
                .show();

        String temp1 = parameter.replaceFirst("\"", "");
        if (temp1.endsWith("\"")) {
            temp1 = temp1.substring(0, temp1.length() - 1);
        }
        String url = App.shared.getUrl(temp1);
        Intent updateIntent = new Intent(MainActivity.this, UpdateService.class);
        updateIntent.putExtra("app_name", R.string.app_name);
        updateIntent.putExtra("downurl", url);
        startService(updateIntent);
    }

    @Override
    @JavascriptInterface
    public void fixOrientation(String parameter) {
        // TODO Auto-generated method stub
        if (parameter.equals("0")) {
            App.shared.setOrientation(1);
        } else if (parameter.equals("1")) {
            App.shared.setOrientation(0);
        }
        changeOrientation();
    }

    private void changeOrientation() {
        if (App.shared.getOrientation() == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            App.shared.setOrientation(0);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            App.shared.setOrientation(1);
        }
        setOrientationButtonText();
    }

    private void setOrientation() {
        if (App.shared.getOrientation() == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            App.shared.setOrientation(1);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            App.shared.setOrientation(0);
        }
        setOrientationButtonText();
    }

    @Override
    @JavascriptInterface
    public void openTab(String parameter) {
        // TODO Auto-generated method stub
        if (parameter == null) {
            return;
        }
        try {
            JSONObject obj = new JSONObject(parameter);
            String url = (String) obj.get("url");
            String title = (String) obj.get("title");
            Intent i = new Intent(MainActivity.this, MainActivity2.class);
            i.putExtra("ZHT_URL", url);
            i.putExtra("ZHT_Title", title);
            startActivity(i);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        isResume = true;
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        isResume = false;
    }

    @Override
    @JavascriptInterface
    public void navigateByLocation(String parameter) {
        // TODO Auto-generated method stub
        if (parameter == null) {
            return;
        }
        try {
            JSONObject obj = new JSONObject(parameter);
            final double lat = (double) obj.get("lat");
            final double lng = (double) obj.get("lng");

            ArrayList<String> maps = new ArrayList<String>();
            if (MapUtil.isAvilible(MainActivity.this, "com.baidu.BaiduMap")) {
                maps.add("百度地图");
            }
            if (MapUtil.isAvilible(MainActivity.this, "com.autonavi.minimap")) {
                maps.add("高德地图");
            }
            final String[] array = new String[maps.size()];
            maps.toArray(array);
            dialog = ActionSheet.show(MainActivity.this, "请选择地图", array,
                    new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int arg2, long arg3) {
                            String mapName = array[arg2];
                            if (mapName.equals("百度地图")) {
                                double[] latlng = MapUtil
                                        .gaoDeToBaidu(lat, lng);
                                MapUtil.openBaidu(MainActivity.this, latlng[0],
                                        latlng[1]);
                            } else if (mapName.equals("高德地图")) {
                                MapUtil.openGaoDe(MainActivity.this, lat, lng);
                            }
                            dialog.dismiss();
                            dialog = null;
                        }

                    }, new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    });

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    @JavascriptInterface
    public void navigateByAddress(String parameter) {
        // TODO Auto-generated method stub
        if (parameter == null) {
            return;
        }
        try {
            JSONObject obj = new JSONObject(parameter);
            final String address = (String) obj.get("address");
            ArrayList<String> maps = new ArrayList<String>();
            if (MapUtil.isAvilible(MainActivity.this, "com.baidu.BaiduMap")) {
                maps.add("百度地图");
            }
            if (MapUtil.isAvilible(MainActivity.this, "com.autonavi.minimap")) {
                maps.add("高德地图");
            }
            final String[] array = new String[maps.size()];
            maps.toArray(array);
            dialog = ActionSheet.show(MainActivity.this, "请选择地图", array,
                    new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int arg2, long arg3) {
                            String mapName = array[arg2];
                            if (mapName.equals("百度地图")) {
                                MapUtil.openBaidu(MainActivity.this, address);
                            } else if (mapName.equals("高德地图")) {
                                MapUtil.openGaoDe(MainActivity.this, address);
                            }
                            dialog.dismiss();
                            dialog = null;
                        }

                    }, new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    });

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void OnLocationResult(double latitude, double longitude) {
        // TODO Auto-generated method stub
        App.shared.stopLocation();
        App.shared.setiLocation(null);
        String args = "{\"latitude\":" + latitude + ",\"longitude\":"
                + longitude + "}";
        jsCallBack.executeWithParams(args);
    }

    @Override
    @JavascriptInterface
    public void getLocation(JSFunction callback) {
        // TODO Auto-generated method stub
        App.shared.setiLocation(this);
        jsCallBack = callback;
        App.shared.startLocation();
    }

    @Override
    @JavascriptInterface
    public void saveData(String parameter, JSFunction callback) {
        try {
            JSONObject obj = new JSONObject(parameter);
            String key = (String) obj.get("key");
            String value = (String) obj.get("value");
            SharedPreferences sp = getSharedPreferences("OfflineData", MODE_PRIVATE);
            Editor editor = sp.edit();
            editor.putString(key, value);
            editor.commit();
            callback.executeWithParams("1");
        } catch (Exception ex) {
            callback.executeWithParams("0");
        }
    }

    @Override
    @JavascriptInterface
    public void getData(String parameter, JSFunction callback) {
        try {
            JSONObject obj = new JSONObject(parameter);
            String key = (String) obj.get("key");
            SharedPreferences sp = getSharedPreferences("OfflineData", MODE_PRIVATE);
            String data = sp.getString(key, "");
            callback.executeWithParams(data);
        } catch (Exception ex) {
            callback.executeWithParams("");
        }
    }

    @Override
    public void openVideo(String parameter) {
        try {
            JSONObject obj = new JSONObject(parameter);
            String url = (String) obj.get("url");
            String accessToken = (String) obj.get("accessToken");
            String title = (String) obj.get("title");
            Intent intent = new Intent(MainActivity.this, VideoActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("accessToken", accessToken);
            intent.putExtra("title", title);
            startActivity(intent);
        } catch (Exception ex) {

        }
    }

    @Override
    public void printImage(String parameter, JSFunction callback) {
        try {
            if (!isPrinterConnected()) {
                return;
            }
            JSONObject obj = new JSONObject(parameter);
            String url = (String) obj.get("url");
            PrintInfo printInfo = new PrintInfo();
            int pageWidth = obj.getInt("pageWidth");
            int pageHeight = obj.getInt("pageHeight");
            int x = obj.getInt("x");
            int y = obj.getInt("y");
            int imageWidth = obj.getInt("imageWidth");
            int imageHeight = obj.getInt("imageHeight");
            printInfo.pageWidth = pageWidth;
            printInfo.pageHeight = pageHeight;
            printInfo.x = x;
            printInfo.y = y;
            printInfo.imageWidth = imageWidth;
            printInfo.imageHeight = imageHeight;
            this.jsCallBack = callback;
            new MyDownloadTask(MainActivity.this, printInfo).execute(url);
        } catch (Exception ex) {
            callback.executeWithParams("0");
        }
    }


    public class MyDownloadTask extends AsyncTask<String, Void, Bitmap> {

        private ProgressDialog dialog;
        private PrintInfo printInfo;

        public MyDownloadTask(Context context, PrintInfo printInfo) {
            dialog = new ProgressDialog(context);
            dialog.setTitle("提示信息");
            dialog.setMessage("正在下载，请稍候...");
            this.printInfo = printInfo;
        }

        /**
         * 表示任务执行之前的操作
         */
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            dialog.show();
        }

        /**
         * 主要是完成耗时的操作
         */
        @Override
        protected Bitmap doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            // 使用网络连接类HttpClient类王城对网络数据的提取
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(arg0[0]);
            Bitmap bitmap = null;
            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    byte[] data = EntityUtils.toByteArray(httpEntity);
                    bitmap = BitmapFactory
                            .decodeByteArray(data, 0, data.length);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            return bitmap;
        }

        /**
         * 主要是更新UI的操作
         */
        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            dialog.dismiss();
            if (result != null) {
                //打印图片
                printBitmap(result, printInfo);
            } else {
                jsCallBack.executeWithParams("0");
            }
        }
    }

    private void printBitmap(Bitmap bitmap, PrintInfo printInfo) {
        // 得到 IDzPrinter 对象
        IDzPrinter printer = IDzPrinter.Factory.getInstance();
// 连接配对的第一个打印机对象
        printer.connect(IDzPrinter.Factory.getFirstPrinter());
        //printer.print(bitmap, getPrintParam(0,0));
        AtBitmap api = new AtBitmap(printer);
        // 开始绘图任务，传入参数(页面宽度, 页面高度)
        api.startJob(printInfo.pageWidth, printInfo.pageHeight);
// 开始绘制图片
        api.drawBitmap(bitmap, printInfo.x, printInfo.y, printInfo.imageWidth, printInfo.imageHeight);
// 结束绘图任务
        api.endJob();
// 打印
        printer.print(api, null);
        jsCallBack.executeWithParams("1");
    }

    // 判断当前打印机是否连接
    private boolean isPrinterConnected() {
        // 调用IDzPrinter对象的getPrinterState方法获取当前打印机的连接状态
        IDzPrinter.PrinterState state = IDzPrinter.Factory.getInstance().getPrinterState();

        // 打印机未连接
        if (state == null || state.equals(IDzPrinter.PrinterState.Disconnected)) {
            Toast.makeText(MainActivity.this, "打印机未连接", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }

        // 打印机正在连接
        if (state.equals(IDzPrinter.PrinterState.Connecting)) {
            Toast.makeText(MainActivity.this, "打印机正在连接", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        // 打印机已连接
        return true;
    }


    //陆菲20180828 精臣打印机部分
    //文字打印
    public void printTextByJC(String parameter, JSFunction callback) {
        try {
            if (!isJCPrinterConnected()) {
                return;
            }
            //默认值单位mm
            double width = 50;//页面宽度
            double height = 30;//页面高度
            double fontHeight = 2.5;//字体高度
            double lineSpacing = 0.5;//行间距
            double oneHeight = 6;//一维码行高度
            double oneWidth = 40;//一维码行宽度
            double oneTextHeight = 2;//一维码数字高度
            double oneLineSpacing = 0.5;//一维码行间距
            String oneText = "12345678";//一维码默认值
            int fontStyle = 1;
            double x = 0;//偏移x轴
            double y = 0;//偏移y轴
            api.startJob(50, 30, 0);
            JSONObject obj = new JSONObject(parameter);
            double a_rowHeight = 0;
            double a_rowWidth = 0;
            double a_fontHeight = 0;
            //文字
            if (obj.has("printdata")) {
                JSONArray printdata = (JSONArray) obj.get("printdata");
                for (int i = 0; i < printdata.length(); i++) {
                    JSONObject row = new JSONObject(printdata.get(i).toString());
                    a_rowHeight = row.has("height") ? Double.valueOf(row.get("height").toString()) : height;
                    a_rowWidth = row.has("width") ? Double.valueOf(row.get("width").toString()) : width;
                    a_fontHeight = row.has("fontHeight") ? Double.valueOf(row.get("fontHeight").toString()) : fontHeight;
                    int a_fontStyle = row.has("fontStyle") ? (Integer) row.get("fontStyle") : fontStyle;
                    double a_x = row.has("x") ? Double.valueOf(row.get("x").toString()) : x;
                    y = i * (a_fontHeight + lineSpacing);
                    double a_y = row.has("y") ? Double.valueOf(row.get("y").toString()) : y;
                    // 开始绘图任务，传入参数(页面宽度, 页面高度，顺时针转交度)
                    api.drawText(row.get("text").toString(), a_x, a_y, a_rowWidth, a_rowHeight, a_fontHeight, a_fontStyle);
                }
            }

            //一维码
            if (obj.has("onedBarcde")) {
                JSONObject onedBarcde = new JSONObject(obj.get("onedBarcde").toString());
                double o_width = onedBarcde.has("width") ? Double.valueOf(onedBarcde.get("width").toString()) : oneWidth;
                double o_height = onedBarcde.has("height") ? Double.valueOf(onedBarcde.get("height").toString()) : oneHeight;
                double o_textHeight = onedBarcde.has("textHeight") ? Double.valueOf(onedBarcde.get("textHeight").toString()) : oneTextHeight;
                double o_lineSpacing = onedBarcde.has("lineSpacing") ? Double.valueOf(onedBarcde.get("lineSpacing").toString()) : oneLineSpacing;
                String o_text = onedBarcde.has("text") ? onedBarcde.get("text").toString() : oneText;
                y = y + o_lineSpacing + a_fontHeight;
                // 传入参数(需要绘制的一维码的数据,类型， 绘制的一维码左上角水平位置, 绘制的一维码左上角垂直位置, 绘制的一维码水平宽度, 绘制的一维码垂直高度)
                api.draw1DBarcode(o_text, 60, x, y, o_width, o_height, 0);
            }

            // 结束绘图任务提交打印
            api.commitJob();
        } catch (Exception ex) {
            callback.executeWithParams("0");

        }
    }


    //图片打印
    public void printImageByJC(String parameter, JSFunction callback) {
        try {
            if (!isJCPrinterConnected()) {
                return;
            }
            JSONObject obj = new JSONObject(parameter);
            String url = (String) obj.get("url");
            final Integer pageWidth = (Integer) (obj.has("pageWidth") ? obj.get("pageWidth") : -1);
            final Integer pageHeight = (Integer) (obj.has("pageHeight") ? obj.get("pageHeight") : -1);
            final Integer x = (Integer) (obj.has("x") ? obj.get("x") : 0);
            final Integer y = (Integer) (obj.has("y") ? obj.get("y") : 0);
            //加载网络图片
            GetBitmaByUrl imge = new GetBitmaByUrl();
            imge.setOnDataFinishedListener(new OnDataFinishedListener() {
                @Override
                public void onDataSuccessfully(Object data) {
                    Bitmap bitmap = (Bitmap) data;

                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int newWidth = pageWidth == -1 ? width : pageWidth;
                    int newHeight = pageHeight == -1 ? height : pageHeight;
                    // 计算缩放比例
                    float scaleWidth = ((float) newWidth) / width;
                    float scaleHeight = ((float) newHeight) / height;
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    Bitmap mbitmap = Bitmap.createBitmap(bitmap, x, y, width, height, matrix, true);
                    bitmap = mbitmap;
                    //打印参数
                    Bundle param = getPrintParam(1, 0);
                    //开始打印
                    boolean isPrint = api.printBitmap(bitmap, null);
                    if (isPrint)
                        Toast.makeText(MainActivity.this, "打印成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDataFailed() {
                    Toast.makeText(MainActivity.this, "图片获取失败，请确定网络是否畅通。", Toast.LENGTH_SHORT).show();
                }
            });
            imge.execute(url);
        } catch (Exception ex) {
            callback.executeWithParams("0");
        }
    }

    // 连接精臣打印机请求成功提交时操作
    private boolean isJCPrinterConnected() {
        //测试获取打印机列表20180828
        pairedPrinters = api.getAllPrinterAddresses();
        if (pairedPrinters.size() > 0) {
            // 连接打印机的请求提交成功，刷新界面提示
            IDzPrinter.PrinterAddress printerAddress = pairedPrinters.get(0);
            //设备连接打印机
            boolean isopen = api.openPrinterByAddress(printerAddress);
            //判断是否已连接成功
            IDzPrinter.PrinterState state = api.getPrinterState();
            if (state == null || state.equals(IDzPrinter.PrinterState.Disconnected)) {
                Toast.makeText(MainActivity.this, "打印机" + printerAddress.shownName + "未连接，请确保蓝牙已连上打印机,并到蓝牙设置中删除不需要连接的打印机。", Toast.LENGTH_SHORT).show();
                return false;
            } else if (state.equals(IDzPrinter.PrinterState.Connecting)) {
                Toast.makeText(MainActivity.this, "正在尝试连接" + printerAddress.shownName + "打印机，请确保蓝牙已连上打印机,并到蓝牙设置中删除不需要连接的打印机。。", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Toast.makeText(MainActivity.this, "打印机" + printerAddress.shownName + "已连接", Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            Toast.makeText(MainActivity.this, "未检测到设备打印机", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void sqliteExecuteSql(String parameter, JSFunction callback) {

    }

    @Override
    public void sqliteSaveToLocal(String parameter, JSFunction callback) {
        try {
            JSONObject obj = new JSONObject(parameter);
            if (obj.has("key") && obj.has("value")) {
                dao.SaveToLacal(obj.get("key").toString(), obj.get("value").toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "保存失败" + e.getMessage(), Toast.LENGTH_SHORT);
        }
    }


    //    回调接口
    public interface OnDataFinishedListener {

        public void onDataSuccessfully(Object data);

        public void onDataFailed();

    }

    //异步调用
    class GetBitmaByUrl extends AsyncTask<String, Void, Bitmap> {
        private Bitmap res;
        OnDataFinishedListener onDataFinishedListener;

        public void setOnDataFinishedListener(
                OnDataFinishedListener onDataFinishedListener) {
            this.onDataFinishedListener = onDataFinishedListener;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap bitmap = null;
            try {
                //加载一个网络图片
                InputStream is = new URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                onDataFinishedListener.onDataSuccessfully(result);
            } else {
                onDataFinishedListener.onDataFailed();
            }
        }
    }


    // 打印参数
    private int printQuality = -1;
    private int printDensity = -1;
    private int printSpeed = -1;
    private int gapType = -1;

    // 获取打印时需要的打印参数
    private Bundle getPrintParam(int copies, int orientation) {
        Bundle param = new Bundle();

        // 打印浓度
        if (printDensity >= 0) {
            param.putInt(PrintParamName.PRINT_DENSITY, printDensity);
        }

        // 打印速度
        if (printSpeed >= 0) {
            param.putInt(PrintParamName.PRINT_SPEED, printSpeed);
        }

        // 间隔类型
        if (gapType >= 0) {
            param.putInt(PrintParamName.GAP_TYPE, gapType);
        }

        // 打印页面旋转角度
        if (orientation != 0) {
            param.putInt(PrintParamName.PRINT_DIRECTION, orientation);
        }

        // 打印份数
        if (copies > 1) {
            param.putInt(PrintParamName.PRINT_COPIES, copies);
        }

        return param;
    }
}
