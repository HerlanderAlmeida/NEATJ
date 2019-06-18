package genetic;

import genetic.generics.Castable;
import genetic.genome.Genomic;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * Individuals are members of a population.
 */
public interface Individual extends Instantiable, Castable<Individual>, Genomic
{
	@Override
	Individual getInstance();
	
	Individual copy();
}
