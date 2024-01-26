package com.example.mytabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SelectServer
{
	private static String server_http_server = "";
	static boolean need_show_select_page(Context context){
		// 获取 SharedPreferences 对象
		SharedPreferences sharedPreferences =  context.getSharedPreferences("ServerConfig", Context.MODE_PRIVATE);
		String server_name = sharedPreferences.getString("server_name", "");
		if(server_name.isEmpty()){
			return true;
		}
		MyHttp.setServerHttp(get_server_http_str(context));
		return false;
	}

	static void save_server_config(Context context,String server_name){
		SharedPreferences sharedPreferences =  context.getSharedPreferences("ServerConfig", Context.MODE_PRIVATE);
		if(!server_name.endsWith("/")){
			server_name=server_name+"/";
		}
		// 存储数据
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("server_name", server_name);
		MyHttp.setServerHttp(server_name);
		server_http_server = server_name;
		editor.apply();
	}

	private static String get_server_http_str(Context context){
		if(server_http_server.isEmpty()){
			SharedPreferences sharedPreferences =  context.getSharedPreferences("ServerConfig", Context.MODE_PRIVATE);
			server_http_server = sharedPreferences.getString("server_name", "");
		}
		return server_http_server;
	}
	public static void restartApp(Context context)
	{
		Intent intent =
				context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
		if (intent != null)
		{
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

}
