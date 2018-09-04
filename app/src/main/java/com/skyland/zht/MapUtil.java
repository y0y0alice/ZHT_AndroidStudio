package com.skyland.zht;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class MapUtil {

	public static boolean isAvilible(Context context, String packageName) {
		final PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
		List<String> packageNames = new ArrayList<String>();
		if (packageInfos != null) {
			for (int i = 0; i < packageInfos.size(); i++) {
				String packName = packageInfos.get(i).packageName;
				packageNames.add(packName);
			}
		}
		return packageNames.contains(packageName);
	}

	public static void openBaidu(Context context, double lat, double lng) {
		try {
			Intent intent = Intent.getIntent("intent://map/direction?destination=latlng:"
		+lat+","+lng+"|name:&mode=driving&referer=Autohome|GasStation#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
			context.startActivity(intent);
		} catch (URISyntaxException e) {

		}
	}

	public static void openGaoDe(Context context, double lat, double lng) {
		try {
			Intent intent = Intent
					.getIntent("androidamap://route?sourceApplication=&dlat="
							+ lat + "&dlon=" + lng + "&dev=0&t=2&m=0");
			context.startActivity(intent);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static double[] bdToGaoDe(double bd_lat, double bd_lon) {
	    double[] gd_lat_lon = new double[2];
	    double PI = 3.14159265358979324 * 3000.0 / 180.0;
	    double x = bd_lon - 0.0065, y = bd_lat - 0.006;
	    double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * PI);
	    double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * PI);
	    gd_lat_lon[1] = z * Math.cos(theta);
	    gd_lat_lon[0] = z * Math.sin(theta);
	    return gd_lat_lon;
	 }

	public static double[] gaoDeToBaidu(double gd_lon, double gd_lat) {
	    double[] bd_lat_lon = new double[2];
	    double PI = 3.14159265358979324 * 3000.0 / 180.0;
	    double x = gd_lon, y = gd_lat;
	    double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * PI);
	    double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * PI);
	    bd_lat_lon[0] = z * Math.cos(theta) + 0.0065;
	    bd_lat_lon[1] = z * Math.sin(theta) + 0.006;
	    return bd_lat_lon;
	}
	
	public static void openBaidu(Context context, String address) {
		try {
			Intent intent = Intent.getIntent("intent://map/direction?destination=name:"
		+address+"&mode=driving&referer=Autohome|GasStation#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
			context.startActivity(intent);
		} catch (URISyntaxException e) {

		}
	}

	public static void openGaoDe(Context context, String address) {
		try {
			Intent intent = Intent
					.getIntent("androidamap://keywordNavi?sourceApplication=&keyword="+address+"&style=2");
			context.startActivity(intent);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
