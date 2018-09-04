package com.skyland.zht;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecordActivity extends ActionBarActivity {

	MediaRecorder mRecorder;
	MediaPlayer mPlayer;
	String path;
	TextView txtTime;
	Button btnOpearte;
	boolean started = false;
	LinearLayout view1;
	LinearLayout view2;

	long startTime;
	Timer timer;
	TimerTask mTimerTask;
	Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);

		view1 = (LinearLayout) findViewById(R.id.view1);
		view2 = (LinearLayout) findViewById(R.id.view2);

		txtTime = (TextView) findViewById(R.id.txtTime);
		btnOpearte = (Button) findViewById(R.id.btnOpearte);
		btnOpearte.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!started) {
					txtTime.setText("00:00:00");
					mRecorder = new MediaRecorder();
					mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
					mRecorder.setOutputFile(path);
					mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
					try {
						mRecorder.prepare();
						mRecorder.start();
						startTime = System.nanoTime();
						startTimer();
					} catch (IOException e) {

					}
					started = true;
					btnOpearte.setText("停止");
				} else {
					mRecorder.stop();
					mRecorder.release();
					mRecorder = null;
					stopTimer();
					started = false;
					btnOpearte.setText("开始");
					view2.setVisibility(View.VISIBLE);
					view1.setVisibility(View.GONE);
				}
			}
		});

		Button btnUse = (Button) findViewById(R.id.btnUse);
		btnUse.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(RESULT_OK);
				finish();
			}
		});

		Button btnPlay = (Button) findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mPlayer = new MediaPlayer();
				try {
					mPlayer.setDataSource(path);
					mPlayer.prepare();
					mPlayer.start();
				} catch (IOException e) {

				}
			}
		});

		Button btnRetake = (Button) findViewById(R.id.btnRetake);
		btnRetake.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mPlayer != null) {
					mPlayer.release();
					mPlayer = null;
				}

				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
				txtTime.setText("00:00:00");
				view2.setVisibility(View.GONE);
				view1.setVisibility(View.VISIBLE);
			}
		});

		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(RESULT_CANCELED);
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
				finish();
			}
		});

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					long estimatedTime = System.nanoTime() - startTime;
					long duration=TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS);
					long hours = duration / 3600;
					long minutes = (duration - hours * 3600) / 60;
					long seconds = duration - (hours * 3600 + minutes * 60);

					String text = (String.valueOf(hours).length() == 1 ? ("0" + hours)
							: hours)
							+ ":"
							+ (String.valueOf(minutes).length() == 1 ? ("0" + minutes)
									: minutes)
							+ ":"
							+ (String.valueOf(seconds).length() == 1 ? ("0" + seconds)
									: seconds);
					txtTime.setText(text);
					break;
				default:
					break;
				}
			}
		};

		Bundle bundle = getIntent().getExtras();
		path = bundle.getString("Path");
	}

	private void startTimer() {
		timer = new Timer();
		mTimerTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(1);
			}
		};
		timer.schedule(mTimerTask, 0, 1000);
	}

	private void stopTimer() {
		mHandler.sendEmptyMessage(1);
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
		
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}

}
