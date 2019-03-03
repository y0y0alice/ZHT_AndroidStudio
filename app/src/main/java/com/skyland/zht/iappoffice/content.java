package com.skyland.zht.iappoffice;

import android.os.Environment;

interface constant {
    final static int NEW_FILE = 0;
    final static int NEW_FILE_TEMPLATE = 1;
    final static int TOUCH_TOLERANCE = 4;
    final static int INIT_PAINTWIDTH = 15;
    final static float FONT_SIZE = 20.0f;
    final static int WEB_FILE = 0;
    final static int LOCAL_FILE = 1;

    final static int LOAD_SAVE_ING = 99 ;

    final static String SDCARD_ROOT_PATH = Environment
            .getExternalStorageDirectory().getPath().toString();

    final static String SERVICE_IWEBOFFICE = "com.kinggrid.iappofficeapi";
    final static String BROADCAST_BACK_DOWN = "com.kinggrid.iappoffice.back";
    final static String BROADCAST_HOME_DOWN = "com.kinggrid.iappoffice.home";
    final static String BROADCAST_FILE_SAVE = "com.kinggrid.iappoffice.save";
    final static String BROADCAST_FILE_CLOSE = "com.kinggrid.iappoffice.close";
    final static String BROADCAST_FILE_SAVE_PIC = "com.kinggrid.iappoffice.save.pic";
    final static String BROADCAST_FILE_SAVEAS_PDF = "com.kinggrid.file.saveas.end";

    final static int INIT_FAIL = 0;
    final static int INIT_SUCCESS = 1;
    final static int COPYRIGHT_VALUE_NULL = 1000;
    final static int COPYRIGHT_RESULT_FAIL = 1001;
    final static int COPYRIGHT_RESULT_SUCCESS = 1002;
    final static String COPYRIGHT_VALUE_FOREVER = "";
    //过期时间：2018-12-19
//	final static String COPYRIGHT_VALUE_FORTRY = "SxD/phFsuhBWZSmMVtSjKZmm/c/3zSMrkV2Bbj5tznSkEVZmTwJv0wwMmH/+p6wLiUHbjadYueX9v51H9GgnjUhmNW1xPkB++KQqSv/VKLDsR8V6RvNmv0xyTLOrQoGzAT81iKFYb1SZ/Zera1cjGwQSq79AcI/N/6DgBIfpnlwiEiP2am/4w4+38lfUELaNFry8HbpbpTqV4sqXN1WpeJ7CHHwcDBnMVj8djMthFaapMFm/i6swvGEQ2JoygFU3W8onCO1AgMAD2SkxfJXM/mX1uF23u5oNhx5kpmkBkb3x6aD2yiupr6ji7hzsE6/Qng3l3AbK2vtwyJLdcl2md6r5JJO51PJS2vAlVxcmvGGVWEbHWAH22+t7LdPt+jENOIq5GN/n4KME0L/SFgUD1b8zb/8DFI+sDLA8bVOqHBiSgCNRP4FpYjl8hG/IVrYXOzDNrpoUGsPwMMlLKBA40uW8fXpxdRHfEuWC1PB9ruQ=";
    final static String COPYRIGHT_VALUE_FORTRY = "SxD/phFsuhBWZSmMVtSjKZmm/c/3zSMrkV2Bbj5tznSkEVZmTwJv0wwMmH/+p6wLiUHbjadYueX9v51H9GgnjUhmNW1xPkB++KQqSv/VKLDsR8V6RvNmv0xyTLOrQoGzAT81iKFYb1SZ/Zera1cjGwQSq79AcI/N/6DgBIfpnlwiEiP2am/4w4+38lfUELaNFry8HbpbpTqV4sqXN1WpeJ7CHHwcDBnMVj8djMthFaapMFm/i6swvGEQ2JoygFU38558QhLaX/Jr1koWwK15kEmTvMG/nhvTilibTVNrCtS1VzVwUlmsSQF90/OYTdIhuI7ZTJqU764j1aPQnpiJKCoW/T3lHnHSqUG9ekm19Ty6lZhbc0wZsYY7F8fGE4DHZs4YOxNd7fnxKM4Dd4Klf1+OBmeDcECRDSxKMJbBZX2hOjA9xBaBPEEybxot0XBztGIYfNWOY83ltjqKjeToDSIzRqZYYUbAtzuYjy8C+3Ix04ev8Hm/as8N+FyWSdRWDRk0QFLuqgyKytzjbVWa9W3+KjjXk4eWYscdbEPDzss=";

    final static String WPS_TASK = "com.kinggrid.iappofficeapi";
    final static int TAST_STOP = 0;
    final static int TAST_START = 1;
    final static int WPS_SAVE = 0;
    final static int WPS_SAVEAS = 1;
    final static int WPS_INSERTTEXT = 2;
    final static int WPS_INSERTIMAGE = 3;
    final static int WPS_INSERTHANDWRITE = 4;
    final static int WPS_CLOSE = 5;
    final static int WPS_EXIT = 6;


    final static boolean INCLUDE_BLANK_AREA = true;//

    final static String BROADCAST_VIS_IMG = "cn.kg.broadcast.visibleimg";
    final static String BROADCAST_IMG_PATH = "com.kinggrid.imgpath";
    final static String BROADCAST_SIGN_IMG_PATH = "com.kinggrid.signimgpath";
    final static String BROADCAST_CLOSE_WPS = "return_close_wps";
    final static String SERVICE_IAPPOFFICE = "com.kinggrid.iappofficeservice";
    final static String BROADCAST_NOT_FINDOFF = "com.kinggrid.notfindoffice";
    final static String BROADCAST_SIGN_CHECKBOX_CHANGED = "com.kinggrid.signature.checkbox.changed";
    final static String BROADCAST_SIGNATURE_SHOW = "com.kinggrid.iappoffice.fullsignature.show";
    final static String BROADCAST_FILE_OPEN_END = "com.kinggrid.file.open.end";
    final static String BROADCAST_FILE_SAVEAS = "com.kinggrid.iappoffice.saveas";
//	final static String BROADCAST_BACK_DOWN = "com.kinggrid.iappoffice.back";
//	final static String BROADCAST_HOME_DOWN = "com.kinggrid.iappoffice.home";
//	final static String BROADCAST_FILE_SAVE = "com.kinggrid.iappoffice.save";
//	final static String BROADCAST_FILE_CLOSE = "com.kinggrid.iappoffice.close";

    final static boolean FLAG_OK = true ;
    final static boolean FLAG_CANCEL = false ;
    final static int FLAG_CLOSE = 1 ;

    final static String WPS_PACKAGE = "cn.wps.moffice_ent";

    final static int TASK_LOAD = 1 ;
    final static int TASK_SAVE = 2 ;
    final static int TASK_SUCCESS = 0 ;
    final static int TASK_FAIL = -1 ;
    final static int TASK_CONN_FAIL = -2 ;

    final static String SERVICE_TOOLBAR = "com.kinggrid.toolbar";

    final static int FOR_REQUEST_FILE = 1001;
    final static int FOR_REQUEST_PIC  = 1002;
    final static int FOR_REQUEST_HANDWRITE  = 1003;
    final static int FOR_REQUEST_PENSET  = 1004;
    final static int FOR_REQUEST_POP = 1005;

    final static int FOR_RESULT_FILE = 2004;
    final static int FOR_RESULT_PIC  = 2005;
    final static int FOR_RESULT_HANDWRITE  = 2006;
    final static int FOR_RESULT_PENSET  = 2007;
    final static int FOR_RESULT_POP = 2008;

}
