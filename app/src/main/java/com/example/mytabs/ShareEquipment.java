package com.example.mytabs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class ShareEquipment extends Activity
{
	ListView listView;
	Button button;
	long[] list_eid;
	String[] list_name;
	Boolean all_check=false;
	int http_get_fig;//分享码请求状态
	String share_ma="";
	UserData userData;
	/*
	http_get_fig
	0	未请求过 允许选择设备
	1	正在请求	禁止选择设备
	2	成功	禁止选择设备
	 */
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view_button);

		//Log.i("TAG", "onCreate: 	1");
		listView=findViewById(R.id.list_view);
		button=findViewById(R.id.list_view_button);
		Intent intent=getIntent();
		//Log.i("TAG", "onCreate: 	2");
		list_name=intent.getStringArrayExtra("list_name");
		list_eid=intent.getLongArrayExtra("list_eid");
		MySqlLite sql = new MySqlLite(getApplicationContext());
		userData=sql.get_login_data();
		if(userData==null)
		{
			Toast.makeText(ShareEquipment.this, "数据库加载异常", Toast.LENGTH_SHORT).show();
			return;
		}

		//Log.i("TAG", "onCreate: 	3");
		final MyList myList=new MyList(ShareEquipment.this,list_name,list_eid);
		//Log.i("TAG", "onCreate: 	4");
		listView.setAdapter(myList);
		//Log.i("TAG", "onCreate: 	5");
		myList.notifyDataSetChanged();
		http_get_fig=0;
		button.setText("点我生成分享码/长按全选");all_check=false;
		button.setLongClickable(true);
		button.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				Log.e("TAG", "onCreate onLongClick:");
				if(all_check)
				{
					all_check=false;
					myList.set_all_checkBox_false();
					button.setText("点我生成分享码/长按全选");
				}
				else
				{
					all_check=true;
					myList.set_all_checkBox_true();
					button.setText("点我生成分享码/长按取消全选");
				}
				return true;
			}
		});
		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ArrayList<Long> eid_list;
				eid_list=myList.get_eid_list();
				if(eid_list.size()==0&&http_get_fig==0)
				{
					Toast.makeText(ShareEquipment.this, "请至少选择一个设备", Toast.LENGTH_SHORT).show();
				}
				else if(http_get_fig==0)
				{
					Log.e("TAG", "onCreate onClick: "+eid_list.size());

					HashMap<String,Object> dict = userData.get_dict_data();
					StringBuilder str1=new StringBuilder();
					str1.append("");
					for(long a:eid_list)
					{
						Log.e("TAG", "onCreate onClick: eid="+a);
						str1.append(a).append("-");
						//发送网络请求分享吗
						//对线程加锁
						//修改按键提示字
						button.setText("正在请求");
						http_get_fig=1;
					}
					dict.put("share_eid",str1.toString());
					new MyHttp2(userData).thread_post_do_data("for_friend",MyJson.toJson(dict),userData.net_md5,hander_2);

				}
				else if(http_get_fig==1)
				{
					Toast.makeText(ShareEquipment.this, "正在请求分享码，请稍等", Toast.LENGTH_SHORT).show();
				}
				else if(http_get_fig==2)
				{
					//button.setText("点我复制分享码");
					ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					cm.setPrimaryClip(ClipData.newPlainText(null,share_ma));
					Toast.makeText(ShareEquipment.this, "复制成功 \n想要重新生成，请重新打开此页面", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@SuppressLint("HandlerLeak")
	private final Handler hander_2 = new Handler()//负责处理按键里的线程
	{
		public void handleMessage(@NonNull Message msg)
		{
			http_get_fig = 0;
			button.setText("点我生成分享码/长按全选");
			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Toast.makeText(ShareEquipment.this, "无法链接到服务器,请检查网络", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Toast.makeText(ShareEquipment.this, "数据解析失败，请重新登录", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == 0)
			{
				share_ma = (String)msg.obj;
				Toast.makeText(ShareEquipment.this, "分享码获取成功", Toast.LENGTH_SHORT).show();
				http_get_fig = 2;
				button.setText("点我复制分享码");
			}
			else if (msg.what > 0)
			{
				Toast.makeText(ShareEquipment.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
			}

		}
	};

	private class MyList extends BaseAdapter
	{
		Context context;
		long[] list_eid;
		String[] list_name;
		ArrayList<CheckBox> checkBoxs;
		/*listview_show_id
		news=1	新闻
		uget=2	用户订阅
		other=3	其他
		pic=4	图片
		 */

		public MyList(Context context, String[] list_name,long[] list_eid)
		{
			super();
			this.context = context;
			this.list_name=list_name;
			this.list_eid=list_eid;
			this.checkBoxs=new ArrayList<>();
		}

		@Override
		public int getCount()
		{
			return this.list_eid.length;
		}

		@Override
		public Object getItem(int position)
		{
			return position;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView==null)
			{
				convertView=View.inflate(getApplicationContext(),R.layout.share_chiose_item,null);
			}
			final CheckBox checkBox=convertView.findViewById(R.id.checkBox);
			if(checkBoxs.size()<=position)
			{
				checkBoxs.add(checkBox);
			}
			else
			{
				checkBoxs.set(position,checkBox);
			}
			checkBox.setText(list_name[position]);
			checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					if(http_get_fig>0)
					{
						checkBox.setChecked(!isChecked);
					}
					else
					{
						checkBox.setChecked(isChecked);
					}
				}
			});
			return convertView;
		}

		public ArrayList<Long> get_eid_list()
		{
			CheckBox checkBox;
			ArrayList<Long> eid_list=new ArrayList<>();
			for(int i=0;i<checkBoxs.size();i++)
			{
				checkBox=checkBoxs.get(i);
				if(checkBox.isChecked())
				{
					eid_list.add(list_eid[i]);
				}
			}
			return eid_list;
		}

		public void set_all_checkBox_true()
		{
			CheckBox checkBox;
			for(int i=0;i<checkBoxs.size();i++)
			{
				checkBox=checkBoxs.get(i);
				checkBox.setChecked(true);
			}
		}
		public void set_all_checkBox_false()
		{
			CheckBox checkBox;
			for(int i=0;i<checkBoxs.size();i++)
			{
				checkBox=checkBoxs.get(i);
				checkBox.setChecked(false);
			}
		}

	}


}
