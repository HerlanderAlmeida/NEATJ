package neat;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import genetic.Population;
import genetic.evaluate.Evaluation;

public class Species<T extends SpeciesIndividual<R>, R extends Number & Comparable<R>>
{
	public static final Random random = ThreadLocalRandom.current();
	private Population<T> population;
	private T representative;
	private double maxFitness;
	private double averageFitness;
	private int capacity;
	private int staleness;
	
	public Species()
	{
		this.maxFitness = Double.MIN_VALUE;
	}
	
	public Population<T> population()
	{
		return this.population;
	}
	
	public void population(Population<T> population)
	{
		this.population = population;
	}
	
	public int capacity()
	{
		return this.capacity;
	}
	
	public void capacity(int capacity)
	{
		this.capacity = capacity;
	}
	
	public T representative()
	{
		return this.representative;
	}
	
	public void representative(T representative)
	{
		this.representative = representative;
	}
	
	public void updateRepresentative()
	{
		if(population.size() > 0)
		{
			representative = population.get(random.nextInt(population.size()));
		}
	}
	
	public void adjustFitness()
	{
		averageFitness = population.stream().map(SpeciesIndividual::fitness)
			.mapToDouble(Number::doubleValue).sum();
		population.forEach(indiv -> indiv.divideFitness(population.size()));
		averageFitness = averageFitness / population.size();
	}
	
	public void age()
	{
		var maxIndividual = population.stream()
			.max(Comparator.comparing(SpeciesIndividual::fitness));
		if(maxIndividual.isPresent() && maxIndividual.get().fitness().doubleValue() > maxFitness)
		{
			maxFitness = maxIndividual.get().fitness().doubleValue();
			staleness(0);
		}
		else
		{
			staleness(staleness() + 1);
		}
	}
	
	public void revive()
	{
		staleness(0);
	}
	
	/**
	 * Assumption: species populations are sorted highest to lowest
	 */
	public void eliminate(double proportion)
	{
		var eliminating = (int) (proportion * population.size());
		population.subList(population.size() - eliminating, population.size()).clear();
	}
	
	public List<Evaluation<T, R>> fitnessList()
	{
		return population.stream().map(indiv -> new Evaluation<>(indiv, indiv.fitness()))
			.collect(Collectors.toUnmodifiableList());
	}
	
	public int staleness()
	{
		return staleness;
	}
	
	public void staleness(int staleness)
	{
		this.staleness = staleness;
	}
	
	public double averageFitness()
	{
		return averageFitness;
	}
	
	public void averageFitness(double averageFitness)
	{
		this.averageFitness = averageFitness;
	}
	
	public boolean perished()
	{
		return population.isEmpty();
	}
	
	public void perish()
	{
		population.clear();
	}
	
	public Stream<T> stream()
	{
		return population.stream();
	}
}
