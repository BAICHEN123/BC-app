package com.example.mytabs;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EquipmentShowOldDateActivity extends AppCompatActivity
{
	DrawOldDateView drawView;
	MyFloatingActionButton fab;
	UserData userData;

	private Button button_min, button_max, button_mode;
	private String min_data, max_data;

	final String[] modes = {"day", "month", "year"};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		min_data = "";
		max_data = "";
		super.onCreate(savedInstanceState);
		setContentView(R.layout.equipment_show_old_date);
		drawView = findViewById(R.id.draw_old_date_View);
		final float refresh_rate = getWindowManager().getDefaultDisplay().getRefreshRate();//获取系统的刷新频率
		Log.e("TAG", "onCreate: refresh_rate " + refresh_rate);
		drawView.setRefresh_rate(refresh_rate);

		Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		fab = findViewById(R.id.draw_old_date_fab_view);
		fab.setMAX(point.x, point.y);//限制可移动的范围
		fab.setOnClickListener(this::flush_fab_view_click);

		userData = new MySqlLite(EquipmentShowOldDateActivity.this).get_login_data();

		button_min = findViewById(R.id.button_min_);
		button_min.setOnClickListener(this::showDatePicker);
		button_max = findViewById(R.id.button_max_);
		button_max.setOnClickListener(this::showDatePicker);

		button_mode = findViewById(R.id.button_mode);
		button_mode.setOnClickListener(this::show_mode_select_view);
		defaut_flush_view();

	}
	private void defaut_flush_view(){

		button_mode.setText(modes[0]);
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		String selectedDate = String.format(Locale.CHINA, "%d-%02d-%02d", year, month + 1, dayOfMonth);
		select_new_date(selectedDate,button_min);
		flush_fab_view_click(fab);

	}

	private void flush_data_select()
	{
		min_data = "";
		max_data = "";
		button_min.setText(getString(R.string.ZuiXiaoRiQi));
		button_max.setText(getString(R.string.ZuiDaRiQI));
	}

//	private void flush_min_max_data(){
//		if(mode == 2){
//			min_data = min_data.replace("-12-31$","-01-01");
//			max_data = max_data.replace("-01-01$","-12-31");
//		}else if (mode == 1){
//
//		}
//		button_min.setText(min_data);
//		button_max.setText(max_data);
//	}


	private void show_mode_select_view(View v)
	{

		new AlertDialog.Builder(EquipmentShowOldDateActivity.this)
				.setTitle("请选择符号")
				.setItems(modes, (dialog, which) -> {
					button_mode.setText(modes[which]);
					flush_data_select();
				})
				.show();
	}

	private void select_new_date(String selectedDate,View v){
		Log.i("TAG", selectedDate);
		{
			if (v == button_min)
			{

				if (!max_data.isEmpty() && selectedDate.compareTo(max_data) > 0)
				{
					button_min.setText(max_data);
					min_data = max_data;

					button_max.setText(selectedDate);
					max_data = selectedDate;

				}
				else
				{
					button_min.setText(selectedDate);
					min_data = selectedDate;
				}

			}
			else if (v == button_max)
			{
				if (!min_data.isEmpty() && selectedDate.compareTo(min_data) < 0)
				{
					button_max.setText(min_data);
					max_data = min_data;

					button_min.setText(selectedDate);
					min_data = selectedDate;
				}
				else
				{
					button_max.setText(selectedDate);
					max_data = selectedDate;
				}
			}
		}
		if (min_data.isEmpty())
		{
			button_min.setText(max_data);
			min_data = max_data;
		}
		if (max_data.isEmpty())
		{
			button_max.setText(min_data);
			max_data = min_data;
		}
	}

	private void showDatePicker(View v)
	{
		// 获取当前日期
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

		// 创建一个日期选择对话框
		DatePickerDialog datePickerDialog = new DatePickerDialog(EquipmentShowOldDateActivity.this,
																 (view, year1, month1, dayOfMonth1) -> {
//																		 String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
																	 String selectedDate = String.format(Locale.CHINA, "%d-%02d-%02d", year1, month1 + 1, dayOfMonth1);
																	 select_new_date(selectedDate,v);
																 }, year, month, dayOfMonth);

		// 显示日期选择对话框
		datePickerDialog.show();
	}


	public void flush_fab_view_click(View v)
	{
		if (min_data.isEmpty() || max_data.isEmpty())
		{
			Toast.makeText(EquipmentShowOldDateActivity.this, "请选择日期", Toast.LENGTH_SHORT).show();
			return;
		}
		HashMap<String, Object> dict = userData.get_dict_data();
		Intent intent_old = getIntent();
		final String eid = intent_old.getStringExtra("eid");
		//["uid", "eid", "mode", "min_data", "max_data"]
		dict.put("eid", eid);
		dict.put("mode", modes[0]);
		dict.put("min_data", min_data);
		dict.put("max_data", max_data);
		new MyHttp2(userData).thread_post_str("select_old_date", MyJson.toJson(dict), userData.net_md5, hander_2, 0);

	}

	private final Handler hander_2 = new Handler(Objects.requireNonNull(Looper.myLooper()))
	{//响应网络请求
		public void handleMessage(Message msg)
		{

			if (msg.what == MyHttp2.MyHttp_ERROR)
			{
				Log.e("TAG", "handleMessage: MyHttp error");
				Toast.makeText(getApplicationContext(), "网络异常", Toast.LENGTH_SHORT).show();
				//在这里重新发送请求 ？
			}
			else if (msg.what == MyHttp2.MyKeyer_ERROR)
			{
				Log.e("TAG", "handleMessage: MyHttp error");
				Toast.makeText(getApplicationContext(), "解析数据错误，请重新登录", Toast.LENGTH_SHORT).show();

			}
			else
			{
				String str_data = (String) msg.obj;
				HashMap<String, Object> dict = MyJson.parseJson(str_data);
				Object obj = dict.get("end");
				Object obj_file = dict.get("file");
				Object obj_message = dict.get("message");
				if (obj == null || !MyJson.objEqToValue(obj, 0.0) || obj_message == null)
				{
					Log.i("TAG", "handleMessage: " + obj + "file" + obj_file + "\nobj_message\n" + obj_message);
					return;
				}
				new_thread_do_old_date_message(obj_message, hander_flush_zhexian);
				Log.i("TAG", "handleMessage: 折线绘制 获取数据成功");
			}
		}
	};


	private final Handler hander_flush_zhexian = new Handler(Objects.requireNonNull(Looper.myLooper()))
	{//响应网络请求
		public void handleMessage(Message msg)
		{

			if (msg.what <= 0)
			{
				Log.e("TAG", "hander_flush_zhexian : error 数组长度不够");
				Toast.makeText(getApplicationContext(), "数组长度 0", Toast.LENGTH_SHORT).show();
				return;
			}
			drawView.invalidate();
		}
	};

	private void new_thread_do_old_date_message(Object message, final Handler handler)
	{
		new Thread()
		{
			public void run()
			{

				Message msg = new Message();
				ArrayList<DrawOldDateView.DrawLineData> tmp1 = do_old_date_message(message, handler);
				drawView.setYValues(tmp1);
				Log.i("TAG", "handleMessage: 折线绘制 整理数据成功");
				msg.what = tmp1.size();
				handler.sendMessage(msg);
			}
		}.start();
	}

	private ArrayList<DrawOldDateView.DrawLineData> do_old_date_message(Object message, final Handler handler)
	{
		if (!(message instanceof ArrayList))
		{
			return null;
		}
		ArrayList<?> list = (ArrayList<?>) message;
		String last_data = "0";
		String last_time = "0";
		String tmp_date;
		String tmp_data;
		String tmp_time;
		ArrayList<?> tmp;
		HashMap<String, ArrayList<EquipmentDataItem>> dict1 = new HashMap<>();
		HashMap<String, DrawOldDateView.DrawLineData> dict2 = new HashMap<>();
		ArrayList<DrawOldDateView.DrawTimeData> list_data_time = new ArrayList<>();
		int index = 0;
		ArrayList<String> names = new ArrayList<>();
		//get_do_message_name_date
		for (Object item : list)
		{
			if (!(item instanceof ArrayList))
			{
				continue;
			}
			tmp = (ArrayList<?>) item;
			if (tmp.size() != 3 || !(tmp.get(0) instanceof String) || !(tmp.get(1) instanceof String) || !(tmp.get(2) instanceof String))
			{
				continue;
			}
			tmp_date = (String) tmp.get(0);
			tmp_data = (String) tmp.get(1);
			tmp_time = (String) tmp.get(2);
			assert last_data.compareTo(tmp_data) <= 0 : last_data + "" + tmp_data;
			assert !last_data.equals(tmp_data) || last_time.compareTo(tmp_time) <= 0 : last_time + "" + tmp_time;// 第二天
			last_data = tmp_data;
			last_time = tmp_time;
			list_data_time.add(new DrawOldDateView.DrawTimeData(tmp_data,tmp_time));
			HashMap<String, EquipmentDataItem> datas = MyEquipment.do_message_date(tmp_date);
			if (names.size() == 0)
			{
				names = MyEquipment.get_do_message_name_date();
			}
			for (Map.Entry<String, EquipmentDataItem> item_equipmentDataItem : datas.entrySet())
			{
				if (!names.contains(item_equipmentDataItem.getKey()))
				{
					names.add(item_equipmentDataItem.getKey());
				}
				ArrayList<EquipmentDataItem> tmp1;
				DrawOldDateView.DrawLineData tmp2;

				if (!dict1.containsKey(item_equipmentDataItem.getKey()))
				{
					tmp1 = new ArrayList<>();
					tmp2 = new DrawOldDateView.DrawLineData(item_equipmentDataItem.getKey(), item_equipmentDataItem.getValue().unit, list.size());
					tmp2.continue_index = index;
					// 设备里的变量新增之后，屏蔽一段无效变量。
					// 最优方式是搞一批集合，这样可以完美处理所有变量时有时无的问题，
					// 其次是将所有无效的变量改成float的max或min中的一个，画图的时候单独频闭
					dict1.put(item_equipmentDataItem.getKey(), tmp1);
					dict2.put(item_equipmentDataItem.getKey(), tmp2);
				}
				else
				{
					tmp1 = dict1.get(item_equipmentDataItem.getKey());
					tmp2 = dict2.get(item_equipmentDataItem.getKey());
				}
				assert tmp1 != null;
				assert tmp2 != null;
				tmp1.add(item_equipmentDataItem.getValue());
				float now = item_equipmentDataItem.getValue().get_zhexian_float_value();
				tmp2.data[index] = now;
				float max = item_equipmentDataItem.getValue().get_zhexian_float_max();
				float min = item_equipmentDataItem.getValue().get_zhexian_float_min();
				if (max == min && max == 0)
				{
					max = now;
					min = now;
				}
				if (tmp2.max == Float.MAX_VALUE && tmp2.min == Float.MIN_VALUE)
				{
					tmp2.max = max;
					tmp2.min = min;
				}
				else
				{
					if (max > tmp2.max)
					{
						tmp2.max = max;
					}
					if (min < tmp2.min)
					{
						tmp2.min = min;
					}
				}
				if (now > tmp2.max)
				{
					tmp2.max = now;
				}
				else if (now < tmp2.min)
				{
					tmp2.min = now;
				}
				assert now <= tmp2.max && now >= tmp2.min;
			}
			index++;
			/*
			0 是数据
			1 是日期
			2 是时间
			 */
			if (index % 80 == 0)
			{
				ArrayList<DrawOldDateView.DrawLineData> list_of_names = new ArrayList<>();
				for (String name : names)
				{
					list_of_names.add(dict2.get(name));
				}
				Message msg = new Message();
				drawView.setYValues(list_of_names);
				drawView.setDrawTimeDataList(list_data_time);
				Log.i("TAG", "handleMessage: 折线绘制 整理数据成功");
				msg.what = list_of_names.size();
				handler.sendMessage(msg);

			}

		}

		ArrayList<DrawOldDateView.DrawLineData> list_of_names = new ArrayList<>();
		for (String name : names)
		{
			DrawOldDateView.DrawLineData item = dict2.get(name);
			assert item != null;
			item.random_color();
			item.flush_value_max_min();
			list_of_names.add(item);
		}
		drawView.setDrawTimeDataList(list_data_time);

		return list_of_names;

	}

}