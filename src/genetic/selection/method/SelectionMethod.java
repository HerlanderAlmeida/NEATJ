package genetic.selection.method;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import genetic.Individual;
import genetic.evaluate.Evaluation;

public abstract class SelectionMethod<T extends Individual>
{
	protected static final ThreadLocalRandom random = ThreadLocalRandom.current();
	private int iterations;
	protected Supplier<Boolean> checker = () -> iterations-- <= 0;
	
	public SelectionMethod<T> withIterations(int iterations)
	{
		this.iterations = iterations;
		return this;
	}
	
	public boolean finished()
	{
		return checker.get();
	}

	public <R extends Number & Comparable<R>> T select(List<Evaluation<T, R>> ranked)
	{
		throw new UnsupportedOperationException("The select method has not been implemented for "+getClass().getSimpleName()+" yet!");
	}
}
