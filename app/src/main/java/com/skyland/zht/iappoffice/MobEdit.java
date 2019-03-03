package com.skyland.zht.iappoffice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kinggrid.iappoffice.IAppOffice;
import com.skyland.zht.MainActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MobEdit implements constant {
    public IAppOffice iappoffice;//控件对象
    TempBean tempbean;
    Activity nContext;
    private String URL_Path;
    private static final String SDCARD_ROOT_PATH = Environment
            .getExternalStorageDirectory().getPath().toString();//SD
    private String Des_Path;
    private static MobEditReceiver getRec;
    private String  returnResult;

    public MobEdit(String param, Activity context) {
        Gson gson = new Gson();
        tempbean = gson.fromJson(param, TempBean.class);
        nContext = context;
        //初始化服务端访问地址
        URL_Path = "http://" + tempbean.host + "/iWebOfficeService.data?action=ProcessRequest";
        //初始化文件夹
        initDoccument();
        //注册意图
        registerIntentFilters();
        //获取文件名称以及各式
        getFileName();
        //初始化iappoffice
        initIAppOffice();
        //打开文档
        openWebDocument();
    }

    //注册意图
    public void registerIntentFilters() {
        if (getRec == null) {
            IntentFilter backFilter = new IntentFilter();
            backFilter.addAction(BROADCAST_BACK_DOWN);
            IntentFilter homeFilter = new IntentFilter();
            homeFilter.addAction(BROADCAST_HOME_DOWN);
            IntentFilter saveFilter = new IntentFilter();
            saveFilter.addAction(BROADCAST_FILE_SAVE);
            IntentFilter closeFilter = new IntentFilter();
            closeFilter.addAction(BROADCAST_FILE_CLOSE);
            IntentFilter notFindFilter = new IntentFilter();
            notFindFilter.addAction("com.kinggrid.notfind.office");
            IntentFilter savePicFilter = new IntentFilter();
            savePicFilter.addAction(BROADCAST_FILE_SAVE_PIC);
            IntentFilter showHandwriteFilter = new IntentFilter();
            showHandwriteFilter.addAction("com.kinggrid.iappoffice.showHandwrite");
            IntentFilter homekeyFilter = new IntentFilter();
            homekeyFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            IntentFilter saveAsPDFFilter = new IntentFilter();
            saveAsPDFFilter.addAction(BROADCAST_FILE_SAVEAS_PDF);
            IntentFilter openEndFilter = new IntentFilter();
            openEndFilter.addAction(BROADCAST_FILE_OPEN_END);

            getRec = new MobEditReceiver();
            nContext.registerReceiver(getRec, backFilter);
            nContext.registerReceiver(getRec, homeFilter);
            nContext.registerReceiver(getRec, saveFilter);
            nContext.registerReceiver(getRec, closeFilter);
            nContext.registerReceiver(getRec, notFindFilter);
            nContext.registerReceiver(getRec, savePicFilter);
            nContext.registerReceiver(getRec, showHandwriteFilter);
            nContext.registerReceiver(getRec, homekeyFilter);
            nContext.registerReceiver(getRec, saveAsPDFFilter);
            nContext.registerReceiver(getRec, openEndFilter);
        }
    }

    public void getFileName(){
     String fileName =   tempbean.sourcePath.substring(tempbean.sourcePath.lastIndexOf("/")+1);
        Des_Path=  SDCARD_ROOT_PATH + "/ZHT/doc/"+fileName;
    }

    //
    public void initIAppOffice(){
        iappoffice = new IAppOffice(nContext);
        iappoffice.setFileProviderAuthor("com.skyland.zht.fileProvider");
        iappoffice.setCopyRight(tempbean.copyRight);
        int isSuccess = iappoffice.init();
    }

    //初始化文件夹
    public void initDoccument() {
        judeDirExists(new File(SDCARD_ROOT_PATH + "/ZHT/doc/"));
    }

    // 判断文件夹是否存在
    public static void judeDirExists(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                System.out.println("dir exists");
            } else {
                System.out.println("the same name file exists, can not create dir");
            }
        } else {
            System.out.println("dir not exists, create it ...");
            file.mkdir();
        }
    }

    //    打开网络文件
    public void openWebDocument() {
        boolean isInstalled = isWPSInstalled();
        if (isInstalled) {
            new Thread() {
                public void run() {
                    downloadFile();
                    appOpen();
                }
            }.start();
        } else {
            showTip("请安装WPS专业版！");
        }
    }

    public void appOpen(){
        iappoffice.setUserName(tempbean.username);
        iappoffice.setFileName(Des_Path);
        iappoffice.setIsReviseMode(true);
        iappoffice.setEditMode(true);
        iappoffice.setShowReviewingPaneRightDefault(false);
        iappoffice.appOpen(true);//打开文档，这里true表示打开本地文档
    }

    //从服务器下载文件到本地
    public void downloadFile() {
        try {
            HttpURLConnection conn;
            int bytesum = 0;
            int byteread = 0;
            Gson gson = new Gson();
            Map<String, String> map = new HashMap<String, String>();
            map.put("FILEPATH", tempbean.sourcePath);
            map.put("OPTION", "LOADFILE");
            String formData = URLEncoder.encode(gson.toJson(map), "utf-8");
            String mergeURL = URL_Path + "&FormData=" + formData;
            URL url = new URL(mergeURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);// 超时5秒
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                FileOutputStream fs = new FileOutputStream(Des_Path);
                int fileSize = conn.getContentLength();// 根据响应获取文件大小
                byte[] buffer = new byte[1204];
                int length;
                while (((byteread = is.read(buffer)) != -1)) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //提示
    public void showTip(String tipInfor) {
        Toast.makeText(nContext, tipInfor, Toast.LENGTH_SHORT).show();
    }

    //判断wps是否安装
    public boolean isWPSInstalled() {
        if (iappoffice.isWPSInstalled()) return true;//判断是否安装WPS
        return false;
    }

    //多文件上传
    public static String uploadFile(String actionUrl, String[] uploadFilePaths) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        DataOutputStream ds = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;

        try {
            // 统一资源
            URL url = new URL(actionUrl);
            // 连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            // http的连接类
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

            // 设置是否从httpUrlConnection读入，默认情况下是true;
            httpURLConnection.setDoInput(true);
            // 设置是否向httpUrlConnection输出
            httpURLConnection.setDoOutput(true);
            // Post 请求不能使用缓存
            httpURLConnection.setUseCaches(false);
            // 设定请求的方法，默认是GET
            httpURLConnection.setRequestMethod("POST");
            // 设置字符编码连接参数
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            // 设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            // 设置请求内容类型
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            // 设置DataOutputStream
            ds = new DataOutputStream(httpURLConnection.getOutputStream());
            for (int i = 0; i < uploadFilePaths.length; i++) {
                String uploadFile = uploadFilePaths[i];
                String filename = uploadFile.substring(uploadFile.lastIndexOf("//") + 1);
                ds.writeBytes(twoHyphens + boundary + end);
                ds.writeBytes(
                        "Content-Disposition: form-data; " + "name=\"FileData\";filename=\"" + filename + "\"" + end);
                ds.writeBytes(end);
                FileInputStream fStream = new FileInputStream(uploadFile);
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int length = -1;
                while ((length = fStream.read(buffer)) != -1) {
                    ds.write(buffer, 0, length);
                }
                ds.writeBytes(end);
                /* close streams */
                fStream.close();
            }
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
            /* close streams */
            ds.flush();
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception(
                        "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);
                tempLine = null;
                resultBuffer = new StringBuffer();
                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                    resultBuffer.append("\n");
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            resultBuffer.append(e.getMessage());
        } finally {
            if (ds != null) {
                try {
                    ds.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return resultBuffer.toString();
        }
    }

    //关闭后询问是否上传服务器
    public void floseAndAnswearUpload(){
        // 工厂设计模式，得到创建对话框的工厂
        AlertDialog.Builder builder = new AlertDialog.Builder(nContext);
        builder.setTitle("提示");
        builder.setMessage("是否保存到服务器？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            Thread thread = null;
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thread = new ThreadWatchar();
                thread.start();
                try {
                    thread.join();
                    if(returnResult.length()>0)
                    {
                        Toast.makeText(nContext, "上传失败！"+returnResult, Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(nContext, "上传成功！"+returnResult, Toast.LENGTH_LONG).show();
                    }
                } catch (InterruptedException e) {
                    Toast.makeText(nContext, "上传失败！"+returnResult+e, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(nContext, "选择否", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    //手机在线编辑参数转换模板
    class TempBean {
        public String copyRight;//appkey编号

        public String sourcePath;//附件原路径

        private String username;//修订人姓名

        private String host;//网站地址和端口号

    }

    //广播接收
    public class MobEditReceiver extends BroadcastReceiver{
        boolean isSave = false;
        boolean isSaveAs = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BROADCAST_BACK_DOWN.equals(intent.getAction())) {
                Log.d(TAG, "key back down");
            } else if (BROADCAST_HOME_DOWN.equals(intent.getAction())) {
                Log.d(TAG, "key home down");
            } else if (BROADCAST_FILE_SAVE.equals(intent.getAction())) {
                isSave = true;
                Log.d(TAG, BROADCAST_FILE_SAVE + " : file save");
            } else if (BROADCAST_FILE_CLOSE.equals(intent.getAction())) {
                fileClose();
                isSave = false;
                Log.d(TAG, BROADCAST_FILE_CLOSE + " : file close");
            } else if ("com.kinggrid.notfind.office".equals(intent.getAction())) {
                Log.d(TAG, "wps office not find");
            } else if (BROADCAST_FILE_SAVE_PIC.equals(intent.getAction())) {
                Log.d(TAG, "office save pic over");
            } else if (BROADCAST_FILE_SAVEAS_PDF.equals(intent.getAction())) {
                Log.d(TAG, "office save as pdf over");
            } else if (BROADCAST_FILE_SAVEAS.equals(intent.getAction())) {
                isSaveAs = true;
            }
        }
    }

    class ThreadWatchar extends Thread{
        @Override
        public void run(){
            returnResult = setUploadParam();
        }
    }

    //设置上传参数
    private String setUploadParam(){
        //上传的文件路径
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<String, String>();
        //服务器路径
        map.put("FILEPATH", tempbean.sourcePath);
        map.put("OPTION", "SAVEFILE");
        String formData = null;
        try {
            formData = URLEncoder.encode(gson.toJson(map), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String urlParam = URL_Path + "&FormData=" + formData;

        String result = IWebOfficeHandler.uploadFile(urlParam, new String[]{Des_Path});
        return result;
    }

    public void fileClose() {
        // 工厂设计模式，得到创建对话框的工厂
        AlertDialog.Builder builder = new AlertDialog.Builder(nContext);
        builder.setTitle("提示");
        builder.setMessage("是否保存到服务器？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            Thread thread = null;
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thread = new ThreadWatchar();
                thread.start();
                try {
                    thread.join();
                    if(returnResult.length()>0)
                    {
                        Toast.makeText(nContext, "上传失败！"+returnResult, Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(nContext, "上传成功！"+returnResult, Toast.LENGTH_LONG).show();
                    }
                } catch (InterruptedException e) {
                    Toast.makeText(nContext, "上传失败！"+returnResult+e, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(nContext, "选择否", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}


