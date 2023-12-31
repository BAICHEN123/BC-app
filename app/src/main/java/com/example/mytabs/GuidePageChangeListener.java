package com.example.mytabs;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class GuidePageChangeListener implements TabLayout.OnTabSelectedListener
{
	FloatingActionButton fab;
	int fig;
	public GuidePageChangeListener(FloatingActionButton fab)
	{
		super();
		this.fab=fab;
		this.fig=0;

	}
	@Override
	public void onTabSelected(TabLayout.Tab tab)
	{
		Log.e("TAG", "onTabSelected: "+tab.getPosition());
		fig=tab.getPosition();
		if(fig==0)
		{
			fab.setImageResource(R.drawable.ic_perm_group_sync_settings);
		}
		else if(fig==1)
		{
			fab.setImageResource(R.drawable.plus_sign);
		}
		else if(fig==2)
		{
			fab.setImageResource(R.drawable.ic_menu_share_holo_light1);
		}
	}

	@Override
	public void onTabUnselected(TabLayout.Tab tab)
	{

		Log.e("TAG", "onTabUnselected: "+tab.getPosition());
	}

	@Override
	public void onTabReselected(TabLayout.Tab tab)
	{

		Log.e("TAG", "onTabReselected: "+tab.getPosition());
	}
	public int get_fig()
	{
		return this.fig;
	}
}
