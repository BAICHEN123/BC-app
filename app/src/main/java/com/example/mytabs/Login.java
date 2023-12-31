package com.example.mytabs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.HashMap;

public class Login extends Activity
{
	//向主界面返回数据时要包含
	/*
	email
	password
	new_time
	net_md5
	 */
	protected static final int ERROR = 0;
	UserData userdata;
	TextView log_edit1;
	EditText log_edit2;
	boolean thread_post_key = true;

	//boolean thread_time_key=true;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		Log.i("login", "onCreate: 尝试打开登陆界面");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		MySqlLite sql = new MySqlLite(getApplicationContext());
		final String[] str_email = sql.get_all_email();
		if (str_email == null || str_email.length == 0)
		{
			Intent intent = new Intent(Login.this, LoginYouXiang.class);
			startActivityForResult(intent, 1);//打开邮箱登陆界面
		}
		else
		{
			for (String str1 : str_email)
			{
				Log.e("TAG", "onCreate: " + str1);
			}
		}
		log_edit1 = findViewById(R.id.log_edit1);
		log_edit1.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new AlertDialog.Builder(Login.this)
						.setTitle("请选择账号")
						.setItems(str_email, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								assert str_email != null;
								userdata = sql.get_login_data(str_email[which]);
								assert userdata != null;
								log_edit1.setText(str_email[which]);
							}
						})
						.show();
			}
		});

		log_edit2 = findViewById(R.id.log_edit2);
		setResult(0);//没有携带任何数据，没有用户登录
		//Toast.makeText(this, "该功能正在完善安全。请用邮箱登录，或者注册账号", Toast.LENGTH_SHORT).show();
		Log.i("login", "onCreate: 成功打开登陆界面");
	}

	/*接受打开界面的返回值*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("TAG", "onActivityResult: 登录界面收到信息1");
		setResult(resultCode, data);
		finish();
		Log.i("TAG", "onActivityResult: 登录界面收到信息2");
	}

	public void log_youxiang(View v)
	{
		//点击按键  用邮箱登录
		//切换到邮箱登录界面
		Intent intent = new Intent(Login.this, LoginYouXiang.class);
		startActivityForResult(intent, 1);//打开邮箱登陆界面
	}

	public void zhuce(View v)
	{
		//点击注册按键，切换都注册界面
		Log.i("TAG", "zhuce: 尝试打开注册界面1");

		Intent intent = new Intent(Login.this, ZhuCe.class);
		startActivityForResult(intent, 0);//打开注册界面

		Log.i("TAG", "zhuce: 打开注册界面3");
	}

	@SuppressLint("HandlerLeak")//处理登录post数据
	private final Handler handler_log = new Handler()
	{
		public void handleMessage(Message msg)
		{

			String post_data = (String)msg.obj;//容器储存post返回数据，可用于登录
			if(msg.what==MyHttp2.MyHttp_ERROR)
			{
				thread_post_key=true;
				Toast.makeText(Login.this, "失去连接，服务器不要我了>_<", Toast.LENGTH_SHORT).show();
			}
			else if(msg.what==MyHttp2.MyKeyer_ERROR)
			{
				thread_post_key=true;
				Toast.makeText(Login.this, "身份过期，请使用邮箱登录", Toast.LENGTH_SHORT).show();
			}
			else if(msg.what==MyHttp2.MyOk)
			{
//				if(!post_data.startsWith("end"))
//				{
//					Toast.makeText(Login.this, "验证码错误", Toast.LENGTH_SHORT).show();
//					thread_post_key=true;
//					return;
//				}
				final int end = JaoYan.get_end_id(post_data);
				//处理post返回数据email_data
				/*post数据：
					发送：
						发送用户输入的验证码和get邮箱返回的验证码加密结果
						用户的邮箱
					接受：
						net_md5		//用户用于向服务器发起请求的请求码
						new_time 	//服务器最后登录时间

				 */
				Log.i("TAG", "handleMessage: " + post_data);
				if (end == 0)
				{
					String[] post_data_item = post_data.split("&");
					MySqlLite sqlLite = new MySqlLite(getApplicationContext());
					if (post_data_item.length == 4)
					{
						//用户注册之后从未修改过个人信息
						userdata.net_md5 = post_data_item[2];
						userdata.new_time = Long.parseLong(post_data_item[1]);
						userdata.password = "";
						assert userdata.uid == Long.parseLong(post_data_item[3]);
						Log.i("TAG", "handleMessage: " + userdata.get_str_data());
						Toast.makeText(Login.this, "登录成功", Toast.LENGTH_SHORT).show();
						//返回数据，用户登录
						sqlLite.add_UserData(userdata);
						setResult(1);
						//结束这个页面活动
						finish();
					}
					else if (post_data_item.length == 8)
					{
						//用户注册之后修改过注册信息
						//将服务器传回的数据加载到本地
						//Log.i("TAG", "handleMessage: " + userdata.net_md5 + userdata.new_time);
						Toast.makeText(Login.this, "登录成功", Toast.LENGTH_SHORT).show();
						//返回数据，用户登录
						userdata.new_time = Long.parseLong(post_data_item[1]);
						userdata.net_md5 = post_data_item[2];
						assert userdata.uid == Long.parseLong(post_data_item[3]);
						userdata.name = post_data_item[4];
						userdata.sex = Byte.parseByte(post_data_item[5]);
						userdata.user_head[0] = post_data_item[6];
						userdata.user_head[1] = post_data_item[7];
						Log.i("TAG", "handleMessage: " + userdata.get_str_data());
						sqlLite.add_UserData(userdata);
						setResult(1);
						//结束这个页面活动
						finish();
					}
				}

			}
			else
			{
				Toast.makeText(Login.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
				thread_post_key=true;
			}
		}
	};

	public void login_password(View v)
	{
		if (!thread_post_key)
		{
			Toast.makeText(Login.this, "稍等下，正在与服务器叽咕叽咕￥%@##*&……", Toast.LENGTH_SHORT).show();
			return;
		}


		//点击登录按键的处理函数
		userdata.email = log_edit1.getText().toString().trim();
		userdata.password = log_edit2.getText().toString().trim();
		/*
		0	邮箱是否为空
		1	邮箱是否合法
		2	密码是否为空

		 */
		if(userdata==null)
		{
			Toast.makeText(Login.this, "请选择账号或者选择邮箱接收验证码登录", Toast.LENGTH_SHORT).show();
		}
		else if (userdata.email.equals(""))//0用户邮箱空
		{
			Log.i("TAG", "run: 邮箱为空" + userdata.email);
			Log.i("TAG", "run: 邮箱为空");
			Toast.makeText(Login.this, "没有邮箱不可以哦O_O", Toast.LENGTH_SHORT).show();
		}
		else if (!JaoYan.jy_email(userdata.email))//1邮箱非法
		{
			Log.i("TAG", "yx_login: 邮箱非法");
			Toast.makeText(Login.this, "这个邮箱我不认识呃>_<", Toast.LENGTH_SHORT).show();

		}
		else if (userdata.password.equals(""))//3密码为空
		{
			Log.i("TAG", "yx_login: 密码为空");
			Toast.makeText(Login.this, "没有密码，才不给你进去‘_‘", Toast.LENGTH_SHORT).show();
		}
		else if (thread_post_key)
		{
			userdata.password = MyKeyer.MyMd5(userdata.password);
			assert userdata.password != null;
			//向服务器发送请求
			//或者查询本地数据库登录
			Log.i("TAG", "yx_login: 进行登录操作");

			HashMap<String,Object> dict = userdata.get_dict_data();
			dict.put("email",userdata.email);
			dict.put("email",userdata.email);
			dict.put("password",userdata.password);
			new MyHttp2(userdata).thread_post_do_data("pw_login", MyJson.toJson(dict), userdata.net_md5, handler_log);
			//new MyHttp().send_Post_String("email_log",MyKeyer.keyer_get_bytes("email="+userdata.email+"&password="+userdata.password,"pass"),handler_log,CHANGE_UI);
			//Toast.makeText(Login.this, "该功能正在完善安全。请用邮箱登录，或者注册账号", Toast.LENGTH_SHORT).show();
			thread_post_key = false;
		}
	}
}
