package com.example.mytabs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class ZhuCe extends Activity
{
	protected static final int CHANGE_UI = 1, ERROR = -1;
	EditText zc_edit1;// = findViewById(R.id.zc_edit1);//邮箱
	EditText zc_edit2;// = findViewById(R.id.zc_edit2);//密码框框
	EditText zc_edit3;// = findViewById(R.id.zc_edit3);//校验密码框框
	EditText zc_edit4;// = findViewById(R.id.zc_edit4);//验证码框框
	Button zc_button1;//=findViewById(R.id.zc_button1);

	byte send_time_num;
	boolean send_time_key;
	byte post_time_num;
	boolean thread_post_key = true;
	String email_data = null;//储存发送邮件的返回的数据
	String post_data = null;//储存post返回的数据
	String app_password = null;
	String user_password = null;
	String user_password2 = null;
	String jx_password = null;
	UserData userdata;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		Log.i("login", "onCreate: 尝试打开注册界面");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zhuce);
		zc_edit1 = findViewById(R.id.zc_edit1);//邮箱
		zc_edit1.addTextChangedListener(new TextWatcher()
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
				if (JaoYan.jy_email(zc_edit1.getText().toString().trim()))
				{
					zc_edit1.setTextColor(Color.parseColor("#000000"));
				}
				else
				{
					zc_edit1.setTextColor(Color.parseColor("#FF0000"));
				}
			}
		});
		zc_edit2 = findViewById(R.id.zc_edit2);//密码框框
		zc_edit3 = findViewById(R.id.zc_edit3);//校验密吗框框
		zc_edit4 = findViewById(R.id.zc_edit4);//验证码框框
		zc_button1 = findViewById(R.id.zc_button1);
		send_time_key = true;
		userdata = new UserData();
		setResult(0);
		Log.i("login", "onCreate: 成功打开注册界面");
	}

	@SuppressLint("HandlerLeak")//处理发送验证码的数据
	private final Handler handler1 = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.what == CHANGE_UI)
			{
				email_data = msg.obj.toString();//容器储存返回码
				final int end = JaoYan.get_end_id(email_data);
				final String log = JaoYan.get_log_id(email_data);
				if (end > 0)
				{
					if (log != null)
					{
						send_time_key = true;//解除线程锁
						Toast.makeText(ZhuCe.this, log, Toast.LENGTH_SHORT).show();
					}
					else
					{
						Log.e("TAG", "handleMessage: handler1 log =null ");
						Toast.makeText(ZhuCe.this, "服务器返回值异常", Toast.LENGTH_SHORT).show();
					}
				}
				else if (end == 0 && JaoYan.jy_password(log))
				{
					app_password = log;//储存邮箱返回验证码 可用于加密，解密
					Toast.makeText(ZhuCe.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Log.e("TAG", "handleMessage: handler1 end =-1 ");
					Toast.makeText(ZhuCe.this, "服务器返回值异常", Toast.LENGTH_SHORT).show();
				}
			}
			else if (msg.what == ERROR)
			{
				send_time_key = true;//解除线程锁
				Toast.makeText(ZhuCe.this, "失去连接", Toast.LENGTH_SHORT).show();
			}
		}
	};
	@SuppressLint("HandlerLeak")//处理注册post数据
	private final Handler handler2 = new Handler()
	{
		@RequiresApi(api = Build.VERSION_CODES.KITKAT)
		public void handleMessage(Message msg)
		{
			if (msg.what == CHANGE_UI)
			{
				//容器储存post返回数据，可用于登录
				byte[] bytes2 = MyKeyer.keyer_get_bytes((byte[]) msg.obj, jx_password.getBytes());
				post_data = null;
				try
				{
					post_data = new String(bytes2, "utf8");
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}

				if (post_data == null || !post_data.startsWith("end=0"))
				{
					try
					{
						post_data = new String((byte[]) msg.obj, "utf8");
					}
					catch (UnsupportedEncodingException e)
					{
						e.printStackTrace();
						Toast.makeText(ZhuCe.this, "服务器返回了不认识的数据emmmm", Toast.LENGTH_SHORT).show();
						return;
					}
				}

				Log.i("TAG", "验证码输入错误" + post_data);
				final int end = JaoYan.get_end_id(post_data);
				final String log = JaoYan.get_log_id(post_data);
				//在这里添加对post数据的处理语句
				// 判定用户是否邓丽成功
				//并更新用户的请求码

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
						userdata.new_time = Long.parseLong(post_data_item[1]);
						userdata.net_md5 = post_data_item[2];
						userdata.uid = Long.parseLong(post_data_item[3]);
						sqlLite.add_UserData(userdata);
						//返回数据，用户登录
						setResult(1);
						Toast.makeText(ZhuCe.this, "注册成功，自动登录", Toast.LENGTH_SHORT).show();
						//结束这个页面活动
						finish();
					}
				}
				else if (end > 0 && log != null)
				{
					Toast.makeText(ZhuCe.this, log, Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(ZhuCe.this, "服务器返回了不认识的数据emmmm", Toast.LENGTH_SHORT).show();
					Log.i("TAG", "handleMessage: 服务器返回了不认识的数据emmmm   " + post_data);
				}
			}
			else if (msg.what == ERROR)
			{//重新开启线程锁
				Toast.makeText(ZhuCe.this, "网络异常", Toast.LENGTH_SHORT).show();
			}
			thread_post_key = true;
		}
	};

	public void zc_sendemail(View v)
	{

		//判断时间间隔，防止请求过量
		//成功发送邮件之后time_key设置为true
		userdata.email = zc_edit1.getText().toString().trim();
		if (send_time_key && JaoYan.jy_email(userdata.email))
		{
			//拼接url，发送请求
			new MyHttp().thread_send_Post_String(MyHttp.IP + "email_send_zhuce", "email=" + userdata.email, handler1, CHANGE_UI);
			//延时使能按键
			new Thread()
			{
				public void run()
				{
					send_time_num = 60;
					try
					{
						while (send_time_num-- > 0)
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
		else if (send_time_key)
		{
			Toast.makeText(ZhuCe.this, "这个邮箱我看不懂啊&#@%&*", Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(ZhuCe.this, send_time_num + "S后再试", Toast.LENGTH_SHORT).show();
		}
	}

	public void zc_zhuce(View v)
	{
		Log.i("TAG", "run: 点击注册按键");
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
			Toast.makeText(ZhuCe.this, "没有邮箱不可以哦O_O", Toast.LENGTH_SHORT).show();
		}
		else if (!userdata.email.equals(zc_edit1.getText().toString().trim()))//2用户的邮箱发生变化
		{
			Log.i("TAG", "run: 还未发送该邮箱的验证码");
			Toast.makeText(ZhuCe.this, "还未发送该邮箱的验证码!_!", Toast.LENGTH_SHORT).show();
		}
		else if (userdata.password.length() == 0 || zc_edit3.toString().trim().length() == 0)//3用户的密码为空
		{
			Log.i("TAG", "run: 用户密码为空");
			Toast.makeText(ZhuCe.this, "注意安全，你的密码空了!_!", Toast.LENGTH_SHORT).show();
		}
		else if (!userdata.password.equals(user_password2))//4用户两次密码不一样
		{
			Log.i("TAG", "run: 用户输入的两次密码不一致");
			Toast.makeText(ZhuCe.this, "您输入的密码不一致-_-", Toast.LENGTH_SHORT).show();
		}
		else if (user_password.equals(""))//5用户未输入的验证码
		{
			Log.i("TAG", "run: 用户未输入的验证码" + user_password);
			Log.i("TAG", "run: 用户未输入的验证码");
			Toast.makeText(ZhuCe.this, "验证码不见了O_O", Toast.LENGTH_SHORT).show();
		}
		else if (!JaoYan.jy_password(user_password))//6用户输入的验证码非法
		{
			Log.i("TAG", "run: 用户输入的验证码非法");
			Toast.makeText(ZhuCe.this, "在检查一下验证码哦^_^", Toast.LENGTH_SHORT).show();
		}
		else if (email_data.length() == 0)//7APP未收到邮箱的验证码	//可能是服务器的问题
		{//判断邮箱的验证码是否到达
			Log.i("TAG", "run: 验证码APP未收到");
			Toast.makeText(ZhuCe.this, "还未收到邮箱验证码-_-", Toast.LENGTH_SHORT).show();
		}
		else if (!JaoYan.jy_password(app_password))//8APP收到的验证码非法  //可能客户端有人抓包，或者服务器有问题
		{//判断邮箱的验证码是否到达
			Log.i("TAG", "run: APP收到的验证码非法");
			Toast.makeText(ZhuCe.this, "APP收到的验证码非法-_-", Toast.LENGTH_SHORT).show();
		}
		else if (thread_post_key)
		{
			userdata.password = MyKeyer.MyMd5(userdata.password);
			assert userdata.password != null;
			user_password = MyKeyer.MyMd5(user_password + userdata.email);
			assert user_password != null;
			//使用验证码进行加密

			//对数据进行加密

			//发送post请求
			//new MyHttp().send_Post_String("http://10.120.52.165:8080/email_log"+MyKeyer.MyMd5(userdata.email), "email=" + userdata.email + "&password1=" + userdata.password + "&password2=" + user_password, handler2, CHANGE_UI);
			Log.i("TAG", "zc_zhuce: key=" + user_password);
			HashMap<String,Object> dict = new HashMap<>();
			dict.put("email",userdata.email);
			dict.put("password1",userdata.password);
			dict.put("password2",user_password);
			new MyHttp().thread_send_post_bytes(MyHttp.IP + "email_log_zhuce" + MyKeyer.MyMd5(userdata.email), MyKeyer.keyer_get_bytes(MyJson.toJson(dict), user_password), handler2, CHANGE_UI);
			jx_password = user_password;
			thread_post_key = false;
		}
		else
		{
			//Toast.makeText(ZhuCe.this, "正在与服务器叽咕叽咕￥%@##*&……", Toast.LENGTH_SHORT).show();
			Toast.makeText(ZhuCe.this, "改了吗，就来按按按……" + post_time_num + "S", Toast.LENGTH_SHORT).show();
		}
	}

}

