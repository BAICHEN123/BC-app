package com.example.mytabs;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MyHttp
{
	static final int ERROR = -1;
	public static final int TIMEOUT=5000;//http网络请求超时时间

//	static final String IP = "http://192.168.31.28:8080/";
	static final String IP = "http://121.89.243.207:8080/";
	//static final String IP = "http://192.168.137.5:8080/";
	//static final String IP = "http://192.168.0.102:8080/";
	//static final String IP = "http://10.120.52.165:8080/";
	//private static
	//加个线程锁，防止数量过多

	//错误码全部返回-1 ERROR
	//设置成功码可选

	public void thread_send_Post_String(final String net_url, final String post_data, final Handler handler, final int CHANGE_UI)
	{
		new Thread()
		{
			public void run()
			{
				//String post_data = "email=" + userdata.email + "&password1=" + userdata.password + "&password2=" + user_password;
				OutputStreamWriter out = null;
				BufferedReader in = null;
				StringBuilder result = new StringBuilder();
				try
				{
					URL realUrl = new URL(net_url);
					// 打开和URL之间的连接
					URLConnection conn = realUrl.openConnection();
					conn.setConnectTimeout(TIMEOUT);
					// 设置通用的请求属性
					conn.setRequestProperty("Accept", "*/*");
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
					// 发送POST请求必须设置如下两行
					conn.setDoOutput(true);
					conn.setDoInput(true);
					// 获取URLConnection对象对应的输出流
					out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
					// 发送请求参数
					out.append(post_data);
					// flush输出流的缓冲
					out.flush();
					// 定义BufferedReader输入流来读取URL的响应
					in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
					String line;
					while ((line = in.readLine()) != null)
					{
						result.append(line.trim());
					}
					Message msg = new Message();
					msg.what = CHANGE_UI;
					msg.obj = result.toString();
					handler.sendMessage(msg);
				}
				catch (Exception e)
				{
					//System.out.println("发送 POST 请求出现异常！" + e);
					//e.printStackTrace();
					Log.i("MyHttp", "MyHttp_POST: 请求异常1" + e.getMessage());
					Message msg = new Message();
					msg.what = ERROR;
					//msg.obj = result;
					handler.sendMessage(msg);
				}
				//使用finally块来关闭输出流、输入流
				finally
				{
					try
					{
						if (out != null)
						{
							out.close();
						}
						if (in != null)
						{
							in.close();
						}
					}
					catch (IOException e)
					{
						Log.i("MyHttp", "MyHttp_POST: 关闭异常2" + e.getMessage());
						//ex.printStackTrace();
					}
				}
			}
		}.start();
	}

	public void thread_send_Post_String(final String net_url, final byte[] post_data, final Handler handler, final int CHANGE_UI)
	{
		new Thread()
		{
			public void run()
			{
				//String post_data = "email=" + userdata.email + "&password1=" + userdata.password + "&password2=" + user_password;
				OutputStream out = null;
				BufferedReader in = null;
				StringBuilder result = new StringBuilder();
				try
				{
					URL realUrl = new URL(net_url);
					// 打开和URL之间的连接
					URLConnection conn = realUrl.openConnection();
					conn.setConnectTimeout(TIMEOUT);
					// 设置通用的请求属性
					conn.setRequestProperty("Accept", "*/*");
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
					// 发送POST请求必须设置如下两行
					conn.setDoOutput(true);
					conn.setDoInput(true);
					// 获取URLConnection对象对应的输出流
					out = conn.getOutputStream();
					// 发送请求参数
					//out.append(post_data);
					out.write(post_data);
					// flush输出流的缓冲
					out.flush();

					// 定义BufferedReader输入流来读取URL的响应
					in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
					String line;
					while ((line = in.readLine()) != null)
					{
						result.append(line.trim());
					}
					Message msg = new Message();
					msg.what = CHANGE_UI;
					msg.obj = result.toString();
					handler.sendMessage(msg);
				}
				catch (Exception e)
				{
					//System.out.println("发送 POST 请求出现异常！" + e);
					//e.printStackTrace();
					Log.i("MyHttp", "MyHttp_POST: 请求异常1" + e.getMessage());

					Message msg = new Message();
					msg.what = ERROR;
					//msg.obj = result;
					handler.sendMessage(msg);
				}
				//使用finally块来关闭输出流、输入流
				finally
				{
					try
					{
						if (out != null)
						{
							out.close();
						}
						if (in != null)
						{
							in.close();
						}
					}
					catch (IOException e)
					{
						Log.i("MyHttp", "MyHttp_POST: 关闭异常2" + e.getMessage());
						//ex.printStackTrace();
					}
				}
			}
		}.start();
	}

//
//	public void send_Get_String(final String net_url, final Handler handler, final int CHANGE_UI)
//	{
//		new Thread()
//		{
//
//			public void run()
//			{
//				try
//				{
//					BufferedReader in = null;
//					StringBuilder result = new StringBuilder();
//					Log.i("TAG", "run: get1");
//					HttpURLConnection conn;
//					URL url = new URL(net_url);
//					conn = (HttpURLConnection) url.openConnection();
//					conn.setRequestMethod("GET");
//					conn.setRequestProperty("accept", "*/*");
//					conn.setRequestProperty("connection", "Keep-Alive");
//					conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
//					conn.setConnectTimeout(TIMEOUT);
//					conn.setReadTimeout(TIMEOUT);
//					int code = conn.getResponseCode();
//					if (code == 200)
//					{
//						/*InputStream is = conn.getInputStream();
//						byte[] bytes;
//						Log.i("TAG", "run: 请求"+ is.available());
//						bytes = new byte[is.available()];
//						is.read(bytes);*/
//						in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//						String line;
//						while ((line = in.readLine()) != null)
//						{
//							result.append(line.trim());
//						}
//						Message msg = new Message();
//						msg.what = CHANGE_UI;
//						Log.i("TAG", "run: 请求" + result);
//						msg.obj = result.toString();
//						handler.sendMessage(msg);
//					}
//					else
//					{
//						Log.i("TAG", "run: 请求失败");
//						Message msg = new Message();
//						msg.what = ERROR;
//						handler.sendMessage(msg);
//					}
//				}
//				catch (Exception e)
//				{
//					Log.i("TAG", "run: get请求出错" + e.getMessage());
//					//e.printStackTrace();
//					Message msg = new Message();
//					msg.what = ERROR;
//					handler.sendMessage(msg);
//				}
//			}
//		}.start();
//	}

	//此函数有数据接收上线，1G以上的数据请自行斟酌
	public void thread_send_get__bytes(final String net_url, final Handler handler, final int CHANGE_UI)
	{
		new Thread()//开启请求发送邮件的线程
		{
			public void run()
			{
				InputStream is = null;
				HttpURLConnection conn = null;
				try
				{

					URL url = new URL(net_url);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(TIMEOUT);
					conn.setReadTimeout(TIMEOUT);
					conn.setRequestProperty("accept", "*/*");
					//conn.setRequestProperty("Accept-Encoding", "identity");
					conn.setRequestProperty("connection", "Keep-Alive");
					conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
					conn.setDoInput(true);
					int code = conn.getResponseCode();
					if (code == 200)
					{
						ArrayList<byte[]> buff_byte=new ArrayList<>();
						is = conn.getInputStream();
						final int len=1024;
						byte[] bytes = new byte[len];
						int ff=is.read(bytes,0,len);//ff统计字节数
						int end=0;//记录返回码
						//Log.i("TAG1", "\n "+ff);
						while(end!=-1)
						{
							//Log.i("TAG", " "+ff);
							ff=ff+end;
							if(ff==len)
							{
								buff_byte.add(bytes);
								bytes=new byte[len];
								ff=0;
							}
							end=is.read(bytes,ff,len-ff);

							//Log.i("TAG", " "+ff);
						}
						//buff_byte.add(bytes);//这是最后一个包，不完整
						//Log.i("ZhuCe", "run:总大小 B " + (buff_byte.size()*len+ff));//这里计算的长度是正确的
						byte[] bytess=new byte[buff_byte.size()*len+ff];
						int len_back=0;
						for(byte[] byte1:buff_byte)
						{
							for(byte byte_:byte1)
							{
								bytess[len_back]=byte_;
								len_back++;
							}
						}
						for(int i=0;i<ff;i++)
						{
							bytess[len_back]=bytes[i];
							len_back++;
						}
						//Log.i("TAG", "run: "+MyKeyer.MyMd5(bytess));;
						//Log.i("ZhuCe", "run:   len_back  "+len_back);
						Message msg = new Message();
						msg.what = CHANGE_UI;
						msg.obj = bytess;
						handler.sendMessage(msg);
					}
					else
					{
						Log.i("TAG", "run: 请求失败");
						Message msg = new Message();
						msg.what = ERROR;
						handler.sendMessage(msg);
					}
				}
				catch (Exception e)
				{
					Log.i("TAG", "run: 请求出错" + e.getMessage());
					//e.printStackTrace();
					Message msg = new Message();
					msg.what = ERROR;
					handler.sendMessage(msg);
				}
				finally
				{
					try
					{
						if (is != null)
						{
							is.close();
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					if (conn != null)
					{
						conn.disconnect();
					}
				}
			}
		}.start();
	}

//	public void send_Post_Bytes(final String net_url, final String post_data, final Handler handler, final int CHANGE_UI)
//	{
//		new Thread()
//		{
//			public void run()
//			{
//				//String post_data = "email=" + userdata.email + "&password1=" + userdata.password + "&password2=" + user_password;
//				OutputStreamWriter out = null;
//				InputStream is = null;
//				try
//				{
//					URL realUrl = new URL(net_url);
//					// 打开和URL之间的连接
//					URLConnection conn = realUrl.openConnection();
//					// 设置通用的请求属性
//					conn.setRequestProperty("Accept", "*/*");
//					conn.setRequestProperty("Connection", "Keep-Alive");
//					conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
//					conn.setRequestProperty("Conrenr-Length", post_data.length() + "");
//					// 发送POST请求必须设置如下两行
//					conn.setDoOutput(true);
//					conn.setDoInput(true);
//					// 获取URLConnection对象对应的输出流
//					out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
//					// 发送请求参数
//					out.append(post_data);
//					// flush输出流的缓冲
//					out.flush();
//					// 定义BufferedReader输入流来读取URL的响应
//					is = conn.getInputStream();
//					byte[] bytes = new byte[is.available()];
//					is.read(bytes);
//
//					Message msg = new Message();
//					msg.what = CHANGE_UI;
//					msg.obj = bytes.clone();
//					handler.sendMessage(msg);
//				}
//				catch (Exception e)
//				{
//					//System.out.println("发送 POST 请求出现异常！" + e);
//					//e.printStackTrace();
//					Log.i("MyHttp", "MyHttp_POST: 请求异常1" + e.getMessage());
//
//					Message msg = new Message();
//					msg.what = ERROR;
//					//msg.obj = result;
//					handler.sendMessage(msg);
//				}
//				//使用finally块来关闭输出流、输入流
//				finally
//				{
//					try
//					{
//						if (out != null)
//						{
//							out.close();
//						}
//						if (is != null)
//						{
//							is.close();
//						}
//					}
//					catch (IOException e)
//					{
//						Log.i("MyHttp", "MyHttp_POST: 关闭异常2" + e.getMessage());
//						//ex.printStackTrace();
//					}
//				}
//			}
//		}.start();
//	}

//	public void send_Get_Bitmap(final String net_url, final Handler handler, final int CHANGE_UI)
//	{
//		new Thread()//开启请求发送邮件的线程
//		{
//
//			public void run()
//			{
//				InputStream is = null;
//				try
//				{
//					HttpURLConnection conn;
//					URL url = new URL(net_url);
//					conn = (HttpURLConnection) url.openConnection();
//					conn.setRequestMethod("GET");
//					conn.setConnectTimeout(TIMEOUT);
//					conn.setRequestProperty("Accept", "*/*");
//					conn.setRequestProperty("Connection", "Keep-Alive");
//					conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
//					int code = conn.getResponseCode();
//					if (code == 200)
//					{
//						is = conn.getInputStream();
//						//String string_getdata = new String(bytes);
//						//Log.i("ZhuCe", "run: " + string_getdata.trim());
//						Bitmap bitmap;
//						bitmap = BitmapFactory.decodeStream(is);
//						is.close();
//						//myViewData.setBitmapData(bitmap);
//						Message msg = new Message();
//						msg.what = CHANGE_UI;
//						//Log.i("TAG", "run: 请求1"+ Arrays.toString(bytes));
//						msg.obj = bitmap;
//						handler.sendMessage(msg);
//					}
//					else
//					{
//						Log.i("TAG", "run: 请求失败");
//						Message msg = new Message();
//						msg.what = ERROR;
//						msg.obj = CHANGE_UI;
//						handler.sendMessage(msg);
//					}
//				}
//				catch (Exception e)
//				{
//					Log.i("TAG", "run: 请求出错" + net_url + e.getMessage());
//					//e.printStackTrace();
//					Message msg = new Message();
//					msg.what = ERROR;
//					msg.obj = CHANGE_UI;
//					handler.sendMessage(msg);
//				}
//				finally
//				{
//					try
//					{
//						if (is != null)
//						{
//							is.close();
//						}
//					}
//					catch (IOException e)
//					{
//						Log.i("MyHttp", "MyHttp_POST: 关闭异常2" + e.getMessage());
//						//ex.printStackTrace();
//					}
//				}
//			}
//		}.start();
//	}

//	public Message send_Post_String(final String net_url, final String post_data, final int CHANGE_UI)
//	{
//		//String post_data = "email=" + userdata.email + "&password1=" + userdata.password + "&password2=" + user_password;
//		OutputStreamWriter out = null;
//		BufferedReader in = null;
//		StringBuilder result = new StringBuilder();
//		Message msg = new Message();
//		msg.what = ERROR;
//		try
//		{
//			URL realUrl = new URL(net_url);
//			// 打开和URL之间的连接
//			URLConnection conn = realUrl.openConnection();
//			conn.setConnectTimeout(TIMEOUT);
//			// 设置通用的请求属性
//			conn.setRequestProperty("Accept", "*/*");
//			conn.setRequestProperty("Connection", "Keep-Alive");
//			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
//			// 发送POST请求必须设置如下两行
//			conn.setDoOutput(true);
//			conn.setDoInput(true);
//			// 获取URLConnection对象对应的输出流
//			out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
//			// 发送请求参数
//			out.append(post_data);
//			// flush输出流的缓冲
//			out.flush();
//			// 定义BufferedReader输入流来读取URL的响应
//			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//			String line;
//			while ((line = in.readLine()) != null)
//			{
//				result.append(line.trim());
//			}
//			//Message msg = new Message();
//			msg.what = CHANGE_UI;
//			msg.obj = result.toString();
//		}
//		catch (IOException e)
//		{
//			//System.out.println("发送 POST 请求出现异常！" + e);
//			//e.printStackTrace();
//			Log.i("MyHttp", "MyHttp_POST: 请求异常1" + e.getMessage());
//			msg.what = ERROR;
//			//msg.obj = result;
//		}
//		//使用finally块来关闭输出流、输入流
//		finally
//		{
//			try
//			{
//				if (out != null)
//				{
//					out.close();
//				}
//				if (in != null)
//				{
//					in.close();
//				}
//			}
//			catch (IOException e)
//			{
//				Log.i("MyHttp", "MyHttp_POST: 关闭异常2" + e.getMessage());
//				//ex.printStackTrace();
//			}
//		}
//		return msg;
//	}

	public void thread_send_post_bytes(final String net_url, final byte[] post_data, final Handler handler, final int CHANGE_UI)
	{
		new Thread()
		{
			public void run()
			{
				byte[] bytes=send_Post_bytes(net_url,post_data);
				Message msg = new Message();
				msg.obj = bytes;
				if(bytes==null)
				{
					msg.what = ERROR;
				}
				else
				{
					msg.what = CHANGE_UI;
				}
				handler.sendMessage(msg);

			}
		}.start();
	}


	public byte[] send_Post_bytes(final String net_url, final byte[] post_data)
	{
		//String post_data = "email=" + userdata.email + "&password1=" + userdata.password + "&password2=" + user_password;
		OutputStream out = null;
		InputStream is = null;
		HttpURLConnection conn;
		byte[] bytess=null;
		try
		{
			URL realUrl = new URL(net_url);
			// 打开和URL之间的连接
			//URLConnection conn = realUrl.openConnection();
			conn = (HttpURLConnection) realUrl.openConnection();
			conn.setConnectTimeout(TIMEOUT);
			// 设置通用的请求属性
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = conn.getOutputStream();
			// 发送请求参数
			out.write(post_data);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应

			int code = conn.getResponseCode();
			if (code == 200)
			{
				ArrayList<byte[]> buff_byte=new ArrayList<>();
				is = conn.getInputStream();
				final int len=1024;
				byte[] bytes = new byte[len];
				int ff=is.read(bytes,0,len);//ff统计字节数
				int end=0;//记录返回码
				//Log.i("TAG1", "\n "+ff);
				while(end!=-1)
				{
					//Log.i("TAG", " "+ff);
					ff=ff+end;
					if(ff==len)
					{
						buff_byte.add(bytes);
						bytes=new byte[len];
						ff=0;
					}
					end=is.read(bytes,ff,len-ff);

					//Log.i("TAG", " "+ff);
				}
				//buff_byte.add(bytes);//这是最后一个包，不完整
				//Log.i("ZhuCe", "run:总大小 B " + (buff_byte.size()*len+ff));//这里计算的长度是正确的
				bytess=new byte[buff_byte.size()*len+ff];
				int len_back=0;
				for(byte[] byte1:buff_byte)
				{
					for(byte byte_:byte1)
					{
						bytess[len_back]=byte_;
						len_back++;
					}
				}
				for(int i=0;i<ff;i++)
				{
					bytess[len_back]=bytes[i];
					len_back++;
				}
				//Log.i("TAG", "run: "+MyKeyer.MyMd5(bytess));;
				//Log.i("ZhuCe", "run:   len_back  "+len_back);
			}

		}
		catch (IOException e)
		{
			//System.out.println("发送 POST 请求出现异常！" + e);
			//e.printStackTrace();
			Log.i("MyHttp", "MyHttp_POST: 请求异常1" + e.getMessage());
			//msg.obj = result;
		}
		//使用finally块来关闭输出流、输入流
		finally
		{
			try
			{
				if (out != null)
				{
					out.close();
				}
				if (is != null)
				{
					is.close();
				}
			}
			catch (IOException e)
			{
				Log.i("MyHttp", "MyHttp_POST: 关闭异常2" + e.getMessage());
				//ex.printStackTrace();
			}
		}

		return bytess;
	}

}
