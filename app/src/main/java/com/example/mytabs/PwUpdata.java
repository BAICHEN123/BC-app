package com.example.mytabs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.HashMap;

public class PwUpdata extends Activity
{
	protected static final int CHANGE_UI = 1, ERROR = -1;
	TextView zc_edit1;// = findViewById(R.id.edit1);//邮箱
	EditText zc_edit2;// = findViewById(R.id.edit2);//密码框框
	EditText zc_edit3;// = findViewById(R.id.edit3);//校验密码框框
	EditText zc_edit4;// = findViewById(R.id.edit4);//验证码框框
	Button zc_button1;//=findViewById(R.id.button1);

	byte send_time_num;
	boolean send_time_key;
	boolean thread_post_key = true;
	String user_password = null;
	String user_password2 = null;
	UserData userdata;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		Log.i("login", "onCreate: 尝试打开 PwUpdata 界面");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pw_updata);
		zc_edit1 = findViewById(R.id.edit1);//邮箱
		zc_edit2 = findViewById(R.id.edit2);//密码框框
		zc_edit3 = findViewById(R.id.edit3);//校验密吗框框
		zc_edit4 = findViewById(R.id.edit4);//验证码框框
		zc_button1 = findViewById(R.id.button1);
		send_time_key = true;
		userdata = new MySqlLite(getApplicationContext()).get_login_data();
		zc_edit1.setText(userdata.email);
		Log.i("login", "onCreate: 成功打开 PwUpdata 界面");
	}

	@SuppressLint("HandlerLeak")//处理发送验证码的数据
	private final Handler handler1 = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(PwUpdata.this, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
				send_time_key = true;//解除发送验证码锁
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(PwUpdata.this, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
				send_time_key = true;//解除发送验证码锁
			}
			else if (msg.what == 0)
			{
				Toast.makeText(PwUpdata.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
			}
			else if(msg.what>0)
			{
				send_time_key = true;//解除发送验证码锁
				Toast.makeText(PwUpdata.this, (String)msg.obj, Toast.LENGTH_SHORT).show();

			}
		}
	};
	@SuppressLint("HandlerLeak")//处理注册post数据
	private final Handler handler2 = new Handler()
	{
		@RequiresApi(api = Build.VERSION_CODES.KITKAT)
		public void handleMessage(Message msg)
		{

			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(PwUpdata.this, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
				thread_post_key = true;//解除确认按键锁
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(PwUpdata.this, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
				thread_post_key = true;//解除确认按键锁
			}
			else if (msg.what == 0)
			{
				Toast.makeText(PwUpdata.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
				setResult(2);
				finish();
			}
			else if(msg.what>0)
			{
				thread_post_key = true;//解除确认按键锁
				Toast.makeText(PwUpdata.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
			}
		}
	};

	public void zc_sendemail(View v)
	{
		if(!send_time_key)
		{
			Toast.makeText(PwUpdata.this, send_time_num + "S后再试", Toast.LENGTH_SHORT).show();
			return;
		}

		//判断时间间隔，防止请求过量
		//成功发送邮件之后time_key设置为true
		if (JaoYan.jy_email(userdata.email))
		{
			//拼接url，发送请求

			HashMap<String,Object> dict = userdata.get_dict_data();
			dict.put("email",userdata.email);
			new MyHttp2(userdata).thread_post_do_data("pw_updata_send", MyJson.toJson(dict), userdata.net_md5, handler1);
			send_time_key = false;
			new Thread()
			{
				public void run()
				{
					try
					{
						sleep(60*1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					send_time_key=true;
				}
			}.start();

		}
		else
		{
			Toast.makeText(PwUpdata.this, "这个邮箱我看不懂啊&#@%&*", Toast.LENGTH_SHORT).show();
		}
	}

	public void zc_zhuce(View v)
	{
		Log.i("TAG", "run: 点击注册按键");

		if(!thread_post_key)
		{
			Toast.makeText(PwUpdata.this, "正在通讯", Toast.LENGTH_SHORT).show();
			return;
		}

		user_password = zc_edit4.getText().toString().trim();//用户输入的邮箱验证码
		userdata.password = zc_edit2.getText().toString().trim();//用户的账号密码
		user_password2 = zc_edit3.getText().toString().trim();
		/*
		1邮箱合法 //无需验证，之前发邮件的时候已经验证过了，只需验证2
		2邮箱未变化

		3密码不为空
		4密码两次一致

		5用户验证码不空
		6用户验证码合法

		7APP收到邮箱验证码
		8APP收到的验证码合法
		 */
		if (userdata.email.equals(""))
		{
			Log.i("TAG", "run: 邮箱为空" + userdata.email);
			Log.i("TAG", "run: 邮箱为空");
			Toast.makeText(PwUpdata.this, "没有邮箱不可以哦O_O", Toast.LENGTH_SHORT).show();
		}
		else if (!userdata.email.equals(zc_edit1.getText().toString().trim()))//2用户的邮箱发生变化
		{
			Log.i("TAG", "run: 还未发送该邮箱的验证码");
			Toast.makeText(PwUpdata.this, "还未发送该邮箱的验证码!_!", Toast.LENGTH_SHORT).show();
		}
		else if (userdata.password.length() == 0 || zc_edit3.toString().trim().length() == 0)//3用户的密码为空
		{
			Log.i("TAG", "run: 用户密码为空");
			Toast.makeText(PwUpdata.this, "注意安全，你的密码空了!_!", Toast.LENGTH_SHORT).show();
		}
		else if (!userdata.password.equals(user_password2))//4用户两次密码不一样
		{
			Log.i("TAG", "run: 用户输入的两次密码不一致");
			Toast.makeText(PwUpdata.this, "您输入的密码不一致-_-", Toast.LENGTH_SHORT).show();
		}
		else if (user_password.equals(""))//5用户未输入的验证码
		{
			Log.i("TAG", "run: 用户未输入的验证码" + user_password);
			Log.i("TAG", "run: 用户未输入的验证码");
			Toast.makeText(PwUpdata.this, "验证码不见了O_O", Toast.LENGTH_SHORT).show();
		}
		else if (!JaoYan.jy_password(user_password))//6用户输入的验证码非法
		{
			Log.i("TAG", "run: 用户输入的验证码非法");
			Toast.makeText(PwUpdata.this, "在检查一下验证码哦^_^", Toast.LENGTH_SHORT).show();
		}
		else
		{
			userdata.password=MyKeyer.MyMd5(userdata.password);
			assert userdata.password!=null;
			//user_password=MyKeyer.MyMd5(user_password+userdata.email);
			//assert user_password!=null;
			//使用验证码进行加密

			//对数据进行加密

			//发送post请求
			//new MyHttp().send_Post_String("http://10.120.52.165:8080/email_log"+MyKeyer.MyMd5(userdata.email), "email=" + userdata.email + "&password1=" + userdata.password + "&password2=" + user_password, handler2, CHANGE_UI);
			Log.i("TAG", "zc_zhuce: key=" + user_password);

			HashMap<String,Object> dict = userdata.get_dict_data();
			dict.put("email",userdata.email);
			dict.put("password",userdata.password);
			dict.put("pw2",user_password);
			new MyHttp2(userdata).thread_post_do_data("pw_updata", MyJson.toJson(dict), userdata.net_md5, handler2);

			thread_post_key = false;
		}
	}

}

