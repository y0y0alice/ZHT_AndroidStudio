package com.skyland.zht;

import java.io.File;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.ResponseHandlerInterface;

public class UpdateService extends Service {
	private static final int DOWN_OK = 1; // 下载完成
	private static final int DOWN_ERROR = 0;

	private String down_url;
	private String app_name;
	private String user_id;

	private NotificationManager notificationManager;
	private Notification notification;

	private Intent updateIntent;
	private PendingIntent pendingIntent;
	private String updateFile;

	private int notification_id = 0;
	long totalSize = 0;// 文件总大小

	AsyncHttpClient httpClient = new AsyncHttpClient();
	/***
	 * 更新UI
	 */
	final Handler handler = new Handler() {
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_OK:
				// 下载完成，点击安装

				// Toast.makeText(UpdateService.this, "文件下载到:"+new
				// File(updateFile).getParent(), Toast.LENGTH_LONG)
				// .show();
				// try {
				// startActivity(installApkIntent);
				// } catch (Exception ex) {
				// Toast.makeText(UpdateService.this,
				// "没有适合软件打开该文件！",
				// Toast.LENGTH_LONG).show();
				// }
				break;
			case DOWN_ERROR:
				// notification.setLatestEventInfo(UpdateService.this, app_name,
				// "下载失败", pendingIntent);
				notification.contentView.setTextViewText(
						R.id.notificationTitle, "下载失败");
				Toast.makeText(UpdateService.this, "文件下载失败", Toast.LENGTH_LONG)
						.show();
			default:
				stopService(updateIntent);
				break;
			}
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			try {
				httpClient.setTimeout(120000);
				app_name = intent.getStringExtra("app_name");
				down_url = intent.getStringExtra("downurl");
				Uri uri = Uri.parse(down_url);
				user_id = uri.getQueryParameter("userid");
				if (user_id == null) {
					user_id = "";
				}
				// 创建通知
				createNotification();
				// 开始下载
				downloadUpdateFile(down_url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/***
	 * 创建通知栏
	 */
	RemoteViews contentView;

	@SuppressWarnings("deprecation")
	public void createNotification() {

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		// 这个参数是通知提示闪出来的值.
		notification.tickerText = "开始下载";

		// pendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);

		// 这里面的参数是通知栏view显示的内容
		// notification.setLatestEventInfo(this, app_name, "下载：0%",
		// pendingIntent);

		// notificationManager.notify(notification_id, notification);

		/***
		 * 在这里我们用自定的view来显示Notification
		 */
		contentView = new RemoteViews(getPackageName(),
				R.layout.notification_item);
		contentView.setTextViewText(R.id.notificationTitle, "正在下载");
		contentView.setTextViewText(R.id.notificationPercent, "0%");
		contentView.setProgressBar(R.id.notificationProgress, 100, 0, false);
		contentView
		.setViewVisibility(
				R.id.notificationProgress,
				View.VISIBLE);
		notification.contentView = contentView;

		updateIntent = new Intent(this, MainActivity.class);
		updateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);

		notification.contentIntent = pendingIntent;
		notificationManager.notify(notification_id, notification);
	}

	long startTime;
	long elapsedTime = 0L;
	String result = "";

	/***
	 * 下载文件
	 */
	public void downloadUpdateFile(String down_url) throws Exception {
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
		BasicClientCookie newCookie = new BasicClientCookie("userid", user_id);
		newCookie.setVersion(1);
		newCookie.setDomain(Uri.parse(down_url).getHost());
		newCookie.setPath("/");
		cookieStore.addCookie(newCookie);
		httpClient.setCookieStore(cookieStore);

		httpClient.get(UpdateService.this, down_url,
				new FileAsyncHttpResponseHandler(UpdateService.this) {

					@Override
					public void onProgress(long bytesWritten, long totalSize) {
						// TODO Auto-generated method stub
						super.onProgress(bytesWritten, totalSize);
						double x_double = bytesWritten * 1.0;
						double tempresult = x_double / totalSize;
						DecimalFormat df1 = new DecimalFormat("0.00"); // ##.00%
						// 百分比格式，后面不足2位的用0补齐
						result = df1.format(tempresult);

						if (elapsedTime > 500) {
							startTime = System.currentTimeMillis();
							elapsedTime = 0;
							new Handler(Looper.getMainLooper())
									.post(new Runnable() {
										@Override
										public void run() {

											contentView
													.setTextViewText(
															R.id.notificationPercent,
															(int) (Float
																	.parseFloat(result) * 100)
																	+ "%");
											contentView.setProgressBar(
													R.id.notificationProgress,
													100,
													(int) (Float
															.parseFloat(result) * 100),
													false);
											notificationManager.notify(
													notification_id,
													notification);

										}
									});

						} else
							elapsedTime = new Date().getTime() - startTime;

					}

					@Override
					public void onSuccess(int arg0, Header[] arg1,
							final File arg2) {
						// TODO Auto-generated method stub
						if (arg1 == null) {
							return;
						}
						HashMap<String, String> hasMap = convertHeadersToHashMap(arg1);
						String header = hasMap.get("Content-Disposition");
						if (header != null && header.contains("filename=")) {
							int start = header.indexOf("filename=")
									+ "filename=".length();
							header = header.substring(start);
						} else {
							Toast.makeText(UpdateService.this, "服务器没有返回文件",
									Toast.LENGTH_LONG).show();
							return;
						}
						if (header.length() == 0) {
							return;
						}
						String fileName = URLDecoder.decode(header);
						final File dbFile = Utility.getDownloadStoreDir(
								UpdateService.this, fileName);
						if (dbFile.exists()) {
							dbFile.delete();
						}
						updateFile = dbFile.getPath();
						boolean sucess = FileUtil.moveFile(arg2, dbFile);
						if (sucess) {
							// Message message = handler.obtainMessage();
							// message.what = DOWN_OK;
							// handler.sendMessage(message);

							new Handler(Looper.getMainLooper())
									.post(new Runnable() {
										@Override
										public void run() {

											Intent installApkIntent = getFileIntent(new File(
													updateFile));
											pendingIntent = PendingIntent
													.getActivity(
															UpdateService.this,
															0,
															installApkIntent, 0);
											notification.contentIntent = pendingIntent;
											notification.flags |= Notification.FLAG_AUTO_CANCEL;
											// notification.setLatestEventInfo(UpdateService.this,
											// app_name,
											// "下载成功，点击打开", pendingIntent);
											notification.contentView
													.setTextViewText(
															R.id.notificationTitle,
															"下载成功，点击打开");
											notification.contentView
													.setViewVisibility(
															R.id.notificationProgress,
															View.GONE);
											notificationManager.notify(
													notification_id,
													notification);
											stopService(updateIntent);

											Intent intent = new Intent();
											intent.putExtra("filePath",
													updateFile);
											intent.setAction("com.skyland.file");
											sendBroadcast(intent);
										}
									});

						}
					}

					@Override
					public void onFailure(int arg0, Header[] arg1,
							Throwable arg2, File arg3) {
						// TODO Auto-generated method stub
						Message message = handler.obtainMessage();
						message.what = DOWN_ERROR;
						handler.sendMessage(message);
					}

				});
	}

	private HashMap<String, String> convertHeadersToHashMap(Header[] headers) {
		HashMap<String, String> result = new HashMap<String, String>(
				headers.length);
		for (Header header : headers) {
			result.put(header.getName(), header.getValue());
		}
		return result;
	}

	// 下载完成后打开安装apk界面
	public static void installApk(File file, Context context) {
		// L.i("msg", "版本更新获取sd卡的安装包的路径=" + file.getAbsolutePath());
		Intent openFile = getFileIntent(file);
		context.startActivity(openFile);

	}

	public static Intent getFileIntent(File file) {
		Uri uri = Uri.fromFile(file);
		String type = getMIMEType(file);
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(uri, type);
		return intent;
	}

	public static String getMIMEType(File f) {
		String type = "";
		String fName = f.getName();
		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("3gp") || end.equals("mp4")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else if (end.equals("pdf")) {
			type = "application/pdf";
		} else if (end.equals("apk")) {
			// android.permission.INSTALL_PACKAGES
			type = "application/vnd.android.package-archive";
		} else if (end.endsWith("doc") || end.endsWith("docx")) {
			type = "application/msword";
		} else if (end.endsWith("xls") || end.endsWith("xlsx")) {
			type = "application/vnd.ms-excel";
		} else if (end.endsWith("ppt") || end.endsWith("pptx")) {
			type = "application/vnd.ms-powerpoint";
		} else if (end.endsWith("txt")) {
			type = "text/plain";
		} else {
			type = "*";
		}
		/*
		 * if (!end.equals("apk")) { type += "/*"; }
		 */
		return type;
	}
}