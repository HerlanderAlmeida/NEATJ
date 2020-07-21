package genetic.crossover;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import genetic.Individual;
import genetic.evaluate.Evaluation;
import genetic.selection.method.SelectionMethod;

public class CrossoverSelection<T extends Individual> extends SelectionMethod<T>
{
	protected SelectionMethod<T> firstSelector;
	protected SelectionMethod<T> secondSelector;
	protected BinaryOperator<T> crossover;
	protected double crossoverProbability = 1;
	protected UnaryOperator<T> crossedMutation = t -> t;
	protected UnaryOperator<T> uncrossedMutation = t -> t;

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

	public CrossoverSelection<T> withCrossedMutation(UnaryOperator<T> crossedMutation)
	{
		this.crossedMutation = crossedMutation;
		return this;
	}

	public CrossoverSelection<T> withUncrossedMutation(UnaryOperator<T> uncrossedMutation)
	{
		this.uncrossedMutation = uncrossedMutation;
		return this;
	}

	@Override
	public void reset()
	{
		super.reset();
		this.firstSelector.reset();
		this.secondSelector.reset();
	}

	@Override
	public <R extends Number & Comparable<R>> T selectIndividual(List<Evaluation<T, R>> ranked)
	{
		var first = this.firstSelector.select(ranked);
		var second = this.secondSelector.select(ranked);
		if(random.nextDouble() < this.crossoverProbability && ranked.size() > 1)
		{
			return this.crossedMutation.apply(this.crossover.apply(first, second).copy().cast());
		}
		else
		{
			return this.uncrossedMutation
				.apply((random.nextBoolean() ? first : second).copy().cast());
		}
	}
}
