package com.example.mytabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MyFloatingActionButton extends FloatingActionButton
{
	Context context;
	private float MAX_x;
	private float MAX_y;
	private GestureDetector gestureDetector; //滑动手势
	private OnClickListener clickListener1;

	public MyFloatingActionButton(Context context)
	{
		super(context);
		//Log.i("TAG", "onTouchEvent: getWidth  " + getWidth());
		//Log.i("TAG", "onTouchEvent: getHeight  " + getHeight());
		this.context = context;
	}

	public MyFloatingActionButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		//Log.i("TAG", "onTouchEvent: getWidth  " + getWidth());
		//Log.i("TAG", "onTouchEvent: getHeight  " + getHeight());
		this.context = context;
		if (gestureDetector == null)
		{
			gestureDetector = new GestureDetector(context, new MyOnGestureListener());
		}
	}

	public MyFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		//Log.i("TAG", "onTouchEvent: getWidth  " + getWidth());
		//Log.i("TAG", "onTouchEvent: getHeight  " + getHeight());
		this.context = context;
//		if(gestureDetector==null)
//		{
//			gestureDetector = new GestureDetector(context, new MyOnGestureListener());
//		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		return gestureDetector.onTouchEvent(ev);
	}

	@Override
	public void setOnClickListener(@Nullable OnClickListener l)
	{
		//super.setOnClickListener(l);
		this.clickListener1 = l;
	}

	private void onclick_this()
	{
		if (this.clickListener1 != null)
		{
			this.clickListener1.onClick(this);
		}
	}


	class MyOnGestureListener implements GestureDetector.OnGestureListener
	{

		@Override
		public boolean onDown(MotionEvent e)
		{
			//Log.e("TAG", "onDown: ");
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e)
		{
			Log.e("TAG", "onShowPress: ");
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e)
		{
			//Log.e("TAG", "onSingleTapUp: 1");
			onclick_this();
			//Log.e("TAG", "onSingleTapUp: 2");
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			//Log.i("TAG", "onScroll: x=" + x + " y=" + y + " e1x=" + e1.getX() + " e1y=" + e1.getY() + " distanceX=" + distanceX + " distanceY=" + distanceY);
			//Log.i("TAG", "onScroll: e2.getRawX()=" + e2.getRawX() + " e2.getRawY()=" + e2.getRawY() + " e2x=" + e2.getX() + " e2y=" + e2.getY());
			float x = e2.getRawX() - getWidth() / 2.0f;
			float y = e2.getRawY() - getHeight();
			if (x < 0)
			{
				x = 0;
			}
			else if (x > MAX_x - getWidth())
			{
				x = MAX_x - getWidth();
			}

			if (y < getHeight())
			{
				y = getHeight();
			}
			else if (y > MAX_y - getHeight())
			{
				y = MAX_y - getHeight();
			}
			//Log.i("TAG", "onScroll: move x=" + x + " y=" + y);
			move1(x, y);
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e)
		{
			Log.e("TAG", "onLongPress: ");
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			Log.e("TAG", "onFling: ");
			return false;
		}
	}

	public void setMAX(float X, float Y)
	{
		Log.e("TAG", "setMAX: x= " + X + "y= " + Y);
		this.MAX_x = X;
		this.MAX_y = Y;
	}

	private void move1(float x, float y)
	{
		setX(x);
		setY(y);
	}
}
