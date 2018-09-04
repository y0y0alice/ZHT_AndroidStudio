package com.skyland.zht;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.ezvizuikit.open.EZUIError;
import com.ezvizuikit.open.EZUIKit;
import com.ezvizuikit.open.EZUIPlayer;

import java.util.Calendar;


public class VideoActivity extends Activity implements com.ezvizuikit.open.EZUIPlayer.EZUIPlayerCallBack {

    private static final String AppKey = "f66dc9feec694cf2b9a7ef88325c826d";
    private EZUIPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        //获取EZUIPlayer实例
        mPlayer = (EZUIPlayer) findViewById(R.id.player_ui);
        String title = getIntent().getExtras().getString("title");
        setTitle(title);
        //初始化EZUIKit
        EZUIKit.initWithAppKey(App.shared, AppKey);
        String accessToken = getIntent().getExtras().getString("accessToken");
        //设置授权token
        EZUIKit.setAccessToken(accessToken);
        //设置播放回调callback
        mPlayer.setCallBack(VideoActivity.this);
        //创建loadingview
        ProgressBar mLoadView = new ProgressBar(VideoActivity.this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLoadView.setLayoutParams(lp);
        //设置loadingview
        mPlayer.setLoadingView(mLoadView);
        String url = getIntent().getExtras().getString("url");
        //设置播放参数
        mPlayer.setUrl(url);
    }

    @Override
    public void onPrepared() {
        mPlayer.startPlay();
    }

    @Override
    public void onPlaySuccess() {

    }

    @Override
    public void onPlayFail(EZUIError var1) {

    }

    @Override
    public void onVideoSizeChange(int var1, int var2) {

    }

    @Override
    public void onPlayTime(Calendar var1) {

    }

    @Override
    public void onPlayFinish() {

    }

    protected void onStop() {
        super.onStop();
        //停止播放
        mPlayer.stopPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        mPlayer.releasePlayer();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //切换为竖屏
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int sW = dm.widthPixels;
        int sH = dm.heightPixels;

        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int contentTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentTop - statusBarHeight;
        sH=sH-statusBarHeight-titleBarHeight;

        if (newConfig.orientation == this.getResources().getConfiguration().ORIENTATION_PORTRAIT) {
            mPlayer.setSurfaceSize(sW,sH);
        }
        //切换为横屏
        else if (newConfig.orientation == this.getResources().getConfiguration().ORIENTATION_LANDSCAPE) {
            mPlayer.setSurfaceSize(sW,sH);
        }
    }
}

