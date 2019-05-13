package genetic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * A population consists of a fixed size pool of Individuals.
 */
public class Population<T extends Individual<? super T>> implements Supplier<List<Individual<?>>>
{
	final List<Individual<?>> indivs;
	
	private Population(int size)
	{
		indivs = new ArrayList<Individual<?>>(size);
	}
	
	public Population(int n, T instantiable)
	{
		this(n);
		IntStream.range(0, n).forEach(i -> indivs.add((Individual<?>)instantiable.getInstance()));
	}
	
	public Population(int n, Supplier<T> supplier)
	{
		this(n);
		IntStream.range(0, n).forEach(i -> indivs.add(supplier.get()));
	}
	
	public void addAll(Collection<? extends Individual<?>> c)
	{
		indivs.addAll(c);
	}
	
	public boolean removeAll(Collection<?> c)
	{
		return indivs.removeAll(c);
	}
	
	public boolean add(Individual<?> i)
	{
		return indivs.add(i);
	}
	
	public boolean remove(Object o)
	{
		return indivs.remove(o);
	}
	
	public Individual<?> get(int i)
	{
		return indivs.get(i);
	}
	
	public List<Individual<?>> get()
	{
		return indivs;
	}
	
	public int size()
	{
		return indivs.size();
	}
	
	public Population<?> join(Population<?> other)
	{
		Population<?> joined = new Population<>(this.size() + other.size());
		joined.indivs.addAll(this.indivs);
		joined.indivs.addAll(other.indivs);
		return joined;
	}
	
	public String toString()
	{
		if(this.size() > 7)
			return String.format("Population: [%s, %s, %s, ... %d more Individuals ..., %s, %s, %s], size: %d",
				indivs.get(0), indivs.get(1), indivs.get(2), size() - 6, indivs.get(size() - 3), indivs.get(size() - 2),
				indivs.get(size() - 1), indivs.size());
		return String.format("Population: %s", indivs);
	}
	
}