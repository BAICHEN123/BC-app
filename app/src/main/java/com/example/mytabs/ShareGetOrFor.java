package com.example.mytabs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

public class ShareGetOrFor extends Activity
{
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_get_or_for);
		Button button_for_friends = findViewById(R.id.for_friends);
		Button button_get_friends = findViewById(R.id.get_friends);
		button_for_friends.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				Log.i("TAG", "onCreate:	button_for_friends	onClick	1");
				Intent intent=getIntent();
				Intent intent_new= new Intent(ShareGetOrFor.this,ShareEquipment.class);
				intent_new.putExtra("list_name",intent.getStringArrayExtra("list_name"));
				intent_new.putExtra("list_eid",intent.getLongArrayExtra("list_eid"));
				startActivityForResult(intent_new, 1);//打开分享页面
				Log.i("TAG", "onCreate: 	button_for_friends	onClick	2");

			}
		});
		button_get_friends.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Log.i("TAG", "onCreate:	button_get_friends	onClick	1");
				Intent intent_new= new Intent(ShareGetOrFor.this,GetShare.class);
				startActivityForResult(intent_new, 2);//打开接收页面
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==1&&resultCode==1)
		{
			//setResult(1);
			finish();
		}
		else if(requestCode==2&&resultCode==1)
		{
			setResult(2);
			finish();
		}

	}
}
