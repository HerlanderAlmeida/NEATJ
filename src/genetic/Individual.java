package genetic;

import genetic.generics.Castable;
import genetic.genome.Genomic;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * Individuals are members of a population.
 */
public abstract class Individual implements Castable<Individual>, Genomic
{
	public abstract Individual copy();
}