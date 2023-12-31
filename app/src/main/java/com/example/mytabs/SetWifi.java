package com.example.mytabs;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class SetWifi
{
	static WifiManager wifimanager;
	List<ScanResult> wifi_list;
	Context context;

	public SetWifi(Context context)
	{
		wifimanager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		this.context = context;
	}

	public void connect_wifi(String ssid, String password)
	{

		//Log.i("login", "onCreate: 获取文本框内容，并尝试链接wifi	1");
		//wifimanager=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
		//String str1="HCC_APP";
		//String str2="12345678";

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			Log.i("TAG", "connect_wifi: 1");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
			{
				Log.i("TAG", "connect_wifi: ");
				final List<WifiNetworkSuggestion> suggestionsList1 = wifimanager.getNetworkSuggestions();
				for (WifiNetworkSuggestion a : suggestionsList1)
				{
					Log.i("TAG", "connect_wifi: " + a.getSsid());
				}
			}
			final WifiNetworkSuggestion suggestion2 =
					new WifiNetworkSuggestion.Builder().setSsid(ssid).setWpa2Passphrase(password).setIsAppInteractionRequired(false).build();
			//final WifiNetworkSuggestion suggestion3 = new WifiNetworkSuggestion.Builder().setSsid("HCC_APP").setWpa3Passphrase("12345678").setIsAppInteractionRequired(false).build();
			final List<WifiNetworkSuggestion> suggestionsList = new ArrayList<>();
			suggestionsList.add(suggestion2);
			//suggestionsList.add(suggestion3);
			final int status = wifimanager.addNetworkSuggestions(suggestionsList);
			if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS)
			{
				//在这里有可能会==13，这是建议被重复添加的结果
				Log.e("TAG", "onCreate:  wifi建议返回码 " + status);
				//return false;
			}
			else
			{
				//打开wifi界面刷新一下状态，不然等的太久了
				Log.e("TAG", "onCreate:  请刷新一下wifi状态 ");
				//Toast.makeText(this.context, "请刷新一下wifi状态", Toast.LENGTH_SHORT).show();
			}
			// Optional (Wait for post connection broadcast to one of your suggestions)
			/*
			final IntentFilter intentFilter = new IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);

			final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					Log.e("TAG", "onCreate:  ok  1");
					if (!intent.getAction().equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION))
					{
						return;
					}
					Log.e("TAG", "onCreate:  ok  2");
					final int status = wifimanager.removeNetworkSuggestions(suggestionsList);
					Log.e("TAG", "onCreate:  ok 3" +status);
					// do post connect processing here...
				}
			};
			context.registerReceiver(broadcastReceiver, intentFilter);
			 */
		}
		else
		{

			WifiConfiguration config = new WifiConfiguration();
			config.allowedAuthAlgorithms.clear();
			config.allowedGroupCiphers.clear();
			config.allowedKeyManagement.clear();
			config.allowedPairwiseCiphers.clear();
			config.allowedProtocols.clear();

			// 指定对应的SSID
			config.SSID = "\"" + ssid + "\"";
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;

			assert wifimanager != null;
			//netId = wifimanager.addNetwork(config);//添加网络
			wifimanager.enableNetwork(wifimanager.addNetwork(config), true);//使能网络
			//Log.i("TAG", "GetWifi_button_ok3: "+netId);
			Log.i("TAG", "GetWifi_button_ok3: " + IP_inttoString(wifimanager.getConnectionInfo().getIpAddress()));
			// 这个方法的第一个参数是需要连接wifi网络的networkId，第二个参数是指连接当前wifi网络是否需要断开其他网络
			// 无论是否连接上，都返回true。。。。

		/*
		WifiInfo WifiInfo1=wifimanager.getConnectionInfo();//获取当前的wifi信息
		Log.e("TAG", "123"+WifiInfo1.getSSID()+WifiInfo1.getBSSID()+WifiInfo1.getIpAddress()+WifiInfo1.getHiddenSSID());
		wifimanager.disconnect();//断开当前wifi
		 */
		}
	}

	public String get_wifi_ssid()
	{
		WifiInfo WifiInfo1 = wifimanager.getConnectionInfo();
		return WifiInfo1.getSSID().trim();
	}

	public void wifi_dis()
	{
		wifimanager.disconnect();//断开当前wifi
	}


	public boolean WiFi_start()
	{

		Log.i("TAG", "GetWifi_button_ok1: " + wifimanager.getWifiState());
		switch (wifimanager.getWifiState())
		{
			case WifiManager.WIFI_STATE_DISABLED:
				Log.i("TAG", "GetWifi_button_ok1: WiFi已经完全关闭的状态");
				//break;
			case WifiManager.WIFI_STATE_DISABLING:
				Log.i("TAG", "GetWifi_button_ok1: WiFi正要关闭的状态");
				//break;
			case WifiManager.WIFI_STATE_UNKNOWN:
				Log.i("TAG", "GetWifi_button_ok1: WiFi未知的状态, WiFi开启, 关闭过程中出现异常");
				//break;
			case WifiManager.WIFI_STATE_ENABLING:
				Log.i("TAG", "GetWifi_button_ok1: WiFi正要开启的状态");
				return false;
			//break;
			case WifiManager.WIFI_STATE_ENABLED:
				//Log.i("TAG", "GetWifi_button_ok1: WiFi已经完全开启的状态");

				if (ActivityCompat.checkSelfPermission(this.context,
													   Manifest.permission.ACCESS_FINE_LOCATION
				) != PackageManager.PERMISSION_GRANTED)
				{
					return false;
				}
				wifi_list = wifimanager.getScanResults();//获取扫描到的wifi名字
				for (int i = 0; i < wifi_list.size(); i++)
				{
					Log.i("TAG", "wifi	" + i + ": " + wifi_list.get(i).SSID);
				}

				return true;
			//break;

		}
		return false;
	}

	public boolean wifi_exist(String ssid)
	{
		if (WiFi_start())
		{
			if (ActivityCompat.checkSelfPermission(this.context,
												   Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED)
			{
				return false;
			}
			wifi_list = wifimanager.getScanResults();//获取扫描wifi
			//wifi_list.get(i).SSID//wifi的名字
			for (int i = 0; i < wifi_list.size(); i++)
			{
				if(ssid.equals(wifi_list.get(i).SSID))
				{
					return true;
				}
			}
		}
		return false;
	}


	public String IP_inttoString(int ip1)
	{
		return (ip1&0xff)+"."+((ip1>>8)&0xff)+"."+((ip1>>16)&0xff)+"."+((ip1>>24)&0xff);
	}
	public String get_IP_service()
	{
		int ip1=wifimanager.getConnectionInfo().getIpAddress();
		return (ip1&0xff)+"."+((ip1>>8)&0xff)+"."+((ip1>>16)&0xff)+"."+1;
	}
	public int get_IP()
	{
		return wifimanager.getConnectionInfo().getIpAddress();
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();

	}
}
