package utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class RecordDeserializer
{
	public static <T> JsonDeserializer<T> forClass(Class<T> clazz)
	{
		return new JsonDeserializer<T>() {
			@Override
			public T deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException
			{
				if(!clazz.isRecord())
				{
					throw new JsonParseException(
						"RecordDeserializer can't deserialize non-record %s!"
							.formatted(typeOfT));
				}
				var types = new ArrayList<Class<?>>();
				var params = new ArrayList<Object>();
				var obj = json.getAsJsonObject();
				var components = clazz.getRecordComponents();
				for(var component : components)
				{
					types.add(component.getType());
					var componentElem = obj.get(component.getName());
					if(componentElem.isJsonObject())
					{
						params.add(context.deserialize(componentElem.getAsJsonObject(),
							component.getGenericType()));
					}
					else if(componentElem.isJsonPrimitive())
					{
						params.add(context.deserialize(componentElem.getAsJsonPrimitive(),
							component.getGenericType()));
					}
					else if(componentElem.isJsonArray())
					{
						params.add(context.deserialize(componentElem.getAsJsonArray(),
							component.getGenericType()));
					}
					else if(componentElem.isJsonNull())
					{
						params.add(context.deserialize(componentElem.getAsJsonNull(),
							component.getGenericType()));
					}
				}
				Constructor<T> constructor;
				try
				{
					constructor = clazz.getDeclaredConstructor(types.toArray(new Class<?>[0]));
					constructor.setAccessible(true);
					var instance = constructor.newInstance(params.toArray());
					constructor.setAccessible(false);
					return instance;
				}
				catch(NoSuchMethodException | SecurityException | InstantiationException |
					IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					e.printStackTrace();
					throw new IllegalArgumentException(json.toString());
				}
			}
		};
	}
}
