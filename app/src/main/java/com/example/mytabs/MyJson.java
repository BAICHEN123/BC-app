package com.example.mytabs;

import com.google.gson.Gson;

import java.util.HashMap;

public class MyJson
{

	public static HashMap<String, Object> parseJson(String jsonString)
	{
		Gson gson = new Gson();
		HashMap dict = gson.fromJson(jsonString, HashMap.class);
		return dict;
		// 在这里你可以使用解析后的数据进行后续操作
	}

	public static String toJson(HashMap<String, Object> dict)
	{
		Gson gson = new Gson();
		String json1 = gson.toJson(dict);
		return json1;
		// 在这里你可以使用解析后的数据进行后续操作
	}

	public static Boolean objIsDouble(Object obj){
		return obj instanceof Double;
	}
	public static Boolean objEqToValue(Object obj,Double value){
		if(!MyJson.objIsDouble(obj) ){
			return false;
		}
		double doubleValue = (double) obj;
		return doubleValue == value;
	}


}
