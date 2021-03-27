package genetic.selection.method;

import java.util.List;

import genetic.Individual;
import genetic.evaluate.Evaluation;

public class ElitistSelection<T extends Individual> extends SelectionMethod<T>
{
	private int currentIndex;

	public ElitistSelection()
	{
		this.withIndex(0);
	}

	public ElitistSelection(int iterations)
	{
		super(iterations);
		this.withIndex(0);
	}

	private ElitistSelection<T> withIndex(int index)
	{
		this.currentIndex = index;
		return this;
	}

	@Override
	public void reset()
	{
		super.reset();
		this.withIndex(0);
	}

	/**
	 * Inputs ranked from highest fitness to lowest fitness
	 */
	@Override
	public <R extends Number & Comparable<R>> T selectIndividual(List<Evaluation<T, R>> ranked)
	{
		var selected = ranked.get(this.currentIndex);
		this.currentIndex = (this.currentIndex + 1) % ranked.size();
		return selected.individual();
	}
}
