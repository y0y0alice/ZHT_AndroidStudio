package com.skyland.zht;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class CustomSettingActivity extends Activity {

    Button btnSave;
    ImageButton btnBack;
    EditText txtAddress;
    EditText offlineAddress;
    RadioGroup textSizRadioGroup;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_setting);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

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
        textSizRadioGroup = (RadioGroup) findViewById(R.id.testSizeRadioGroup);
        textSizRadioGroup.setOnCheckedChangeListener(new setTestSizeOnCheckedChangeListener());

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new SaveClickListener());

        String addressString = settings.getString("address_preference", "http://10.45.1.215:8088");
        String offlineString = settings.getString("address_offline", "");
        txtAddress.setText(addressString);
        setTextSizeRadioGroup();
    }

    /**
     * 设置“字体大小”按钮组的选项
     */
    public void setTextSizeRadioGroup() {
        String textSize = settings.getString("textSize", "normal");
        switch (textSize) {
            case "small":
                textSizRadioGroup.check(R.id.btnSmall);
                break;
            case "normal":
                textSizRadioGroup.check(R.id.btnNormal);
                break;
            case "larger":
                textSizRadioGroup.check(R.id.btnLarger);
                break;
            case "largest":
                textSizRadioGroup.check(R.id.btnLargest);
                break;

        }

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


    private class setTestSizeOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            RadioButton radbtn = (RadioButton) findViewById(checkedId);
            switch (radbtn.getId()) {
                case R.id.btnSmall:
                    saveTextSize("small");
                    break;
                case R.id.btnNormal:
                    saveTextSize("normal");
                    break;
                case R.id.btnLarger:
                    saveTextSize("larger");
                    break;
                case R.id.btnLargest:
                    saveTextSize("largest");
                    break;
            }
        }
    }


    private void saveTextSize(String testSize) {
        Editor editor = settings.edit();
        editor.putString("textSize", testSize);
        editor.commit();
        Log.d("textSize:", settings.getString("textSize", "normal"));
    }

    private class SaveClickListener implements View.OnClickListener {

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

            Editor editor = settings.edit();
            editor.putString("address_preference", txtAddress.getText().toString().trim());
            editor.putString("address_offline", offlineAddress.getText().toString().trim());
            editor.commit();
            setResult(RESULT_OK);
            finish();
        }
    }
}
