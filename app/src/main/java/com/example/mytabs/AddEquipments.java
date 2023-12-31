package com.example.mytabs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddEquipments extends Activity
{
	EditText text1, text2;
	TextView text_view1;
	SetWifi setwifi;
	Long uid;
	boolean tcp_succeed_sign = true;//循环是否继续的判断标志
	boolean succeed_sign = true;//循环是否继续的判断标志
	boolean interval_time_sign = false;//隔一段时间发送一句正在进行
	boolean binding_equipment_sign = true;
	Context context;
	UserData userData;

	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		Log.i("login", "onCreate: 尝试打开获取wifi界面");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_equipments);

		Intent intent = getIntent();
		uid = intent.getLongExtra("uid", 0);

		Log.i("TAG", "onCreate: 收到的uid的值为	" + uid);
		setResult(1);//设置返回值
		text1 = findViewById(R.id.GetWifi_text1);
		text2 = findViewById(R.id.GetWifi_text2);
		text_view1 = findViewById(R.id.GetWifi_text3);
		setwifi = new SetWifi(getApplicationContext());
		Log.i("login", "onCreate: 成功打开获取wifi界面");
		context = AddEquipments.this;

		MySqlLite sql = new MySqlLite(getApplicationContext());
		userData = sql.get_login_data();

	}

	@SuppressLint("SetTextI18n")
	public void GetWifi_button_ok(View v)
	{
		if (!setwifi.WiFi_start())
		{
			Toast.makeText(AddEquipments.this, "请打开wifi", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
			return;
		}


		//验证用户输入的WiFi和密码是否可以连接上wifi。
		//检索wifi列表，查看附近有无设备启动。
		//链接设备的WiFi。
		//发送要 共享wifi 账号和密码。
		//接收设备的信息。芯片id和mac等，用于向服务器查询是否上线，绑定等
		//验证设备上线	//等待 设备的WiFi 关闭	//或者等待服务器的相应。
		final String ssid1 = text1.getText().toString().trim();
		final String password1 = text2.getText().toString().trim();
		final String ssid = "\"" + ssid1 + "\"";
		//final String password="\""+password1+"\"";


		if (!setwifi.wifi_exist(ssid1))//验证周围是否有用户输入的WiFi//验证用户输入的WiFi和密码是否可以连接上wifi。
		{
			Log.e("TAG", "run: 寻找" + ssid + "失败");
			Toast.makeText(AddEquipments.this, "附近未搜索到	" + ssid + "\n", Toast.LENGTH_SHORT).show();
			text_view1.setText(text_view1.getText() + "附近未搜索到	" + ssid + "\n");
			return;
		}
		else if (!setwifi.wifi_exist("HCC_APP"))//检索wifi列表，查看附近有无设备启动。
		{

//			for (int i = 0; i < setwifi.wifi_list.size(); i++)
//			{
//				Log.i("TAG", "wifi " + i + ":" + setwifi.wifi_list.get(i).SSID);
//			}
			Log.e("TAG", "run: 寻找HCC_APP失败");
			//Toast.makeText(AddEquipments.this, "附近未搜索到设备，请确保设备处于待链接状态", Toast.LENGTH_SHORT).show();
			text_view1.setText(text_view1.getText() + "附近未搜索到设备" + "\n");
			new AlertDialog.Builder(this)
					.setTitle("附近未搜索到设备")
					.setMessage("1.请确保节点处于待连接模式。\n2.请在wifi列表中看到 HCC_APP。")
					.setPositiveButton("打开wifi页面", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									Log.i("TAG", "onClick: 已阅");
									startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
								}
							}
					).setNeutralButton("已阅", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Log.i("TAG", "onClick: 已阅2");
						}
					}
			).show();
			return;
		}
		else if (interval_time_sign)
		{
			Toast.makeText(AddEquipments.this, "已开始", Toast.LENGTH_SHORT).show();
			return;
		}

		//不在验证了，控制手机链接wifi在高版本的api中不友好//验证用户输入的WiFi和密码是否可以连接上wifi。
		//setwifi.connect_wifi(ssid1, password1);//尝试链接用户将要共享的WiFi
		setwifi.connect_wifi("HCC_APP", "12345678");//请求用户帮忙链接wifi

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
		{
			final IntentFilter intentFilter;

			intentFilter = new IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
			final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					Log.e("TAG", "onCreate:  ok  1");
					if (!WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION.equals(intent.getAction()))
					{
						return;
					}
					Log.e("TAG", "onCreate:  ok  2");
					//final int status = ((WifiManager) Objects.requireNonNull(getApplicationContext().getSystemService(Context.WIFI_SERVICE))).removeNetworkSuggestions(suggestionsList);
					//Log.e("TAG", "onCreate:  ok 3" + status);
					Toast.makeText(AddEquipments.this, "成功链接，可以返回", Toast.LENGTH_SHORT).show();
					// do post connect processing here...
				}
			};
			getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
			Toast.makeText(AddEquipments.this, "请点击/连接wifi  HCC_APP", Toast.LENGTH_LONG).show();
			startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
		}

		tcp_succeed_sign = true;//手机收到设备消息的标志
		succeed_sign = true;//超时、成功的信号
		interval_time_sign = true;//隔一段时间发一句正在进行

		new Thread()
		{
			@Override
			public void run()
			{
				super.run();
				int i = 10;
				/*
				while (i-- > 0)
				{
					try
					{
						Thread.sleep(1000);
						Log.i("TAG", "run: 尝试链接" + ssid + setwifi.get_wifi_ssid());
						if (ssid.equals(setwifi.get_wifi_ssid()))
						{//当前WiFi和设备共享的WiFi一样
							Message msg = new Message();
							msg.what = 1;//用户输入的WiFi有效
							hander_1.sendMessage(msg);
							break;
						}
					}
					catch (InterruptedException e)
					{
						Log.e("TAG", "run: sleep 失败");
					}
				}
				if (i <= 0)
				{
					succeed_sign = false;
					interval_time_sign = false;
					return;
				}

				//链接设备的WiFi。
				//setwifi.wifi_dis();
				Log.e("TAG", "run: 断开wifi 尝试链接HCC_APP");
				//setwifi.connect_wifi("HCC_APP", "12345678");
				//i = 10;
				 */
				//检测用户是否链接到 HCC_APP 这个wifi
				while (i-- > 0)
				{
					try
					{
						Thread.sleep(10000);
						Log.i("TAG", "run: " + setwifi.get_IP());
						//setwifi.connect_wifi("HCC_APP", "12345678");
						if (setwifi.WiFi_start() && "\"HCC_APP\"".equals(setwifi.get_wifi_ssid()) && setwifi.get_IP() != 0)
						{
							Message msg = new Message();
							msg.what = 2;//链接节点设备的WiFi成功。
							hander_1.sendMessage(msg);
							break;
						}
						else
						{
							//再次发送请求
							setwifi.connect_wifi("HCC_APP", "12345678");
						}
					}
					catch (InterruptedException e)
					{
						Log.e("TAG", "run: sleep 失败");
					}
				}
				if (i <= 0)
				{
					Log.e("TAG", "run: 链接HCC_APP失败");
					succeed_sign = false;
					interval_time_sign = false;
					return;
				}
				while (tcp_succeed_sign && setwifi.get_IP() != 0)
				{
					//发送要 共享wifi 账号和密码。
					MyTcp mytcp1 = new MyTcp(setwifi.get_IP_service(), 9997);

					mytcp1.Tcp_handler_get(hander_1, 0);//接收设备的信息。芯片id和mac等，用于向服务器查询是否上线，绑定等
					//mytcp1.Tcp_handler_get(hander_1,4);

					try
					{
						if (mytcp1.socket1 != null)
						{
							mytcp1.Tcp_send("+UID:" + uid+"\n");
							sleep(100);
							mytcp1.Tcp_send("+SSID:" + ssid1+"\n");
							sleep(100);
							mytcp1.Tcp_send("+PW:" + password1+"\n");
							sleep(100);
						}
						sleep(2000);
						Log.e("TAG", "run: sleep结束1");
					}
					catch (InterruptedException e)
					{
						Log.e("TAG", "run: sleep出错0" + e.getMessage());
					}
					try
					{
						Log.e("TAG", "run: 开始销毁TCP线程");
						mytcp1.finalize();
					}
					catch (Throwable throwable)
					{
						Log.e("TAG", "run: mytcp1销毁出错" + throwable.getMessage());
						break;
					}
				}
				//setwifi.wifi_dis();//断开与节点的链接。
				succeed_sign = false;
				interval_time_sign = false;
				//setwifi.connect_wifi(ssid1, password1);
				Message msg = new Message();
				msg.what = 4;//链接节点设备的WiFi成功。
				hander_1.sendMessage(msg);
			}
		}.start();


		//发送要 共享wifi 账号和密码。
		//验证设备上线	//等待 设备的WiFi 关闭	//或者等待服务器的相应。
	}


	//这个是个和节点通讯的tcp调用的
	@SuppressLint("HandlerLeak")
	private final Handler hander_1 = new Handler()//负责处理按键里的线程
	{
		@SuppressLint("SetTextI18n")
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case -3:
					text_view1.setText(text_view1.getText() + "与设备/节点通讯出错：\n" + msg.obj + '\n');
					break;
				case MyTcp.ERROR_READ:
				case MyTcp.ERROR_SEND:
					text_view1.setText(text_view1.getText() + "与设备/节点通讯出错：\n");
					break;
				case 0:
					text_view1.setText(text_view1.getText() + "与节点通讯成功！\n" + "尝试从服务器获取设备节点信息\n");
					tcp_succeed_sign = false;
					interval_time_sign = false;
					try
					{
						final String str1 = new String((byte[]) msg.obj, "utf-8");
						//text_view1.setText(text_view1.getText()+"	"+str1+"\n");
						Log.e("TAG", "run: 收到节点数据1：\n" + str1);
						text_view1.setText(text_view1.getText() + "来自节点:" + str1 + "\n");
						if (!str1.contains("uid") || !str1.contains("chip_id"))
						{
							return;
						}
						/*在这里解析下面的数据 str1或(byte[])msg.obj
							AT+CIFSR
							+CIFSR:APIP,"192.168.4.1"
							+CIFSR:APMAC,"4a:3f:da:7e:0d:a5"
							id=0x38FFD705,0x4D583734,0x29662043
							succeed!\r\n
						 */
						//因为分包的问题，wifi密码校验和这个信息一起收到了，我需要把他从uid这里切开，只发送uid和chipid到服务器
						//86Y1>64m,uid=1,chip_id=1427060
						new Thread()
						{
							@Override
							public void run()
							{
								super.run();
								int i = 10;
								binding_equipment_sign = true;

								Pattern pattern = Pattern.compile(",(uid=[\\d]+,chip_id=[\\d]+)$");
								Matcher m = pattern.matcher(str1);
								String str2;
								if (!m.find())
								{
									Message msg = new Message();
									msg.what = -3;
									msg.obj = str1;
									hander_1.sendMessage(msg);
									return;
								}
								str2 = m.group(1);

								HashMap<String,Object> dict = userData.get_dict_data();
								pattern = Pattern.compile(",uid=[\\d]+,chip_id=([\\d]+)$");
								m = pattern.matcher(str1);
								assert  m.find():",uid=[\\d]+,chip_id=([\\d]+)$";
								dict.put("chip_id",m.group(1));

								while (binding_equipment_sign && i-- > 0)
								{
									//new MyHttp().thread_send_Post_String(MyHttp.IP + "binding_equipment", str2, hander_2, 1);

									new MyHttp2(userData).thread_post_do_data("binding_equipment",MyJson.toJson(dict) ,userData.net_md5,hander_2);
									try
									{
										Thread.sleep(5000);
									}
									catch (InterruptedException e)
									{
										Log.i("TAG", "run: sleep GG" + e.getMessage());
									}
								}
							}
						}.start();
					}
					catch (UnsupportedEncodingException e)
					{
						text_view1.setText(text_view1.getText() + "	" + msg.obj + "\n");//byte[]对象直接打印，应该不会报错吧？
						Log.e("TAG", "run: 收到节点数据整理时出错：\n" + msg.obj);
					}
					break;
				case 1://用户输入的WiFi有效
					text_view1.setText(text_view1.getText() + "验证WiFi成功\n");
					break;
				case 2://成功连接上设备节点
					text_view1.setText(text_view1.getText() + "成功连接上设备节点\n");
					break;
				case 3://正在进行
					text_view1.setText(text_view1.getText() + "正在进行...\n");
					break;
				case 4:
					text_view1.setText(text_view1.getText() + "与节点通讯成功，请恢复正常的网络状态\n");
					new AlertDialog.Builder(context)
							.setTitle("与节点通讯成功")
							.setMessage("请恢复正常的网络状态")
							.setPositiveButton("打开wifi页面", new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(DialogInterface dialog, int which)
										{
											Log.i("TAG", "onClick: 已阅1");
											startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
										}
									}
							).setNeutralButton("已阅", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									Log.i("TAG", "onClick: 已阅2");
								}
							}
					).show();
					break;
					/*
				case 4://输出节点返回的信息
					text_view1.setText(text_view1.getText() + "来自节点:"+msg.obj.toString()+"\n");
					break;

					 */
			}
		}
	};

	//这个是个和服务器通讯的http调用的
	@SuppressLint("HandlerLeak")
	private final Handler hander_2 = new Handler()
	{
		@SuppressLint("SetTextI18n")
		public void handleMessage(Message msg)
		{
			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Log.e("TAG", "handleMessage: MyHttp error");
				Toast.makeText(getApplicationContext(), "网络异常", Toast.LENGTH_SHORT).show();
				//在这里重新发送请求 ？
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Log.e("TAG", "handleMessage: MyHttp error");
				Toast.makeText(getApplicationContext(), "解析数据错误，请重新登录", Toast.LENGTH_SHORT).show();

			}
			else if (msg.what == 0)
			{
				final String log = (String) msg.obj;
				binding_equipment_sign = false;
				text_view1.setText(text_view1.getText() + "节点已找到\n" + log + "\n\n");
				try{

				new AlertDialog.Builder(context)
						.setTitle("绑定成功")
						.setMessage(log)
						.setPositiveButton("退出绑定页面", new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										Log.i("TAG", "onClick: 退出绑定页面");
										finish();
									}
								}
						).setNeutralButton("再来一个", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								Log.i("TAG", "onClick: 再来一个");
							}
						}
				).show();
				}
				catch (Exception e)
				{
					//用户离开此页面之后，网络请求到达，会触发异常
					e.printStackTrace();
				}
			}
			else if (msg.what > 0)
			{
				text_view1.setText(text_view1.getText() + "服务器：" + msg.obj + "\n");
			}
			else
			{
				text_view1.setText(text_view1.getText() + "服务器：" + msg.obj + "\n");
				//Log.i("TAG", "handleMessage: 服务器返回了不认识的数据emmmm   " + post_data);
			}
		}
	};

}
