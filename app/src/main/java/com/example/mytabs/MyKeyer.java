package com.example.mytabs;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

class MyKeyer
{

	/*调用示范  已过期
	ClassMyMd5 mymd5=new ClassMyMd5();
	try
		{
			byte[] key="ab".getBytes();
			byte[] data="让俺试试中文".getBytes();
			byte[] end;
			end=mymd5.keyer_get_bytes(data,key);
			System.out.print(new String(end));
			System.out.print(mymd5.keyer_get_string(end,key));
		}
		catch(Exception e)
		{
			System.out.print("转换出错");
		}
	 */
	/*
		ClassMyMd5 mymd5=new ClassMyMd5();
		System.out.print(mymd5.MyMd5("abb")+"\n");
		try
		{
			System.out.print(mymd5.MyMd5("abb".getBytes())+"\n");
		}
		catch(Exception e)
		{
			System.out.print("转换出错");
		}
	 */
	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static final char[] md5_end=new char[32];
	public static String MyMd5(String md5_data)
	{
		try
		{
			MessageDigest md=MessageDigest.getInstance("MD5");
			md.update(md5_data.getBytes("UTF-8"));
			byte[] digest = md.digest();
			//System.out.print(digest.length);
			//System.out.print(String.valueOf(digest)+"\n");
			for(int i=0;i<16;i=i+1)
			{
				md5_end[2*i+1]=HEX_DIGITS[digest[i]&15];
				md5_end[2*i]=HEX_DIGITS[(digest[i]&240)>>4];
				//System.out.print(i);
			}
			return String.valueOf(md5_end);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	public static String MyMd5(byte[] md5_data)
	{
		try
		{
			MessageDigest md=MessageDigest.getInstance("MD5");
			md.update(md5_data);
			byte[] digest = md.digest();
			//System.out.print(digest.length);
			//System.out.print(String.valueOf(digest)+"\n");
			for(int i=0;i<16;i=i+1)
			{
				md5_end[2*i+1]=HEX_DIGITS[digest[i]&15];
				md5_end[2*i]=HEX_DIGITS[(digest[i]&240)>>4];
				//System.out.print(i);
			}
			return String.valueOf(md5_end);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public static byte[] keyer_get_bytes(byte[] data,byte[] key)
	{
		byte[] data_end=new byte[data.length];
		for(int i=0;i<data.length;i++)
		{
			data_end[i]=(byte)((byte)(data[i]&~key[i%key.length])|(byte)(~data[i]&key[i%key.length]));
		}
		return data_end;
	}

	public static byte[] keyer_get_bytes(String data1,String key1)
	{
		byte[] data;
		byte[] key;
		try
		{
			data=data1.getBytes();
			key=key1.getBytes();

		}
		catch(Exception e)
		{
			return null;
		}
		byte[] data_end=new byte[data.length];
		for(int i=0;i<data.length;i++)
		{
			data_end[i]=(byte)((byte)(data[i]&~key[i%key.length])|(byte)(~data[i]&key[i%key.length]));
		}
		return data_end;
	}

	public static String keyer_get_string(byte[] data,byte[] key)
	{
		byte[] data_end=new byte[data.length];
		for(int i=0;i<data.length;i++)
		{
			data_end[i]=(byte)((byte)(data[i]&~key[i%key.length])|(byte)(~data[i]&key[i%key.length]));
		}
		return new String(data_end);
	}
	public static String keyer_get_string(byte[] data,String key1)
	{
		byte[] key;
		try
		{
			key=key1.getBytes();
		}
		catch(Exception e)
		{
			return null;
		}
		byte[] data_end=new byte[data.length];
		for(int i=0;i<data.length;i++)
		{
			data_end[i]=(byte)((byte)(data[i]&~key[i%key.length])|(byte)(~data[i]&key[i%key.length]));
		}
		return new String(data_end);
	}

	public byte[] to_hex(String str1)
	{
		try
		{
			byte[] a=str1.getBytes("utf-8");
			byte[] b=new byte[a.length*2];
			int p1=0,p2=0;
			for(p1=0,p2=0;p1<a.length;p1++,p2=p2+2)
			{
				b[p2]=(byte)((a[p1]>>4)|64);
				b[p2+1]=(byte)((a[p1]&15)|64);
			}
			return b;
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
