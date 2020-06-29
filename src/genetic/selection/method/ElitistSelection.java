package genetic.selection.method;

import java.util.List;

import genetic.Individual;
import genetic.evaluate.Evaluation;

public class ElitistSelection<T extends Individual> extends SelectionMethod<T>
{
	private int currentIndex;
	
	public ElitistSelection(int iterations)
	{
		this.withIndex(0).withIterations(iterations);
	}
	
	public ElitistSelection()
	{
		this.checker = () -> false;
	}
	
	private ElitistSelection<T> withIndex(int index)
	{
		this.currentIndex = index;
		return this;
	}
	
	@Override
	public <R extends Number & Comparable<R>> T select(List<Evaluation<T, R>> ranked)
	{
		var selected = ranked.get(currentIndex);
		currentIndex = (currentIndex + 1) % ranked.size();
		return selected.individual();
	}
}
