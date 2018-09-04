package com.skyland.zht;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		boolean ret = false;
		ConnectivityManager connectMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobNetInfo = connectMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifiNetInfo = connectMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		App app = (App) context.getApplicationContext();
		if (mobNetInfo != null) {
			if(mobNetInfo.isConnected())
			{
				ret=true;
			}
		}
		if (wifiNetInfo != null) {
			if(wifiNetInfo.isConnected())
			{
				ret=true;
			}
		}
		app.setConnected(ret);
	}

}
