package com.example.mytabs;

import java.util.HashMap;

public class UserData
{
	//邮箱 密码 名称
	String email,password,name,net_md5;
	//毫秒时间戳

	//存储时间
	//System.currentTimeMillis()
	long new_time;
	long uid;
	//储存用户的头像信息
	//储存方法，【0】图片的MD5，【1】图片格式（后缀名）
	String[] user_head=new String[2];

	//储存性别 11 00
	byte sex;
	public UserData()
	{
		uid=0;
		email="";
		password="123456";
		name="未闻君名";
		new_time=System.currentTimeMillis();
		user_head[0]="";
		user_head[1]="jpg";
		sex=2;
	}

	public HashMap<String,Object> get_dict_data()
	{
		HashMap<String,Object> dict = new HashMap();
		dict.put("uid",this.uid);
//		String str2 = MyJson.toJson(dict);
		return dict;
	}
	public String get_str_data()
	{
		HashMap<String,Object> dict = new HashMap();
		dict.put("uid",this.uid);
		String str2 = MyJson.toJson(dict);
		return str2;
	}
}
