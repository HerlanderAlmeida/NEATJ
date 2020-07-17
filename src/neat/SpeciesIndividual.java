package neat;

import genetic.Individual;

public abstract class SpeciesIndividual<R extends Number & Comparable<R>> extends Individual
{
	public abstract R difference(SpeciesIndividual<R> other, SpeciationParameters parameters);
	public abstract R fitness();
	public abstract void fitness(R fitness);
	public abstract void divideFitness(double sharers);
}
