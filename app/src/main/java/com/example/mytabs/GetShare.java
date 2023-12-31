package com.example.mytabs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public class GetShare extends Activity
{
	Button get,exit;
	TextView textView;
	EditText editText;
	int button_key=0;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.re_name_equipment);
		get=findViewById(R.id.button1);
		exit=findViewById(R.id.button2);
		textView=findViewById(R.id.title_name);
		editText=findViewById(R.id.name);
		textView.setText("分享码:");
		get.setText("接收");
		editText.setHint(R.string.请输入分享码);
		MySqlLite sql = new MySqlLite(getApplicationContext());
		UserData userData=sql.get_login_data();
		get.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(button_key==0)
				{
					String str_share = editText.getText().toString().trim();
					str_share = str_share.toLowerCase();
					Log.e("TAG", "onClick: str_share  "+str_share);
					if (!JaoYan.jy_md5(str_share))
					{
						Toast.makeText(GetShare.this, "再检查一下", Toast.LENGTH_SHORT).show();
						return;
					}
					//new MyHttp().thread_send_Post_String(MyHttp.IP + "get_share", str1+"share_ma="+str_share, hander_2, 1);
					HashMap<String,Object> dict = userData.get_dict_data();
					dict.put("share_ma",str_share);
					new MyHttp2(userData).thread_post_do_data("get_share", MyJson.toJson(dict), userData.net_md5, hander_2);

					button_key = 1;
				}
				else if(button_key==1)
				{
					Toast.makeText(GetShare.this, "正在联系服务器处理", Toast.LENGTH_SHORT).show();
				}
			}
		});
		exit.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

	}


	@SuppressLint("HandlerLeak")
	private final Handler hander_2 = new Handler()//负责处理按键里的线程
	{
		public void handleMessage(@NonNull Message msg)
		{

			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(GetShare.this, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(GetShare.this, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == 0)
			{
				//#温度:28#湿度:68#@灯0[0-1]:1#@灯1[0-1]:1#
				//msg.what 是视图的编号，从0开始
				String str_data = (String) msg.obj;
				Toast.makeText(GetShare.this, str_data, Toast.LENGTH_SHORT).show();
				setResult(1);
				finish();
			}
			else if (msg.what > 0)
			{
				Toast.makeText(GetShare.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
			}
			button_key = 0;
		}
	};
}
