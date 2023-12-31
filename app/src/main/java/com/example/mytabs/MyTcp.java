package com.example.mytabs;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MyTcp
{
	public Socket socket1;
	OutputStream os;
	InputStream is;
	Handler handler;
	final int TIME_INTERVAL = 100;////每个包的检测间隔 ms
	final int SUM_DETECTION = 300;//检测包的次数
	final static int ERROR_SEND = -2;//TCP链接故障，可能已经断开或者还未链接上
	final static int ERROR_READ = -1;//TCP链接故障，可能已经断开或者还未链接上
	final static int SUCCEED_SEND = 0;

	//需要自己开启线程调用
	public MyTcp(String IP, int port)
	{
		//启动对象时直接建立线程，一直监听，将收到的数据通过handle返回
		try
		{
			Log.e("TAG", "MyTcp: 尝试建立连接");
			socket1 = new Socket(IP, port);
			os = socket1.getOutputStream();
			is = socket1.getInputStream();
			handler = null;
			Log.e("TAG", "MyTcp: 建立成功");
		}
		catch (Exception e)
		{
			//Log.e("TAG", "MyTcp: 1"+e.getMessage());
			socket1 = null;
			//Log.e("TAG", "MyTcp: 2"+e.getMessage());
		}
	}

	/*
	传入参数之后直接可以调用，
	接收到的数据会通过 handler 直接传回
	发送数据结果会通过 handler 传回
	监听函数在收到最后一个包之后的约 SUM_DETECTION*TIME_INTERVAL ms 后死去，同时带走整个 TCP 链接
	 */
	public MyTcp(final String IP, final int port, final Handler handler)
	{
		this.handler = handler;
		new Thread()
		{
			@Override
			public void run()
			{
				super.run();
				//启动对象时直接建立线程，一直监听，将收到的数据通过handle返回
				try
				{
					Log.e("TAG", "MyTcp: 尝试建立连接0");
					socket1 = new Socket(IP, port);
					Log.e("TAG", "MyTcp: 尝试建立连接1");
					os = socket1.getOutputStream();
					is = socket1.getInputStream();
					Log.e("TAG", "MyTcp: 尝试建立连接2");

					Log.e("TAG", "MyTcp: 尝试建立连接3");
					Tcp_handler_get(handler);
					Log.e("TAG", "MyTcp: 建立成功");
				}
				catch (Exception e)
				{
					Log.e("TAG", "MyTcp: 1" + e.getMessage());
					socket1 = null;
					Log.e("TAG", "MyTcp: 2" + e.getMessage());
				}
			}
		}.start();
	}

	/*

	@SuppressLint("HandlerLeak")
	private Handler hander_1=new Handler()
	{
		public void handleMessage(Message msg)
		{
			if(msg.what>0)
			{//收到的数据
				byte[] data1=(byte[])msg.obj;
				try
				{
					Log.i("TAG", "handleMessage	1: "+ new String(data1,"GBK"));
				}
				catch (UnsupportedEncodingException e)
				{
					Log.i("TAG", "handleMessage	1: "+Arrays.toString(data1));
				}
			}
			else if(msg.what==0)
			{//发送成功
				byte[] data1=(byte[])msg.obj;
				try
				{
					Log.i("TAG", "handleMessage	0: "+ new String(data1,"GBK"));
				}
				catch (UnsupportedEncodingException e)
				{
					Log.i("TAG", "handleMessage	0: "+Arrays.toString(data1));
				}
				//Log.i("TAG", "handleMessage2: TCP链接故障，可能已经断开");
			}
			else
			{//错误
				Log.i("TAG", "handleMessage	-: TCP链接故障，可能已经断开");
			}
		}
	};

	 */

	public boolean Tcp_send(String data)
	{
		if (socket1 == null)
		{//初始化时失败，无法进行通讯
			return false;
		}
		try
		{
			os.write(data.getBytes("utf-8"));
			os.flush();
			return true;
		}
		catch (Exception e)
		{
			Log.e("TAG", "Tcp_send: " + e.getMessage());
			return false;
		}
	}

	public byte[] Tcp_get()
	{
		if (socket1 == null)
		{//初始化时失败，无法进行通讯
			Log.e("TAG", "Tcp_get: 获取内容失败1");
			return null;
		}
		try
		{
			byte[] bytes = new byte[is.available()];
			Log.e("TAG", "Tcp_get: 字节返回值	" + is.read(bytes));
			//String str1=new String(bytes,"gbk");
			return bytes;
		}
		catch (Exception e)
		{
			Log.e("TAG", "Tcp_get: 获取内容失败2");
			return null;
		}
	}

	public void Tcp_handler_send(byte[] data)
	{
		if (this.handler != null)
		{
			this.Tcp_handler_send(data, this.handler);
		}
	}


	public void Tcp_handler_send(final byte[] data, final Handler handler)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				if (socket1 == null)
				{//初始化时失败，无法进行通讯
					//return false;
					//Message msg = new Message();
					//msg.what = ERROR_SEND;
					//handler.sendMessage(msg);
					return;
				}
				try
				{
					os.write(data);
					os.flush();
					Message msg = new Message();
					msg.what = SUCCEED_SEND;
					msg.obj = data;
					handler.sendMessage(msg);
					//return true;
				}
				catch (Exception e)
				{
					Log.e("TAG", "Tcp_send: " + e.getMessage());
					//return false;
					if(socket1 == null)return ;
					Message msg = new Message();
					msg.what = ERROR_SEND;
					handler.sendMessage(msg);
				}
			}
		}.start();
	}


	/*
	TCP接收监听函数。
	从最后一个有效收包开始计时，线程一共存活 SUM_DETECTION*TIME_INTERVAL ms
 	*/
	private void Tcp_handler_get(final Handler handler)
	{
		new Thread()
		{
			public void run()
			{
				//从最后一个有效收包开始计时，线程一共存活 SUM_DETECTION*TIME_INTERVAL ms

				int i = SUM_DETECTION;
				while (true)
				{
					if (socket1 == null)
					{//初始化时失败，无法进行通讯
						Log.e("TAG", "Tcp_get: 线程已结束");
						break;
					}
					try
					{
						Thread.sleep(TIME_INTERVAL);
						byte[] bytes = new byte[is.available()];
						if (is.read(bytes) > 0)
						{
							Message msg = new Message();
							msg.what = 1;
							msg.obj = bytes;
							handler.sendMessage(msg);
							i = SUM_DETECTION;
						}
						else
						{
							if (i == 0)
							{
								break;
							}
							i--;
						}
						//String str1=new String(bytes,"gbk");
						//return bytes;
					}
					catch (Exception e)
					{
						if(socket1==null)return;
						Log.e("TAG", "Tcp_get: 获取内容失败2");
						//return null;
						Message msg = new Message();
						msg.what = ERROR_READ;
						handler.sendMessage(msg);
						break;
					}
				}
				if (socket1 != null)
				{
					try
					{
						//在对象被销毁之前释放TCP链接
						socket1.close();
						is.close();
						os.close();
						Log.e("TAG", "MyTcp: 链接已关闭");
						socket1 = null;
					}
					catch (Exception e)
					{
						Log.e("TAG", "MyTcp: 链接关闭失败" + e.getMessage());
					}
				}
			}
		}.start();
	}

	/*
	TCP接收监听函数。
	从最后一个有效收包开始计时，线程一共存活 SUM_DETECTION*TIME_INTERVAL ms
	 */
	public void Tcp_handler_get(final Handler handler,final int id)
	{
		new Thread()
		{
			public void run()
			{
				//从最后一个有效收包开始计时，线程一共存活 SUM_DETECTION*TIME_INTERVAL ms

				int i = SUM_DETECTION;
				while (true)
				{
					if (socket1 == null)
					{//初始化时失败，无法进行通讯
						Log.e("TAG", "Tcp_get:线程已结束");
						break;
					}
					try
					{
						Thread.sleep(TIME_INTERVAL);
						byte[] bytes = new byte[is.available()];
						if (is.read(bytes) > 0)
						{
							Message msg = new Message();
							msg.what = id;
							msg.obj = bytes;
							handler.sendMessage(msg);
							i = SUM_DETECTION;
						}
						else
						{
							if (i == 0)
							{
								break;
							}
							i--;
						}
						//String str1=new String(bytes,"gbk");
						//return bytes;
					}
					catch (InterruptedException e)//sleep
					{
						Log.e("TAG", "Tcp_get: sleep获取内容失败2");
						//return null;
					}
					catch (IOException e)//available
					{
						if(socket1 == null)return ;
						Log.e("TAG", "run: IOException TCP流读取失败"+e.getMessage());
						Message msg = new Message();
						msg.what = ERROR_READ;
						handler.sendMessage(msg);
					}
				}
				if (socket1 != null)
				{
					try
					{
						//在对象被销毁之前释放TCP链接
						socket1.close();
						is.close();
						os.close();
						Log.e("TAG", "MyTcp: 链接已关闭");
						socket1 = null;
					}
					catch (Exception e)
					{
						Log.e("TAG", "MyTcp: 链接关闭失败" + e.getMessage());
					}
				}
			}
		}.start();
	}

	//强制销毁之前关闭所有链接
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		if (socket1 != null)
		{
			try
			{
				//在对象被销毁之前释放TCP链接
				socket1.close();
				if(is!=null)is.close();
				if(os!=null)os.close();
				Log.e("TAG", "MyTcp: 链接已关闭");
				socket1 = null;//监听线程检测到此值为空会结束
			}
			catch (Exception e)
			{
				Log.e("TAG", "MyTcp: 链接关闭失败" + e.getMessage());
			}
		}
	}


}
