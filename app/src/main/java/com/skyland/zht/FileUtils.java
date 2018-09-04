package com.skyland.zht;

import java.io.File;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class FileUtils {

	public static boolean isSDcardExist() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	public static File getDiskCacheDir(Context context, String uniqueName) {
		final String cachePath = isSDcardExist() ? getExternalCacheDir(context).getPath() : context.getCacheDir().getPath();
		return new File(cachePath + File.separator + uniqueName);
	}

	public static File getExternalCacheDir(Context context) {
		if (context == null) {
			return new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.jsyc.wedof/cache/");
		}
		final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}

	public static String md5(String paramString) {
		String returnStr;
		try {
			MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
			if (paramString != null) {
				localMessageDigest.update(paramString.getBytes());
			}
			returnStr = byteToHexString(localMessageDigest.digest());
			return returnStr;
		} catch (Exception e) {
			return paramString;
		}
	}

	/**
	 * 将指定byte数组转换成16进制字符串
	 * 
	 * @param b
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static String byteToHexString(byte[] b) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			hexString.append(hex.toUpperCase());
		}
		return hexString.toString();
	}

	public static String getFileSize(Context context, File file) {
		String fileSize = "0";
		if (file.exists()) {
			long oldFileSize = getFileSize(file);
			fileSize = FormatFileSize(oldFileSize);
		}
		return fileSize;
	}

	public static long getFileSize(File f) {
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}

	/**
	 * 递归求取目录文件个数
	 * 
	 * @param f
	 * @return
	 */
	public static long getlist(File f) {
		long size = 0;
		File flist[] = f.listFiles();
		size = flist.length;
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getlist(flist[i]);
				size--;
			}
		}
		return size;
	}

	public static String FormatFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			if (fileS == 0) {
				fileSizeString = "0.0M";
			} else {
				fileSizeString = df.format((double) fileS) + "B";
			}
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	private boolean flag = true;
	private File file = null;
	public static File currentPhotoFile = null;

	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * 
	 * @param sPath
	 *            要删除的目录或文件
	 * @return 删除成功返回 true，否则返回 false。
	 */
	public boolean deleteFolder(String sPath) {
		flag = false;
		file = new File(sPath);
		// 判断目录或文件是否存在
		if (!file.exists()) { // 不存在返回 false
			return flag;
		} else {
			// 判断是否为文件
			if (file.isFile()) { // 为文件时调用删除文件方法
				return deleteFile(sPath);
			} else { // 为目录时调用删除目录方法
				return deleteDirectory(sPath);
			}
		}
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param sPath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	private boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	private boolean deleteFile(String sPath) {
		flag = false;
		file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 检索目录中的以head为头的文件;
	 * 
	 * @param parent
	 * @param head
	 * @return
	 */
	public static LinkedList<File> getFile(File parent, String head) {
		LinkedList<File> listsFiles = new LinkedList<File>();
		if (parent.exists()) {
			String[] list = parent.list();
			if (list != null) {
				for (int i = 0; i < list.length; i++) {
					if (list[i].startsWith(head)) {
						listsFiles.add(new File(parent.getAbsoluteFile() + File.separator + list[i]));
					}
				}
			}
		}
		return listsFiles;
	}

	/**
	 * 获取剩余空间
	 * 
	 * @param path
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getFreeSize(File path) {
		StatFs stat = new StatFs(path.getPath());
		long availableBlocks = stat.getAvailableBlocks();
		long blockSize = stat.getBlockSize();
		return availableBlocks * blockSize;
	}

	/**
	 * 判断文件路径是否存在
	 * 
	 * @param filePath
	 * @return true 存在 false 不存在
	 */
	public static boolean isExist(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}
}
