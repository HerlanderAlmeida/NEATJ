package utils;

import com.google.gson.Gson;

public enum GsonUtils
{
	INSTANCE;

	private Gson gson = new Gson();

	public static <T> T fromJson(String json, Class<T> classOfT)
	{
		return INSTANCE.gson.fromJson(json, classOfT);
	}

	public static String toJson(Object object)
	{
		if(object == null)
		{
			return "";
		}
		return INSTANCE.gson.toJson(object);
	}
}
