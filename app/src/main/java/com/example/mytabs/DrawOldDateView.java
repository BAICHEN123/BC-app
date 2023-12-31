package com.example.mytabs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DrawOldDateView extends View
{
	static int[] line_color_list =
			{Color.parseColor("#008A9C"), Color.parseColor("#D64F38"), Color.parseColor("#B9C88C"),
			 Color.parseColor("#6F5892"), Color.parseColor("#69AA4B"),
			 Color.parseColor("#8B5B40"),};
	static int[] text_color_list =
			{Color.parseColor("#008A9B"), Color.parseColor("#D68784"), Color.parseColor("#47316F"),
			 Color.parseColor("#CF6C9D"), Color.parseColor("#B8C88C"),
			 Color.parseColor("#EBC975"),};
	final int str_data_ = 5;//字符串数据类型的数量
	final int str_data__ = 30;//这个数必须小于6*8，大于6*8会有未知错误
	final int drawLineData_ = 6;
	final int x_count_in_mWidth = 80;//屏幕分给的区域，显示几列数据
	final int time_aDay_Datas = 8;//我爬的天气预报一天只有8个时刻的天气详情
	//private final int SIZE_DATA = 5;//一个屏幕的宽度，显示的数据个数
	public int color_of_click_line = Color.parseColor("#5E887C");
	//在 activity 中调用 getWindowManager().getDefaultDisplay().getRefreshRate() 可以获得屏幕刷新率
	int y_count_in_mHeight = 6;//屏幕分给的区域，显示几行数据
	DrawLineData[] drawLineData = null;
	ArrayList<DrawTimeData> drawTimeData = null;
	String[][] str_data = null;
	Bitmap[] bitmap_list = null;
	//储存一周的天气粗略信息
	DrawLineData week_lowtmp_drawLineData, week_hightmp_drawLineData;
	Bitmap[][] week_bitmap_list;
	String[][] week_date;
	int draw_mode = 1;// 0 ·  1 -
	private Context mContext;
	private int refresh_rate = 24;//刷新率
	private long refresh_cycle = 1000 / refresh_rate;//帧间隔时间
	private int mWidth;//屏幕的宽度
	private int mHeight;//屏幕的高度
	private int Date_Data_x;//两个数据之间的横向间隔
	private int Date_Data_y;//两个数据之间的竖向间隔
	private int x_scroll;//x轴原点的位置相对于屏幕的位置
	private int y_scroll;//x轴原点的位置相对于屏幕的位置
	private int max_xScroll;//限制图像向X的正半轴移动
	private int min_xScroll;//限制图像向负半轴运动
	private int max_yScroll;//限制图像向X的正半轴移动
	private int min_yScroll;//限制图像向负半轴运动
	private int bigCircleR = 7; //折线图中的圆圈
	private int smallCircleR = 5; //折线图中为了避免折线穿透的圆圈
	private Paint paintWhite, paintBlue, paintRed, paintBack, paintText, dashPaint, paintTime;
	private int xyTextSize = sp2px(10); //xy轴文字大小
	private GestureDetector gestureDetector; //滑动手势
	private float click_x = 0;
	private float click_y = 0;

	public DrawOldDateView(Context context)
	{
		super(context);
	}

	public DrawOldDateView(Context context, @Nullable AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public DrawOldDateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		mContext = context;
		initPaint();
		gestureDetector = new GestureDetector(context, new MyOnGestureListener());//手势事件
	}

	/*计算单个数据展示的宽和高
	修改两个变量的值
	Date_Data_x
	Date_Data_y
	调用api获取屏幕分辨率
	 */
	void new_Data_Data_distance()
	{
		//mWidth = getWidth();
		//mHeight = getHeight();
		Log.e("TAG", "new_Data_Data_distance: mWidth  " + mWidth + "  mHeight   " + mHeight);
		Date_Data_x = mWidth / x_count_in_mWidth;
		Date_Data_y = mHeight / y_count_in_mHeight;
	}

	void new_max_min_xScroll()//重新计算x轴可移动的范围
	{
		max_xScroll = (int) (Math.max(Date_Data_x, getWidth()) * 0.618);
		if (drawLineData == null || drawLineData[0].data.length < x_count_in_mWidth / 2)//不允许左右滑动
		{
			//max_xScroll=0;
			min_xScroll = 0;
			return;
		}
		min_xScroll = (int) (-Date_Data_x * (drawLineData[0].data.length + 1) + getWidth() * 0.618);
	}

	void new_max_min_yScroll()//重新计算y轴可移动的范围
	{
		max_yScroll = Date_Data_y / 2;
		if (drawLineData == null)//不允许左右滑动
		{
			//max_xScroll=0;
			min_yScroll = 0;
			return;
		}
		if (drawLineData.length < y_count_in_mHeight)
		{
			min_yScroll = 0;
		}
		else
		{
			min_yScroll = -Date_Data_y * (drawLineData.length - y_count_in_mHeight + 5);
		}
	}

	public void setRefresh_rate(float refresh_rate)
	{
		if (refresh_rate > 45.0)
		{
			refresh_rate = 45.0f;
		}
		this.refresh_rate = (int) (refresh_rate);
		this.refresh_cycle = 1000 / this.refresh_rate;
	}

	public void setY_count_in_mHeight(int y_count_in_mHeight)
	{
		this.y_count_in_mHeight = Math.max(y_count_in_mHeight, 2);
		new_Data_Data_distance();
		new_max_min_yScroll();
		paintText.setTextSize(xyTextSize);
		paintTime.setTextSize(xyTextSize);
	}

	/*
		规定线的粗细颜色等内容
		 */
	private void initPaint()
	{
		paintWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintWhite.setColor(Color.WHITE);
		paintWhite.setStyle(Paint.Style.STROKE);

		paintBlue = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintBlue.setColor(Color.parseColor("#0198cd"));
		paintBlue.setStrokeWidth(3f);
		paintBlue.setStyle(Paint.Style.STROKE);

		paintBack = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintBack.setColor(Color.parseColor("#272727"));
		paintBack.setStyle(Paint.Style.FILL);

		paintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintRed.setColor(Color.RED);
		paintRed.setStrokeWidth(3f);
		paintRed.setStyle(Paint.Style.STROKE);

		paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintText.setColor(Color.RED);
		paintText.setTextSize(xyTextSize);
		paintText.setStrokeWidth(2f);

		paintTime = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintTime.setColor(Color.parseColor("#FF00FF"));
		paintTime.setTextSize(xyTextSize);
		paintTime.setStrokeWidth(2f);

		dashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dashPaint.setColor(Color.WHITE);
		dashPaint.setStyle(Paint.Style.STROKE);
		dashPaint.setStrokeWidth(1f);
	}

	/*查找字符串数组里是否有要查找的字符串，
	k 查找的范围
	 */
	private boolean String_in_Strings(int k, String[] strings, String str1)
	{
		//for (String str2 : strings)
		for (int i = k; i < strings.length; i++)
		{
			if (str1.equals(strings[i]))
			{
				return true;
			}
		}
		return false;
	}

	/*
	为这个视图类填充数据
	 */
	public void setYValues(ArrayList<DrawOldDateView.DrawLineData> dataArrayList)
	{
		if (dataArrayList.size() == 0)
		{
			return;//清除旧数据
		}
		drawLineData = new DrawLineData[dataArrayList.size()];
		int index = 0;
		for (DrawLineData lineData : dataArrayList)
		{
			drawLineData[index] = lineData;
			index++;
		}
		new_max_min_xScroll();//重新计算x轴可移动的范围
		new_max_min_yScroll();//重新计算y轴可移动的范围
	}

	public void setDrawTimeDataList(ArrayList<DrawOldDateView.DrawTimeData> list)
	{
		drawTimeData = list;
	}

	/*
	视图初始化时会调用此函数
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		if (changed)
		{
			mWidth = getWidth();
			mHeight = getHeight();
			new_Data_Data_distance();//计算数据图像占用的宽高大小

			x_scroll = Date_Data_x / 2;//初始时将图像向屏幕的右面移动半个数据间隔
			new_max_min_xScroll();//计算x轴可移动的范围
			new_max_min_yScroll();//计算x轴可移动的范围
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		//		super.onDraw(canvas);
		//		Path path = new Path();
		//		绘制折线//单日最高温折线
		//		path.moveTo(x_scroll, mHeight);
		//		path.lineTo(0,0);
		//		canvas.drawPath(path, paintRed);
		//		canvas.drawBitmap(11,1);
		//		Log.i("TAG", "onDraw: ");

		/*
		top_need 顶线
		bottom_need 底线
		 */
		if (click_x == 0 && click_y == 0)
		{
			click_x = getWidth() * 0.618f;
			click_y = getHeight() * 0.618f;
		}
		draw_of_click(canvas);
		draw_time_text(canvas);
		if (drawLineData == null)
		{
			// Log.i("TAG", "onDraw   drawLineData==null: ");
			return;
		}
		int top_need = getTextHeight(paintText, "00.00");//顶部坐标
		int bottom_need;//单个线条的底部坐标
		final int row_height = mHeight / y_count_in_mHeight;//屏幕分到的区域显示三个数据//该值为单个数据线条的高度

		top_need = top_need + y_scroll;
		//Log.i("TAG", "onDraw: draw_week_weather top: " + top_need);
		final int week_high = 0;//draw_week_weather(canvas, top_need);
		top_need = top_need + week_high;//绘制一周的粗略信息，然后将
		//Log.i("TAG", "onDraw: draw_week_weather bottom " + top_need);
		//Log.i("TAG", "onDraw: draw_week_weather y_scroll " + y_scroll);
		//Log.i("TAG", "onDraw: draw_week_weather week_high " + week_high);
		final int top_need_time_text = top_need + Date_Data_x / 2;//保存这个坐标，预留给日详细天气的小时标记
		top_need = top_need + Date_Data_x;//周粗略信息和日详细信息的分界区域
		bottom_need = top_need + row_height * 2 / 3;


		// draw_bitmap(canvas, bottom_need, top_need, 0, bitmap_list);
		top_need = top_need + row_height * 2 / 3;
		bottom_need = row_height + top_need;

		//绘制一些数据的折线和标题
		for (int i = 0; i < drawLineData.length; i++)
		{
			drawLine1_AndTitle(canvas, bottom_need, top_need, drawLineData[i]);
			top_need = top_need + row_height / 10;
			top_need = top_need + row_height;
			bottom_need = top_need + row_height;
		}
		if (str_data == null || str_data[0][0] == null || str_data[0][0].equals(""))
		{
			// Log.e("TAG", "onDraw   str_data!=null: ");
			return;
		}
		//绘制日小时文字标注
		if (top_need_time_text < 0)
		{
			drawTextOnLine(canvas, str_data[0], Date_Data_x,
						   5 * getTextHeight(paintText, "00.00") / 2, Date_Data_x / 6, paintTime
			);
			drawTextOnLine(canvas, str_data[4], Date_Data_x, getTextHeight(paintText, "00.00"),
						   Date_Data_x / 6, paintTime
			);
		}
		else
		{
			drawTextOnLine(canvas, str_data[0], Date_Data_x,
						   top_need_time_text + 5 * getTextHeight(paintText, "00.00") / 2,
						   Date_Data_x / 6, paintTime
			);
			drawTextOnLine(canvas, str_data[4], Date_Data_x,
						   top_need_time_text + getTextHeight(paintText, "00.00"), Date_Data_x / 6,
						   paintTime
			);
		}


	}

	/*
	绘制周粗略数据
	返回自己占用的空间的高度值
	 */
	private int draw_week_weather(Canvas canvas, int top_need)
	{
		int count_high = 0;
		count_high = count_high +
					 drawTextOnLine(canvas, week_date[0], Date_Data_x, top_need + count_high,
									Date_Data_x / 3, paintText
					 ) + getTextHeight(paintText, "00.00");
		count_high = count_high +
					 drawTextOnLine(canvas, week_date[1], Date_Data_x, top_need + count_high,
									Date_Data_x / 3, paintText
					 ) + getTextHeight(paintText, "00.00");
		count_high = count_high +
					 draw_bitmap(canvas, top_need + Date_Data_x + count_high, top_need + count_high,
								 0, week_bitmap_list[0]
					 );

		int bottom_need;
		bottom_need = top_need + count_high + Date_Data_x * 3 / 2;
		drawLine1(canvas, bottom_need, top_need + count_high, week_hightmp_drawLineData);
		drawLine1(canvas, bottom_need, top_need + count_high, week_lowtmp_drawLineData);
		count_high = count_high + Date_Data_x * 3 / 2;

		count_high = count_high +
					 draw_bitmap(canvas, top_need + Date_Data_x + count_high, top_need + count_high,
								 0, week_bitmap_list[1]
					 );

		return count_high;
	}

	/*绘制一行大小相同的图片
	该函数对此程序进行过适配，无法直接拿走调用
	Canvas canvas		绘图类
	int bottom_need		底线
	int top_need		顶线
	int start_X			行首缩进
	Bitmap[] bitmap_list图片数组
	返回自己高度的大小
	*/
	private int draw_bitmap(
			Canvas canvas, int bottom_need, int top_need, int start_X, Bitmap[] bitmap_list)
	{
		int img_start_x;
		int img_end_x;
		for (int i = 0; i < bitmap_list.length; i++)
		{
			img_start_x = i * Date_Data_x + start_X + x_scroll - Date_Data_x / 2;
			img_end_x = img_start_x + bottom_need - top_need;
			//Log.e("TAG", "drawLine  1111: ");
			if (bitmap_list[i] != null)
			{
				Rect rect = new Rect(img_start_x, top_need, img_end_x, bottom_need);
				//两对数据，确定两个坐标，确定一个矩形。
				//Log.e("TAG", "drawLine  1111: ");
				canvas.drawBitmap(bitmap_list[i], null, rect, null);
				//Log.e("TAG", "drawLine  2222: ");
			}
		}
		return bottom_need - top_need;
	}

	/*写两行字
	时间
	08：00  11：00 …………
	Canvas canvas       绘图类
	String[] strings    要绘制的字符串数组
	int Text_Text       两个文字之间的横向距离
	int top_need
	String name_title
	 */
	private int drawTextOnLine_AndTitle(
			Canvas canvas, String[] strings, int Text_Text, int top_need, int start_X,
			String name_title, Paint paintText)//绘制折线和折线的总标题
	{
		canvas.drawText(name_title, start_X + x_scroll - dip2px(25), top_need, paintText);
		top_need = top_need + getTextHeight(paintText, "00.00");
		for (int i = 0; i < strings.length; i++)
		{
			//Log.e("TAG", "strings  "+i+": "+strings[i]);
			if (strings[i] == null || strings[i].equals(""))
			{
				continue;
			}
			canvas.drawText(strings[i], start_X + x_scroll + i * Text_Text - dip2px(25),
							top_need + getTextHeight(paintText, "00.00"), paintText
			);
			//Log.e("TAG", "onDraw  8: ");
		}
		return getTextHeight(paintText, "00.00");//返回新的top_need
	}

	/*写一行字
	08：00  11：00 …………
	Canvas canvas       绘图类
	String[] strings    要绘制的字符串数组
	int Text_Text       两个文字之间的横向距离
	int top_need
	int start_X			行首缩进值
	 */
	private int drawTextOnLine(
			Canvas canvas, String[] strings, int Text_Text, int top_need, int start_X,
			Paint paintText)//绘制折线和折线的总标题
	{
		for (int i = 0; i < strings.length; i++)
		{
			//Log.e("TAG", "strings  "+i+": "+strings[i]);
			if (strings[i] == null)
			{
				continue;
			}
			canvas.drawText(strings[i], start_X + x_scroll + i * Text_Text - dip2px(25),
							top_need + getTextHeight(paintText, "00.00"), paintText
			);
		}
		return getTextHeight(paintText, "00.00");//返回新的top_need
	}

	//绘制数据线条和标题
	private void drawLine1_AndTitle(
			Canvas canvas, int bottom_need, int top_need, DrawLineData _drawLineData)//绘制折线和折线的总标题
	{

		int old_text_color = paintText.getColor();
		paintText.setColor(_drawLineData.line_title_color);

		String title_str;
		if (_drawLineData.value_min == Float.MIN_VALUE ||
			_drawLineData.value_max == Float.MAX_VALUE)
		{
			title_str = String.format(Locale.CHINA, "%s/%s ", _drawLineData.data_name,
									  _drawLineData.data_units
			);
		}
		else
		{
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
			decimalFormat.setDecimalSeparatorAlwaysShown(false);
			title_str = String.format(Locale.CHINA, "%s/%s %s:%s %s:%s", _drawLineData.data_name,
									  _drawLineData.data_units, mContext.getString(R.string.最小值),
									  decimalFormat.format(_drawLineData.value_min),
									  mContext.getString(R.string.最大值),
									  decimalFormat.format(_drawLineData.value_max)
			);
		}
		canvas.drawText(title_str, dip2px(25),
						top_need + getTextHeight(paintText, "00.00") - xyTextSize / 2.0f, paintText
		);
		drawLine1(canvas, bottom_need, top_need + getTextHeight(paintText, "00.00"), _drawLineData);
		paintText.setColor(old_text_color);
	}

	/*绘制折线和折线点的数据
	Canvas canvas               画图类
	int bottom_need             折线的底部坐标
	int top_need                折线的顶部坐标
	DrawLineData _drawLineData  折线数据类，包含数据的标题、单位、数值数组
	*/
	private void drawLine1(Canvas canvas, int bottom_need, int top_need, DrawLineData _drawLineData)
	{
		if (_drawLineData == null)
		{
			return;
		}
		int index_of_click = (int) ((click_x - x_scroll) / Date_Data_x);
		if (index_of_click < 0)
		{
			index_of_click = 0;
		}
		else if (index_of_click >= _drawLineData.data.length)
		{
			index_of_click = _drawLineData.data.length - 1;
		}
		//        if (BuildConfig.DEBUG && mHeight <= bottom_need)
		//        {
		//            throw new AssertionError("DrawDayWeather.drawLine1() 中，出现 mHeight < (bottom_need + top_need) ");
		//        }
		float aver = (bottom_need - top_need) /
					 Math.max(_drawLineData.max - _drawLineData.min, 0.001f); //y轴最小单位的距离


		Paint paint;
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(_drawLineData.line_title_color);
		paint.setStrokeWidth(4f);
		paint.setStyle(Paint.Style.STROKE);
		Path path = new Path();

		final int text_date_count =
				x_count_in_mWidth - getTextWidth(paintText, "00.00") / Date_Data_x - 1;
		int index = 0;
		float in_count_min_index = 0;
		float in_count_max_index = 0;
		float in_count_min = _drawLineData.max + 1;
		float in_count_max = _drawLineData.min - 1;

		int need_draw_start_index = (int) ((-x_scroll) / Date_Data_x);
		if (need_draw_start_index < _drawLineData.continue_index)
		{
			need_draw_start_index = _drawLineData.continue_index;
		}
		if (need_draw_start_index < 0)
		{
			need_draw_start_index = 0;
		}
		int need_draw_end_index = (int) ((getWidth() - x_scroll) / Date_Data_x) + 2;
		need_draw_end_index = Math.min(need_draw_end_index, _drawLineData.data.length);

		if (draw_mode == 1)
		{
			path.moveTo(x_scroll,
						bottom_need - (aver * (_drawLineData.data[need_draw_start_index] - _drawLineData.min))
			);
		}
		float xdata;
		for (int i = need_draw_start_index; i < need_draw_end_index; i++)
		{
			xdata = _drawLineData.data[i];
			if (xdata > in_count_max)
			{
				in_count_max = xdata;
				in_count_max_index = i;
			}
			if (xdata < in_count_min)
			{
				in_count_min = xdata;
				in_count_min_index = i;
			}
			index++;

			if (draw_mode == 1)
			{
				path.lineTo(x_scroll + i * Date_Data_x,
							bottom_need - (aver * (xdata - _drawLineData.min))
				);
			}
			else if (draw_mode == 0)
			{
				canvas.drawPoint(x_scroll + i * Date_Data_x,
								 bottom_need - (aver * (xdata - _drawLineData.min)), paint
				);
			}

			if (index >= text_date_count ||
				(need_draw_end_index - need_draw_start_index < text_date_count &&
				 i == need_draw_end_index - 1))
			{
				//每隔text_date_count进行标记，标记最大值和最小值
				//max
				if (Math.abs(in_count_max_index - index_of_click) > 3)
				{
					String str_data = String.valueOf(in_count_max);
					Rect rect = getTextRect(paintText, str_data);
					float y = bottom_need - (aver * (in_count_max - _drawLineData.min));
					float x = x_scroll + in_count_max_index * Date_Data_x + dip2px(8);
					// rect.width();
					// rect.height();
					if (y - rect.height() < top_need)
					{
						y = top_need + rect.height();
					}
					if (x + rect.width() > getWidth())
					{
						x = x - rect.width();
					}
					canvas.drawText(String.valueOf(in_count_max), x, y, paintText);
					canvas.drawCircle(x_scroll + in_count_max_index * Date_Data_x,
									  bottom_need - (aver * (in_count_max - _drawLineData.min)),
									  bigCircleR, paint
					);
					//canvas.drawCircle(x_scroll + in_count_max_index * Date_Data_x, bottom_need - (aver * (in_count_max - _drawLineData.min)), smallCircleR, paintBack);
				}

				//min
				if (Math.abs(in_count_min_index - index_of_click) > 3 &&
					in_count_min != in_count_max)
				{
					float y;
					float x;
					String str_data = String.valueOf(in_count_min);
					Rect rect = getTextRect(paintText, str_data);
					// rect.width();
					// rect.height();
					y = bottom_need - (aver * (in_count_min - _drawLineData.min));
					if (y - rect.height() < top_need)
					{
						y = top_need + rect.height();
					}
					x = x_scroll + in_count_min_index * Date_Data_x + dip2px(8);
					if (x + rect.width() > getWidth())
					{
						x = x - rect.width();
					}
					canvas.drawText(String.valueOf(in_count_min), x, y, paintText);
					canvas.drawCircle(x_scroll + in_count_min_index * Date_Data_x,
									  bottom_need - (aver * (in_count_min - _drawLineData.min)),
									  bigCircleR, paint
					);
					//canvas.drawCircle(x_scroll + in_count_min_index * Date_Data_x, bottom_need - (aver * (in_count_min - _drawLineData.min)), smallCircleR, paintBack);
				}
				index = 0;
				in_count_min = _drawLineData.max + 1;
				in_count_max = _drawLineData.min - 1;
			}
		}
		/*
		{
			//这里给最后一段不够text_date_count进行标记
			//max
			if (bottom_need - (aver * (in_count_max - _drawLineData.min)) - getTextHeight(paintText, "00.00") < top_need)
			{
				canvas.drawText(String.valueOf(in_count_max), x_scroll + in_count_max_index * Date_Data_x + dip2px(8), top_need + getTextHeight(paintText, "00.00"), paintText);
			}
			else
			{
				canvas.drawText(String.valueOf(in_count_max), x_scroll + in_count_max_index * Date_Data_x + dip2px(8), bottom_need - (aver * (in_count_max - _drawLineData.min)), paintText);
			}

			canvas.drawCircle(x_scroll + in_count_max_index * Date_Data_x, bottom_need - (aver * (in_count_max - _drawLineData.min)), bigCircleR, paint);
			canvas.drawCircle(x_scroll + in_count_max_index * Date_Data_x, bottom_need - (aver * (in_count_max - _drawLineData.min)), smallCircleR, paintBack);
			//min
			if (in_count_min != in_count_max)
			{
				if (bottom_need - (aver * (in_count_min - _drawLineData.min)) - getTextHeight(paintText, "00.00") < top_need)
				{
					canvas.drawText(String.valueOf(in_count_min), x_scroll + in_count_min_index * Date_Data_x + dip2px(8), top_need + getTextHeight(paintText, "00.00"), paintText);
				}
				else
				{
					canvas.drawText(String.valueOf(in_count_min), x_scroll + in_count_min_index * Date_Data_x + dip2px(8), bottom_need - (aver * (in_count_min - _drawLineData.min)), paintText);
				}
				canvas.drawCircle(x_scroll + in_count_min_index * Date_Data_x, bottom_need - (aver * (in_count_min - _drawLineData.min)), bigCircleR, paint);
				canvas.drawCircle(x_scroll + in_count_min_index * Date_Data_x, bottom_need - (aver * (in_count_min - _drawLineData.min)), smallCircleR, paintBack);
			}
		}
		 */
		if (draw_mode == 1)
		{
			canvas.drawPath(path, paint);//将线条连起来
		}


		if (index_of_click < _drawLineData.data.length && index_of_click>=_drawLineData.continue_index)
		{
			in_count_max = _drawLineData.data[index_of_click];
			{
				String str_data = String.valueOf(in_count_max);
				Rect rect = getTextRect(paintText, str_data);
				float x = click_x;
				float y = bottom_need - (aver * (in_count_max - _drawLineData.min));
				if (y - rect.height() < top_need)
				{
					y = top_need + rect.height();
				}
				if (x + rect.width() > getWidth())
				{
					x = x - rect.width();
				}
				canvas.drawText(String.valueOf(in_count_max), x, y, paintText);
				canvas.drawCircle(x_scroll + index_of_click * Date_Data_x,
								  bottom_need - (aver * (in_count_max - _drawLineData.min)),
								  bigCircleR, paint
				);
				//canvas.drawCircle(x_scroll + index_of_click * Date_Data_x, bottom_need - (aver * (in_count_max - _drawLineData.min)), smallCircleR, paintBack);
			}
		}

	}

	private void draw_time_text(Canvas canvas)
	{
		if (drawTimeData == null || drawLineData == null || drawLineData.length < 1 ||
			drawTimeData.size() != drawLineData[0].data.length)
		{
			return;
		}
		int index_of_start = (int) ((20 - x_scroll) / Date_Data_x);
		DrawTimeData last_data = new DrawTimeData("0000-00-00", "99:59:59");
		int index_end = Math.min((int) ((getWidth() - x_scroll) / Date_Data_x) + 2,
								 drawLineData[0].data.length
		);
		if (index_of_start < 0)
		{
			index_of_start = 0;
		}
		int text_hight = getTextHeight(paintText, "00");
		float top_need = dip2px(8);
		float date_y = top_need + text_hight;
		float time_y = date_y + dip2px(8) + text_hight;
		float kedu_y = time_y + dip2px(8) + text_hight;
		DrawTimeData now_data;
		String tmp;
		boolean need_kedu;
		Path path = new Path();
		float bili = (float) Math.sqrt(2);
		float Date_Data_x_r45 = Date_Data_x * bili / 2;
		float x_scroll_r45 = x_scroll * bili / 2;
		boolean is_first = true;
		for (int i = index_of_start; i < index_end; i++)
		{
			need_kedu = false;
			now_data = drawTimeData.get(i);
			tmp = now_data.get_draw_date(last_data);
			if (tmp.length() > 0)
			{
				need_kedu = true;
				Rect rect = getTextRect(paintText, tmp);
				float x = x_scroll + i * Date_Data_x + dip2px(8);
				// rect.width();
				// rect.height();
				if (x + rect.width() > getWidth())
				{
					x = x - rect.width();
				}
				canvas.drawText(tmp, x, date_y, paintText);
			}
			tmp = now_data.get_draw_time(last_data);
			if (tmp.length() > 0)
			{
				need_kedu = true;
				canvas.save();
				canvas.rotate(45);
				//				canvas.translate(-getHeight(), 0);

				// 设置竖向绘制文本的字体大小和颜色
				Paint paint = new Paint();
				paint.setTextSize(paintText.getTextSize());
				paint.setColor(paintText.getColor());
				Rect rect = getTextRect(paintText, tmp);
				float x = x_scroll_r45 + i * Date_Data_x_r45 + dip2px(8);
				// rect.width();
				// rect.height();
				if (is_first && x < 20 + Date_Data_x * 2)
				{
					is_first = false;
					x = 5;
				}
				else if (x + rect.width() > getWidth())
				{
					x = x - rect.width();
				}
				canvas.drawText(tmp, x + (time_y * bili / 2), -x + (time_y * bili / 2), paint);
				canvas.restore();
			}
			if (need_kedu)
			{
				float x = x_scroll + i * Date_Data_x;
				path.moveTo(x, top_need);
				path.lineTo(x, kedu_y);
			}
			last_data = now_data;
		}
		if (!path.isEmpty())
		{
			Paint paint;
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(color_of_click_line);
			paint.setStrokeWidth(3f);
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawPath(path, paint);//将线条连起来
			paint.setTextSize(paintText.getTextSize());
			int index_of_click = (int) ((click_x - x_scroll) / Date_Data_x);
			if (index_of_click < 0)
			{
				index_of_click = 0;
			}
			else if (index_of_click >= drawTimeData.size())
			{
				index_of_click = drawTimeData.size() - 1;
			}
			float date_y1 = kedu_y + dip2px(8) + text_hight * 2;
			float time_y2 = date_y1 + dip2px(8) + text_hight;
			now_data = drawTimeData.get(index_of_click);
			canvas.drawText(now_data.date, click_x, date_y1, paintText);
			canvas.drawText(now_data.time, click_x, time_y2, paintText);
		}

	}


	/*


	 */
	private void draw_of_click(
			Canvas canvas)//, int bottom_need, int top_need, DrawLineData _drawLineData)
	{
		// int index = click_x

		Paint paint;
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(color_of_click_line);
		paint.setStrokeWidth(5f);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(click_x, click_y, 20, paint);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(color_of_click_line);
		paint.setStrokeWidth(3f);
		paint.setStyle(Paint.Style.STROKE);
		// 设置虚线样式
		float[] intervals = {10f, 5f}; // 虚线的间隔和实线的长度
		float phase = 0; // 虚线的偏移量
		DashPathEffect dashPathEffect = new DashPathEffect(intervals, phase);
		paint.setPathEffect(dashPathEffect);
		Path path = new Path();
		path.moveTo(click_x, 0);
		path.lineTo(click_x, getHeight());
		canvas.drawPath(path, paint);//将线条连起来


	}


	/*
		移交触摸事件的处理权限
	*/
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//return super.onTouchEvent(event);
		if (str_data__ < 5)//这里修改最小可滚动的数据点的个数
		{
			return false;
		}
		gestureDetector.onTouchEvent(event);
		return true;
	}

	/**
	 * 获取文字的高度
	 */
	private int getTextHeight(Paint paint, String text)
	{
		Rect rect = new Rect();
		paint.getTextBounds(text, 0, text.length(), rect);
		return rect.height();
	}

	/**
	 * 获取文字的宽度
	 */
	private int getTextWidth(Paint paint, String text)
	{
		Rect rect = new Rect();
		paint.getTextBounds(text, 0, text.length(), rect);
		return rect.width();
	}

	/**
	 * 获取文字的宽度
	 */
	private Rect getTextRect(Paint paint, String text)
	{
		Rect rect = new Rect();
		paint.getTextBounds(text, 0, text.length(), rect);
		return rect;
	}

	public int dip2px(int dp)
	{
		float density = getContext().getResources().getDisplayMetrics().density;
		return (int) (dp * density + 0.5);
	}

	/**
	 * sp转换px
	 */
	public int sp2px(int spValue)
	{
		final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	//折现的数据储存类
	static class DrawLineData
	{
		static int last_line_color = 0;
		static int last_text_color = 0;
		String data_name;
		String data_units;
		int line_title_color;
		int text_color;
		float data[];
		float max = Float.MAX_VALUE;
		float min = Float.MIN_VALUE;

		float value_max = Float.MAX_VALUE;
		float value_min = Float.MIN_VALUE;
		int continue_index = -1;

		public DrawLineData(String data_name, String data_units, int SIZE_data, String color)
		{
			this.data_name = data_name;
			this.data_units = data_units;
			data = new float[SIZE_data];
			this.line_title_color = Color.parseColor(color);
			text_color = this.line_title_color;
		}

		public DrawLineData(String data_name, String data_units, int SIZE_data)
		{
			this.data_name = data_name;
			this.data_units = data_units;
			data = new float[SIZE_data];
			random_color();
		}

		public void random_color()
		{
			this.line_title_color =
					line_color_list[(int) (Math.random() * line_color_list.length - 1)];
			text_color = text_color_list[(int) (Math.random() * text_color_list.length - 1)];
			while (text_color_list.length > 1 && text_color == last_text_color)
			{
				text_color = text_color_list[(int) (Math.random() * text_color_list.length - 1)];
			}
			while (line_color_list.length > 1 && this.line_title_color == last_line_color)
			{
				this.line_title_color =
						line_color_list[(int) (Math.random() * line_color_list.length - 1)];
			}
			last_line_color = this.line_title_color;
			last_text_color = text_color;
		}

		public void flush_value_max_min()
		{
			value_min = data[0];
			value_max = data[0];
			for (float i : data)
			{
				if (value_min > i)
				{
					value_min = i;
				}
				else if (value_max < i)
				{
					value_max = i;
				}
			}
		}
	}

	static class DrawTimeData
	{

		String date;
		String time;

		public DrawTimeData(String date, String time)
		{
			assert JaoYan.is_date(date);
			assert JaoYan.is_time(time);
			this.date = date;
			this.time = time.substring(0, time.length() - 3);//00:01  :49
		}

		public static int getCommonPrefixLength(String str1, String str2)
		{
			int minLength = Math.min(str1.length(), str2.length());
			int commonPrefixLength = 0;

			for (int i = 0; i < minLength; i++)
			{
				if (str1.charAt(i) == str2.charAt(i))
				{
					commonPrefixLength++;
				}
				else
				{
					break;
				}
			}

			return commonPrefixLength;
		}

		public String get_draw_date(DrawTimeData last_date)
		{
			//2023-12-16
			switch (getCommonPrefixLength(this.date, last_date.date))
			{
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				{
					// 2023
					return this.date;

				}
				case 5:
				case 6:
				case 7:
				{
					// -12
					return this.date.substring(this.date.length() - 5);

				}
				case 8:
				case 9:
				{
					// -15
					return this.date.substring(this.date.length() - 2);

				}
				case 10:
				default:
				{
					// 完全一样
					return "";

				}

			}
		}

		public String get_draw_time(DrawTimeData last_date)
		{
			//00:01
			switch (getCommonPrefixLength(this.time, last_date.time))
			{
				case 0:
				case 1:
				case 2:
				{
					// 00
					return this.time;

				}
				case 3:
				{
					// :01
					return this.time.substring(this.time.length() - 2);

				}
				case 4:
				case 5:
				default:
				{
					// :01
					return "";

				}

			}
		}

	}


	class MyOnGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener
	{
		Handler hander_inertia = null;//手指滑动页面之后的惯性运动
		Runnable runnable_inertia = null;


		@Override
		public boolean onDown(MotionEvent e)
		{
			if (hander_inertia != null && runnable_inertia != null)
			{
				hander_inertia.removeCallbacks(runnable_inertia);
			}
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e)
		{

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e)
		{
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			//注意：这里的distanceX是e1.getX()-e2.getX()
			//float x = e1.getX();
			//Log.i("TAG", "onScroll  distanceX: " + distanceX);

			distanceX = -distanceX;
			if (x_scroll + distanceX > max_xScroll)
			{
				x_scroll = max_xScroll;
			}
			else if (x_scroll + distanceX < min_xScroll)
			{
				x_scroll = min_xScroll;
			}
			else
			{
				x_scroll = (int) (x_scroll + distanceX);
			}


			distanceY = -distanceY;
			if (y_scroll + distanceY > max_yScroll)
			{
				y_scroll = max_yScroll;
			}
			else if (y_scroll + distanceY < min_yScroll)
			{
				y_scroll = min_yScroll;
			}
			else
			{
				y_scroll = (int) (y_scroll + distanceY);
			}
			if (hander_inertia != null && runnable_inertia != null)
			{
				hander_inertia.removeCallbacks(runnable_inertia);
			}
			//Log.i("TAG", "onScroll  x_scroll: " + x_scroll);
			//Log.i("TAG", "onScroll  y_scroll: " + y_scroll);
			invalidate();//刷新图像
			return false;
		}


		@Override
		public void onLongPress(MotionEvent e)
		{
			Log.i("TAG", "onLongPress: ");
			if (draw_mode == 1)
			{
				draw_mode = 0;
			}
			else
			{
				draw_mode = 1;
			}
			invalidate();//刷新图像
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			//e1.getDownTime();
			//e2.getDownTime();
			//Log.i("TAG", "onFling: velocityX  " + velocityX + "  velocityY  " + velocityY);

			//使用 Handler 对象和 Runnable 对象，通过每隔 x 毫秒刷新一次图像来完成滑动的图像惯性移动
			/*
			https://www.jianshu.com/p/f50074c2da20
			https://www.jianshu.com/p/44b322dfc040
			removeCallbacks		删掉任务对象
			postDelayed			定时执行一个任务对象
			post				将任务对象添加到队列
			 */
			hander_inertia = new Handler();
			final int _velocityX = (int) velocityX;
			final int _velocityY = (int) velocityY;
			runnable_inertia = new Runnable()
			{
				@Override
				public void run()
				{
					//Log.i("TAG", "run: x_scroll  " + x_scroll + "  y_scroll   " + x_scroll);
					int distanceX, distanceY;
					boolean go_x = false, go_y = false;
					distanceX = _velocityX / refresh_rate;
					distanceY = _velocityY / refresh_rate;
					if (Math.abs((float) _velocityX) / Math.abs((float) _velocityY) > 2.5)
					{
						distanceY = 0;
					}
					else if (Math.abs((float) _velocityY) / Math.abs((float) _velocityX) > 2.5)
					{
						distanceX = 0;
					}
					//Log.i("TAG", "run: distanceX  " + distanceX + "  distanceY   " + distanceY);
					if (distanceX == 0)
					{
						go_x = true;
					}
					else if (x_scroll + distanceX > max_xScroll)
					{
						x_scroll = max_xScroll;
					}
					else if (x_scroll + distanceX < min_xScroll)
					{
						x_scroll = min_xScroll;
					}
					else
					{
						go_x = true;
						x_scroll = x_scroll + distanceX;
					}

					if (distanceY == 0)
					{
						go_y = true;
					}
					else if (y_scroll + distanceY > max_yScroll)
					{
						y_scroll = max_yScroll;
					}
					else if (y_scroll + distanceY < min_yScroll)
					{
						y_scroll = min_yScroll;
					}
					else
					{
						go_y = true;
						y_scroll = y_scroll + distanceY;
					}

					if (go_x || go_y)
					{
						hander_inertia.postDelayed(this, refresh_cycle);//1000ms / 刷新率 =帧间隔时间
					}
					else
					{
						hander_inertia.removeCallbacks(this);
					}
					//					x_scroll = x_scroll + _velocityX / refresh_rate;
					//					y_scroll = y_scroll + _velocityY / refresh_rate;
					//					hander_inertia.postDelayed(this, refresh_cycle);//1000ms / 刷新率 =帧间隔时间

					//Log.i("TAG", "run: x_scroll  " + x_scroll + "  y_scroll   " + x_scroll);
					//Log.i("TAG", "run: refresh_cycle  " + refresh_cycle);
					invalidate();//刷新图像
				}
			};
			hander_inertia.postDelayed(runnable_inertia, refresh_cycle);//1000ms / 刷新率 =帧间隔时间
			return false;
		}


		@Override
		public boolean onSingleTapConfirmed(@NonNull MotionEvent e)
		{
			// 记录按下的位置，在X轴上画一条线，把线对应坐标上的内容描述出来
			click_x = e.getX();
			click_y = e.getY();
			invalidate();//刷新图像
			return true;
		}

		@Override
		public boolean onDoubleTap(@NonNull MotionEvent e)
		{
			if (xyTextSize == sp2px(10))
			{
				xyTextSize = sp2px(20);
				setY_count_in_mHeight(3);
			}
			else
			{
				xyTextSize = sp2px(10);
				setY_count_in_mHeight(6);
			}
			invalidate();//刷新图像
			return true;
		}

		@Override
		public boolean onDoubleTapEvent(@NonNull MotionEvent e)
		{
			return false;
		}
	}
}
