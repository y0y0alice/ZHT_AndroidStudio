package com.skyland.zht;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.TextHttpResponseHandler;

public class TagService extends Service {

	private final static int seconds = 10; // 半分钟上传一次

	HashSet<String> tags = new HashSet<String>();
	AsyncHttpClient httpClient;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		httpClient = new AsyncHttpClient();
		httpClient.setTimeout(120000);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		App app = (App) getApplicationContext();
		UserInfo userInfo = app.getUserInfo();
		if (userInfo == null) {
			stopSelf();
			return startId;
		}
		UserTypeEnum userType = userInfo.getUserType();
		if (userType == UserTypeEnum.NotLogin) {
			stopSelf();
			App.shared.tagServiceRuning=false;
			return startId;
		}
		String appId = userInfo.getAppId();
		if (App.shared.getTag().equals(appId)) {
			stopSelf();
			App.shared.tagServiceRuning=false;
			return startId;
		}
		tags.clear();
		tags.add(appId);
		setAlias(true);
		return startId;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private static final int MSG_SET_ALIAS = 1001;
	private static final int MSG_SAVE_ALIAS = 1002;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);

			String registrationID = JPushInterface
					.getRegistrationID(TagService.this);
			App app = (App) getApplicationContext();
			switch (msg.what) {
			case MSG_SET_ALIAS: {
				if (app.isConnected() == false) {
					setAlias(false);
					return;
				}
				if (!TextUtils.isEmpty(registrationID)) {
					JPushInterface.setTags(TagService.this, tags,
							mAliasCallback);
				} else {
					setAlias(false);
				}
			}
				break;
			case MSG_SAVE_ALIAS: {
				String query = Config.PushPage + "&appid="
						+ App.shared.getUserInfo().getAppId() 
						+ "&aliasTag="
						+ App.shared.getUserInfo().getAppId() + "&os_type=1";
				String url = App.shared.getUrl(query);
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
				String userid = App.shared.getUserInfo().getUserId();
				BasicClientCookie newCookie = new BasicClientCookie("userid",
						userid);
				newCookie.setVersion(1);
				newCookie.setDomain(Uri.parse(url).getHost());
				newCookie.setPath("/");
				cookieStore.addCookie(newCookie);
				httpClient.setCookieStore(cookieStore);
				httpClient.get(TagService.this, url,
						new TextHttpResponseHandler() {

							@Override
							public void onSuccess(int arg0, Header[] arg1,
									String arg2) {
								// TODO Auto-generated method stub

								try {
									JSONObject jsonObject = new JSONObject(arg2);
									boolean success = (boolean) jsonObject
											.get("success");
									// {"success":false,"msg":"系统未开启设备通知功能!"}
 									if (success) {
										App.shared.setTag(App.shared
												.getUserInfo().getAppId());
										App.shared.tagServiceRuning=false;
										stopSelf();
									} else {
										saveAlias(false);
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									saveAlias(false);
								}
							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									String arg2, Throwable arg3) {
								// TODO Auto-generated method stub
								saveAlias(false);
							}
						});

			}
				break;
			default:

			}
		}
	};

	private void setAlias(boolean immediate) {
		Message msg = new Message();
		msg.what = MSG_SET_ALIAS;
		if (immediate) {
			mHandler.sendMessage(msg);
		} else {
			mHandler.sendMessageDelayed(msg, 1000 * seconds);
		}
	}

	private void saveAlias(boolean immediate) {
		Message msg = new Message();
		msg.what = MSG_SAVE_ALIAS;
		if (immediate) {
			mHandler.sendMessage(msg);
		} else {
			mHandler.sendMessageDelayed(msg, 1000 * seconds);
		}
	}

	private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
		@Override
		public void gotResult(int code, String alias, Set<String> tags) {
			switch (code) {
			case 0: {
				saveAlias(true);
			}
				break;
			case 6002: {
				setAlias(false);
			}
				break;
			default:

			}
		}
	};
}
