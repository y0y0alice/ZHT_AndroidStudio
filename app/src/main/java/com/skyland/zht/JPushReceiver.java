package com.skyland.zht;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

public class JPushReceiver extends BroadcastReceiver {

	// private static final int MSG_NOTIFICATION_OPENED = 1002;
	private static final String TAG = "JPush";

	/*
	 * private final Handler mHandler = new Handler() {
	 * 
	 * @Override public void handleMessage(android.os.Message msg) {
	 * super.handleMessage(msg); switch (msg.what) { case
	 * MSG_NOTIFICATION_OPENED: {
	 * 
	 * String extra = msg.getData().getString("EXTRA_EXTRA"); Context context =
	 * (Context) msg.obj;
	 * 
	 * JSONObject customJson = null; try { customJson = new JSONObject(extra);
	 * String type = null; if (!customJson.isNull("Type")) { type =
	 * customJson.getString("Type"); } if (TextUtils.isEmpty(type)) { Intent i =
	 * new Intent(context,MainActivity.class);
	 * i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
	 * Intent.FLAG_ACTIVITY_CLEAR_TOP); context.startActivity(i); } else if
	 * (type.equals("NewOrder")) {
	 * 
	 * String orderListType = customJson .getString("OrderListType"); String
	 * orderType = customJson.getString("OrderType"); String docGuid =
	 * customJson.getString("DocGuid"); Intent i = new Intent(context,
	 * OrderDetailActivity.class);
	 * i.putExtra(OrderDetailActivity.P_ORDER_LIST_TYPE,
	 * Integer.parseInt(orderListType));
	 * i.putExtra(OrderDetailActivity.P_ORDER_TYPE,
	 * Integer.parseInt(orderType)); i.putExtra(OrderDetailActivity.P_ORDER_ID,
	 * docGuid); i.putExtra(OrderDetailActivity.P_FROM_NOTIFICATION, true);
	 * i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
	 * Intent.FLAG_ACTIVITY_CLEAR_TOP); context.startActivity(i);
	 * 
	 * 
	 * } else if (type.equals("CancelOrder")) {
	 * 
	 * String orderListType = customJson .getString("OrderListType"); String
	 * orderType = customJson.getString("OrderType"); String docGuid =
	 * customJson.getString("DocGuid"); Intent i = new Intent(context,
	 * OrderDetailActivity.class);
	 * i.putExtra(OrderDetailActivity.P_ORDER_LIST_TYPE,
	 * Integer.parseInt(orderListType));
	 * i.putExtra(OrderDetailActivity.P_ORDER_TYPE,
	 * Integer.parseInt(orderType)); i.putExtra(OrderDetailActivity.P_ORDER_ID,
	 * docGuid); i.putExtra(OrderDetailActivity.P_FROM_NOTIFICATION, true);
	 * i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
	 * Intent.FLAG_ACTIVITY_CLEAR_TOP); context.startActivity(i);
	 * 
	 * } } catch (JSONException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } } break; default: break; }
	 * 
	 * } };
	 */

	@Override
	public void onReceive(Context context, Intent intent) {
		/*
		 * if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
		 * .getAction())) { String extra =
		 * bundle.getString(JPushInterface.EXTRA_EXTRA); if
		 * (TextUtils.isEmpty(extra)) { return; } Message msg = new Message();
		 * msg.obj = context; msg.what = MSG_NOTIFICATION_OPENED; Bundle b = new
		 * Bundle(); b.putCharSequence("EXTRA_EXTRA", extra); msg.setData(b);
		 * mHandler.sendMessage(msg); }
		 */
		Bundle bundle = intent.getExtras();
		// Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() +
		// ", extras: " + printBundle(bundle));

		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String regId = bundle
					.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
			// send the Registration Id to your server...

		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
				.getAction())) {
			Log.d(TAG,
					"[MyReceiver] 接收到推送下来的自定义消息: "
							+ bundle.getString(JPushInterface.EXTRA_MESSAGE));
			//processCustomMessage(context, bundle);

		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
				.getAction())) {
			Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
			int notifactionId = bundle
					.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
			Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
				.getAction())) {

			Log.d(TAG, "[MyReceiver] 用户点击打开了通知");

			// 打开自定义的Activity
			Intent i = new Intent(context, MainActivity.class);
			i.putExtras(bundle);
			// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(i);

		} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent
				.getAction())) {
			Log.d(TAG,
					"[MyReceiver] 用户收到到RICH PUSH CALLBACK: "
							+ bundle.getString(JPushInterface.EXTRA_EXTRA));
			// 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
			// 打开一个网页等..

		} else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent
				.getAction())) {
			boolean connected = intent.getBooleanExtra(
					JPushInterface.EXTRA_CONNECTION_CHANGE, false);
			Log.w(TAG, "[MyReceiver]" + intent.getAction()
					+ " connected state change to " + connected);
		} else {
			Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
		}
	}

	/*
	 * // 打印所有的 intent extra 数据 private static String printBundle(Bundle bundle)
	 * { StringBuilder sb = new StringBuilder(); for (String key :
	 * bundle.keySet()) { if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID))
	 * { sb.append("\nkey:" + key + ", value:" + bundle.getInt(key)); }else
	 * if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
	 * sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key)); } else
	 * if (key.equals(JPushInterface.EXTRA_EXTRA)) { if
	 * (bundle.getString(JPushInterface.EXTRA_EXTRA).isEmpty()) { Log.i(TAG,
	 * "This message has no Extra data"); continue; }
	 * 
	 * try { JSONObject json = new
	 * JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
	 * Iterator<String> it = json.keys();
	 * 
	 * while (it.hasNext()) { String myKey = it.next().toString();
	 * sb.append("\nkey:" + key + ", value: [" + myKey + " - "
	 * +json.optString(myKey) + "]"); } } catch (JSONException e) { Log.e(TAG,
	 * "Get message extra JSON error!"); }
	 * 
	 * } else { sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
	 * } } return sb.toString(); }
	 */
	/*
	 * //send msg to MainActivity private void processCustomMessage(Context
	 * context, Bundle bundle) {
	 * 
	 * if (MainActivity.isForeground) { String message =
	 * bundle.getString(JPushInterface.EXTRA_MESSAGE); String extras =
	 * bundle.getString(JPushInterface.EXTRA_EXTRA); Intent msgIntent = new
	 * Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
	 * msgIntent.putExtra(MainActivity.KEY_MESSAGE, message); if
	 * (!ExampleUtil.isEmpty(extras)) { try { JSONObject extraJson = new
	 * JSONObject(extras); if (null != extraJson && extraJson.length() > 0) {
	 * msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras); } } catch
	 * (JSONException e) {
	 * 
	 * }
	 * 
	 * } context.sendBroadcast(msgIntent); }
	 * 
	 * }
	 */
}
