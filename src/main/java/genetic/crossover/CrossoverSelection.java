package genetic.crossover;

import java.util.List;
import java.util.function.BinaryOperator;

import genetic.Individual;
import genetic.evaluate.Evaluation;
import genetic.selection.method.SelectionMethod;

public class CrossoverSelection<T extends Individual> extends SelectionMethod<T>
{
	protected SelectionMethod<T> firstSelector;
	protected SelectionMethod<T> secondSelector;
	protected BinaryOperator<T> crossover;
	protected double crossoverProbability = 1;
	
	public CrossoverSelection()
	{
	}
	
	public CrossoverSelection(int iterations)
	{
		super(iterations);
	}
	
	public CrossoverSelection<T> withSelectionMethod(SelectionMethod<T> method)
	{
		return this.withFirstSelector(method).withSecondSelector(method);
	}
	
	public CrossoverSelection<T> withFirstSelector(SelectionMethod<T> method)
	{
		this.firstSelector = method;
		return this;
	}

	public CrossoverSelection<T> withSecondSelector(SelectionMethod<T> method)
	{
		this.secondSelector = method;
		return this;
	}
	
	public CrossoverSelection<T> withCrossoverMethod(CrossoverMethod<T> crossover)
	{
		this.crossover = crossover;
		return this;
	}
	
	public CrossoverSelection<T> withCrossoverProbability(double crossoverProbability)
	{
		this.crossoverProbability = crossoverProbability;
		return this;
	}
	
	@Override
	public void reset()
	{
		super.reset();
		firstSelector.reset();
		secondSelector.reset();
	}
	
	@Override
	public <R extends Number & Comparable<R>> T selectIndividual(List<Evaluation<T, R>> ranked)
	{
		var first = firstSelector.select(ranked);
		var second = secondSelector.select(ranked);
		if(random.nextDouble() < crossoverProbability)
		{
			return this.crossover.apply(first, second);
		}
		else
		{
			return random.nextBoolean() ? first : second;
		}
	}
}
