package com.example.mytabs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public class ReNameEquipment extends Activity
{
	Button save, quit;
	EditText EditText_name;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.re_name_equipment);
		save = findViewById(R.id.button1);
		quit = findViewById(R.id.button2);
		EditText_name = findViewById(R.id.name);
		Log.i("TAG", "onCreate: 加载界面ReNameEquipment");
		Intent intent = getIntent();
		final String ename = intent.getStringExtra("name");
		final String eid = intent.getStringExtra("eid");
		//final String uid = intent.getStringExtra("uid");
		UserData userData=new MySqlLite(ReNameEquipment.this).get_login_data();

		Log.i("TAG", "onCreate: name " + ename + "    eid  " + eid);
		EditText_name.setText(ename);

		quit.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		save.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String new_name = EditText_name.getText().toString().trim();
				Log.i("TAG", "onCreate:  new_name  " + new_name);

				if (ename.equals(new_name))
				{
					finish();
				}
//				if (!JaoYan.jy_special_char(new_name))
//				{
//					Toast.makeText(ReNameEquipment.this, "请去掉特殊符号和空格 `~!@#$%^&*()_+\"'{}[]\n\\/<>,.|", Toast.LENGTH_SHORT).show();
//					return;
//				}
				if (new_name.length() > 30)
				{
					Toast.makeText(ReNameEquipment.this, "太长了", Toast.LENGTH_SHORT).show();
					return;

				}
				HashMap<String,Object> dict = userData.get_dict_data();
				dict.put("eid",eid);
				dict.put("name",new_name);
				dict.put("oldname",ename);
				new MyHttp2(userData).thread_post_do_data("rename_equipment",MyJson.toJson(dict),userData.net_md5, hander_rename);

			}
		});
		Log.i("TAG", "onCreate: rename activity end ");


	}

	@SuppressLint("HandlerLeak")
	Handler hander_rename = new Handler()
	{
		@Override
		public void handleMessage(@NonNull Message msg)
		{
			if(msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(ReNameEquipment.this, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
			}
			else if(msg.what==MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(ReNameEquipment.this, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
			}
			else if(msg.what == 0)
			{

				Toast.makeText(ReNameEquipment.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				setResult(1);
				finish();
			}
			else if(msg.what >0)
			{
				Toast.makeText(ReNameEquipment.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
			}

		}
	};
}
