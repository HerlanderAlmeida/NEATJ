package genetic.selection.method;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import genetic.Individual;
import genetic.evaluate.Evaluation;

public abstract class SelectionMethod<T extends Individual>
{
	protected static final Random random = ThreadLocalRandom.current();
	private int iterations;
	private int currentIteration;
	protected Supplier<Boolean> checker;
	
	public SelectionMethod()
	{
		this.withChecker(() -> false);
	}
	
	public SelectionMethod(int iterations)
	{
		this.withIterations(iterations).withChecker(() -> this.iterations <= this.currentIteration);
	}
	
	private SelectionMethod<T> withIterations(int iterations)
	{
		this.iterations = iterations;
		return this;
	}
	
	protected SelectionMethod<T> withChecker(Supplier<Boolean> checker)
	{
		this.checker = checker;
		return this;
	}
	
	protected void iterate()
	{
		if(finished())
			throw new IllegalStateException("Maximum iterations for selection method reached!");
		this.currentIteration++;
	}
	
	public void reset()
	{
		this.currentIteration = 0;
	}
	
	public boolean finished()
	{
		return checker.get();
	}
	
	protected abstract <R extends Number & Comparable<R>> T selectIndividual(List<Evaluation<T, R>> ranked);

	public final <R extends Number & Comparable<R>> T select(List<Evaluation<T, R>> ranked)
	{
		iterate();
		return selectIndividual(ranked);
	}
}
