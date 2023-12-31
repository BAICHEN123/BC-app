package com.example.mytabs;

import android.os.Handler;
import android.os.Message;

import java.io.UnsupportedEncodingException;

public class MyHttp2
{
	static final int MyHttp_ERROR = MyHttp.ERROR;
	static final int MyKeyer_ERROR = -2;
	static final int MyOk = 0;
	static MyHttp myHttp;
	String path_fig;

	public MyHttp2(UserData userData)
	{
		path_fig = "" + userData.uid;
		if (myHttp == null)
		{
			myHttp = new MyHttp();
		}
	}

	public MyHttp2(String fig)
	{
		path_fig = fig;
		if (myHttp == null)
		{
			myHttp = new MyHttp();
		}
	}


	private String post_str(final String net_url, final String post_data, String key, Message msg)
	{
		byte[] bytes = MyKeyer.keyer_get_bytes(post_data, key);
		assert bytes != null;
		bytes = myHttp.send_Post_bytes(MyHttp.IP + net_url + "=" + path_fig, bytes);
		if (bytes == null)
		{
			msg.what = MyHttp_ERROR;
			return null;
		}
		return MyKeyer.keyer_get_string(bytes, key);
	}

	public Message post_str(final String net_url, final String post_data, String key, int fig)
	{
		byte[] bytes = MyKeyer.keyer_get_bytes(post_data, key);
		assert bytes != null;
		Message msg = new Message();
		msg.what = fig;

		bytes = myHttp.send_Post_bytes(MyHttp.IP + net_url + "=" + path_fig, bytes);
		//Log.e("TAG", "post_str: " + new String(bytes));
		if (bytes == null)
		{
			msg.what = MyHttp_ERROR;
			return msg;
		}

		byte[] bytes2 = MyKeyer.keyer_get_bytes(bytes, key.getBytes());//这里解析错了也可以转换成字符串,无法判断是否成功转换成正常的内容，除非增加起始标记

		String str1;
		try
		{
			str1 = new String(bytes2, "utf8");
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			msg.what = MyKeyer_ERROR;
			msg.obj = new String(bytes);
			return msg;
		}
		//Log.e("TAG", "post_str: " + str1);
		msg.obj = str1;
		return msg;
	}

	public void thread_post_str(final String net_url, final String post_data, String key, final Handler handler)
	{
		new Thread()
		{
			public void run()
			{
				Message msg = post_str(net_url, post_data, key, MyOk);
				handler.sendMessage(msg);
			}
		}.start();
	}

//	public Message post_msg(final String net_url, final String post_data, String key, final int CHANGE_UI)
//	{
//		Message msg = new Message();
//		msg.what = MyOk;
//		String end = post_str(net_url, post_data, key, msg);
//		if (end == null && msg.what == MyOk)
//		{
//			msg.what = MyKeyer_ERROR;
//			return msg;
//		}
//		else if (end == null && msg.what == MyHttp_ERROR)
//		{
//			return msg;
//		}
//		msg.what = CHANGE_UI;
//		msg.obj = end;
//		return msg;
//	}


	public void thread_post_str(final String net_url, final String post_data, String key, final Handler handler, final int CHANGE_UI)
	{
		new Thread()//开启请求发送邮件的线程
		{
			public void run()
			{
				Message msg = post_str(net_url, post_data, key, CHANGE_UI);
				handler.sendMessage(msg);
			}
		}.start();
	}

	public void thread_post_do_data(final String net_url, final String post_data, String key, final Handler handler)
	{
		new Thread()//开启请求发送邮件的线程
		{
			public void run()
			{
				Message msg = new Message();
				msg.what = MyOk;
				String end_str = post_str(net_url, post_data, key, msg);
				if (end_str == null && msg.what == MyOk)
				{
					msg.what = MyKeyer_ERROR;
					handler.sendMessage(msg);
					return;
				}
				else if (end_str == null || msg.what == MyHttp_ERROR)
				{
					handler.sendMessage(msg);
					return;
				}
				if (!end_str.startsWith("end="))
				{
					msg.what = MyKeyer_ERROR;
					handler.sendMessage(msg);
					return;
				}
				final int end = JaoYan.get_end_id(end_str);
				String log = JaoYan.get_log_id(end_str);
				if (log == null || "".equals(log))
				{
					log = end_str;
				}
				msg.what = end;
				msg.obj = log;
				handler.sendMessage(msg);
			}
		}.start();
	}


}

