package com.example.mytabs;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static android.view.View.inflate;

public class LDXuanZeAdapter extends BaseExpandableListAdapter
{

	Context context;
	ArrayList<MyEquipment> myEquipments;

	public LDXuanZeAdapter(Context context)
	{
		this.context = context;
		myEquipments = new ArrayList<>();
	}


	@Override
	public int getGroupCount()
	{
		return myEquipments.size();
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		if (myEquipments.get(groupPosition).size() == 0 || myEquipments.get(groupPosition).get_item_name(0).equals("状态"))
		{
			return 0;
		}
		return myEquipments.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		return null;
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		View view;
		//Log.i("TAG", "getGroupView: :	1");
		if (convertView == null)
		{
			view = inflate(this.context, R.layout.item_number_title, null);
		}
		else
		{
			view = convertView;
		}

		TextView textView1 = view.findViewById(R.id.item_title_number);
		textView1.setText(String.valueOf(groupPosition + 1));
		Button button1 = view.findViewById(R.id.item_title_data);
		button1.setText(this.myEquipments.get(groupPosition).name);
		ImageView imageView = view.findViewById(R.id.open_items_image);
		if (isExpanded)
		{
			imageView.setImageResource(R.drawable.expander_close_holo_light);
		}
		else
		{
			imageView.setImageResource(R.drawable.expander_open_holo_light);
		}

		return view;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		View view;
		final EquipmentDataItem itemdata = this.myEquipments.get(groupPosition).get_item_data(childPosition);
		view = inflate(this.context, R.layout.item_title_text, null);
		TextView textView1 = view.findViewById(R.id.equipment_data_item_title);
		textView1.setText(itemdata.item_name);
		if (itemdata.ke_yi_xiu_gai())
		{
			textView1.setTextColor(Color.parseColor("#6200EE"));
		}
//		textView1.setOnClickListener(new View.OnClickListener(){
//
//			@Override
//			public void onClick(View v)
//			{
//
//			}
//		});

		return view;//当这里返回null的时候程序会 结束/崩溃
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}
}
