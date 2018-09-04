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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

public class SettingActivity extends Activity {
	
	Spinner spinner1;
	Button btnSave;
	ImageButton btnBack;
	ArrayAdapter<AddressBean> addressAdapter;
	String host="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		spinner1=(Spinner)findViewById(R.id.spinner1);
		addressAdapter=new ArrayAdapter<AddressBean>(SettingActivity.this, android.R.layout.simple_spinner_item);
		AddressBean bean=new AddressBean();
		bean.setURL("http://112.93.116.169:8080");
		bean.setName("外网");
		addressAdapter.add(bean);
		bean=new AddressBean();
		bean.setURL("http://10.40.211.5:8080");
		bean.setName("内网");
		addressAdapter.add(bean);
		addressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(addressAdapter);
		spinner1
         .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> adapterView,
                                        View view, int position, long id) {
            	 AddressBean addressBean = addressAdapter
                         .getItem(position);
            	 host=addressBean.getURL();
             }

             public void onNothingSelected(AdapterView<?> view) {

             }
         }); 
		btnBack=(ImageButton)findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		btnSave=(Button)findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(spinner1.getSelectedItem()==null)
				{
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            SettingActivity.this)
                            .setMessage("请选择服务地址")
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
						.getDefaultSharedPreferences(SettingActivity.this);
				Editor editor= settings.edit();
				editor.putString("address_preference", host);
				editor.commit();
				setResult(RESULT_OK);
				finish();
			}
		});
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		String addressString = settings.getString("address_preference", "");
		host=addressString;
		if(addressString.equals("http://112.93.116.169:8080"))
		{
			spinner1.setSelection(0);
		}
		else if(addressString.equals("http://10.45.211.5:8080"))
		{
			spinner1.setSelection(1);
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		return super.onOptionsItemSelected(item);
	}


}
