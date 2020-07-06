package genetic;

import genetic.generics.Castable;
import genetic.genome.Genomic;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * Individuals are members of a population.
 */
public abstract class Individual implements Instantiable, Castable<Individual>, Genomic
{
	@Override
	public abstract Individual getInstance();
	
	public abstract Individual copy();
	
	protected final <T extends Individual> T crossoverError()
	{
		throw new IllegalArgumentException("Argument is not an instance of " + getClass().getSimpleName());
	}
}