package neat;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import genetic.Population;
import genetic.evaluate.Evaluation;
import utils.Exclude;

public class Species<T extends SpeciesIndividual<R>, R extends Number & Comparable<R>>
{
	public static final Random random = ThreadLocalRandom.current();
	private Population<T> population;
	private T representative;
	private double maxFitness;
	@Exclude
	private FitnessMeasure<T, R> fitnessMeasure;
	private double fitness;
	private int capacity;
	private int staleness;

	public Species(FitnessMeasure<T, R> fitnessMeasure)
	{
		this.revive();
		this.fitnessMeasure(fitnessMeasure);
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
		if(this.population.size() > 0)
		{
			this.representative = this.population.get(random.nextInt(this.population.size()));
		}
	}

	public void adjustFitness()
	{
		this.fitness = this.fitnessMeasure.apply(this);
		this.population.forEach(indiv -> indiv.divideFitness(this.population.size()));
	}

	public void age(OptionalDouble currentFitness)
	{
		if(currentFitness.isPresent())
		{
			var fitness = currentFitness.getAsDouble();
			if(fitness > this.maxFitness())
			{
				this.maxFitness(fitness);
				this.staleness(0);
				return;
			}
		}
		this.staleness(this.staleness() + 1);
	}

	public void revive()
	{
		this.maxFitness(Double.MIN_VALUE);
		this.staleness(0);
	}

	/**
	 * Assumption: species populations are sorted highest to lowest
	 */
	public void eliminate(double proportion)
	{
		var eliminating = (int) (proportion * this.population.size());
		this.population.subList(this.population.size() - eliminating, this.population.size())
			.clear();
	}

	public List<Evaluation<T, R>> fitnessList()
	{
		return this.population.stream().map(indiv -> new Evaluation<>(indiv, indiv.fitness()))
			.collect(Collectors.toUnmodifiableList());
	}

	public int staleness()
	{
		return this.staleness;
	}

	public void staleness(int staleness)
	{
		this.staleness = staleness;
	}

	public double maxFitness()
	{
		return this.maxFitness;
	}

	public void maxFitness(double maxFitness)
	{
		this.maxFitness = maxFitness;
	}

	public double fitness()
	{
		return this.fitness;
	}

	public void fitness(double averageFitness)
	{
		this.fitness = averageFitness;
	}

	public FitnessMeasure<T, R> fitnessMeasure()
	{
		return this.fitnessMeasure;
	}

	public void fitnessMeasure(FitnessMeasure<T, R> fitnessMeasure)
	{
		this.fitnessMeasure = fitnessMeasure;
	}

	public boolean perished()
	{
		return this.population.isEmpty();
	}

	public void perish()
	{
		this.population.clear();
	}

	public Stream<T> stream()
	{
		return this.population.stream();
	}

	public Stream<T> parallelStream()
	{
		return this.population.parallelStream();
	}

	public DoubleStream fitnesses()
	{
		return this.population.stream().map(SpeciesIndividual::fitness)
			.mapToDouble(Number::doubleValue);
	}
}
