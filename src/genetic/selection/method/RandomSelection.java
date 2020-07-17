package genetic.selection.method;

import java.util.List;

import genetic.Individual;
import genetic.evaluate.Evaluation;

public class RandomSelection<T extends Individual> extends SelectionMethod<T>
{
	public RandomSelection()
	{
		super();
	}
	
	public RandomSelection(int iterations)
	{
		super(iterations);
	}
	
	@Override
	protected <R extends Number & Comparable<R>> T selectIndividual(List<Evaluation<T, R>> ranked)
	{
		return ranked.get(random.nextInt(ranked.size())).individual();
	}
}
