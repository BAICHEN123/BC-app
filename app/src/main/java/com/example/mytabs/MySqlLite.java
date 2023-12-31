package com.example.mytabs;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class MySqlLite extends SQLiteOpenHelper
{

	public MySqlLite(Context context)
	{
		super(context, "UserData.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		//初次创建sqllsit对象时调用此语句
		db.execSQL("create table UserData(id integer primary key,email char(200) UNIQUE,name char(200),new_time INTEGER,head_md5 char(32),head_end char(5),net_md5 char(32),sex  INTEGER,uid INTEGER)");
		db.execSQL("insert into UserData(id,uid) values(1,0)");
		//创建用户存档，id=1储存最后一次登录的用户的邮箱。从id=1开始，正常储存用户信息
		/*
		create table UserData(
		 	integer	primary key ,	//id
		 	email	char(200) UNIQUE,		//email
		 	name	char(200),				//name
		 	new_time	INTEGER,		//最后一次和服务器互动时间，用于本地强制注销
		 	head_md5	char(32),		//储存用户头像文件的名字，用MD5可以防止重复，但是求MD5需要时间，以后再改，暂时以用户邮箱和用户最后一次修改头像的时间命名
		 	head_end	char(5),		//储存用户的头像文件的后缀名
		 	net_md5	char(32),		//储存用户的服务器请求码
		 	sex	INTEGER,			//性别
		 	uid	INTEGER			//服务器数据库的用户id
		 	)
		 */
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		//当super的version变化时会调用这个语句
		db.execSQL("alter table UserData add uid INTEGER");
	}

	@SuppressLint("Recycle")
	public void add_UserData(UserData userdata)//用户的数据更新和入库，主要给注册调用
	{
		Cursor cursor;
		cursor = this.getWritableDatabase().rawQuery("select * from UserData where id>1 and email=?", new String[]{userdata.email});
		if (cursor.moveToFirst())
		{
			//此用户的账号本地有记录，更新数据即可

			Log.i("TAG", "addUserData: 1");
			this.getWritableDatabase().close();
			Log.i("TAG", "addUserData: f1");
			ContentValues values = new ContentValues();
			Log.i("TAG", "addUserData: f2");
			//values.put("email",userdata.email);
			//if (!Objects.equals(userdata.name, ""))
			values.put("name", userdata.name);
			values.put("sex", userdata.sex);
			//if (!Objects.equals(userdata.net_md5, ""))
			values.put("net_md5", userdata.net_md5);
			//if (userdata.new_time != 0)
			values.put("new_time", userdata.new_time);
			//if (!Objects.equals(userdata.user_head[0], ""))
			values.put("head_md5", userdata.user_head[0]);
			//if (!Objects.equals(userdata.user_head[1], "head"))
			values.put("head_end", userdata.user_head[1]);
			//if (userdata.uid!=0)
			values.put("uid", userdata.uid);
			//values.put("id",1);
			this.getWritableDatabase().update("UserData", values, "id>1 and email=?", new String[]{userdata.email});
			Log.i("TAG", "addUserData: f4");
			this.getWritableDatabase().close();
			Log.i("TAG", "addUserData: f5");
		}
		else
		{
			//此用户是本地新用户，需要入库
			Log.i("TAG", "addUserData: 2");
			this.getWritableDatabase().close();
			ContentValues values = new ContentValues();
			values.put("email", userdata.email);
			values.put("uid", userdata.uid);
			values.put("name", userdata.name);
			values.put("sex", userdata.sex);
			values.put("net_md5", userdata.net_md5);
			values.put("new_time", userdata.new_time);
			values.put("head_md5", userdata.user_head[0]);
			values.put("head_end", userdata.user_head[1]);
			this.getWritableDatabase().insert("UserData", null, values);
			this.getWritableDatabase().close();
			Log.i("TAG", "addUserData: 3");
		}
		//cursor.close();
		new_UserData(userdata);
	}

	@SuppressLint("Recycle")
	private void new_UserData(UserData userdata)//用于记录最后一次登录的用户
	{
		Cursor cursor;
		cursor = this.getWritableDatabase().rawQuery("select * from UserData where uid=? and id>1", new String[]{String.valueOf(userdata.uid)});
		if (cursor.moveToFirst())
		{
			//此用户的账号本地有记录，更新数据即可

			Log.i("TAG", "addUserData: 1号元素更新");
			this.getWritableDatabase().close();
			ContentValues values = new ContentValues();
			values.put("uid", userdata.uid);
			this.getWritableDatabase().update("UserData", values, "id=?", new String[]{String.valueOf(1)});
			this.getWritableDatabase().close();
		}
	}

	public void exit_user()
	{
		ContentValues values = new ContentValues();
		values.put("uid", 0);
		this.getWritableDatabase().update("UserData", values, "id=?", new String[]{String.valueOf(1)});
		this.getWritableDatabase().close();
	}

	@SuppressLint("Recycle")
	public void setSqlUserData(UserData userdata)////用户的数据更新和入库，编辑用户信息界面调用
	{
		Cursor cursor;
		cursor = this.getWritableDatabase().rawQuery("select * from UserData where id>1 and email=?", new String[]{userdata.email});
		if (cursor.moveToFirst())
		{
			//此用户的账号本地有记录，更新数据即可

			Log.i("TAG", "addUserData: 用户信息更新开始");
			this.getWritableDatabase().close();
			ContentValues values = new ContentValues();
			if (!"".equals(userdata.name))
				values.put("name", userdata.name);
			if (userdata.sex != 2)
				values.put("sex", userdata.sex);
			if (!"".equals(userdata.net_md5))
				values.put("net_md5", userdata.net_md5);
			if (userdata.new_time != 0)
				values.put("new_time", userdata.new_time);
			if (!"".equals(userdata.user_head[0]))
				values.put("head_md5", userdata.user_head[0]);
			if (!"".equals(userdata.user_head[1]))
				values.put("head_end", userdata.user_head[1]);
			//if (userdata.uid!=0)
			//values.put("uid", userdata.uid);
			this.getWritableDatabase().update("UserData", values, "id>1 and email=?", new String[]{userdata.email});
			this.getWritableDatabase().close();
			Log.i("TAG", "addUserData: 用户信息更新结束");
		}
		else
		{
			Log.i("TAG", "addUserData: 编辑用户信息界面调用，未查询到用户信息");
			this.getWritableDatabase().close();
		}
	}

	public UserData get_login_data()
	{
		UserData userdata = new UserData();
		SQLiteDatabase db = this.getWritableDatabase();
		@SuppressLint("Recycle") Cursor cursor = db.rawQuery("select * from UserData where uid=(select uid from UserData where id=1) and id <> ?", new String[]{String.valueOf(1)});
		if (cursor.moveToFirst())
		{
			userdata.email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
			userdata.user_head[0] = cursor.getString(cursor.getColumnIndexOrThrow("head_md5"));
			userdata.user_head[1] = cursor.getString(cursor.getColumnIndexOrThrow("head_end"));
			userdata.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
			userdata.sex = Byte.parseByte(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("sex"))));
			userdata.new_time = cursor.getLong(cursor.getColumnIndexOrThrow("new_time"));
			userdata.net_md5 = cursor.getString(cursor.getColumnIndexOrThrow("net_md5"));
			userdata.uid = cursor.getLong(cursor.getColumnIndexOrThrow("uid"));
		}
		else
		{
			userdata = null;
		}
		return userdata;
	}

	public UserData get_login_data(String email)
	{
		UserData userdata = new UserData();
		SQLiteDatabase db = this.getWritableDatabase();

		@SuppressLint("Recycle") Cursor cursor = db.rawQuery("select * from UserData where id>1 and email=?", new String[]{email});
		if (cursor.moveToFirst())
		{
			userdata.email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
			userdata.user_head[0] = cursor.getString(cursor.getColumnIndexOrThrow("head_md5"));
			userdata.user_head[1] = cursor.getString(cursor.getColumnIndexOrThrow("head_end"));
			userdata.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
			userdata.sex = Byte.parseByte(cursor.getString(cursor.getColumnIndexOrThrow("sex")));
			userdata.new_time = cursor.getLong(cursor.getColumnIndexOrThrow("new_time"));
			userdata.net_md5 = cursor.getString(cursor.getColumnIndexOrThrow("net_md5"));
			userdata.uid = cursor.getLong(cursor.getColumnIndexOrThrow("uid"));
		}
		else
		{
			userdata = null;
		}
		return userdata;
	}


	public String[] get_all_email()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		@SuppressLint("Recycle") Cursor cursor = db.rawQuery("select email from UserData where id>1", null);
		if(cursor.getCount()==0)
		{
			return null;
		}
		String[] str_email=new String[cursor.getCount()];
		int i=0;
		while(cursor.moveToNext())
		{
			str_email[i]=cursor.getString(cursor.getColumnIndexOrThrow("email"));
			i=i+1;
		}
		return str_email;
	}
}
