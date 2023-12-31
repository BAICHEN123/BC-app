package com.example.mytabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.inflate;

public class LdExpandableListAdapter extends BaseExpandableListAdapter
{
	Context context;
	ArrayList<ALdData> lddatas;
	ArrayList<MyEquipment> myEquipments;
	UserData userData;
	//本来想从主函数把用户信息传过来的，但是担心传过来的对象被修改之后，这里没有更新，所以打算每次打包之前从数据库加载

	public LdExpandableListAdapter(Context context, ArrayList<ALdData> lddatas, ArrayList<MyEquipment> myEquipments)
	{
		this.context = context;
		this.lddatas = lddatas;
		this.myEquipments = myEquipments;
	}

	public void set_userdata(UserData userData)
	{
		this.userData = userData;
	}


	@Override
	public int getGroupCount()
	{
		if (lddatas == null)
		{
			return 0;
		}
		return lddatas.size();
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		//目前先实现基本功能，以后在搞什么相同监听分组展示
		return 5;
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		return childPosition;
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
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		View view;
		if (convertView == null)
		{
			view = inflate(this.context, R.layout.ld_title, null);
		}
		else
		{
			view = convertView;
		}
		TextView textView = view.findViewById(R.id.ld_title_number);
		textView.setText(String.valueOf(groupPosition + 1));
		TextView textView2 = view.findViewById(R.id.ld_title_name);
		//textView2.setText(this.lddatas.get(groupPosition).fname+"&"+this.lddatas.get(groupPosition).gname.replace("@",""));
		textView2.setText(get_id_name(this.lddatas.get(groupPosition).feid) + "&" + get_id_name(this.lddatas.get(groupPosition).geid));

		ImageView imageView = view.findViewById(R.id.ld_title_image);
		if (isExpanded)
		{
			imageView.setImageResource(R.drawable.expander_close_holo_light);
		}
		else
		{
			imageView.setImageResource(R.drawable.expander_open_holo_light);
		}

		return view;
	}

	public static final int VIEW_ID_tishi1 = EquipmentDataItem.VIEW_ID_MAX + 1;//第一个提示视图的id
	public static final int VIEW_ID_tishi2 = EquipmentDataItem.VIEW_ID_MAX + 2;//第2个提示视图的id
	public static final int VIEW_ID_queren = EquipmentDataItem.VIEW_ID_MAX + 3;//确认按键的视图id

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		//关于这里的视图回收，我发现了一个 setId() 和 getId() 利用这个东西，我成功的识别了他的类型，并重复使用，防止在键盘弹出的时候，丢失用户的数据

		//这里注释掉之后就无法正常键盘输入，不注释掉就容易试图错乱
//		if(convertView!=null)
//		{
//			Log.e("TAG", "getChildView: convertView  null " );
//			return convertView;
//		}

		View view;
		TextView textview_info;
		EquipmentDataItem itemdata1;
		TextView textView1;
		TextView textView2;
		//Log.e("TAG", "getChildView: list view apder ");
		//键盘消失前后会重新构建所有的视图，所以所有的更改都没有保存？
		switch (childPosition)
		{
			case 0:
				if (convertView != null && convertView.getId() == VIEW_ID_tishi1)
				{
					view = convertView;
				}
				else
				{

					view = inflate(this.context, R.layout.ld_set_tishi1, null);
				}
				textview_info = view.findViewById(R.id.ld_set_tishi1_text);
				textview_info.setText("当 [" + get_id_name(this.lddatas.get(groupPosition).feid) + "] 条件成立时");
				view.setId(VIEW_ID_tishi1);
				return view;
			case 1:
				//myEquipment1 = this.myEquipments.get(this.fgroupid);
//				if (convertView != null && convertView.getId() == AddLD.Listapder.ID_tiaojian)
//				{
//					//Log.e("TAG", "getChildView: getId 回收，利用11111111111");
//					//其实这样直接把原来的视图填充进去是不太好的，可能导致其他地方的相同的试图被拿来使用。
//					//但是我简单测试没有出现这样的情况
//					//可能是他处理过了？或者只回收了相同父布局下的视图？我不知道，但是他现在能运行，没有明显的错误
//					return convertView;
//				}
				if (lddatas.get(groupPosition).fitem == null)
				{
					itemdata1 = geteDataItem(lddatas.get(groupPosition).feid, lddatas.get(groupPosition).fname);
					if (itemdata1 == null)
					{
						view = inflate(this.context, R.layout.ld_faqi, null);
						textView1 = view.findViewById(R.id.ld_faqi_name);
						textView2 = view.findViewById(R.id.ld_faqi_data);
						textView1.setText(get_id_name(this.lddatas.get(groupPosition).feid));
						textView2.setText(this.lddatas.get(groupPosition).fname + this.lddatas.get(groupPosition).fuhao + this.lddatas.get(groupPosition).fdata);
						return view;
					}
					lddatas.get(groupPosition).set_fdata(itemdata1);
				}
				//这个判断的作用就是，不要再新建一个联动对象，不然就无法正常获取修改之后的内容
				view = lddatas.get(groupPosition).fitem.get_ld_set_view(lddatas.get(groupPosition).fitem, this.context, EquipmentDataItem.ITEM_IS_IF, lddatas.get(groupPosition), convertView);
				//view.setId(AddLD.Listapder.ID_tiaojian);
				return view;
			case 2:
				if (convertView != null && convertView.getId() == VIEW_ID_tishi2)
				{
					view = convertView;
				}
				else
				{
					view = inflate(this.context, R.layout.ld_set_tishi1, null);
				}
				textview_info = view.findViewById(R.id.ld_set_tishi1_text);
				textview_info.setText("[" + get_id_name(this.lddatas.get(groupPosition).geid) + "] 执行下面操作");
				view.setId(VIEW_ID_tishi2);
				return view;
			case 3:
				if (lddatas.get(groupPosition).jitem == null)
				{
					itemdata1 = geteDataItem(lddatas.get(groupPosition).geid, lddatas.get(groupPosition).gname);
					if (itemdata1 == null)
					{
						view = inflate(this.context, R.layout.ld_jieshou, null);
						textView1 = view.findViewById(R.id.ld_jieshou_name);
						textView2 = view.findViewById(R.id.ld_jieshou_data);
						textView1.setText(get_id_name(this.lddatas.get(groupPosition).geid));
						textView2.setText(this.lddatas.get(groupPosition).gname + ':' + this.lddatas.get(groupPosition).gdata);
						return view;
					}
					lddatas.get(groupPosition).set_gdata(itemdata1);
				}
				//这个判断的作用就是，不要再新建一个联动对象，不然就无法正常获取修改之后的内容
				//return lddatas.get(groupPosition).jitem.get_ld_set_view(lddatas.get(groupPosition).jitem, this.context, childPosition, lddatas.get(groupPosition));
				view = lddatas.get(groupPosition).jitem.get_ld_set_view(lddatas.get(groupPosition).jitem, this.context, EquipmentDataItem.ITEM_IS_DO, lddatas.get(groupPosition), convertView);
				//view.setId(AddLD.Listapder.ID_renwu1);
				return view;
			case 4:
				Button but;
				if (convertView != null && convertView.getId() == VIEW_ID_queren)
				{
					but = (Button) convertView;
				}
				else
				{
					but = new Button(this.context);
					but.setId(VIEW_ID_queren);
				}
				but.setText("点我保存修改/长按删除");
				but.setOnLongClickListener(new View.OnLongClickListener()
				{
					@Override
					public boolean onLongClick(View v)
					{
						Log.e("TAG", "onLongClick: ");
						new AlertDialog.Builder(context)
								.setTitle("删除?")
								.setNeutralButton("删除", new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										//删除和更新之后都是重新情趣一遍，所以都用 hander_ld_delete
										//new MyHttp().thread_send_Post_String(MyHttp.IP + "ld_delete", userData.get_str_data() + "data=" + lddatas.get(groupPosition).id, hander_ld_delete, 1);

										HashMap<String, Object> dict = userData.get_dict_data();
										dict.put("data", lddatas.get(groupPosition).id);

										new MyHttp2(userData).thread_post_do_data("ld_delete", MyJson.toJson(dict), userData.net_md5, hander_ld_delete);
										//Log.e("TAG", "onClick: ", );
									}
								})
								.setNegativeButton("取消", null)
								.show();
						return true;
					}
				});
				but.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//Log.e("TAG", "onClick: "+ aLdData.fuhao+"   "+myEquipments.get(fgroupid).get_String_data()+"   "+myEquipments.get(jgroupid).get_String_data());
						//Log.e("TAG", "onClick: " + aLdData.fname + aLdData.fuhao + myEquipments.get(fgroupid).get_item_data(fchildid).get_ht_end_value_str());
						//Log.e("TAG", "onClick: " + aLdData.gname + ":" + myEquipments.get(jgroupid).get_item_data(jchildid).get_ht_end_value_str());
						//验证用户输入的信息是否完整
//						if(lddatas.get(groupPosition).fuhao==null)
//						{
//							Toast.makeText(context, "请点击“点击选择”，选择数值比较符号", Toast.LENGTH_SHORT).show();
//							return;
//						}
						//发送数据请求
						ALdData aLdData = lddatas.get(groupPosition);
						if(aLdData.fitem == null || aLdData.jitem == null ){
							Toast.makeText(context, "离线无法修改/变量不存在", Toast.LENGTH_SHORT).show();
							return;
						}
						//这后面是修改，所以要携带上原来的编号
						String str_data = "A" + aLdData.id + "F" + aLdData.fid + "J" + aLdData.gid + "D" + aLdData.fdata.did +
								"@" + aLdData.feid + "$" + aLdData.fname + aLdData.fuhao + aLdData.fitem.get_ht_end_value_str()
								+ "$@"
								+ aLdData.geid + aLdData.gname + ":" + aLdData.jitem.get_ht_end_value_str();
						HashMap<String, Object> dict = userData.get_dict_data();
						dict.put("data", str_data);
						Log.e("TAG", "onClick 111111: " + str_data);
						//调用了 hander_ld_delete 是因为处理方式产不多，都是显示提示，然后刷新
						//new MyHttp().thread_send_Post_String(MyHttp.IP + "ld_updata", userData.get_str_data() + str_data, hander_ld_delete, 1);
						new MyHttp2(userData).thread_post_do_data("ld_updata", MyJson.toJson(dict), userData.net_md5, hander_ld_delete);

					}
				});
				return but;
			default:
				return null;
		}
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return false;
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
		return "未知设备";
	}

	private EquipmentDataItem geteDataItem(long eid, String itemname)
	{
		for (MyEquipment mye : myEquipments)
		{
			if (mye.eid == eid)
			{
				EquipmentDataItem it1 = mye.get_item_data(itemname.replace("@", ""));
				if (it1 == null)
				{
					Log.e("TAG", "geteDataItem: it1==null");
					return null;
				}
				return new EquipmentDataItem(it1);
			}
		}
		Log.e("TAG", "geteDataItem: 没有名字");
		return null;
	}


	/*
	获取 eid = msg.what 的设备的传感器等信息，并刷新界面显示
	 */
	@SuppressLint("HandlerLeak")
	public final Handler hander_ld_delete = new Handler()//负责处理按键里的线程
	{
		public void handleMessage(Message msg)
		{
			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(context, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(context, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == 0)
			{
				String str_data = (String) msg.obj;
				Toast.makeText(context, str_data, Toast.LENGTH_SHORT).show();
				new MyHttp2(userData).thread_post_do_data("ld_select", userData.get_str_data(), userData.net_md5, hander_ld);
			}
			else if (msg.what > 0)
			{
				Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
			}
			notifyDataSetChanged();
		}
	};


	/*

	 */
	@SuppressLint("HandlerLeak")
	public final Handler hander_ld = new Handler()//负责处理按键里的线程
	{
		public void handleMessage(Message msg)
		{
			lddatas.clear();
			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(context, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(context, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == 0)
			{
				notifyDataSetChanged();//先把他清空，刷新一次，回收大量的旧视图
				String str_data = (String) msg.obj;
				//Log.e("TAG", "handleMessage: notifyDataSetChanged 看之后有没有使用垃圾" );//没有
				for (String str1 : str_data.split("#+"))
				{
					lddatas.add(new ALdData(str1));
				}

			}
			else if (msg.what > 0)
			{
				Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
			}
			notifyDataSetChanged();//不管成功与否，一定要更新视图

		}
	};
}
