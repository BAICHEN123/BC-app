package com.example.mytabs;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;

public class MyEquipment
{
	/*需要包含以下要素
	服务器传回的数据字段	1:1@nullname
	eid			1:1	1
	name		nullname
	 */
	//String data;
	Long eid;
	String name;
	private ArrayList<String> item_names;
	private final HashMap<String, EquipmentDataItem> datas;
	private final HashMap<String, String[]> infos;
	private final HashMap<String, EquipmentDataItem> user_set_datas;

	public final static int NEED_INFO = 1;//需要请求info
	public final static int HAVE_INFO = 2;//已经有info

	private static final Pattern pattern1 = Pattern.compile("@([^\\s@,，:]+)\\[(\\d+)-(\\d+)]:((?:[^,，@#]+[,，]?)+)");
	//private static final Pattern pattern2 = Pattern.compile("(@(\\S+)\\[(\\d+)-(\\d+)]):(\\d+)");


	public MyEquipment(String data)
	{
		//this.data = data;
		//Log.e("TAG", "MyEquipment: "+data);
		this.eid = Long.parseLong(data.split(":")[0]);
		//Log.e("TAG", "MyEquipment: "+this.eid);
		this.name = data.split("\\d+:\\d+@")[1];
		//Log.e("TAG", "MyEquipment: "+this.name);
		datas = new HashMap<>();
		infos = new HashMap<>();
		user_set_datas = new HashMap<>();
		this.item_names = new ArrayList<>();
	}
	private MyEquipment(Long eid)
	{
		//this.data = data;
		//Log.e("TAG", "MyEquipment: "+data);
		this.eid = eid;
		//Log.e("TAG", "MyEquipment: "+this.eid);
		this.name = "null";
		//Log.e("TAG", "MyEquipment: "+this.name);
		datas = new HashMap<>();
		infos = new HashMap<>();
		user_set_datas = new HashMap<>();
		this.item_names = new ArrayList<>();
	}


	//获取数据条目总数
	public int size()
	{
		return datas.size();
	}

	//获取数据条目的名字//从0 开始
	public String get_item_name(int id)
	{
		//Log.e("TAG", "get_item_name: id="+id+" this.item_names.size()="+this.item_names.size());
		if (this.item_names.size() > id)
		{
			return this.item_names.get(id);
		}
		return null;
	}

	//返回id对应的数据条目对象
	public EquipmentDataItem get_item_data(int id)
	{
		String name = this.get_item_name(id);
		if (name == null)
		{
			return null;
		}
		return this.datas.get(name);
	}

	public EquipmentDataItem get_item_data(String name)
	{
		return datas.get(name);
	}

//	//根据名字获取数据条目的对象
//	public EquipmentDataItem get_item_data(String name)
//	{
//		return this.datas.get(name);
//	}

	//初始化设备的数据条目内容
	public int addData_(String get_data)
	{
		clear_dataitem();
		for (String str_i : get_data.split("#+"))
		{
			if ("".equals(str_i))
			{
				continue;
			}
			String[] list_str = str_i.split(":");
			EquipmentDataItem eitem;
			if (list_str.length == 2)
			{
				eitem = new EquipmentDataItem(list_str[0], list_str[1]);
				datas.put(eitem.item_name, eitem);
				this.item_names.add(eitem.item_name);
			}
		}

		if (infos == null || infos.size() == 0)
		{
			return NEED_INFO;
		}
		else
		{
			if (infos.size() == 1 && infos.containsKey("无描述"))
			{
				return HAVE_INFO;
			}
			//已经有info，填充到合适的位置
			EquipmentDataItem item;
			for (HashMap.Entry<String, String[]> a : infos.entrySet())
			{
				item = datas.get(a.getKey());
				if (item != null)
				{
					item.set_mode_name(a.getValue());
				}

			}
			return HAVE_INFO;
		}
	}

	//清除数据条目
	void clear_dataitem()
	{
		this.datas.clear();
		this.user_set_datas.clear();
		this.item_names = new ArrayList<>();
	}

	public void set_info_isnull()
	{
		infos.put("无描述", null);
	}

	//为数据条目添加模式描述
	public void addInfo_(String get_info)
	{
		//Log.i("TAG", "addInfo_: get_info 	"+get_info);
		infos.clear();
		Matcher m = pattern1.matcher(get_info);
		EquipmentDataItem dateItem;
		while (m.find() && m.groupCount() == 4)
		{
//			Log.i("TAG", "addData_: m.group(5)  "+m.group(5));
//			Log.i("TAG", "addData_: m.group(4)  "+m.group(4));
//			Log.i("TAG", "addData_: m.group(3)  "+m.group(3));
//			Log.i("TAG", "addData_: m.group(2)  "+m.group(2));
//			Log.i("TAG", "addData_: m.group(1)  "+m.group(1));
			dateItem = this.datas.get(m.group(1));
			if (dateItem == null || m.group(4) == null)
			{
				//Log.e("TAG", "addInfo_: dateItem == null || m.group(4) == null");
				break;
			}
			if (dateItem.mode == EquipmentDataItem.SHOW_DATA_INT)
			{
				dateItem.min = Integer.parseInt(Objects.requireNonNull(m.group(2)));
				dateItem.max = Integer.parseInt(Objects.requireNonNull(m.group(3)));
			}
			String[] mode_names = Objects.requireNonNull(m.group(4)).split("[,，]+");
			infos.put(m.group(1), mode_names);//把info储存在这里，清除数据条目的时候不清除info
			dateItem.set_mode_name(mode_names);//储存在这里方便其他地方调用
		}

	}


	public void user_set_date(String data_title, EquipmentDataItem data_value)
	{
		//Log.e("TAG", "user_set_date: "+data_title);
		user_set_datas.put(data_title, data_value);
	}

	//获取用户对设备的所有更改，并清除记录
	public String get_String_data()
	{
		if (user_set_datas.size() == 0)
		{
			return null;
		}
		StringBuilder data = new StringBuilder();
		for (HashMap.Entry<String, EquipmentDataItem> a : user_set_datas.entrySet())
		{
			//Log.e("TAG", "user_set_date: "+a.getKey());
			data.append(a.getValue().set_item).append(":").append(a.getValue().get_ht_end_value_str());
		}
		return data.toString();
	}

	public String getStatusString()
	{
		if (datas.containsKey("状态"))
		{
			return Objects.requireNonNull(datas.get("状态")).text_get_now_values();
		}
		return "就绪";
	}

	public boolean isOnline()
	{
		return !datas.containsKey("状态");
	}

	static MyEquipment static_equipment = new MyEquipment(1L);
	public static HashMap<String, EquipmentDataItem> do_message_date(String data){
		static_equipment.addData_(data);
		return static_equipment.datas;
	}
	public static ArrayList<String> get_do_message_name_date(){
		return static_equipment.item_names;
	}


}
