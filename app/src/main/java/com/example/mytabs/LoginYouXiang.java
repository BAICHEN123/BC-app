package com.example.mytabs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.HashMap;


public class LoginYouXiang extends Activity
{
	EditText log_edit1, log_edit2;
	String email_data = null;//储存发送邮件的返回的数据
	String post_data = null;//储存post返回的数据

	String app_password = null;//储存APP验证码
	String user_password = null;//储存用户输入的验证码
	UserData userdata;
	private int send_time_num;
	private boolean send_time_key = true;
	private boolean thread_post_key = true;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		Log.i("login", "onCreate: 尝试打开邮箱登录界面");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginyouxiang);
		log_edit1 = findViewById(R.id.log_edit1);
		log_edit1.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{

			}

			@Override
			public void afterTextChanged(Editable s)
			{
				if (JaoYan.jy_email(log_edit1.getText().toString().trim()))
				{
					log_edit1.setTextColor(Color.parseColor("#000000"));
				}
				else
				{
					log_edit1.setTextColor(Color.parseColor("#FF0000"));
				}
			}
		});
		log_edit2 = findViewById(R.id.log_edit2);
		userdata = new UserData();
		setResult(0);//没有携带任何数据，没有用户登录
		Log.i("login", "onCreate: 成功打开邮箱登录界面");
	}

	public void zhuce(View v)
	{
		//点击注册按键，切换都注册界面
		Log.i("TAG", "zhuce: 尝试打开注册界面1");

		Intent intent = new Intent(LoginYouXiang.this, ZhuCe.class);
		startActivityForResult(intent, 0);//打开注册界面
		Log.i("TAG", "zhuce: 打开注册界面3");
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		setResult(resultCode, data);
		finish();
	}

	@SuppressLint("HandlerLeak")//处理发送验证码
	private final Handler handler_send = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.what == MyHttp2.MyOk)
			{
				email_data = msg.obj.toString();//容器储存返回数据
				final int end = JaoYan.get_end_id(email_data);
				final String log = JaoYan.get_log_id(email_data);
				if (end > 0 && log != null)
				{
					Toast.makeText(LoginYouXiang.this, log, Toast.LENGTH_SHORT).show();
				}
				else if (end == 0 && JaoYan.jy_password(log))
				{
					app_password = log;//储存返回的验证码
					Toast.makeText(LoginYouXiang.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(LoginYouXiang.this, "服务器返回了不认识的数据emmmm", Toast.LENGTH_SHORT).show();
					Log.i("TAG", "handleMessage: 服务器返回了不认识的数据emmmm   " + post_data);
				}
			}
			else if (msg.what == MyHttp.ERROR)
			{
				Toast.makeText(LoginYouXiang.this, "服务器404", Toast.LENGTH_SHORT).show();
			}
		}
	};

	@SuppressLint("HandlerLeak")//处理登录post数据
	private final Handler handler_log = new Handler()
	{
		@RequiresApi(api = Build.VERSION_CODES.KITKAT)
		public void handleMessage(Message msg)
		{
			thread_post_key = true;
			if(msg.what==MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(LoginYouXiang.this, "服务器不要我了>_<", Toast.LENGTH_SHORT).show();
			}
			else if(msg.what==MyHttp2.MyKeyer_ERROR)
			{
				post_data = msg.obj.toString();//容器储存post返回数据，可用于登录
				final int end = JaoYan.get_end_id(post_data);
				final String log = JaoYan.get_log_id(post_data);
				Toast.makeText(LoginYouXiang.this, log, Toast.LENGTH_SHORT).show();
			}
			else if(msg.what==MyHttp2.MyOk)
			{
				post_data = (String)msg.obj;//容器储存post返回数据，可用于登录
				if(!post_data.startsWith("end"))
				{
					Toast.makeText(LoginYouXiang.this, "验证码错误", Toast.LENGTH_SHORT).show();
					return;
				}
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
				if (end == 0)
				{
					String[] post_data_item = post_data.split("&");
					Log.i("TAG", "handleMessage: " + post_data);
					MySqlLite sqlLite = new MySqlLite(getApplicationContext());
					if (post_data_item.length == 4)
					{
						//用户注册之后从未修改过个人信息
						userdata.net_md5 = post_data_item[2];
						userdata.new_time = Long.parseLong(post_data_item[1]);
						userdata.password = "";
						userdata.uid = Long.parseLong(post_data_item[3]);
						Log.i("TAG", "handleMessage: " + userdata.get_str_data());
						Toast.makeText(LoginYouXiang.this, "登录成功", Toast.LENGTH_SHORT).show();
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
						Toast.makeText(LoginYouXiang.this, "登录成功", Toast.LENGTH_SHORT).show();
						//返回数据，用户登录
						userdata.new_time = Long.parseLong(post_data_item[1]);
						userdata.net_md5 = post_data_item[2];
						userdata.uid = Long.parseLong(post_data_item[3]);
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

		}
	};

	//发送邮件的按钮
	public void yx_send(View v)
	{
		//发送验证码
		//判断时间间隔，防止请求过量
		//成功发送邮件之后time_key设置为true
		userdata.email = log_edit1.getText().toString().trim();
		if (userdata.email.equals(""))
		{
			Toast.makeText(LoginYouXiang.this, "邮箱不见了O_O", Toast.LENGTH_SHORT).show();
		}
		else if (!send_time_key)
		{
			Toast.makeText(LoginYouXiang.this, send_time_num + "S后再试^_^,不然服务器君会罢工的", Toast.LENGTH_SHORT).show();
		}
		else if (!JaoYan.jy_email(userdata.email))
		{

			Toast.makeText(LoginYouXiang.this, "这个邮箱我看不懂啊&#@%&*", Toast.LENGTH_SHORT).show();
		}
		else
		{
			//拼接url，发送请求

			new MyHttp().thread_send_Post_String(MyHttp.getServerHttp() + "email_send_login", "email=" + userdata.email, handler_send, MyHttp2.MyOk);

			new Thread()//延时使能按键
			{
				public void run()
				{
					send_time_num = 5;
					try
					{
						while (send_time_num-- > 0 && (email_data == null || !email_data.equals("")))
						{
							Thread.sleep(1000);
						}
						send_time_key = true;
					}
					catch (Exception e)
					{
						Log.i("TAG", "run: 延时失败");
					}
				}
			}.start();
			send_time_key = false;
		}
	}

	//登录按钮
	public void yx_login(View v)
	{
		if (!thread_post_key)
		{
			Toast.makeText(LoginYouXiang.this, "正在与服务器叽咕叽咕￥%@##*&……", Toast.LENGTH_SHORT).show();
			return;
		}


		user_password = log_edit2.getText().toString().trim();
		//处理用户点击登录的事件
		//判定在发送邮件之后是否修改过邮箱
		/*
		0	邮箱为空
		1	邮箱是否修改
		2	APP验证码是否到达
		3	APP验证码是否合法
		4	用户验证码是否为空
		5	用户输入验证码是否合法
		 */
		if (userdata.email == null || userdata.email.equals(""))//0用户邮箱空
		{
			Log.i("TAG", "run: 邮箱为空" + userdata.email);
			Log.i("TAG", "run: 邮箱为空");
			Toast.makeText(LoginYouXiang.this, "没有邮箱不可以哦O_O", Toast.LENGTH_SHORT).show();
		}
		else if (!userdata.email.equals(log_edit1.getText().toString().trim()))//1邮箱被修改
		{
			Log.i("TAG", "yx_login: 还未发送该邮箱的验证码");
			Toast.makeText(LoginYouXiang.this, "还未发送该邮箱的验证码", Toast.LENGTH_SHORT).show();

		}
		else if (user_password == null || user_password.equals(""))//4用户验证码为空
		{
			Log.i("TAG", "yx_login: 用户未输入的验证码");
			Toast.makeText(LoginYouXiang.this, "验证码不见了O_O", Toast.LENGTH_SHORT).show();
		}
		else if (!JaoYan.jy_password(user_password))//5用户输入的验证码非法
		{
			Log.i("TAG", "yx_login: 用户输入的验证码非法");
			Toast.makeText(LoginYouXiang.this, "在检查一下验证码哦^_^", Toast.LENGTH_SHORT).show();
		}
		else if (app_password == null || app_password.equals(""))//2APP验证码未收到
		{
			Log.i("TAG", "yx_login: 验证码APP未收到");
			Toast.makeText(LoginYouXiang.this, "验证码APP未收到", Toast.LENGTH_SHORT).show();
		}
		else if (!JaoYan.jy_password(app_password))//3APP验证码非法
		{
			Log.i("TAG", "yx_login: APP验证码非法");
			Toast.makeText(LoginYouXiang.this, "APP收到的验证码非法-_-", Toast.LENGTH_SHORT).show();
		}
		else
		{
			user_password = MyKeyer.MyMd5(user_password + userdata.email);
			assert user_password != null;
			/*post数据：
				发送：
					发送用户输入的验证码和get邮箱返回的验证码加密结果
					用户的邮箱
				接受：
					name
					head_md5
					head_end
					sex
					net_md5		//用户用于向服务器发起请求的请求码
					new_time 	//服务器最后登录时间
			 */
			//发送post请求
			//new MyHttp().thread_send_Post_String(MyHttp.IP + "email_log_login" + MyKeyer.MyMd5(userdata.email), MyKeyer.keyer_get_bytes("email=" + userdata.email + "&password1=" + user_password + "&password2=" + app_password, user_password), handler_log, CHANGE_UI);
			HashMap<String,Object> dict = new HashMap<>();
			dict.put("email",userdata.email);
			dict.put("password2",app_password);
			new MyHttp2(MyKeyer.MyMd5(userdata.email)).thread_post_str("email_log_login", MyJson.toJson(dict) , user_password, handler_log);
			thread_post_key = false;
		}
	}

}
