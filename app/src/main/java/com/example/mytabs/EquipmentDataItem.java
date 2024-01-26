package com.example.mytabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.View.inflate;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class EquipmentDataItem
{
	//ht_  开头的函数，表示主要是给滑条调用的


	public static final int SHOW_DATA_INT = -1;//数据仅供展示，不可以修改
	public static final int SHOW_DATA_DOUBLE = -2;//数据仅供展示，不可以修改
	public static final int SHOW_DATA_STR = -3;//数据仅供展示，不可以修改

	public static final int KE_YI_XIU_GAI_INT = 1;//是可以修改的 python int 类型的数据
	public static final int KE_YI_XIU_GAI_DOUBLE = 2;//是可以修改的 python float 类型的数据
	public static final int KE_YI_XIU_GAI_STR = 3;//是可以修改的 python str 类型的数据
	public static final int NO_TYPE = 0;//不知道啥类型


	final String[] FUHAO = new String[]{"<", ">", "=", "~"};
	final String[] FUHAO_INFO = new String[]{"< 小于", "> 大于", "= 等于", "~ 不等于"};

	int mode;
	String item_name, set_item;
	int min, max;
	int int_now;
	double double_min, double_max;
	double double_now;
	String unit;
	String str_now;
	private String[] item_mode_name;


	private static final Pattern pattern1 = Pattern.compile("^((?:-?\\d+)(\\.?\\d{0,2}))\\(?([^\\s)]*)\\)?$");//匹配有单位的小数或整数

	private static final Pattern pattern2 = Pattern.compile("(?:@(\\S+)\\[((?:-?\\d+)(\\.?\\d{0,2}))-((?:-?\\d+)(\\.?\\d{0,2}))])");


	private static final Pattern pattern3 = Pattern.compile("(\\S+)\\[(-?\\d*)-(-?\\d*)]");

	public EquipmentDataItem(EquipmentDataItem old)
	{
		this.mode = old.mode;
		this.item_name = old.item_name;
		this.set_item = old.set_item;
		this.min = old.min;
		this.max = old.max;
		this.int_now = old.int_now;
		this.double_min = old.double_min;
		this.double_max = old.double_max;
		this.double_now = old.double_now;
		this.unit = old.unit;
		this.str_now = old.str_now;
		this.item_mode_name = old.item_mode_name;
	}


	public EquipmentDataItem(String item_name, String now)
	{

		//有点儿担心这里的解析问题，前半部分解析出来个类型，后半部分解析出来一个不一样的类型，


		this.mode = NO_TYPE;
		if (item_name.startsWith("@"))
		{
			Matcher m = pattern2.matcher(item_name);
			if (m.find() && m.groupCount() == 5)
			{

				this.item_name = m.group(1);
				//关于下面为什么要拼上一个 '[' ，这是一个上古版本单片机通讯的问题，单片机依靠该符号或 ':' 做分隔符
				this.set_item = '@' + m.group(1);

				if ("".equals(m.group(3)) && "".equals(m.group(5)))
				{
					this.mode = KE_YI_XIU_GAI_INT;
					min = Integer.parseInt(m.group(2));
					max = Integer.parseInt(m.group(4));

				}
				else if (!"".equals(m.group(3)) && !"".equals(m.group(5)))
				{
					this.mode = KE_YI_XIU_GAI_DOUBLE;
					double_min = Double.parseDouble(m.group(2));
					double_max = Double.parseDouble(m.group(4));

				}
				else
				{
					this.mode = KE_YI_XIU_GAI_STR;
				}
			}
			else
			{
				Log.e("TAG", "EquipmentDataItem: error 识别错误" + item_name);
			}

		}

		if (this.mode == NO_TYPE)
		{
			Matcher m = pattern3.matcher(item_name);
			if (m.find())
			{
				this.item_name = m.group(1);
				this.min = Integer.parseInt(Objects.requireNonNull(m.group(2)));
				this.max = Integer.parseInt(Objects.requireNonNull(m.group(3)));
			}
			else
			{

				this.item_name = item_name;
			}

			this.set_item = this.item_name;
		}
		Matcher m = pattern1.matcher(now);
		if (m.find() && m.groupCount() == 3)
		{
			unit = m.group(3);
			if ("".equals(m.group(2)))
			{
				int_now = Integer.parseInt(m.group(1));
				if (this.mode == NO_TYPE)
				{
					this.mode = SHOW_DATA_INT;
				}
				this.str_now = "" + int_now + unit;
			}
			else
			{
				double_now = Double.parseDouble(m.group(1));
				if (this.mode == NO_TYPE)
				{
					this.mode = SHOW_DATA_DOUBLE;
				}
				this.str_now = String.format(Locale.CHINA, "%.2f", this.double_now) + unit;
			}
		}
		else
		{
			this.str_now = now;
			if (this.mode == NO_TYPE)
			{
				this.mode = SHOW_DATA_STR;
			}
		}

	}


	public int get_now()
	{
		if (this.mode == KE_YI_XIU_GAI_INT)
		{
			return int_now;
		}
		else if (this.mode == KE_YI_XIU_GAI_DOUBLE)
		{
			return (int) double_now;
		}
		else
		{
			return 0;
		}
	}

	public int ht_get_now()
	{
		if (this.mode == KE_YI_XIU_GAI_INT)
		{
			return int_now - this.min;
		}
		else if (this.mode == KE_YI_XIU_GAI_DOUBLE)
		{
			return (int) ((this.double_now - this.double_min) * 100);
		}
		else
		{
			return 0;
		}
	}

	public void set_mode_name(String[] item_mode_name)
	{
		//Log.i("TAG", "set_mode_name: this.max="+this.max+"  this.min="+this.min+"  length="+item_mode_name.length);
		if ((this.mode == SHOW_DATA_INT || this.mode == KE_YI_XIU_GAI_INT) && this.max - this.min + 1 == item_mode_name.length)
		{
			this.item_mode_name = item_mode_name;
			//Log.e("TAG", "set_mode_name: ok");
		}
		//Log.e("TAG", "set_mode_name: end");
	}

	//给滑条调用，获取数值实时显示的描述,同时会赋值给数据
	String ht_get_values(int value)//滑条获取进度显示的内容
	{
		if (this.mode == KE_YI_XIU_GAI_INT)
		{
			value = value + this.min;
			if (value > this.max)
			{
				value = this.max;
			}
			else if (value < this.min)
			{
				value = this.min;
			}
			this.int_now = value;
		}
		else if (this.mode == KE_YI_XIU_GAI_DOUBLE)
		{
			value = value + (int) (this.double_min * 100);
			if (value > this.double_max * 100)
			{
				value = (int) (this.double_max * 100);
			}
			else if (value < this.double_min * 100)
			{
				value = (int) (this.double_min * 100);
			}
			this.double_now = ((double) value) / 100;
			return (((double) value) / 100) + this.unit;
		}

		//Log.e("TAG", "ht_get_values: this.item_mode_name.length "+this.item_mode_name.length +" value="+value);
		if (this.mode != KE_YI_XIU_GAI_INT)
		{
			return "ht_get_values 异常";
		}
		if (item_mode_name == null || this.item_mode_name.length < value - this.min)
		{
			//Log.e("TAG", "ht_get_values: item_mode_name == null || this.item_mode_name.length < value - this.min");
			return value + this.unit;
		}
		else
		{
			//Log.e("TAG", "ht_get_values: item_mode_name[value - this.min]"+item_mode_name[value - this.min]);
			return item_mode_name[value - this.min];
		}
	}


	//给滑条调用，获取数值实时显示的描述
	String ht_get_now_values()//滑条获取进度显示的内容
	{
		if (this.mode == KE_YI_XIU_GAI_DOUBLE || this.mode == SHOW_DATA_DOUBLE)
		{
			return this.double_now + this.unit;
		}

		//Log.e("TAG", "ht_get_values: this.item_mode_name.length "+this.item_mode_name.length +" value="+value);
//		if (this.mode != KE_YI_XIU_GAI_INT)
//		{
//			return "ht_get_now_values 异常";
//		}
		if (this.mode == KE_YI_XIU_GAI_INT || this.mode == SHOW_DATA_INT)
		{
			if (item_mode_name == null || this.item_mode_name.length < this.int_now - this.min)
			{
				//Log.e("TAG", "ht_get_values: item_mode_name == null || this.item_mode_name.length < value - this.min");
				return this.int_now + this.unit;
			}
			else
			{
				//Log.e("TAG", "ht_get_values: item_mode_name[value - this.min]"+item_mode_name[value - this.min]);
				return item_mode_name[this.int_now - this.min];
			}
		}
		else
		{
			assert false;
			return "ht_get_now_values 异常";
		}
	}

	//给显示文本框调用，显示当前数值对应的描述
	String text_get_now_values()
	{
		if (this.mode != SHOW_DATA_INT)
		{
			return str_now;
		}

		if (item_mode_name == null || this.item_mode_name.length < this.int_now - this.min)
		{
			//Log.e("TAG", "ht_get_values: item_mode_name == null || this.item_mode_name.length < value - this.min");
			return this.int_now + this.unit;
		}
		else
		{
			//Log.e("TAG", "ht_get_values: item_mode_name[value - this.min]"+item_mode_name[value - this.min]);
			return item_mode_name[this.int_now - this.min];
		}
	}

	//获取滑条的最大值,最小值为0
	public int ht_get_max()
	{
		if (this.mode == KE_YI_XIU_GAI_INT)
		{
			return this.max - this.min;
		}
		else if (this.mode == KE_YI_XIU_GAI_DOUBLE)
		{
			return (int) (this.double_max - this.double_min) * 100;
		}
		return 0;
	}

	//获取滑条展示的最小值
	public String ht_get_show_mim()
	{
		if (this.mode == KE_YI_XIU_GAI_INT || this.mode == SHOW_DATA_INT)
		{
			return String.valueOf(this.min);
		}
		else if (this.mode == KE_YI_XIU_GAI_DOUBLE || this.mode == SHOW_DATA_DOUBLE)
		{
			return String.format(Locale.CHINA, "%.2f", this.double_min);
		}
		return "get_show_mim error";
	}

	//获取滑条展示的最大值
	public String ht_get_show_max()
	{
		if (this.mode == KE_YI_XIU_GAI_INT || this.mode == SHOW_DATA_INT)
		{
			return String.valueOf(this.max);
		}
		else if (this.mode == KE_YI_XIU_GAI_DOUBLE || this.mode == SHOW_DATA_DOUBLE)
		{
			return String.format(Locale.CHINA, "%.2f", this.double_max);
		}
		return "get_show_max error";
	}

	//这个对象是否是可修改量
	public boolean ke_yi_xiu_gai()
	{
		return this.mode > NO_TYPE;
	}

	//调用这个是给单片机看的，返回的是单片机传过来限定值。
	public String get_ht_end_value_str()
	{
		if (this.mode == KE_YI_XIU_GAI_INT || this.mode == SHOW_DATA_INT)
		{
			return String.valueOf(this.int_now);
		}
		else if (this.mode == KE_YI_XIU_GAI_DOUBLE || this.mode == SHOW_DATA_DOUBLE)
		{
			return String.format(Locale.CHINA, "%.2f", this.double_now);
		}
		else if (this.mode == KE_YI_XIU_GAI_STR || this.mode == SHOW_DATA_STR)
		{
			return str_now;
		}
		return "get_ht_end_value_str error";
	}

	//返回值主要给 editview 调用判断数据类型是否合适
	public boolean set_data(String str1)
	{
		if (this.mode == SHOW_DATA_STR || this.mode == KE_YI_XIU_GAI_STR)
		{
			this.str_now = str1;
			return true;
		}
		Matcher m = pattern1.matcher(str1);
		if (m.find() && m.groupCount() == 3)
		{
			if (this.mode == KE_YI_XIU_GAI_DOUBLE || this.mode == SHOW_DATA_DOUBLE)
			{
				if ("".equals(m.group(3)) || this.unit.equals(m.group(3)))
				{
					this.str_now = m.group(1);
					this.double_now = Double.parseDouble(Objects.requireNonNull(m.group(1)));
					return true;
				}
				return false;
			}
			else if (this.mode == KE_YI_XIU_GAI_INT || this.mode == SHOW_DATA_INT)
			{
				if ("".equals(m.group(2)) && ("".equals(m.group(3)) || this.unit.equals(m.group(3))))
				{
					this.str_now = m.group(1);
					this.int_now = Integer.parseInt(Objects.requireNonNull(m.group(1)));
					return true;
				}
				return false;
			}
			Log.e("TAG", "is_ok_data: error");
		}
		return false;
	}

	// 给绘制折线图用的，获取描点的坐标
	public float get_zhexian_float_value()
	{

		if (this.mode == KE_YI_XIU_GAI_INT || this.mode == SHOW_DATA_INT)
		{
			return (float) int_now;
		}
		else if (this.mode == KE_YI_XIU_GAI_DOUBLE || this.mode == SHOW_DATA_DOUBLE)
		{
			return (float) double_now;
		}
		else
		{
			assert false : "目前不兼容字符串类型的";
		}
		assert false : "永不抵达";
		return 0;
	}

	public float get_zhexian_float_min()
	{

		if (this.mode == KE_YI_XIU_GAI_INT || this.mode == SHOW_DATA_INT)
		{
			return (float) min;
		}
		else if (this.mode == KE_YI_XIU_GAI_DOUBLE || this.mode == SHOW_DATA_DOUBLE)
		{
			return (float) double_min;
		}
		else
		{
			assert false : "目前不兼容字符串类型的";
		}
		assert false : "永不抵达";
		return 0;
	}

	public float get_zhexian_float_max()
	{

		if (this.mode == KE_YI_XIU_GAI_INT || this.mode == SHOW_DATA_INT)
		{
			return (float) max;
		}
		else if (this.mode == KE_YI_XIU_GAI_DOUBLE || this.mode == SHOW_DATA_DOUBLE)
		{
			return (float) double_max;
		}
		else
		{
			assert false : "目前不兼容字符串类型的";
		}
		assert false : "永不抵达";
		return 0;
	}


	final static public int VIEW_ID_MIN = 1;//最小值
	final static public int VIEW_ID_SET_1 = VIEW_ID_MIN + 1;//只有两种状态，用简单的开关就可以了
	final static public int VIEW_ID_SET_2 = VIEW_ID_MIN + 2;//超过两种状态，使用可以截断的滑条来显示和操作
	final static public int VIEW_ID_SET_3 = VIEW_ID_MIN + 3;//有描述，允许点击窗口提示框选择信息

	final static public int VIEW_ID_SHOW_1 = VIEW_ID_MIN + 4;//仅对用户展示数据的条目,且有id描述的视图
	final static public int VIEW_ID_SHOW_2 = VIEW_ID_MIN + 5;//仅对用户展示数据的条目，没有id描述

	final static public int VIEW_ID_MAX = VIEW_ID_SHOW_2 + 1;//最大值

	final static public int ITEM_IS_IF = 0;//这是条件
	final static public int ITEM_IS_DO = 1;//这是条件发生要执行的事件


	//获取联动页面的视图
	public View get_ld_set_view(EquipmentDataItem itemdata, Context context, final int position, ALdData aLdData, View old_view)
	{
		View view = null;
		if (itemdata != null && itemdata.ke_yi_xiu_gai())//可以修改数值的条目
		{
			if (itemdata.ht_get_max() == 1)//只有两种状态，用简单的开关就可以了
			{
				if (position == ITEM_IS_IF)
				{
					aLdData.fuhao = "=";
				}

				if (old_view != null && old_view.getId() == VIEW_ID_SET_1)
				{
					view = old_view;
				}
				else
				{
					view = inflate(context, R.layout.item_title_switch, null);
					view.setId(VIEW_ID_SET_1);
				}
				SwitchMaterial switch1 = view.findViewById(R.id.item_title_switch_switch);
				TextView textView1 = view.findViewById(R.id.item_title_switch_title);
				textView1.setText(itemdata.item_name);
				switch1.setChecked(itemdata.get_now() == 1);
				switch1.setOnCheckedChangeListener((buttonView, isChecked) ->
												   {
													   //出了加入队列，顺便值修改了，这样刷新之后就是显示新的值
													   if (isChecked)
													   {
														   itemdata.ht_get_values(1);
														   //myEquipment.user_set_date(itemdata.item_name, itemdata);
													   }
													   else
													   {
														   itemdata.ht_get_values(0);
														   //myEquipment.user_set_date(itemdata.item_name, itemdata);
													   }
												   });
			}
			else if (itemdata.ht_get_max() > 1)//超过两种状态，使用可以截断的滑条来显示和操作
			{
				SeekBar seekBar;
				if (itemdata.item_mode_name == null || itemdata.item_mode_name.length < itemdata.int_now - itemdata.min)
				{
					//没有数值的描述，当数字处理

					if (old_view != null && old_view.getId() == VIEW_ID_SET_2)
					{
						view = old_view;
					}
					else
					{
						view = inflate(context, R.layout.ld_title_seekbar2, null);
						view.setId(VIEW_ID_SET_2);
					}
					TextView textView1 = view.findViewById(R.id.ld_title_seekbar_title);//此条目的名称
					TextView textView3 = view.findViewById(R.id.ld_title_seekbar_min);//最小值
					TextView textView4 = view.findViewById(R.id.ld_title_seekbar_max);//最大值
					final EditText editText = view.findViewById(R.id.ld_title_seekbar_value);
					seekBar = view.findViewById(R.id.ld_title_seekbar_seekbar);
					textView1.setText(itemdata.item_name);
					seekBar.setMax(itemdata.ht_get_max());//设置滑条的最大值，最小值默认为0，设置最小值在低版本的安卓上面不兼容
					seekBar.setProgress(itemdata.ht_get_now());//设置换条的当前进度值
					//Log.e("TAG", "getChildView: itemdata.ht_get_values(itemdata.get_now()) "+itemdata.ht_get_values(itemdata.get_now()));
					editText.setText(itemdata.ht_get_now_values());//根据当前值显示模式的名字
					editText.setTextColor(Color.parseColor("#000000"));
					textView3.setText(itemdata.ht_get_show_mim());//滑条左端注释
					textView4.setText(itemdata.ht_get_show_max());//滑条右端注释
					final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
					{
						//拖动中
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
						{
							//修改此条目显示滑条当前值的文本框内容
							//如果将此处填充值对应的文本，就可以实现显示状态好对应的描述
							//textView2.setText(String.valueOf(progress + itemdata.min));
							editText.setText(itemdata.ht_get_values(progress));
						}

						//滑动开始//手指刚落到滑条上
						@SuppressLint("RestrictedApi")
						@Override
						public void onStartTrackingTouch(SeekBar seekBar)
						{
							//itemdata.ht_get_values()
						}

						//滑动停止//手指离开屏幕
						@SuppressLint("RestrictedApi")
						@Override
						public void onStopTrackingTouch(SeekBar seekBar)
						{
							//myEquipment.user_set_date(itemdata.item_name, itemdata);
						}
					};

					editText.addTextChangedListener(new TextWatcher()
					{
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after)
						{

						}

						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count)
						{

						}

						@Override
						public void afterTextChanged(Editable s)
						{
							seekBar.setOnSeekBarChangeListener(null);
							if (itemdata.set_data(s.toString()))
							{
								//这里是合法数据的颜色
								editText.setTextColor(Color.parseColor("#000000"));//ht_get_now
								seekBar.setProgress(itemdata.ht_get_now());//设置换条的当前进度值
								itemdata.ht_get_values(itemdata.ht_get_now());
								Log.e("TAG", "afterTextChanged: " + itemdata.item_name + "  " + itemdata.str_now + "   " + itemdata.double_now + "     " + itemdata.get_ht_end_value_str());
								//myEquipment.user_set_date(itemdata.item_name, itemdata);

							}
							else
							{
								editText.setTextColor(Color.parseColor("#FF0000"));
							}
							seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
						}
					});
					seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

				}
				else
				{
					//有描述，允许点击窗口提示框选择信息
					if (old_view != null && old_view.getId() == VIEW_ID_SET_3)
					{
						view = old_view;
					}
					else
					{
						view = inflate(context, R.layout.ld_title_seekbar, null);
						view.setId(VIEW_ID_SET_3);
					}
					TextView textView1 = view.findViewById(R.id.ld_title_seekbar_title);//此条目的名称
					TextView textView3 = view.findViewById(R.id.ld_title_seekbar_min);//最小值
					TextView textView4 = view.findViewById(R.id.ld_title_seekbar_max);//最大值
					final TextView textView2 = view.findViewById(R.id.ld_title_seekbar_value);//滑条当前值的文本显示
					seekBar = view.findViewById(R.id.ld_title_seekbar_seekbar);
					//final TextView textView2_tmp=textView2;
					textView2.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							new AlertDialog.Builder(context)
									.setTitle("请选择符号")
									.setItems(itemdata.item_mode_name, new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(DialogInterface dialog, int which)
										{
											//将修改数据放入
											itemdata.ht_get_values(which);
											textView2.setText(itemdata.item_mode_name[which]);
											seekBar.setProgress(itemdata.ht_get_now());
										}
									})
									.show();
						}
					});


					textView1.setText(itemdata.item_name);
					seekBar.setMax(itemdata.ht_get_max());//设置滑条的最大值，最小值默认为0，设置最小值在低版本的安卓上面不兼容
					seekBar.setProgress(itemdata.ht_get_now());//设置换条的当前进度值
					//Log.e("TAG", "getChildView: itemdata.ht_get_values(itemdata.get_now()) "+itemdata.ht_get_values(itemdata.get_now()));
					textView2.setText(itemdata.ht_get_values(itemdata.get_now()));//根据当前值显示模式的名字
					textView3.setText(itemdata.ht_get_show_mim());//滑条左端注释
					textView4.setText(itemdata.ht_get_show_max());//滑条右端注释
					seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
					{
						//拖动中
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
						{
							//修改此条目显示滑条当前值的文本框内容
							//如果将此处填充值对应的文本，就可以实现显示状态好对应的描述
							//textView2.setText(String.valueOf(progress + itemdata.min));
							textView2.setText(itemdata.ht_get_values(progress));
						}

						//滑动开始//手指刚落到滑条上
						@SuppressLint("RestrictedApi")
						@Override
						public void onStartTrackingTouch(SeekBar seekBar)
						{
							//itemdata.ht_get_values()
						}

						//滑动停止//手指离开屏幕
						@SuppressLint("RestrictedApi")
						@Override
						public void onStopTrackingTouch(SeekBar seekBar)
						{
							//myEquipment.user_set_date(itemdata.item_name, itemdata);
						}
					});

				}

				final TextView textview_fuhao = view.findViewById(R.id.ld_title_seekbar_fuhao);
				if (aLdData.fuhao != null)
				{
					textview_fuhao.setText(aLdData.fuhao);
				}
				if (position == ITEM_IS_IF)
				{
					textview_fuhao.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							new AlertDialog.Builder(context)
									.setTitle("请选择符号")
									.setItems(FUHAO_INFO, new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(DialogInterface dialog, int which)
										{
											aLdData.fuhao = FUHAO[which];
											textview_fuhao.setText(FUHAO[which]);
										}
									})
									.show();
						}
					});
				}
				else if (position == ITEM_IS_DO)
				{
					//当这里是要执行的选择框的时候，就不允许选择符号了，需要执行的任务，必须是一个确定的值.
					//以后或许可以整个变量赋值啥的，现在先算了
					textview_fuhao.setText("设定为");
				}


			}

		}
		else if (itemdata != null && !itemdata.ke_yi_xiu_gai())//仅对用户展示数据的条目
		{
			if (itemdata.item_mode_name != null && itemdata.item_mode_name.length > itemdata.max - itemdata.min)//带备注的量
			{
				if (old_view != null && old_view.getId() == VIEW_ID_SHOW_1)
				{
					view = old_view;
				}
				else
				{
					view = inflate(context, R.layout.ld_title_text2, null);
					view.setId(VIEW_ID_SHOW_1);
				}
				TextView textView1 = view.findViewById(R.id.ld_title_text_name);
				textView1.setText(itemdata.item_name);
				TextView textView2 = view.findViewById(R.id.ld_title_text_data);
				textView2.setText(itemdata.text_get_now_values());
				textView2.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						new AlertDialog.Builder(context)
								.setTitle("请选择符号")
								.setItems(itemdata.item_mode_name, new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										textView2.setText(itemdata.item_mode_name[which]);
										itemdata.int_now = which + itemdata.min;
									}
								})
								.show();

					}
				});
			}
			else//普通只读数据
			{
				//仅对用户展示的条目，不可以作为被操作对象，不允许赋值，只可以读取
				if (old_view != null && old_view.getId() == VIEW_ID_SHOW_2)
				{
					view = old_view;
				}
				else
				{
					view = inflate(context, R.layout.ld_title_text, null);
					view.setId(VIEW_ID_SHOW_2);
				}
				TextView textView1 = view.findViewById(R.id.ld_title_text_name);
				textView1.setText(itemdata.item_name);
				final EditText etextView2 = view.findViewById(R.id.ld_title_text_data);
				etextView2.setText(itemdata.ht_get_now_values());
				etextView2.setTextColor(Color.parseColor("#000000"));
				etextView2.addTextChangedListener(new TextWatcher()
				{
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after)
					{

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count)
					{

					}

					@Override
					public void afterTextChanged(Editable s)
					{
						//Log.e("TAG", "afterTextChanged: "+s.toString());
						//将输入值存入 itemdata 里，存入失败就变色
						if (itemdata.set_data(s.toString()))
						{
							//这里是合法数据的颜色
							etextView2.setTextColor(Color.parseColor("#000000"));
							Log.e("TAG", "afterTextChanged: " + itemdata.item_name + "  " + itemdata.str_now + "   " + itemdata.double_now);
							//myEquipment.user_set_date(itemdata.item_name, itemdata);

						}
						else
						{
							etextView2.setTextColor(Color.parseColor("#FF0000"));
						}
					}
				});

			}
			TextView textview_fuhao = view.findViewById(R.id.ld_title_text_fuhao);
			if (aLdData.fuhao != null)
			{
				textview_fuhao.setText(aLdData.fuhao);
			}
			textview_fuhao.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					new AlertDialog.Builder(context)
							.setTitle("请选择符号")
							.setItems(FUHAO_INFO, new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									//Log.e("TAG", "onClick: "+which+dialog.toString());
									aLdData.fuhao = FUHAO[which];
									textview_fuhao.setText(FUHAO[which]);
								}
							})
							.show();
				}
			});


		}
		return view;
	}

}
