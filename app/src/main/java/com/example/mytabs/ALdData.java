package com.example.mytabs;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
这个类用来储存一个联动的数据
*/
public class ALdData
{
	EquipmentDataItem fitem;
	EquipmentDataItem jitem;

	//我数据库id用的是 u64，我不信这行代码能用到u63
	long id;//储存联动在数据库里的id
	long fid;//储存发起设备的id
	long gid;//储存接收设备的id
	long feid;//储存发起设备的id
	long geid;//储存接收设备的id
	//
	String fname;//“温度”
	String fuhao;//'>'
	FData fdata;//“30”

	//要求 gname+’：‘+gdata 可以被节点识别
	String gname;//需要储存‘@‘
	String gdata;

	//	AbidFfidJjidDdid@20$温度>30@21@开关1:1
	//	"^@(\d+)$([^特殊符号]{1,50})([<>=~])(\d{1,30})@(\d{1,30})(@[^特殊符号]{1,49}):[\d]{1,30})$"

	private static final Pattern pattern3 = Pattern.compile("A(\\d{1,30})F(\\d{1,30})J(\\d{1,30})D(\\d{1,30})@(\\d{1,30})\\$([^<>=~\\s:,]{1,50})([<>=~])([^\\s$@]{1,30})\\$@(\\d{1,30})(@[^<>=~\\s:,]{1,49}):([\\d]{1,30})");

	public ALdData(String str1)
	{
		this.get_values(str1);
	}

	public ALdData()
	{

	}

	private void get_values(String str1)
	{
		id = 0;
		Matcher m = pattern3.matcher(str1);
		if (m.find() && m.groupCount() == 11)
		{
			//既然他都找到8个了，必不等于空
			id = Long.parseLong(m.group(1));
			fid = Long.parseLong(m.group(2));
			gid = Long.parseLong(m.group(3));
			fdata = new FData(m.group(4), m.group(8));
			feid = Long.parseLong(m.group(5));
			fname = m.group(6);
			fuhao = m.group(7);
			geid = Long.parseLong(m.group(9));
			gname = m.group(10);
			gdata = m.group(11);
		}
	}

	public void set_fdata(int mode, String strdata)
	{
		fdata = new FData(mode, strdata);
	}

	public String get_str()
	{
		return "A"+id+"F"+fid+"J"+gid+"D"+fdata.did+"@"+feid+"$"+fname+fuhao+fdata+"@"+geid+gname+":"+gdata;
	}


	void set_fdata(EquipmentDataItem item)
	{
		this.fitem=item;
		//强制转换成 item 要求的数据类型
		//Log.e("TAG", "set_fdata: "+item.mode);
		switch (item.mode)
		{
			case EquipmentDataItem.KE_YI_XIU_GAI_INT:
			case EquipmentDataItem.SHOW_DATA_INT:
				//*****************************************************警告，这里发生了数据丢失
				item.int_now=Integer.parseInt(fdata.str1);
				item.str_now=fdata.str1+item.unit;
				break;
			case EquipmentDataItem.KE_YI_XIU_GAI_DOUBLE:
			case EquipmentDataItem.SHOW_DATA_DOUBLE:
				item.double_now=Double.parseDouble(fdata.str1);
				item.str_now=fdata.str1+item.unit;
				break;
			case EquipmentDataItem.SHOW_DATA_STR:
			case EquipmentDataItem.KE_YI_XIU_GAI_STR:
				item.str_now=fdata.str1;
				break;
		}
	}

	void set_gdata(EquipmentDataItem item)
	{
		this.jitem=item;
		//强制转换成 item 要求的数据类型
		item.str_now=gdata;
		switch (item.mode)
		{
			case EquipmentDataItem.KE_YI_XIU_GAI_INT:
			case EquipmentDataItem.SHOW_DATA_INT:
				//*****************************************************警告，这里发生了数据丢失
				item.int_now=Integer.parseInt(gdata);
			case EquipmentDataItem.KE_YI_XIU_GAI_DOUBLE:
			case EquipmentDataItem.SHOW_DATA_DOUBLE:
				item.double_now=Double.parseDouble(gdata);
//			case EquipmentDataItem.SHOW_DATA_STR:
//			case EquipmentDataItem.KE_YI_XIU_GAI_STR:
//				item.str_now=gdata;
		}
	}


	class FData
	{
		long long1;
		double double1;
		String str1;
		int did;

		public FData(String did, String strdata)
		{
			//关于did的对应的数据类型由服务器上的联动类型表定义
			this.did = Integer.parseInt(did);
			str1=strdata;//不管什么数据类型都要记录一下
			switch (this.did)
			{
				case 1:
					long1 = Long.parseLong(strdata);
				case 2:
					double1 = Double.parseDouble(strdata);
				//case 3:
				//	str1 = strdata;
			}
		}


		public FData(int did, String strdata)
		{
			//关于did的对应的数据类型由服务器上的联动类型表定义
			this.did = did;
			str1=strdata;//不管什么数据类型都要记录一下
			switch (this.did)
			{
				case EquipmentDataItem.KE_YI_XIU_GAI_INT:
				case EquipmentDataItem.SHOW_DATA_INT:
					long1 = Long.parseLong(strdata);
				case EquipmentDataItem.KE_YI_XIU_GAI_DOUBLE:
				case EquipmentDataItem.SHOW_DATA_DOUBLE:
					double1 = Double.parseDouble(strdata);
				case EquipmentDataItem.SHOW_DATA_STR:
				case EquipmentDataItem.KE_YI_XIU_GAI_STR:
					str1 = strdata;
			}
		}

		public String toString()
		{
			switch (did)
			{
				case 1:
					return String.valueOf(long1);
				case 2:
					return String.valueOf(double1);
				case 3:
					return str1;
			}
			return "未知类型";
		}
	}

}
