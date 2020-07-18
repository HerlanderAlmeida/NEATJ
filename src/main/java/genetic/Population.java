package genetic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
	
	public String toString()
	{
		if(this.size() > 7)
			return String.format(
				"Population: [%s, %s, %s, ... %d more Individuals ..., %s, %s, %s], size: %d",
				this.get(0), this.get(1), this.get(2), size() - 6, this.get(size() - 3),
				this.get(size() - 2), this.get(size() - 1), this.size());
		return String.format("Population: %s, size: %d", super.toString(), this.size());
	}
	
}