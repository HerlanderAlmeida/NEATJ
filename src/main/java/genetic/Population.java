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
	/** Version 1.0.2 */
	private static final long serialVersionUID = -855634598875462977L;

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
		if(this.size() > 7)
		{
			return String.format(
				"Population: [%s, %s, %s, ... %d more Individuals ..., %s, %s, %s], size: %d",
				this.get(0), this.get(1), this.get(2), this.size() - 6, this.get(this.size() - 3), this.get(this.size() - 2),
				this.get(this.size() - 1), this.size());
		}
		return String.format("Population: %s, size: %d", super.toString(), this.size());
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