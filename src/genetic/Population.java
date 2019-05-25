package genetic;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * A population consists of a fixed size pool of Individuals.
 */
public class Population<T> implements Supplier<List<T>>, Iterable<T>
{
	final List<T> indivs;
	
	private Population(int size)
	{
		indivs = new ArrayList<>(size);
	}
	
	public Population(int n, boolean primitiveParams, Class<T> cls, Object... params)
	{
		this(n);
		List<Class<?>> classes = new ArrayList<>();
		for(var o : params)
		{
			classes.add(o.getClass());
		}
		if(primitiveParams)
			classes.replaceAll((Class<?> c) ->
			{
				if(c == Byte.class)
					return byte.class;
				if(c == Short.class)
					return short.class;
				if(c == Integer.class)
					return int.class;
				if(c == Long.class)
					return long.class;
				if(c == Float.class)
					return float.class;
				if(c == Double.class)
					return double.class;
				if(c == Boolean.class)
					return boolean.class;
				if(c == Character.class)
					return char.class;
				if(c == Void.class)
					return void.class;
				return c;
			});
		Objects.requireNonNull(cls);
		Objects.requireNonNull(params);
		try
		{
			Constructor<T> c = cls.getConstructor(classes.toArray(new Class<?>[classes.size()]));
			IntStream.range(0, n).forEach(i ->
			{
				try
				{
					indivs.add(cls.cast(c.newInstance(params)));
				}
				catch(Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
			indivs.clear();
		}
	}
	
	public Population(Collection<T> c)
	{
		this(c.size(), c.iterator()::next);
	}
	
	public Population(int n, Supplier<T> supplier)
	{
		this(n);
		Objects.requireNonNull(supplier);
		IntStream.range(0, n).forEach(i -> indivs.add(supplier.get()));
	}
	
	public void addAll(Collection<? extends T> c)
	{
		indivs.addAll(c);
	}
	
	public boolean removeAll(Collection<?> c)
	{
		return indivs.removeAll(c);
	}
	
	public void add(int idx, T i)
	{
		indivs.add(idx, i);
	}
	
	public boolean add(T i)
	{
		return indivs.add(i);
	}
	
	public T remove(int idx)
	{
		return indivs.remove(idx);
	}
	
	public boolean remove(Object o)
	{
		return indivs.remove(o);
	}
	
	public T get(int i)
	{
		return indivs.get(i);
	}
	
	public List<T> get()
	{
		return indivs;
	}
	
	public Iterator<T> iterator()
	{
		return indivs.iterator();
	}
	
	public int size()
	{
		return indivs.size();
	}
	
	public Population<T> join(Population<? extends T> other)
	{
		Population<T> joined = new Population<>(this.size() + other.size());
		joined.indivs.addAll(this.indivs);
		joined.indivs.addAll(other.indivs);
		return joined;
	}
	
	public String toString()
	{
		if(this.size() > 7)
			return String.format(
				"Population: [%s, %s, %s, ... %d more Individuals ..., %s, %s, %s], size: %d",
				indivs.get(0), indivs.get(1), indivs.get(2), size() - 6, indivs.get(size() - 3),
				indivs.get(size() - 2), indivs.get(size() - 1), indivs.size());
		return String.format("Population: %s", indivs);
	}
	
}