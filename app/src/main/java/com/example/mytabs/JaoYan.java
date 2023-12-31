package com.example.mytabs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JaoYan
{
	private static final Pattern pattern = Pattern.compile("[-_0-9a-zA-Z]+@[a-zA-Z0-9]+\\.com");
	private static final Pattern pattern2 = Pattern.compile("[0-9a-fA-F]+");
	private static final Pattern pattern3 = Pattern.compile("[^`~!@#$%^&*()_\\-=:;+\"'?{}\\[\\]\\\\/<>,.|\\s ]+");
	private static final Pattern pattern4 = Pattern.compile("[0-9a-f]{32}");
	private static final Pattern pattern5 = Pattern.compile("end=(\\d+)");
	//private static final Pattern pattern6 = Pattern.compile(",title=(\\d+)");
	private static final Pattern pattern7 = Pattern.compile(",log=([^,]+)");

	private static final Pattern pattern_date = Pattern.compile("[0-9]{4}-(1[012]|0[0-9])-\\d{2}");
	private static final Pattern pattern_time = Pattern.compile("\\d{2}:\\d{2}:\\d{2}");
	public static boolean jy_email(String email)
	{
		//长度限制
		if(email.length()>200)
		{
			return false;
		}
		Matcher re_email = pattern.matcher(email);
		return re_email.matches();
	}

	public static boolean jy_password(String email)
	{
		Matcher re_email = pattern2.matcher(email);
		return re_email.matches();
	}

	public static boolean jy_special_char(String str1)
	{
		Matcher re_email = pattern3.matcher(str1);
		return re_email.matches();
	}

	public static boolean jy_md5(String str1)
	{
		Matcher re_email = pattern4.matcher(str1);
		return re_email.matches();
	}


	/*
		post_data = msg.obj.toString();//容器储存post返回数据，可用于登录
		final int end=JaoYan.get_end_id(post_data);
		final String log=JaoYan.get_log_id(post_data);
		if (end>0&&log!=null)
		{
			Toast.makeText(SetData.this, log, Toast.LENGTH_SHORT).show();
		}
		else if(end==0)
		{

		}
		else
		{
			Toast.makeText(SetData.this, "服务器返回了不认识的数据emmmm", Toast.LENGTH_SHORT).show();
			Log.i("TAG", "handleMessage: 服务器返回了不认识的数据emmmm   " + post_data);
		}
	 */
	public static int get_end_id(String str1)
	{
		Matcher m = pattern5.matcher(str1);
		if(m.find())
		{
			//Log.e("TAG", "get_end_id: str1  " + str1);
			//Log.e("TAG", "get_end_id: m.group(1)  " + m.group(1));
			String str2 = m.group(1);
			if(str2==null)
			{
				return -1;
			}
			return Integer.parseInt(str2);
		}
		else
		{
			return -1;
		}
	}

	public static String get_log_id(String str1)
	{
		Matcher m = pattern7.matcher(str1);
		if(m.find())
		{
			return m.group(1);
		}
		else
		{
			return null;
		}
	}


	public static boolean is_date(String str1){
		Matcher re_email = pattern_date.matcher(str1);
		return re_email.matches();
	}

	public static boolean is_time(String str1){
		Matcher re_email = pattern_time.matcher(str1);
		return re_email.matches();
	}
}
