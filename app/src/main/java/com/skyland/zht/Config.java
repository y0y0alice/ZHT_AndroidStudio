package com.skyland.zht;

public class Config {

    public static boolean developing = false;
    public static boolean customSetting = true;
    public static boolean disableCache = true;
    public static String AppName = "ZHT";
    public static String MainPage = developing ? "login.html" : "login.html";
    public static String VideoPage = developing ? "Handler1.ashx" : "multiMediaSvc.data?action=uploadVideo";
    public static String PicPage = developing ? "Handler1.ashx" : "multiMediaSvc.data?action=uploadPic";
    public static String RecordPage = developing ? "Handler1.ashx" : "multiMediaSvc.data?action=uploadRecord";
    public static String PushPage = developing ? "PushPage.ashx" : "JPush.data?action=registerUserDevice";

    public static int SETTING_REQUEST_CODE = 1;
    public static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2;
    public static int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 3;
    public static int CAPTURE_RECORD_ACTIVITY_REQUEST_CODE = 4;
    public static int CAPTURE_CODE_ACTIVITY_REQUEST_CODE = 5;
    public static int ALBUM_IMAGE_ACTIVITY_REQUEST_CODE = 6;
    public static int ALBUM_MUTI_IMAGE_ACTIVITY_REQUEST_CODE = 7;

    public static final String UserType = "UserType";
    public static final String UserId = "UserId";
    public static final String AppId = "AppId";
    public static final String UserPreferences = "UserPreferences";

    public static String SMALL = "samll";
    public static String NORMAL = "normal";
    public static String LAGER = "larger";
    public static String LARGEST = "largest";

}
