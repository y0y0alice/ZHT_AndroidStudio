package com.skyland.zht;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtil {

	public static boolean moveFile(String fromPath, String toPath) {
		return moveFile(new File(fromPath), new File(toPath));
	}

	public static boolean moveFile(File fromFile, File toFile) {
		boolean result = false;
		try {
			FileInputStream fosfrom = new FileInputStream(fromFile);
			FileOutputStream fosto = new FileOutputStream(toFile);
			byte bt[] = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c);
			}
			fosfrom.close();
			fosto.close();
			result = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		return result;
	}
}
