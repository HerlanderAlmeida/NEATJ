package genetic;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * A population consists of a fixed size pool of Individuals.
 */
public class Population<T extends Individual<? super T>> implements Supplier<Individual<?>[]>
{
	final Individual<?>[] indivs;
	
	private Population(int size)
	{
		indivs = new Individual[size];
	}
	
	public Population(int n, T instantiable)
	{
		this(n);
		for(int i = 0; i < indivs.length; ++i)
			indivs[i] = (Individual<?>) instantiable.getInstance();
	}
	
	public Population(int n, Supplier<T> supplier)
	{
		this(n);
		for(int i = 0; i < indivs.length; ++i)
			indivs[i] = supplier.get();
	}
	
	public Individual<?>[] get()
	{
		return indivs;
	}
	
	public int size()
	{
		return indivs.length;
	}
	
	public Population<?> join(Population<?> other)
	{
		Population<?> joined = new Population<>(this.size() + other.size());
		for(int i = 0; i < this.size(); ++i)
			joined.indivs[i] = this.indivs[i];
		for(int i = 0; i < other.size(); ++i)
			joined.indivs[i + this.size()] = other.indivs[i];
		return joined;
	}
	
	public String toString()
	{
		if(this.size() > 7)
			return String.format("Population:[%s, %s, %s, ... %d more Individuals ..., %s, %s, %s]",
				indivs[0], indivs[1], indivs[2], size() - 6, indivs[size() - 3], indivs[size() - 2],
				indivs[size() - 1]);
		return String.format("Population:%s", Arrays.toString(indivs));
	}
	
}