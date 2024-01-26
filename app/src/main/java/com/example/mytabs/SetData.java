package com.example.mytabs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class SetData extends Activity
{
	private static final int RC_CHOOSE_PHOTO = 1;
	protected static final int CHANGE_UI = 0, ERROR = -1;
	EditText setdata_name;
	TextView setdata_email;
	RadioButton manradio, womanradio;
	ImageView setdata_image;
	UserData userdata;
	Intent intent;
	boolean path = false;
	boolean thread_key_pic = false, thread_key_userdata = false, key_save = true;
	boolean thread_r_pic = true;
	MySqlLite sql;
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{

		Log.i("TAG", "onCreate: 打开编辑用户信息界面");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setdata);
		sql=new MySqlLite(getApplicationContext());
		userdata = sql.get_login_data();
		intent = new Intent();
		setdata_name = findViewById(R.id.setdata_name);
		setdata_email = findViewById(R.id.setdata_email);
		manradio = findViewById(R.id.radioButton1);
		womanradio = findViewById(R.id.radioButton2);
		setdata_image = findViewById(R.id.setdata_image);
		setdata_name.setText(userdata.name);
		setdata_email.setText(userdata.email);
		if (userdata.sex == 1)
		{
			manradio.setChecked(true);
		}
		else if (userdata.sex == 0)
		{
			womanradio.setChecked(true);
		}

		//判断是否有图片，并加载
		if (userdata.user_head[0].compareTo("") != 0)
		{
			try
			{
				//FileInputStream is = new FileInputStream(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + userdata.user_head[0] + "." + userdata.user_head[1]);
				FileInputStream is = getApplicationContext().openFileInput(userdata.user_head[0] + "." + userdata.user_head[1]);
				Bitmap bitmap;
				bitmap = BitmapFactory.decodeStream(is);
				setdata_image.setImageBitmap(bitmap);
				is.close();
				Log.i("TAG", "onCreate: setImageBitmap 11");

			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i("TAG", "onActivityResult: 用户头像加载失败");
			}
		}
		Log.i("TAG", "onCreate: 打开编辑用户信息界面成功");
	}

	public void setdata_exit(View v)
	{
		//取消修改信息的操作
		//结束该界面，返回上个页面
		this.finish();
	}


	@SuppressLint("HandlerLeak")//用户文字信息更新
	private final Handler handler_update = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(SetData.this, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(SetData.this, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == 0)
			{
				//#温度:28#湿度:68#@灯0[0-1]:1#@灯1[0-1]:1#
				//msg.what 是视图的编号，从0开始
				String str_data = (String) msg.obj;
				Toast.makeText(getApplicationContext(), str_data, Toast.LENGTH_SHORT).show();
				thread_key_userdata = true;
				if (thread_key_pic)
				{
					//Toast.makeText(SetData.this, "信息同步成功^_^", Toast.LENGTH_SHORT).show();
					//更新本地数据库
					Log.i("TAG", "onCreate: 尝试编辑数据库");
					MySqlLite sql = new MySqlLite(getApplicationContext());
					sql.setSqlUserData(userdata);
					Log.i("TAG", "onCreate: 编辑数据库成功");
					finish();
				}
			}
			else if (msg.what > 0)
			{
				Toast.makeText(SetData.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
			}
		}
	};
	@SuppressLint("HandlerLeak")//用户头衔更新
	private final Handler handler_update_pic = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.what >= CHANGE_UI)
			{
				String post_data = (String) msg.obj;
				if (post_data.startsWith("update 1"))
				{
					Log.i("TAG", "setdata_save: 成功上传用户头像  " + userdata.user_head[0]);
					thread_key_pic = true;
					if (thread_key_userdata)//文字图片信息全部同步成功
					{
						Toast.makeText(SetData.this, "信息同步成功^_^", Toast.LENGTH_SHORT).show();
						//更新本地数据库
						Log.i("TAG", "onCreate: 尝试编辑数据库");
						MySqlLite sql = new MySqlLite(getApplicationContext());
						sql.setSqlUserData(userdata);
						Log.i("TAG", "onCreate: 编辑数据库成功");
						finish();
					}
				}
				else
				{
					Toast.makeText(SetData.this, "服务器返回了不认识的数据emmmm", Toast.LENGTH_SHORT).show();
					Log.i("TAG", "handleMessage: 服务器返回了不认识的数据emmmm   " + post_data);
				}
			}
			else if (msg.what == ERROR)
			{
				Toast.makeText(SetData.this, "失去与服务器连接>_<", Toast.LENGTH_SHORT).show();
				thread_r_pic = true;
			}
		}
	};


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//相册界面返回照片信息的处理函数
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RC_CHOOSE_PHOTO && RESULT_OK == resultCode)
		{
			Uri uri = data.getData();
			assert uri != null;
			Log.i("TAG", "onActivityResult: " + uri.toString());
			path = true;
			setdata_image.setImageURI(uri);
			try
			{
				InputStream is = getApplicationContext().getContentResolver().openInputStream(uri);
				assert is != null;
				byte[] bytes = new byte[is.available()];
				is.read(bytes);
				is.close();
				userdata.user_head[0] = MyKeyer.MyMd5(bytes);
				userdata.user_head[1] = "jpg";
				//FileOutputStream fileOutputStream = new FileOutputStream(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + userdata.user_head[0] + "." + userdata.user_head[1]);
				//Log.i("TAG", "onCreate: setImageBitmap 11");
//				for(String str1:getApplicationContext().fileList())
//				{
//					Log.i("TAG", str1);
//				}
				FileOutputStream fileOutputStream = getApplicationContext().openFileOutput(userdata.user_head[0] + "." + userdata.user_head[1], Context.MODE_PRIVATE);
				//Log.i("TAG", "setdata_save: 用户头像文件读取信息  buffer.len= " +bytes.length);
				fileOutputStream.write(bytes);
				fileOutputStream.flush();
				fileOutputStream.close();
				Log.i("TAG", "setdata_save: 用户头像文件读取信息  2");

			}
			catch (Exception e)
			{
				Log.e("TAG", "onActivityResult: 图片文件转存出错" + e.getMessage());
			}
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
		{
			Intent intent = new Intent(Intent.ACTION_PICK, null);
			intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
			startActivityForResult(intent, RC_CHOOSE_PHOTO);
		}
	}

	public void get_image(View v)
	{
		//先择头像头片的处理函数v

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			//未授权，申请授权(从相册选择图片需要读取存储卡的权限)
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,}, RC_CHOOSE_PHOTO);
			Log.i("TAG", "get_image: 成功获取权限0");
		}
		else
		{
			//已授权，获取照片
			Log.i("TAG", "get_image: 成功获取权限1");
			//choosePhoto();
			Intent intent = new Intent(Intent.ACTION_PICK, null);
			intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
			startActivityForResult(intent, RC_CHOOSE_PHOTO);
		}
		//收到图片之后将图片放到缓存目录，方便读取


	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public void setdata_save(View v)
	{
		if (key_save)
		{
			key_save = false;
			if (path)//如果头像被修改
			{
				//提交图片给服务器
				new Thread()
				{
					@Override
					public void run()
					{
						FileInputStream fileInputStream;
						byte[] buffer=null;
						try
						{
							fileInputStream = new FileInputStream(getApplicationContext().getFilesDir() + "/" + userdata.user_head[0] + "." + userdata.user_head[1]);
							buffer = new byte[fileInputStream.available()];
							fileInputStream.read(buffer);
							fileInputStream.close();
						}
						catch (FileNotFoundException e)
						{
							e.printStackTrace();

						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						if(buffer==null)
						{
							Toast.makeText(SetData.this, "头像文件读取出错，无法上传至服务器", Toast.LENGTH_SHORT).show();
							return;
						}
						new MyHttp().thread_send_Post_String(
								MyHttp.getServerHttp() + "pic=" + userdata.user_head[0] + "." + userdata.user_head[1], buffer, handler_update_pic, CHANGE_UI);

					}
				}.start();

//				//将用户的头像图片缓存到这个目录getApplicationContext().getFilesDir();
//				try
//				{
//					//FileInputStream fileInputStream = new FileInputStream(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + userdata.user_head[0] + "." + userdata.user_head[1]);
//					//FileOutputStream fileOutputStream = new FileOutputStream(getApplicationContext().getFilesDir() + "/" + userdata.user_head[0] + "." + userdata.user_head[1]);
//					FileInputStream fileInputStream = getApplicationContext().openFileInput(userdata.user_head[0] + "." + userdata.user_head[1]);
//					byte[] buffer = new byte[fileInputStream.available()];
//					//fileInputStream.read(buffer);
//					Log.i("TAG", "setdata_save: 用户头像文件读取信息  fileInputStream.read(buffer) " + fileInputStream.read(buffer));
//					//Log.i("TAG", "setdata_save: 用户头像文件读取信息  fileInputStream" +getApplicationContext().openFileInput(userdata.user_head[0] + "." + userdata.user_head[1]).toString());
//					//提交图片信息给服务器
//					new MyHttp().thread_send_Post_String(MyHttp.IP + "pic=" + userdata.user_head[0] + "." + userdata.user_head[1], buffer, handler_update_pic, CHANGE_UI);
//					fileInputStream.close();
//				}
//				catch (Exception e)
//				{
//					Log.i("TAG", "setdata_save: 上传用户头像出错" + e.getMessage());
//				}
			}
			else
			{
				//无需同步照片
				//将图片锁设为true
				thread_key_pic = true;
			}

			userdata.name = setdata_name.getText().toString().trim();
			if (manradio.isChecked())
			{
				userdata.sex = 1;
			}
			else if (womanradio.isChecked())
			{
				userdata.sex = 0;
			}

			//校验得到的信息


			//打包要返回给上个界面的信息
			intent.putExtra("email", userdata.email);
			intent.putExtra("sex", userdata.sex);
			intent.putExtra("name", userdata.name);
			intent.putExtra("user_head", userdata.user_head);
			setResult(1, intent);


			//提交文字信息给服务器
			//new MyHttp().thread_send_Post_String(MyHttp.IP + "update", "name=" + userdata.name + "&email=" + userdata.email + "&sex=" + userdata.sex + "&user_head_md5=" + userdata.user_head[0] + "&user_head_end=" + userdata.user_head[1], handler_update, CHANGE_UI);

			HashMap<String,Object> dict = userdata.get_dict_data();
			dict.put("name",userdata.name);
			dict.put("email",userdata.email);
			dict.put("sex",userdata.sex);
			dict.put("user_head_md5",userdata.user_head[0]);
			dict.put("user_head_end",userdata.user_head[1]);
			new MyHttp2(userdata).thread_post_do_data("update", MyJson.toJson(dict), userdata.net_md5, handler_update);


		}
		else
		{
			Toast.makeText(SetData.this, "在存了，在存了，别催我-_-", Toast.LENGTH_SHORT).show();
		}

	}
}
