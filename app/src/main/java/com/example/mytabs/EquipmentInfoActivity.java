package com.example.mytabs;


import static com.example.mytabs.MainActivity.FIG_E_RENAME_;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Objects;

public class EquipmentInfoActivity extends Activity
{

	int now_result_code = 0;
	UserData userData;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.equipment_info);
		userData = new MySqlLite(EquipmentInfoActivity.this).get_login_data();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case FIG_E_RENAME_:
			{
				if (resultCode > now_result_code)
				{
					now_result_code = resultCode;
					setResult(resultCode);
				}
			}
		}

	}

	public void button_rename_equipment(View v)
	{
		Intent intent_old = getIntent();
		Intent intent = new Intent(EquipmentInfoActivity.this, ReNameEquipment.class);
		final String ename = intent_old.getStringExtra("name");
		final String eid = intent_old.getStringExtra("eid");
		intent.putExtra("name", ename);
		intent.putExtra("eid", eid);
		startActivityForResult(intent, FIG_E_RENAME_);//打开界面

	}

	public void button_show_old_date_equipment(View v)
	{
		Intent intent_old = getIntent();
		Intent intent = new Intent(EquipmentInfoActivity.this, EquipmentShowOldDateActivity.class);
		final String eid = intent_old.getStringExtra("eid");
		intent.putExtra("eid", eid);
		startActivityForResult(intent, R.layout.equipment_show_old_date);//打开界面
	}

	public void button_log_equipment(View v)
	{

	}

	public void button_delete_equipment(View v)
	{

	}


}
