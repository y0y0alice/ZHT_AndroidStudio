package com.skyland.zht;

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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.calvin.ActionSheet;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.jpush.android.api.JPushInterface;

public class MainActivity2 extends Activity implements JSBridge,ILocation {

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
	Button btnOrientation;
	RelativeLayout settingView;
	boolean isTab = false;
	ImageButton btnBack;
	TextView txtTitle;
	boolean isResume = false;
	Dialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(new Dialog.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				httpClient.cancelRequests(MainActivity2.this, true);
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
					Intent intent = new Intent(MainActivity2.this,
							CustomSettingActivity.class);
					startActivityForResult(intent, Config.SETTING_REQUEST_CODE);
				} else {
					Intent intent = new Intent(MainActivity2.this,
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
		if (url != null && url.length() > 0) {
			mWebView.loadUrl(url);
		} else {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			String addressString = settings.getString("address_preference", "");
			if (Config.developing) {
				// addressString = "http://172.16.38.27:8070";
				// addressString = "http://112.93.116.169:8080";
				//addressString = "http://192.168.1.102:8070";
				addressString = "http://192.168.1.105/cguo/zht";
			}
			if (addressString.equals("")) {
				Editor editor = settings.edit();
				editor.putString("address_preference",
						"http://112.93.116.169:8080");
				editor.commit();
				setResult(RESULT_OK);
				addressString = "http://112.93.116.169:8080";
			}
			if (!addressString.endsWith("/")) {
				addressString = addressString + "/";
			}
			App.shared.setHost(addressString);
			mWebView.loadUrl(App.shared.getUrl(Config.MainPage) + "?appid="
					+ App.shared.getDeviceId());
		}
		App.shared.startTagService();
		setOrientation();
		App.shared.setFirstload(false);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
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
			new AlertDialog.Builder(MainActivity2.this)
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

	private void chooseMutiImage()
	{
		FunctionConfig config=	new  FunctionConfig.Builder().setMutiSelectMaxSize(8)
		.setEnablePreview(true).build();
		GalleryFinal.openGalleryMuti(Config.ALBUM_MUTI_IMAGE_ACTIVITY_REQUEST_CODE, config, new GalleryFinal.OnHanlderResultCallback() {
			@Override
			public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
				ArrayList<String>paths=new ArrayList<String>();
				for (PhotoInfo photoInfo : resultList) {
					String photoPath = photoInfo.getPhotoPath();
					paths.add(photoPath);
				}
				uploadFile(Config.PicPage,paths);
			}

			@Override
			public void onHanlderFailure(int requestCode, String errorMsg) {

			}
		});
	}

	private void chooseOneImage()
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, Config.ALBUM_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	@Override
	public void takephoto(String parameter, JSFunction callback) {
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
			App.shared.getUserInfo().save(MainActivity2.this);
			App.shared.startTagService();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void signOut() {
		App.shared.getUserInfo().signOut();
		App.shared.getUserInfo().save(MainActivity2.this);
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
				Uri originalUri = data.getData();
				if (originalUri.getPath().startsWith("file://")) {
					path = new File(originalUri.getPath());
				} else {
					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor cursor = managedQuery(originalUri, proj, null, null,
							null);
					int column_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					path = new File(cursor.getString(column_index));
				}
				uploadFile(Config.PicPage);

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
	
	
	private void uploadFile(String type)
	{
		ArrayList<String>paths=new ArrayList<String>();
		paths.add(path.getPath());
		uploadFile(type,paths);
	}

	private void uploadFile(String type,ArrayList<String>paths) {

		progressDialog.setMax(100);
		progressDialog.setProgress(0);
		progressDialog.setTitle("正在上传文件,请稍后...");
		// progressDialog.setMessage("正在上传文件:"+path.getName());
		String url = App.shared.getUrl(type);
		RequestParams params = new RequestParams();
		try {
			params.put("data", data);
			params.put("file", path);
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
				msg.arg1 = (int)bytesWritten;
				if (totalSize == 0) {
					msg.arg2 = 100;
				} else {
					msg.arg2 = (int)totalSize;
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
				Toast.makeText(MainActivity2.this, arg2, Toast.LENGTH_LONG)
						.show();
			}
		});

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(!isTab)
		{
			App.shared.setFirstload(true);
		}
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
				new AlertDialog.Builder(MainActivity2.this)
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
											Toast.makeText(MainActivity2.this,
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
		Toast.makeText(MainActivity2.this, "正在下载文件...", Toast.LENGTH_SHORT)
				.show();

		String temp1 = parameter.replaceFirst("\"", "");
		if (temp1.endsWith("\"")) {
			temp1 = temp1.substring(0, temp1.length() - 1);
		}
		String url = App.shared.getUrl(temp1);
		Intent updateIntent = new Intent(MainActivity2.this, UpdateService.class);
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
			Intent i = new Intent(MainActivity2.this, MainActivity2.class);
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
			
			ArrayList<String>maps=new ArrayList<String>();
			if(MapUtil.isAvilible(MainActivity2.this, "com.baidu.BaiduMap"))
			{
				maps.add("百度地图");
			}
			if(MapUtil.isAvilible(MainActivity2.this, "com.autonavi.minimap"))
			{
				maps.add("高德地图");
			}
			final String[] array =new String[maps.size()];
			maps.toArray(array);
			dialog = ActionSheet.show(MainActivity2.this, "请选择地图", array, new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					String mapName=array[arg2];
					if(mapName.equals("百度地图"))
					{
						double[]latlng= MapUtil.gaoDeToBaidu(lat,lng);
						MapUtil.openBaidu(MainActivity2.this, latlng[0], latlng[1]);
					}
					else if(mapName.equals("高德地图"))
					{
						MapUtil.openGaoDe(MainActivity2.this, lat,lng);
					}
					dialog.dismiss();
					dialog=null;
				}

			}, new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
					dialog=null;
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
			ArrayList<String>maps=new ArrayList<String>();
			if(MapUtil.isAvilible(MainActivity2.this, "com.baidu.BaiduMap"))
			{
				maps.add("百度地图");
			}
			if(MapUtil.isAvilible(MainActivity2.this, "com.autonavi.minimap"))
			{
				maps.add("高德地图");
			}
			final String[] array =new String[maps.size()];
			maps.toArray(array);
			dialog = ActionSheet.show(MainActivity2.this, "请选择地图", array, new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					String mapName=array[arg2];
					if(mapName.equals("百度地图"))
					{
						MapUtil.openBaidu(MainActivity2.this, address);
					}
					else if(mapName.equals("高德地图"))
					{
						MapUtil.openGaoDe(MainActivity2.this, address);
					}
					dialog.dismiss();
					dialog=null;
				}

			}, new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
					dialog=null;
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
		
	}

	@Override
	@JavascriptInterface
	public void getLocation(JSFunction callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@JavascriptInterface
	public void saveData(String parameter,JSFunction callback) {

	}

	@Override
	@JavascriptInterface
	public void getData(String parameter,JSFunction callback) {

	}

	@Override
	public void openVideo(String parameter) {

	}

	@Override
	public void printImage(String parameter,JSFunction callback) {

	}

	@Override
	public void printImageByJC(String parameter,JSFunction callback) {

	}

	@Override
	public void printTextByJC(String parameter,JSFunction callback) {

	}
}
