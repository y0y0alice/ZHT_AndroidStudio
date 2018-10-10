package com.skyland.zht;

import java.io.File;
import android.content.Context;
import android.os.Environment;

public class Utility {
	
	public static File getPath(Context context,MediaType type)
	{
		File path=null;
		switch (type) {
		case Photo:
			path=new File(getImageStoreDir(context), getFileName(type));
			break;
		case Video:
			path=new File(getVideoStoreDir(context), getFileName(type));
			break;
		case Audio:
			path=new File(getAudioStoreDir(context), getFileName(type));
			break;
		default:
			break;
		}
		return path;
	}

	public static String getFileName(MediaType type)
	{
		String extension="";
		switch (type) {
		case Photo:
			extension=".jpg";
			break;
		case Video:
			extension=".mp4";
			break;
		case Audio:
			extension=".aac";
			break;
		default:
			break;
		}
		return java.util.UUID.randomUUID().toString()+extension;
	}

	public static File getAudioStoreDir(Context context) {
		return new File(Utility.getAppStoreDir(context),"audios");
	}
	
	public static File getVideoStoreDir(Context context) {
		return new File(Utility.getAppStoreDir(context),"videos");
	}
	
	public static File getImageStoreDir(Context context) {
		return new File(Utility.getAppStoreDir(context),"images");
	}
	
	public static File getDownloadStoreDir(Context context) {
		return new File(Utility.getAppStoreDir(context),"download");
	}
	
	public static File getDownloadStoreDir(Context context,String fileName) {
		File downloadFile= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		if(downloadFile==null)
		{
			downloadFile=getDownloadStoreDir(context);
		}
		return new File(downloadFile, fileName);
	}

	public static File getAppStoreDir(Context context) {
		return new File(Utility.getStoreDir(context), Config.AppName);
	}

	public static File getStoreDir(Context context) {
		if (checkExternalStorageAvailable()) {
			return getExternalDir();
		} else {
			return getCacheDir(context);
		}
	}

	public static File getExternalDir() {
		return Environment.getExternalStorageDirectory();
	}

	public static File getCacheDir(Context context) {
		return context.getCacheDir();
	}

	public static boolean checkExternalStorageAvailable() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return (mExternalStorageAvailable == true && mExternalStorageWriteable == true);
	}

}
