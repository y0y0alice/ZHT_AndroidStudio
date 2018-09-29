package com.skyland.zht;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class CustomSettingActivity extends Activity {

	Button btnSave;
	ImageButton btnBack;
	EditText txtAddress;
	EditText offlineAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_setting);

		txtAddress = (EditText) findViewById(R.id.txtAddress);
		offlineAddress = (EditText) findViewById(R.id.offlineAddress);
		btnBack = (ImageButton) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (txtAddress.getText().toString().trim().length() == 0) {
					AlertDialog alertDialog = new AlertDialog.Builder(
							CustomSettingActivity.this)
							.setMessage("请设置服务地址")
							.setPositiveButton("确定", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									// finishAnimate();
									dialog.dismiss();
								}
							}).setTitle("提示")
							.setIcon(android.R.drawable.ic_dialog_info)
							.create();
					alertDialog.show();
					return;
				}

				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(CustomSettingActivity.this);
				Editor editor = settings.edit();
				editor.putString("address_preference", txtAddress.getText().toString().trim());
				editor.putString("address_offline", offlineAddress.getText().toString().trim());
				editor.commit();
				setResult(RESULT_OK);
				finish();
			}
		});
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		String addressString = settings.getString("address_preference", "");
		String offlineString = settings.getString("address_offline", "");
		txtAddress.setText(addressString);
		offlineAddress.setText(offlineString);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.custom_setting, menu);
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
