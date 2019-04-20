package genetic;

import java.util.function.Supplier;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * Individuals are members of a population.
 */
public class Individual<T extends Individual<? extends T>> implements Instantiable<T>
{
	protected Individual()
	{
		//don't instantiate Individuals directly, subclass it!
	}
	
	@Override
	public Supplier<? super Individual<?>> getProvider()
	{
		return Individual::new;
	}
}
