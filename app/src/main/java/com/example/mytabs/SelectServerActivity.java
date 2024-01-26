package com.example.mytabs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class SelectServerActivity extends Activity
{
	final String[] modes = {"http://", "https://"};
	Button select_http_s;
	Button button_save;
	Button button_quit;
	EditText editText;



	@Override
	protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_server);
		select_http_s = findViewById(R.id.button_http_s);
		button_save = findViewById(R.id.button_save);
		button_quit = findViewById(R.id.button_quit);
		editText = findViewById(R.id.name);
		select_http_s.setOnClickListener(this::onclick_button_http_s);
		button_save.setOnClickListener(this::onclick_button_save);
		button_quit.setOnClickListener(this::onclick_button_quit);
	}

	private void onclick_button_http_s(View v)
	{

		new AlertDialog.Builder(SelectServerActivity.this).setTitle("请选择协议")
														  .setItems(modes, (dialog, which) -> select_http_s.setText(modes[which])).show();
	}

	private void onclick_button_save(View v)
	{
		// 校验？校验个锤锤，仅作简单的符号替换
		String server_str = editText.getText().toString().replace("：", ":").replace("。", ".");
		if (server_str.isEmpty())
		{
			server_str = select_http_s.getText().toString() + getString(R.string.defaut_server);
		}
		else
		{
			server_str = select_http_s.getText().toString() + server_str;
		}
		final String server_str1 = server_str;
		new AlertDialog.Builder(this).setTitle(getString(R.string.这个吗)).setMessage(server_str)
									 .setNeutralButton(getString(R.string.确认),
													   new DialogInterface.OnClickListener()
													   {
														   @Override
														   public void onClick(
																   DialogInterface dialog,
																   int which)
														   {
															   SelectServer.save_server_config(
																	   getApplicationContext(),
																	   server_str1);
															   finish();
														   }
													   }
									 ).setNegativeButton(getString(R.string.取消), null).show();


	}

	private void onclick_button_quit(View v)
	{
		finish();
	}

	public boolean isValidInput(String input)
	{
		// 匹配 "ip:端口号" 格式
		String ipPortPattern = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:(\\d{1,5}))?$";

		// 匹配域名格式
		String domainPattern = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(:(\\d{1,5}))?$";

		// 检验是否符合 "ip:端口号" 或域名格式
		return input.matches(ipPortPattern) || input.matches(domainPattern);
	}
}
