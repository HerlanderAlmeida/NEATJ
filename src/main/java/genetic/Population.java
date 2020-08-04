package genetic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * A population consists of a fixed size pool of Individuals.
 */
public class Population<T extends Individual> extends ArrayList<T>
{
	/** Version 0.0.1 */
	private static final long serialVersionUID = -3987426094906704015L;

	private Population(int size)
	{
		super(size);
	}

	public Population(Collection<T> c)
	{
		this(c.size(), c.iterator()::next);
	}

	public Population(int n, Supplier<T> supplier)
	{
		this(n);
		Objects.requireNonNull(supplier);
		Stream.generate(supplier).limit(n).forEach(this::add);
	}

	@Override
	public String toString()
	{
		if(size() > 7)
		{
			return String.format(
				"Population: [%s, %s, %s, ... %d more Individuals ..., %s, %s, %s], size: %d",
				get(0), get(1), get(2), size() - 6, get(size() - 3), get(size() - 2),
				get(size() - 1), size());
		}
		return String.format("Population: %s, size: %d", super.toString(), size());
	}

	public static <T extends Individual> PopulationDeserializer<T> deserializer(Type type)
	{
		return new PopulationDeserializer<>(type);
	}

	public static class PopulationDeserializer<S extends Individual>
		implements JsonDeserializer<Population<S>>
	{
		private Type individualType;

		public PopulationDeserializer(Type type)
		{
			this.individualType = type;
		}

		@Override
		public Population<S> deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException
		{
			if(json.isJsonArray())
			{
				var individuals = new ArrayList<S>();
				for(var element : json.getAsJsonArray())
				{
					if(element.isJsonObject())
					{
						var individual = element.getAsJsonObject();
						individuals.add(context.deserialize(individual, this.individualType));
					}
					else
					{
						throw new JsonParseException(
							String.format("Json element %s is not an Individual!", json));
					}
				}
				return new Population<>(individuals);
			}
			else
			{
				throw new JsonParseException(
					String.format("Json element %s is not array-like!", json));
			}
		}
	}
}