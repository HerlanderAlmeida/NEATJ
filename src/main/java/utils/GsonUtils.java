package utils;

import java.io.Reader;
import java.lang.reflect.Type;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public enum GsonUtils
{
	INSTANCE;

	private Gson gson = GsonUtils.gsonBuilder().create();

	public void setGson(Gson customized)
	{
		this.gson = customized;
	}

	public Gson getGson()
	{
		return this.gson;
	}

	public Gson swapGson(Gson customized)
	{
		var ret = this.gson;
		setGson(customized);
		return ret;
	}

	public static GsonBuilder gsonBuilder()
	{
		return new GsonBuilder()
			.addSerializationExclusionStrategy(GsonUtils.serializationExclusionStrategy())
			.addDeserializationExclusionStrategy(GsonUtils.deserializationExclusionStrategy());
	}

	public static <T> T fromJson(String json, Class<T> classOfT)
	{
		return INSTANCE.gson.fromJson(json, classOfT);
	}

	public static <T> T fromJson(Reader jsonReader, Class<T> classOfT)
	{
		return INSTANCE.gson.fromJson(jsonReader, classOfT);
	}

	public static <T> T fromJson(Reader jsonReader, Type typeOfT)
	{
		return INSTANCE.gson.fromJson(jsonReader, typeOfT);
	}

	public static String toJson(Object object)
	{
		if(object == null)
		{
			return "";
		}
		return INSTANCE.gson.toJson(object);
	}

	public static ExclusionStrategy serializationExclusionStrategy()
	{
		return new ExclusionStrategy()
		{
			@Override
			public boolean shouldSkipField(FieldAttributes f)
			{
				final var exclude = f.getAnnotation(Exclude.class);
				return exclude != null && exclude.serialize();
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz)
			{
				return false;
			}
		};
	}

	public static ExclusionStrategy deserializationExclusionStrategy()
	{
		return new ExclusionStrategy()
		{
			@Override
			public boolean shouldSkipField(FieldAttributes f)
			{
				final var exclude = f.getAnnotation(Exclude.class);
				return exclude != null && exclude.deserialize();
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz)
			{
				return false;
			}
		};
	}
}
