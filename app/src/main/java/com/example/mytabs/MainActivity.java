package com.example.mytabs;

import static android.view.View.inflate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;



/*
一些问题/想法
1	自动更新界面上的状态
//2	做一个队列（字典是不是更合适），储存用户输入的状态，等用户情绪稳定之后在向服务器发送
?get//	新建一个倒计时线程，用户在一定时间内未再次按下就发送信息
3	tcp改造，关于设备的控制这里，需要长时间的网络请求，所以单线程的http服务器已经不能维持正常工作的，需要开多线程
//4	选项比较多的条目，使用滑条视图来解决
//5	组合包数据搞一个方向包？
//6	设备命名修改界面
//7	高版本安卓的	wifi 兼容	目前兼容q,我也没有更高级的手机
8	服务器 ip 地址储存在一个网站里，这样就不用每次都修改了
//10	用户将自己的设备共享给其他用户
11	解耦合 扒拉代码太累了
12	取消设备分享
//13	一些工作模式目前只能显示数字，我想显示文字描述
	获取info（与get相同级别），获取设备工作模式id的文字描述相关内容，用于滑条修改模式
	<info
	>@工作模式[0-3]:手动，声控，光控，光声混控

	目前进度：基本实现，可以一边滑动一般显示模式的名字。
		但是有个小问题，就是在他刷新的时候闪烁的一下会出现数字，而不是模式的名字
		原因分析：对设备发送指令走的 hander_2 通道 总是会调用设备数据的清除函数 clear_dataitem 和 info获取函数，导致整个数据被全部重新创建一次
				短暂的数字闪烁是因为新建立的数据还没有收到info
		处理方案：	1、重新规定数据修改的协议，确定 *好要不要* 留下实时修改数据条目内容的功能
					2、分离info数组和数据条目数组，分离get指令和set指令的 hander_x
14	每个一小段时间自动刷新的，但是要搞很多控制的，比如刚刚在修改东西的时候要禁止刷新，先规划一下在搞吧
15	节点设备添加一个刷新按钮，点击此按钮，单独刷新这个设备
		@刷新[0-0]:0
 */


public class MainActivity extends AppCompatActivity
{
	//页面请求吗
	static final int FIG_USER_LOGIN_ = 0;//用户登录页面
	static final int FIG_USER_DATA_SET_ = 1;//用户信息修改页面
	static final int FIG_WIFI_PERMISSIONS_ = 2;//获取WiFi权限页面
	static final int FIG_ADD_E_ = 3;//添加设备页面
	static final int FIG_E_RENAME_ = 4;//修改设备名称的页面
	static final int FIG_PW_UPDATA_ = 5;//修改密码页面
	static final int FIG_SHARE_E_ = 6;//设备分享页面
	static final int FIG_ADD_LD_ = 7;//设备分享页面
	static final int FIG_OPEN_WIFI_ = 8;//设置页面打开wifi
	static final int FIG_E_CLICK_NAME_VIEW_ = 9;//点击设备名字出来的页面


	UserData userdata;
	ExpandableListView expandableListView;
	ExpandableListView ldexpandableListView;
	MyExpandableListAdapter myExpandableListAdapter;//https://www.jianshu.com/p/05df9c17a1d8
	LdExpandableListAdapter ldadapter;
	View other_view;
	View liandong_view;
	MySqlLite sql;
	MyPagerAdapter myPagerAdapter;
	MyFloatingActionButton fab;
	ArrayList<ALdData> lddatas;

	//protected static final int CHANGE_UI = 0, ERROR = -1;
	@SuppressLint("Recycle")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		//textView.setText(getResources().getString(R.string.window1));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//给左右滑动页面嵌套列表视图，实现下拉刷新。
		//标签页面的页面的数据视图转换工具类
		myPagerAdapter = new MyPagerAdapter();
		//viewPager标签页面 的设置
		View view;

		//设备页面占位
		view = inflate(MainActivity.this, R.layout.equipment_list, null);
		expandableListView = view.findViewById(R.id.equipment_list_ExpandableListView);

		//填充数据
		myExpandableListAdapter = new MyExpandableListAdapter(MainActivity.this);
		expandableListView.setAdapter(myExpandableListAdapter);
		myPagerAdapter.viewLists.add(view);

		lddatas = new ArrayList<>();
		ldadapter = new LdExpandableListAdapter(MainActivity.this, lddatas, myExpandableListAdapter.myEquipments);
		//关于这里有个很扯的错误，用 getApplicationContext() 获取的 context 无法正常构建弹出窗口


		//联动页面的添加，注意，要按顺序
		liandong_view = inflate(MainActivity.this, R.layout.equipment_list, null);
		ldexpandableListView = liandong_view.findViewById(R.id.equipment_list_ExpandableListView);
		ldexpandableListView.setAdapter(ldadapter);
		myPagerAdapter.viewLists.add(liandong_view);

		//用户页面占位
		other_view = inflate(MainActivity.this, R.layout.other_activity, null);
		myPagerAdapter.viewLists.add(other_view);


		//标签页面的页面
		ViewPager viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(myPagerAdapter);

		//标签页面的标签
		TabLayout tabs = findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);
		Log.i("TAG", "onCreate:mainActivity fab 0");
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
//		Log.i("TAG", "onTouchEvent: point.getWidth  " + point.x);
//		Log.i("TAG", "onTouchEvent: point.getHeight  " + point.y);
//		Log.i("TAG", "onTouchEvent: expandableListView.getWidth  " + other_view.getWidth());
//		Log.i("TAG", "onTouchEvent: expandableListView.getHeight  " + other_view.getHeight());

		fab = findViewById(R.id.fab);
		fab.setMAX(point.x, point.y);//限制可移动的范围
		final GuidePageChangeListener onTabSelectedListener = new GuidePageChangeListener(fab);
		tabs.addOnTabSelectedListener(onTabSelectedListener);
		//从本地数据库查询用户登录信息
		Log.i("TAG", "onCreate:mainActivity数据库 1");


		userdata = null;
		sql = new MySqlLite(getApplicationContext());
		userdata = sql.get_login_data();
		if (userdata == null || userdata.email == null || userdata.email.equals(""))
		{
			//查询数据库，没有用户登录过
			Toast.makeText(MainActivity.this, "没有用户登录", Toast.LENGTH_SHORT).show();
			Intent intent;
			intent = new Intent(MainActivity.this, Login.class);
			Log.i("TAG", "onCreate:mainActivity数据库6");
			startActivityForResult(intent, FIG_USER_LOGIN_);//打开登陆界面
		}
		else
		{
			Log.i("TAG", "onCreate: mainActivity数据库7" + userdata.get_str_data());
			//查询到最后一次登录的用户名
			//userdata = sql.get_login_data();
			Log.i("TAG", "onCreate: mainActivity数据库8 用户本地信息加载完毕" + userdata.get_str_data());
			//Toast.makeText(MainActivity.this, "用户本地信息加载完毕", Toast.LENGTH_SHORT).show();
			new MyHttp2(userdata).thread_post_do_data("get_my_equipment", userdata.get_str_data(), userdata.net_md5, hander_1);
			new MyHttp2(userdata).thread_post_do_data("ld_select", userdata.get_str_data(), userdata.net_md5, ldadapter.hander_ld);

			Log.i("TAG", "onCreate: mainActivity数据库9数据库完毕" + userdata.get_str_data());
		}


		//右下角的图标
		fab.setOnClickListener(view1 ->
							   {
								   //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
								   //Log.i("TAG", "onCreate:setOnClickListener");
								   int tab_page = onTabSelectedListener.get_fig();
								   if (tab_page == 0)
								   {
									   //Log.i("TAG", "onCreate:setOnClickListener	tab_page ==0");
									   //频繁点击刷新按键会导致程序崩溃，将这里几行注释掉之后就没事了。原因推断：绘制图像的时候刷新，数据被清除掉，然后绘制失败了，就崩溃了
//				for (MyEquipment me : myExpandableListAdapter.myEquipments)
//				{
//					//me.user_data = new ArrayList<>();//刷新之前清除掉用户修改的记录
//					me.clear_dataitem();
//				}
									   new MyHttp2(userdata).thread_post_do_data("get_my_equipment", userdata.get_str_data(), userdata.net_md5, hander_1);
									   new MyHttp2(userdata).thread_post_do_data("ld_select", userdata.get_str_data(), userdata.net_md5, ldadapter.hander_ld);
									   //Log.i("TAG", "onCreate:setOnClickListener	tab_page ==0  1");
								   }
								   else if (tab_page == 1)
								   {
									   Log.i("TAG", "onCreate:setOnClickListener  tab_page == 1");

									   Intent intent;
									   intent = new Intent(MainActivity.this, AddLD.class);
									   startActivityForResult(intent, FIG_ADD_LD_);//新建联动
									   Log.i("TAG", "onCreate:setOnClickListener  tab_page == 1   end");
								   }
								   else if (tab_page == 2)
								   {
									   //不同的页面 原先的刷新按键放入不同的功能
									   Intent intent;
									   intent = new Intent(MainActivity.this, ShareGetOrFor.class);
									   //Log.i("TAG", "onCreate:setOnClickListener	1");
									   long[] list_eid = new long[myExpandableListAdapter.myEquipments.size()];
									   String[] list_name = new String[myExpandableListAdapter.myEquipments.size()];

									   //Log.i("TAG", "onCreate:setOnClickListener	2");
									   for (int i = 0; i < myExpandableListAdapter.myEquipments.size(); i++)
									   {
										   list_eid[i] = myExpandableListAdapter.myEquipments.get(i).eid;
										   list_name[i] = myExpandableListAdapter.myEquipments.get(i).name;
									   }
									   //Log.i("TAG", "onCreate:setOnClickListener	3");
									   intent.putExtra("list_eid", list_eid);
									   intent.putExtra("list_name", list_name);
									   //Log.i("TAG", "onCreate:setOnClickListener	4");
									   startActivityForResult(intent, FIG_SHARE_E_);//打开分享页面

								   }
							   });
		ldadapter.set_userdata(userdata);
		Log.i("TAG", "onCreate: onCreate完毕");
	}

	/*
	获取此用户绑定的所有的设备的信息
	负责处理按键里的线程
	*/

	private final Handler hander_1 = new Handler(Looper.getMainLooper())
	{
		public void handleMessage(Message msg)
		{
			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(MainActivity.this, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(MainActivity.this, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == 0)
			{
				//#温度:28#湿度:68#@灯0[0-1]:1#@灯1[0-1]:1#
				//msg.what 是视图的编号，从0开始
				//刷新设备列表
				String log = (String) msg.obj;
				int i = 0;
				assert log != null;
				for (String str_i : log.split("#+"))
				{
					Log.e("TAG", "handleMessage: " + str_i);
					if (str_i != null && !"".equals(str_i))
					{
						if (i < myExpandableListAdapter.myEquipments.size())
						{
							myExpandableListAdapter.myEquipments.set(i, new MyEquipment(str_i));
						}
						else
						{
							myExpandableListAdapter.myEquipments.add(new MyEquipment(str_i));
						}
						i++;
					}
				}
				new_data();
			}
			else if (msg.what > 0)
			{
				Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				myExpandableListAdapter.myEquipments.clear();
			}

			myExpandableListAdapter.notifyDataSetChanged();
			ldadapter.notifyDataSetChanged();

		}
	};

	//刷新已经有的设备的数据
	private void new_data()
	{
		//刷新设备列表视图
		for (int k = 0; k < myExpandableListAdapter.myEquipments.size(); k++)
		{
			HashMap<String, Object> dict = userdata.get_dict_data();
			dict.put("eid", myExpandableListAdapter.myEquipments.get(k).eid);
			new MyHttp2(userdata).thread_post_str("get_equipment_data", MyJson.toJson(dict), userdata.net_md5, hander_2, k);
		}
	}


	/*
	获取 eid = msg.what 的设备的传感器等信息，并刷新界面显示
	 */

	private final Handler hander_2 = new Handler(Looper.getMainLooper())//负责处理按键里的线程
	{
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
			else
			{
				//#温度:28#湿度:68#@灯0[0-1]:1#@灯1[0-1]:1#
				//msg.what 是视图的编号，从0开始
				String str_data = (String) msg.obj;
				Log.e("TAG", "handleMessage: MyHttp ok\n" + str_data);
				final int end = JaoYan.get_end_id(str_data);
				final String log = JaoYan.get_log_id(str_data);
				if (end > 0 && log != null)
				{
					Toast.makeText(MainActivity.this, log, Toast.LENGTH_SHORT).show();
				}
				else if (end == 0 && log != null)
				{
					if ("SET error 2 越权".equals(log))
					{
						Toast.makeText(MainActivity.this, "未查询到您绑定的设备", Toast.LENGTH_SHORT).show();
						myExpandableListAdapter.myEquipments.clear();
					}
					else
					{
						if (myExpandableListAdapter.myEquipments.get(msg.what).addData_(log) == MyEquipment.NEED_INFO)
						{
							//new MyHttp().thread_send_Post_String(MyHttp.IP + "get_equipment_info", userdata.get_str_data().toString() + "eid=" + myExpandableListAdapter.myEquipments.get(msg.what).eid, hander_3, msg.what);
							HashMap<String, Object> dict = userdata.get_dict_data();
							dict.put("eid", myExpandableListAdapter.myEquipments.get(msg.what).eid);
							new MyHttp2(userdata).thread_post_str("get_equipment_info", MyJson.toJson(dict), userdata.net_md5, hander_3, msg.what);

						}
					}
				}
				else
				{
					Toast.makeText(MainActivity.this, "服务器返回了不认识的数据emmmm", Toast.LENGTH_SHORT).show();
					Log.i("TAG", "handleMessage: 服务器返回了不认识的数据emmmm   " + str_data);
				}

			}
			myExpandableListAdapter.notifyDataSetChanged();
			ldadapter.notifyDataSetChanged();
		}
	};


	/*
	获取 eid = msg.what 的设备的状态值对应的 info 详细描述，并刷新界面显示
	 */

	private final Handler hander_3 = new Handler(Looper.getMainLooper())//负责处理按键里的线程
	{
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
			else
			{
				//msg.what 是视图的编号，从0开始
				String str_data = (String) msg.obj;
				Log.e("TAG", "handleMessage: MyHttp ok\n" + str_data);
				final int end = JaoYan.get_end_id(str_data);
				final String log = JaoYan.get_log_id(str_data);
				if (end > 0 && log != null)
				{
					Toast.makeText(MainActivity.this, log, Toast.LENGTH_SHORT).show();
					//info允许为空
					//我随意填充点儿数据，防止他一直进行info请求
					myExpandableListAdapter.myEquipments.get(msg.what).set_info_isnull();
				}
				else if (end == 0 && log != null)
				{
					if ("SET error 2 越权".equals(log))
					{
						Toast.makeText(MainActivity.this, "未查询到您绑定的设备", Toast.LENGTH_SHORT).show();
						myExpandableListAdapter.myEquipments.clear();
					}
					else
					{
						myExpandableListAdapter.myEquipments.get(msg.what).addInfo_(log);
					}
					myExpandableListAdapter.notifyDataSetChanged();
					ldadapter.notifyDataSetChanged();
				}
				else
				{
					Toast.makeText(MainActivity.this, "服务器返回了不认识的数据emmmm", Toast.LENGTH_SHORT).show();
					Log.i("TAG", "handleMessage: 服务器返回了不认识的数据emmmm   " + str_data);
				}
			}
		}

	};


	/*
	删除一个设备之后，刷新全局
	 */

	private final Handler hander_4 = new Handler(Looper.getMainLooper())//负责处理按键里的线程
	{
		public void handleMessage(Message msg)
		{


			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(MainActivity.this, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(MainActivity.this, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what >= 0)
			{
				String str_data = (String) msg.obj;
				myExpandableListAdapter.myEquipments.clear();
				lddatas.clear();
				Toast.makeText(MainActivity.this, str_data, Toast.LENGTH_SHORT).show();
				new MyHttp2(userdata).thread_post_do_data("get_my_equipment", userdata.get_str_data(), userdata.net_md5, hander_1);
				new MyHttp2(userdata).thread_post_do_data("ld_select", userdata.get_str_data(), userdata.net_md5, ldadapter.hander_ld);

			}
		}

	};


	//打开其他页面的监听函数
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{

		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case FIG_USER_LOGIN_:
				if (resultCode == 1)//有账号登录
				{
					//获取到登录界面返回的信息
					Log.i("TAG", "onActivityResult: 处理登录界面返回的消息");
					userdata = null;
					userdata = sql.get_login_data();
					Log.i("TAG", "onActivityResult: get_login_data" + userdata.get_str_data());

					ldadapter.set_userdata(userdata);

					myExpandableListAdapter.myEquipments.clear();
					lddatas.clear();
					myExpandableListAdapter.notifyDataSetChanged();
					ldadapter.notifyDataSetChanged();
					new MyHttp2(userdata).thread_post_do_data("get_my_equipment", userdata.get_str_data(), userdata.net_md5, hander_1);
					new MyHttp2(userdata).thread_post_do_data("ld_select", userdata.get_str_data(), userdata.net_md5, ldadapter.hander_ld);
					Log.i("TAG", "onActivityResult: 收到登录信息");
					return;
				}
				//没有任何账户登录，且原来也没有账户登录
				else if (userdata == null || userdata.email == null || userdata.email.equals(""))
				{
					finish();
					System.exit(0);
					return;
				}
				else
				{
					return;
				}

			case FIG_USER_DATA_SET_:
				if (resultCode == 1 && data != null)
				{
					userdata.name = data.getStringExtra("name");
					userdata.email = data.getStringExtra("email");
					userdata.sex = data.getByteExtra("sex", userdata.sex);
					userdata.user_head = data.getStringArrayExtra("user_head");
				}
				return;
			case FIG_WIFI_PERMISSIONS_:
			case FIG_PW_UPDATA_:

				break;
			case FIG_SHARE_E_:
				if (resultCode == 1)
				{
					new MyHttp2(userdata).thread_post_do_data("ld_select", userdata.get_str_data(), userdata.net_md5, ldadapter.hander_ld);
				}
				//这里不要break，联合下面的刷新设备，一起工作
			case FIG_ADD_E_:
			case FIG_E_RENAME_:
			case FIG_E_CLICK_NAME_VIEW_:
				if (resultCode == 1)
				{
					new MyHttp2(userdata).thread_post_do_data("get_my_equipment", userdata.get_str_data(), userdata.net_md5, hander_1);
				}
				return;
			case FIG_ADD_LD_:
				if (resultCode == 1)
				{
					new MyHttp2(userdata).thread_post_do_data("ld_select", userdata.get_str_data(), userdata.net_md5, ldadapter.hander_ld);
				}
				break;
			case FIG_OPEN_WIFI_:
				if (new SetWifi(getApplicationContext()).WiFi_start())
				{
					Intent intent;
					intent = new Intent(MainActivity.this, AddEquipments.class);
					Log.i("TAG", "new_equipment: " + userdata.uid);
					intent.putExtra("uid", userdata.uid);
					startActivityForResult(intent, FIG_ADD_E_);//打开界面
				}
				else
				{
					Toast.makeText(MainActivity.this, "需要打开wifi才能开始设备的添加", Toast.LENGTH_SHORT).show();
					//startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS),FIG_OPEN_WIFI_);
				}
				break;
		}
	}

	//“我的信息”按键
	public void other_but1(View v)
	{
		Intent intent;
		intent = new Intent(MainActivity.this, LogData.class);
		intent.putExtra("email", userdata.email);
		intent.putExtra("sex", userdata.sex);
		intent.putExtra("name", userdata.name);
		intent.putExtra("user_head", userdata.user_head);
		startActivityForResult(intent, FIG_USER_DATA_SET_);//打开用户信息界面
	}

	//"切换账户"按键
	public void other_but2(View v)//切换用户，其实和用户登录作用一样，在接收函数的处理里会判断用户是否相同
	{
		Intent intent = new Intent(MainActivity.this, Login.class);
		//用户登陆
		startActivityForResult(intent, FIG_USER_LOGIN_);//打开登陆界面
	}

	//注销/退出登录
	public void other_but3(View v)
	{
		Log.i("TAG", "onOptionsItemSelected: 注销");
		if (userdata == null || "".equals(userdata.email) || userdata.email == null)
		{
			Toast.makeText(MainActivity.this, "还没有用户登录哦", Toast.LENGTH_SHORT).show();
		}
		else
		{
			userdata = new UserData();
			sql.exit_user();//调用一下这个，下次打开不自动登录
			Toast.makeText(MainActivity.this, "拜拜喽。（已注销）", Toast.LENGTH_SHORT).show();
		}
		//注销之后没有账户，就结束程序
		finish();
		System.exit(1);
	}

	public void pw_updata(View v)
	{
		Log.i("TAG", "pw_updata: in");
		Intent intent;
		intent = new Intent(MainActivity.this, PwUpdata.class);
		startActivityForResult(intent, FIG_PW_UPDATA_);
		Log.i("TAG", "pw_updata: out");
	}


	//申请权限回调函数
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
		{
			Log.e("TAG", "onRequestPermissionsResult: 获取权限失败");
		}
		else
		{
			new_equipment(null);
		}
	}

	void open_get_Permissions()
	{
		Log.e("TAG", "open_get_Permissions: 1");
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE}, FIG_WIFI_PERMISSIONS_);
		Log.e("TAG", "open_get_Permissions: 2");
	}

	//添加新设备
	public void new_equipment(View v)
	{
		//Toast.makeText(MainActivity.this, "你点击了添加新设备", Toast.LENGTH_SHORT).show();
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
		{
			//未授权，申请授权
			new AlertDialog.Builder(this)
					.setTitle("关于权限用途")
					.setMessage("获取WiFi的控制权是为了连接节点设备，获取定位权限是为了获取wifi列表。并不会以此定位您的位置信息，wifi信息只会与您要绑定的设备共享，程序不会收集、储存您wifi信息和定位信息，更不会将这些上传到服务器。")
					.setPositiveButton("已阅", (dialog, which) -> open_get_Permissions()
					).show();
		}
		else if (new SetWifi(getApplicationContext()).WiFi_start())
		{
			Intent intent;
			intent = new Intent(MainActivity.this, AddEquipments.class);
			Log.i("TAG", "new_equipment: " + userdata.uid);
			intent.putExtra("uid", userdata.uid);
			startActivityForResult(intent, FIG_ADD_E_);//打开界面
		}
		else
		{
			Toast.makeText(MainActivity.this, "请打开wifi\n(用于向节点发送数据)", Toast.LENGTH_SHORT).show();
			startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), FIG_OPEN_WIFI_);
		}

	}


	/*
	标签页面的数据视图类
	左右滑动
	 */
	class MyPagerAdapter extends PagerAdapter
	{
		ArrayList<View> viewLists;//储存页面的视图//务必初始化
		String[] titile_name = {"设备", "联动", "其他", "4"};//储存页面视图的标题

		public MyPagerAdapter()
		{
			super();
			this.viewLists = new ArrayList<>();
		}

		@Override
		public int getCount()
		{
			//return viewLists.size();
			return 3;
		}

		@Override
		public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
		{
			//这里不修改，无法加载页面//原来是默认返回false
			return view == object;
		}


		@NonNull
		@Override
		public Object instantiateItem(@NonNull ViewGroup container, int position)
		{
			View view;
			//所有的页面都是预加载的，不是当即切换当即加载。
			Log.i("TAG", "instantiateItem: " + position);
			//视图的加载
			switch (position)
			{
				case 0:
					view = inflate(MainActivity.this, R.layout.equipment_list, null);
					expandableListView = view.findViewById(R.id.equipment_list_ExpandableListView);
					//给填充数据
					expandableListView.setAdapter(myExpandableListAdapter);
					viewLists.set(0, view);
					container.addView(view);
					return view;
				//break;

				case 1:
					viewLists.set(1, liandong_view);
					container.addView(liandong_view);
					return liandong_view;

				case 2:
					viewLists.set(2, other_view);
					container.addView(other_view);
					return other_view;
				//break;


				default:
					TextView textview1 = new TextView(getApplicationContext());
					textview1.setText("此页面空");
					container.addView(textview1);
					//viewLists.set(1,textview1);
					return textview1;
				//break;


			}
		}

		public void destroyItem(ViewGroup container, int position, @NonNull Object object)
		{
			container.removeView(viewLists.get(position));
			//如果这里报错了，不是这里的问题，而是页面缺少的问题，在哈希表里没有储存id对应的视图对象
		}

		@Nullable
		@Override
		public CharSequence getPageTitle(int position)
		{
			return titile_name[position];
		}

	}

	/*
	可以收起的列表视图的Adapter
	 */
	public class MyExpandableListAdapter extends BaseExpandableListAdapter
	{
		Context context;
		ArrayList<MyEquipment> myEquipments;
		ArrayList<TextView> textView2_s;
		/*
		建立一个线程，对time_onclick_back_ms进行倒计时，等于0的时候就发送更新请求，并将此值置为 -1
		 */
		int time_onclick_back_ms;
		final int TIME_OUT = 900;//ms
		final int TIME_OUT_I = 50;//ms//每隔多少ms检查一次变量
		boolean thread_exit;

		public MyExpandableListAdapter(Context context)
		{
			this.context = context;
			this.myEquipments = new ArrayList<>();
			this.time_onclick_back_ms = 0;
			this.thread_exit = false;
			this.textView2_s = new ArrayList<>();

		}

		//用户操作倒计时线程，用户操作完大约 TIME_OUT ms之后就向服务器提交所有的操作，转发给单片机。用户没操作一次就刷新一次倒计时
		private void start_thread()
		{
			time_onclick_back_ms = TIME_OUT;//每调用一次此函数就刷新一次倒计时
			if (thread_exit)
			{
				return;
			}
			thread_exit = true;
			new Thread()
			{
				public void run()
				{
					while (time_onclick_back_ms > 0)
					{
						while (time_onclick_back_ms > TIME_OUT)
						{
							try
							{
								sleep(TIME_OUT_I);
							} catch (InterruptedException e)
							{
								Log.e("TAG", "run: wait" + e.getMessage());
							}
						}
						try
						{
							//wait(TIME_OUT_I);
							sleep(TIME_OUT_I);
							time_onclick_back_ms = time_onclick_back_ms - TIME_OUT_I;
						} catch (InterruptedException e)
						{
							Log.e("TAG", "run: sleep" + e.getMessage());
						}
					}
					time_onclick_back_ms = -1;
					Message msg = new Message();
					msg.what = 0;
					hander_2_.sendMessage(msg);
					//整理用户最近改变的数据
					int k = 0;
					for (MyEquipment myEquipment : myEquipments)
					{
						String str_data = myEquipment.get_String_data();
						if (str_data != null)
						{
							Log.e("TAG", "run: " + str_data);
							//msg = new MyHttp().send_Post_String(MyHttp.IP + "set_equipment_data", str_user_data + "eid=" + myEquipment.eid + ",send=" + str_data, k);
							HashMap<String, Object> dict = userdata.get_dict_data();
							dict.put("eid", myEquipment.eid);
							dict.put("send", str_data);
							msg = new MyHttp2(userdata).post_str("set_equipment_data", MyJson.toJson(dict), userdata.net_md5, k);
							if (msg.what == MyHttp2.MyHttp_ERROR)
							{
								//网络错误重新发送
								Log.e("TAG", "run: MyHttp_ERROR");
								continue;
							}
							else if (msg.what == MyHttp2.MyKeyer_ERROR)
							{
								//身份过期直接告知重新登录
								Log.e("TAG", "run: MyKeyer_ERROR");
								hander_2_.sendMessage(msg);
								return;
							}
							//这里是成功了

							str_data = (String) msg.obj;
							Log.e("TAG", "handleMessage: MyHttp ok\n" + str_data);
							final int end = JaoYan.get_end_id(str_data);
							final String log = JaoYan.get_log_id(str_data);
							if (end == 0 && log != null)
							{
								Log.e("TAG", "run: end==0&&log!=null         1");
								if (myEquipment.addData_(log) == MyEquipment.NEED_INFO)
								{
									Log.e("TAG", "run: end==0&&log!=null        2");
									//new MyHttp().thread_send_Post_String(MyHttp.IP + "get_equipment_info", userdata.get_str_data().toString() + "eid=" + myExpandableListAdapter.myEquipments.get(msg.what).eid, hander_3, msg.what);

									HashMap<String, Object> dict1 = userdata.get_dict_data();
									dict1.put("eid", myEquipment.eid);
									new MyHttp2(userdata).thread_post_str("get_equipment_info", MyJson.toJson(dict1), userdata.net_md5, hander_3, msg.what);

								}
								//msg.what = 0;
								//hander_2_.sendMessage(msg);
							}
							else
							{
								msg.what = end;
								Log.e("TAG", "run: end!=0 || log==null");
								hander_2_.sendMessage(msg);
							}
						}
						k = k + 1;
					}
					thread_exit = false;
					time_onclick_back_ms = TIME_OUT;
					msg = new Message();
					msg.what = 0;
					hander_2_.sendMessage(msg);
				}
			}.start();
		}


		private final Handler hander_2_ = new Handler(Looper.getMainLooper())//负责处理按键里的线程
		{
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
				else if (msg.what > 0)
				{
					Log.e("TAG", "handleMessage: msg.what>0" + msg.obj);
					Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
				}
				else if (msg.what == 0)
				{
					Log.e("TAG", "handleMessage: msg.what==0");
					myExpandableListAdapter.notifyDataSetChanged();
					ldadapter.notifyDataSetChanged();
				}

			}
		};


		@Override
		public int getGroupCount()
		{
			return this.myEquipments.size();
		}

		@Override
		public int getChildrenCount(int groupPosition)
		{
			if (this.myEquipments.size() == 0)
			{
				return 0;
			}
			return this.myEquipments.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition)
		{
			return this.myEquipments.get(groupPosition).name;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition)
		{
			return this.myEquipments.get(groupPosition).get_item_name(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition)
		{
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition)
		{
			return childPosition;
		}

		@Override
		public boolean hasStableIds()
		{
			return false;//有稳定的ID？
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
		{
			View view;
			//Log.i("TAG", "getGroupView: :	1");
			if (convertView == null)
			{
				view = inflate(MainActivity.this, R.layout.item_number_title, null);
			}
			else
			{
				view = convertView;
			}
			//Log.i("TAG", "getGroupView: :	2");

			//Log.i("TAG", "getGroupView: groupPosition:	" + groupPosition);
			//Log.i("TAG", "getGroupView: this.myEquipments.size:	" + this.textView2_s.size());
			//Log.i("TAG", "getChildView: this.myEquipments.get(groupPosition).data_title.size:	"+this.myEquipments.get(groupPosition).data_title.size());

			//Log.i("TAG", "getGroupView: :	3");
			TextView textView1 = view.findViewById(R.id.item_title_number);
			TextView textView2 = view.findViewById(R.id.item_title_status);
			//添加状态显示栏的对象到列表
			if (this.textView2_s.size() <= groupPosition)
			{
				this.textView2_s.add(textView2);
			}
			else
			{
				this.textView2_s.set(groupPosition, textView2);
			}
			textView1.setText(String.valueOf(groupPosition + 1));
			//因为全局的操作倒计时只有一组，所以当对任何一个设备操作的时候会导致所有的设备进入倒计时
			if (time_onclick_back_ms >= 0 && thread_exit)
			{
				textView2.setText("等待");
			}
			else if (time_onclick_back_ms >= 0)
			{
				MyEquipment e_item = this.myEquipments.get(groupPosition);
				textView2.setText(e_item.getStatusString());
			}
			else
			{
				textView2.setText("更新...");
			}
			//Log.i("TAG", "getGroupView: :	4");
			Button equipment_name_button = view.findViewById(R.id.item_title_data);
			equipment_name_button.setText(this.myEquipments.get(groupPosition).name);
			//Log.i("TAG", "getGroupView: :	5");
			final int i = groupPosition;
			equipment_name_button.setOnClickListener(v ->
													 {
//				Log.e("TAG", "按键的id为: " + i);
//				Log.e("TAG", "名字: " + myEquipments.get(i).name);
//				Log.e("TAG", "eid: " + myEquipments.get(i).eid);
														 //在这里插入修改硬件名称的函数
														 Intent intent = new Intent(MainActivity.this, EquipmentInfoActivity.class);
														 intent.putExtra("name", myEquipments.get(i).name);
														 intent.putExtra("eid", myEquipments.get(i).eid.toString());
//				intent.putExtra("uid", String.valueOf(userdata.uid));
//				startActivityForResult(intent, FIG_E_RENAME_);//打开界面
														 startActivityForResult(intent, FIG_E_CLICK_NAME_VIEW_);//打开界面
													 });
			//Log.i("TAG", "getGroupView: :	6");

			equipment_name_button.setOnLongClickListener(new View.OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					new AlertDialog.Builder(context)
							.setTitle("删除?")
							.setMessage("删除不仅会删除此设备，还会删除由此账户创建的和此设备直接相关的联动")
							.setNeutralButton("删除", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									//new MyHttp().thread_send_Post_String(MyHttp.IP + "equipment_delete", userdata.get_str_data() + "data=" + myEquipments.get(i).eid, hander_4, 1);

									HashMap<String, Object> dict = userdata.get_dict_data();
									dict.put("data", myEquipments.get(i).eid);
									new MyHttp2(userdata).thread_post_do_data("equipment_delete", MyJson.toJson(dict), userdata.net_md5, hander_4);
									//Log.e("TAG", "onClick: ", );
								}
							})
							.setNegativeButton("取消", null)
							.show();
					return true;
				}
			});

			ImageView imageView = view.findViewById(R.id.open_items_image);
			if (isExpanded)
			{
				imageView.setImageResource(R.drawable.expander_close_holo_light);
			}
			else
			{
				imageView.setImageResource(R.drawable.expander_open_holo_light);
			}
			//Log.i("TAG", "getGroupView: :	7");
			return view;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
		{
			View view = null;
			/*
			Log.i("TAG", "getChildView: groupPosition:	"+groupPosition);
			Log.i("TAG", "getChildView: childPosition:	"+childPosition);
			Log.i("TAG", "getChildView: this.myEquipments.size:	"+this.myEquipments.size());
			Log.i("TAG", "getChildView: this.myEquipments.get(groupPosition).data_title.size:	"+this.myEquipments.get(groupPosition).data_title.size());
			Log.i("TAG", "getChildView: :	1");
			 */
			//对数据的名字进行分析，如果‘@灯0[0-1]:1’，就是可以操作的对象，设置成带有控制的视图，原数据修改值之后向服务器发送set请求
			//final int k = groupPosition;//组编号//设备号
			//final int k1 = childPosition;//子项编号//条目号
			final MyEquipment myEquipment = myExpandableListAdapter.myEquipments.get(groupPosition);
			final EquipmentDataItem itemdata = myEquipment.get_item_data(childPosition);
			if (itemdata != null && itemdata.ke_yi_xiu_gai())//可以修改数值的条目
			{
				if (itemdata.ht_get_max() == 1)//只有两种状态，用简单的开关就可以了
				{
					view = inflate(MainActivity.this, R.layout.item_title_switch, null);
					Switch switch1 = view.findViewById(R.id.item_title_switch_switch);
					TextView textView1 = view.findViewById(R.id.item_title_switch_title);
					//final String str1 = this.myEquipments.get(groupPosition).data_title.get(childPosition).split("[@\\[]")[1];
					textView1.setText(itemdata.item_name);
					switch1.setChecked(itemdata.get_now() == 1);
					switch1.setOnCheckedChangeListener((buttonView, isChecked) ->
													   {
														   if (time_onclick_back_ms < 0)
														   {
															   //Log.i("TAG", "getChildView: :	15");
															   buttonView.setChecked(!isChecked);
															   //Log.i("TAG", "getChildView: :	16");
															   Toast.makeText(MainActivity.this, "正在更新...", Toast.LENGTH_SHORT).show();
															   return;
														   }
														   //出了加入队列，顺便值修改了，这样刷新之后就是显示新的值

														   if (isChecked)
														   {
															   itemdata.ht_get_values(1);
															   //注释掉下面这行，会在网络请求的时候发生跳变动画，下同
															   //myExpandableListAdapter.myEquipments.get(k).data_value.set(k1,String.valueOf(1));
														   }
														   else
														   {
															   itemdata.ht_get_values(0);
															   //myExpandableListAdapter.myEquipments.get(k).user_set_date(myExpandableListAdapter.myEquipments.get(k).data_title.get(k1), 0);
															   //myExpandableListAdapter.myEquipments.get(k).data_value.set(k1,String.valueOf(0));
														   }
														   myEquipment.user_set_date(itemdata.item_name, itemdata);
														   textView2_s.get(groupPosition).setText("等待");
														   myExpandableListAdapter.start_thread();
													   });
				}
				else if (itemdata.ht_get_max() > 1)//超过两种状态，使用可以截断的滑条来显示和操作
				{
					view = inflate(MainActivity.this, R.layout.item_title_seekbar, null);
					SeekBar seekBar = view.findViewById(R.id.item_title_seekbar_seekbar);
					TextView textView1 = view.findViewById(R.id.item_title_seekbar_title);//此条目的名称
					final TextView textView2 = view.findViewById(R.id.item_title_seekbar_value);//滑条当前值的文本显示
					TextView textView3 = view.findViewById(R.id.item_title_seekbar_min);//最小值
					TextView textView4 = view.findViewById(R.id.item_title_seekbar_max);//最大值
					textView1.setText(itemdata.item_name);
					seekBar.setMax(itemdata.ht_get_max());//设置滑条的最大值，最小值默认为0，设置最小值在低版本的安卓上面不兼容
					seekBar.setProgress(itemdata.ht_get_now());//设置换条的当前进度值
					//Log.e("TAG", "getChildView: itemdata.ht_get_values(itemdata.get_now()) "+itemdata.ht_get_values(itemdata.get_now()));
					textView2.setText(itemdata.ht_get_now_values());//根据当前值显示模式的名字
					textView3.setText(itemdata.ht_get_show_mim());//滑条左端注释
					textView4.setText(itemdata.ht_get_show_max());//滑条右端注释
					seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
					{
						//拖动中
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
						{
							//修改此条目显示滑条当前值的文本框内容
							//如果将此处填充值对应的文本，就可以实现显示状态好对应的描述
							//textView2.setText(String.valueOf(progress + itemdata.min));
							textView2.setText(itemdata.ht_get_values(progress));
						}

						//滑动开始//手指刚落到滑条上
						@SuppressLint("RestrictedApi")
						@Override
						public void onStartTrackingTouch(SeekBar seekBar)
						{

							fab.setVisibility(View.GONE);//隐藏右下角圆圆的刷新标志/虽然可以移动，但是隐藏更防方便
							textView2_s.get(groupPosition).setText("等待");//修改设备状态信息
							myExpandableListAdapter.start_thread();//更新倒计时
							myExpandableListAdapter.time_onclick_back_ms = TIME_OUT + 1;//设定为比预设值大一，就会无限制等待，让倒计时停滞
						}

						//滑动停止//手指离开屏幕
						@SuppressLint("RestrictedApi")
						@Override
						public void onStopTrackingTouch(SeekBar seekBar)
						{
							fab.setVisibility(View.VISIBLE);//显示圆圆的刷新键
							myEquipment.user_set_date(itemdata.item_name, itemdata);
							myExpandableListAdapter.start_thread();//重新启用，恢复倒计时
						}
					});

				}
			}
			else if (itemdata != null && !itemdata.ke_yi_xiu_gai())//仅对用户展示数据的条目
			{

				view = inflate(MainActivity.this, R.layout.item_title_text, null);
				TextView textView1 = view.findViewById(R.id.equipment_data_item_title);
				textView1.setText(itemdata.item_name);
				TextView textView2 = view.findViewById(R.id.equipment_data_item_data);
				textView2.setText(itemdata.text_get_now_values());
			}else{
				view = inflate(MainActivity.this, R.layout.item_title_text, null);
				TextView textView1 = view.findViewById(R.id.equipment_data_item_title);
				textView1.setText("数据异常");
				TextView textView2 = view.findViewById(R.id.equipment_data_item_data);
				textView2.setText("请刷新");
				Log.e("TAG", "error : 数据整理之后数组长度不对，服务器发过来的数据是没有问题的，多线程导致的？好像是其他地方刷新的时候，刚好这里的数据没有处理完刷新导致的，之后再看看吧，现在不崩溃了，但是会闪一下？");
			}
			return view;//当这里返回null的时候程序会 结束/崩溃
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition)
		{
			return false;
		}


	}


}