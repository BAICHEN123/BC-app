package com.example.mytabs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.inflate;


public class AddLD extends Activity
{
	private static final int xuan_faqi = 1;//选择监听
	private static final int xuan_jieshou = 2;//选择接收
	private static final int xuan_set = 3;//输入比较值
	//private static final int xuan_writ = 4;//等待服务器
	UserData userdata;
	int buzhou;
	LDXuanZeAdapter ldXuanZeAdapter;
	ExpandableListView elistview;
	TextView textView;
	ListView listview;
	Listapder listapder;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xuan_ze);
		buzhou = xuan_faqi;


		elistview = findViewById(R.id.equipment_list_ExpandableListView);
		ldXuanZeAdapter = new LDXuanZeAdapter(getApplicationContext());
		elistview.setOnChildClickListener((parent, v, groupPosition, childPosition, id) ->
		{

			if (buzhou == xuan_jieshou && !listapder.myEquipments.get(groupPosition).get_item_data(childPosition).ke_yi_xiu_gai())
			{
				new AlertDialog.Builder(this)
						.setTitle("请选择一个可以被修改的对象,由字体颜色标记")
						.setNegativeButton("重选", null)

						.show();

				return true;
			}

			Log.e("TAG", "onChildClick: ");
			new AlertDialog.Builder(this)
					.setTitle(ldXuanZeAdapter.myEquipments.get(groupPosition).get_item_name(childPosition))
					.setPositiveButton("确定", (dialog, which) -> onclick_item(groupPosition, childPosition))
					.setNegativeButton("重选", null)
					.show();

			return true;
		});

		elistview.setAdapter(ldXuanZeAdapter);

		textView = findViewById(R.id.text);
		textView.setText("请选择需要监听的量");

		//加载用户信息
		MySqlLite sql = new MySqlLite(getApplicationContext());
		userdata = sql.get_login_data();


		listapder = new Listapder(this, ldXuanZeAdapter.myEquipments);

		//获取用户所有设备的信息
		//new MyHttp().thread_send_Post_String(MyHttp.IP + "get_my_equipment", userdata.get_str_data().toString(), hander_1, 1);
		new MyHttp2(userdata).thread_post_do_data("get_my_equipment", userdata.get_str_data(), userdata.net_md5, hander_1);


	}


	void onclick_item(int groupPosition, int childPosition)
	{
		switch (buzhou)
		{
			case xuan_faqi:
				listapder.set_faqi(groupPosition, childPosition);
				Log.e("TAG", "onclick_item: xuan_faqi " + groupPosition + "   " + childPosition);
				buzhou = xuan_jieshou;
				textView.setText("请选择要控制的量");
				break;
			case xuan_jieshou:
				listapder.set_jiehsou(groupPosition, childPosition);
				Log.e("TAG", "onclick_item: xuan_jieshou " + groupPosition + "   " + childPosition);

				buzhou = xuan_set;
				setContentView(R.layout.ld_user_set);
				//在这后面把新页面的内容填充进去
				listview = findViewById(R.id.ld_user_set_list);
				listview.setAdapter(listapder);
				//listapder.notifyDataSetChanged();
				break;
//			case xuan_set:
//				Log.e("TAG", "onclick_item: xuan_faqi "+groupPosition+"   "+ childPosition);
//			case xuan_writ:
		}
	}


	class Listapder extends BaseAdapter
	{
		final static int ID_tishi1 = EquipmentDataItem.VIEW_ID_MAX + 1;//列表第一个提示视图
		//final static int ID_tiaojian = EquipmentDataItem.VIEW_ID_MAX + 2;//列表里的条件视图
		final static int ID_tishi2 = EquipmentDataItem.VIEW_ID_MAX + 2;//列表里的第二个提示视图
		//final static int ID_renwu1 = EquipmentDataItem.VIEW_ID_MAX + 4;//列表里的执行操作视图
		final static int ID_qurenjian = EquipmentDataItem.VIEW_ID_MAX + 3;//列表里的确认按键

		Context context;
		ArrayList<MyEquipment> myEquipments;
		int fgroupid, fchildid, jgroupid, jchildid;
		ALdData aLdData;

		public Listapder(Context context, ArrayList<MyEquipment> myEquipments)
		{
			this.context = context;
			this.myEquipments = myEquipments;
			aLdData = new ALdData();
		}

		void set_faqi(int groupPosition, int childPosition)
		{
			fgroupid = groupPosition;
			fchildid = childPosition;
			aLdData.fitem = myEquipments.get(groupPosition).get_item_data(childPosition);
			aLdData.feid = myEquipments.get(groupPosition).eid;
			aLdData.fname = myEquipments.get(groupPosition).get_item_data(childPosition).set_item;
		}

		void set_jiehsou(int groupPosition, int childPosition)
		{
			jgroupid = groupPosition;
			jchildid = childPosition;
			aLdData.jitem = myEquipments.get(groupPosition).get_item_data(childPosition);
			aLdData.geid = myEquipments.get(groupPosition).eid;
			aLdData.gname = myEquipments.get(groupPosition).get_item_data(childPosition).set_item;

		}

		@Override
		public int getCount()
		{
			return 5;
		}

		@Override
		public Object getItem(int position)
		{
			return position;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view;
			TextView textview_info;
			//Log.e("TAG", "getView: list view apder " + position);
			switch (position)
			{
				case 0:
					if (convertView != null && convertView.getId() == ID_tishi1)
					{
						return convertView;
					}
					view = inflate(this.context, R.layout.ld_set_tishi1, null);
					textview_info = view.findViewById(R.id.ld_set_tishi1_text);
					textview_info.setText("当 [" + get_id_name(aLdData.feid) + "] 条件成立时");
					view.setId(ID_tishi1);
					return view;
				case 1:
					view = aLdData.fitem.get_ld_set_view(aLdData.fitem, context, EquipmentDataItem.ITEM_IS_IF, aLdData, convertView);
					return view;
				case 2:
					if (convertView != null && convertView.getId() == ID_tishi2)
					{
						return convertView;
					}
					view = inflate(this.context, R.layout.ld_set_tishi1, null);
					textview_info = view.findViewById(R.id.ld_set_tishi1_text);
					textview_info.setText("[" + get_id_name(aLdData.geid) + "] 执行下面操作");
					view.setId(ID_tishi2);
					return view;
				case 3:
					view = aLdData.jitem.get_ld_set_view(aLdData.jitem, context, EquipmentDataItem.ITEM_IS_DO, aLdData, convertView);
					return view;
				case 4:
					if (convertView != null && convertView.getId() == ID_qurenjian)
					{
						return convertView;
					}
					Button but = new Button(this.context);
					but.setText("确定");
					but.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							if (aLdData.fuhao == null || "".equals(aLdData.fuhao) || "?".equals(aLdData.fuhao))
							{
								Toast.makeText(context, "请点击“点击选择”，选择数值比较符号", Toast.LENGTH_SHORT).show();
								return;
							}
							//发送数据请求
							String str_data = "@" + aLdData.feid + "$" + aLdData.fname + aLdData.fuhao + aLdData.fitem.get_ht_end_value_str()
									+ "$@"
									+ aLdData.geid + aLdData.gname + ":" + aLdData.jitem.get_ht_end_value_str();
							Log.e("TAG", "onClick: " + str_data);
							//new MyHttp().thread_send_Post_String(MyHttp.IP+"ld_insert",,hander_ld_add,1);
							HashMap<String,Object> dict = userdata.get_dict_data();
							dict.put("data",str_data);
							new MyHttp2(userdata).thread_post_do_data("ld_insert", MyJson.toJson(dict), userdata.net_md5, hander_ld_add);


						}
					});
					but.setId(ID_qurenjian);
					return but;
				default:
					return null;
			}
		}


		public String get_id_name(long eid)
		{
			for (MyEquipment mye : myEquipments)
			{
				if (mye.eid == eid)
				{
					return mye.name;
				}
			}
			return "";
		}

	}

	/*
	获取此用户绑定的所有的设备的信息
	负责处理按键里的线程
	*/
	@SuppressLint("HandlerLeak")
	private final Handler hander_1 = new Handler()
	{
		public void handleMessage(Message msg)
		{

			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(AddLD.this, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(AddLD.this, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == 0)
			{
				String log = (String) msg.obj;
				if ("count equipment 0".equals(log))
				{
					Toast.makeText(getApplicationContext(), "未查询到您绑定的设备", Toast.LENGTH_SHORT).show();
					ldXuanZeAdapter.myEquipments.clear();
				}
				else
				{
					//刷新设备列表
					int i = 0;
					assert log != null;
					for (String str_i : log.split("#+"))
					{
						Log.e("TAG", "handleMessage: " + str_i);
						if (str_i != null && !"".equals(str_i))
						{
							if (i < ldXuanZeAdapter.myEquipments.size())
							{
								ldXuanZeAdapter.myEquipments.set(i, new MyEquipment(str_i));
							}
							else
							{
								ldXuanZeAdapter.myEquipments.add(new MyEquipment(str_i));
							}
							i++;
						}
					}
					new_data();
				}
				ldXuanZeAdapter.notifyDataSetChanged();
			}
			else if (msg.what > 0)
			{
				Toast.makeText(AddLD.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
			}
		}
	};

	//刷新已经有的设备的数据
	private void new_data()
	{
		//刷新设备列表视图
		for (int k = 0; k < ldXuanZeAdapter.myEquipments.size(); k++)
		{
			HashMap<String,Object> dict = userdata.get_dict_data();
			dict.put("eid",ldXuanZeAdapter.myEquipments.get(k).eid);
			new MyHttp2(userdata).thread_post_str("get_equipment_data", MyJson.toJson(dict), userdata.net_md5, hander_2, k);

		}
	}


	/*
获取 eid = msg.what 的设备的传感器等信息，并刷新界面显示
 */
	@SuppressLint("HandlerLeak")
	private final Handler hander_2 = new Handler()//负责处理按键里的线程
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
					Toast.makeText(getApplicationContext(), log, Toast.LENGTH_SHORT).show();
				}
				else if (end == 0 && log != null)
				{
					if ("SET error 2 越权".equals(log))
					{
						Toast.makeText(getApplicationContext(), "未查询到您绑定的设备", Toast.LENGTH_SHORT).show();
						ldXuanZeAdapter.myEquipments.clear();
					}
					else
					{

						if (ldXuanZeAdapter.myEquipments.get(msg.what).addData_(log) == MyEquipment.NEED_INFO)
						{
							//new MyHttp().thread_send_Post_String(MyHttp.IP + "get_equipment_info", userdata.get_str_data().toString() + "eid=" + ldXuanZeAdapter.myEquipments.get(msg.what).eid, hander_3, msg.what);
							HashMap<String,Object> dict = userdata.get_dict_data();
							dict.put("eid",ldXuanZeAdapter.myEquipments.get(msg.what).eid);
							new MyHttp2(userdata).thread_post_str("get_equipment_info", MyJson.toJson(dict), userdata.net_md5, hander_3, msg.what);
						}
					}
				}
				else
				{
					Toast.makeText(getApplicationContext(), "服务器返回了不认识的数据emmmm", Toast.LENGTH_SHORT).show();
					Log.i("TAG", "handleMessage: 服务器返回了不认识的数据emmmm   " + str_data);
				}

			}
			ldXuanZeAdapter.notifyDataSetChanged();
		}
	};


	/*
	获取 eid = msg.what 的设备的状态值对应的 info 详细描述，并刷新界面显示
	 */
	@SuppressLint("HandlerLeak")
	private final Handler hander_3 = new Handler()//负责处理按键里的线程
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
					Toast.makeText(getApplicationContext(), log, Toast.LENGTH_SHORT).show();
					//info允许为空
					//我随意填充点儿数据，防止他一直进行info请求
					ldXuanZeAdapter.myEquipments.get(msg.what).set_info_isnull();
				}
				else if (end == 0 && log != null)
				{
					if ("SET error 2 越权".equals(log))
					{
						Toast.makeText(getApplicationContext(), "未查询到您绑定的设备", Toast.LENGTH_SHORT).show();
						ldXuanZeAdapter.myEquipments = new ArrayList<>();
					}
					else
					{
						ldXuanZeAdapter.myEquipments.get(msg.what).addInfo_(log);
					}
					//这里只要把数据管理好就行了，没有刷新的必要了，我只在分组里展示名字，没有展示状态
					//ldXuanZe.notifyDataSetChanged();
				}
				else
				{
					Toast.makeText(getApplicationContext(), "服务器返回了不认识的数据emmmm", Toast.LENGTH_SHORT).show();
					Log.i("TAG", "handleMessage: 服务器返回了不认识的数据emmmm   " + str_data);
				}
			}
		}

	};


	/*
获取 eid = msg.what 的设备的传感器等信息，并刷新界面显示
 */
	@SuppressLint("HandlerLeak")
	private final Handler hander_ld_add = new Handler()//负责处理按键里的线程
	{
		public void handleMessage(Message msg)
		{


			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(AddLD.this, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(AddLD.this, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == 0)
			{
				//#温度:28#湿度:68#@灯0[0-1]:1#@灯1[0-1]:1#
				//msg.what 是视图的编号，从0开始
				String str_data = (String) msg.obj;
				Toast.makeText(getApplicationContext(), str_data, Toast.LENGTH_SHORT).show();
				setResult(1);
				finish();
			}
			else if (msg.what > 0)
			{
				Toast.makeText(AddLD.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
			}

		}
	};

}
