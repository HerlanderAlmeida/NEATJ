package genetic.selection.method;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import genetic.Individual;
import genetic.evaluate.Evaluation;

public abstract class SelectionMethod<T extends Individual>
{
	protected static final Random random = ThreadLocalRandom.current();
	private int iterations;
	private int currentIteration;
	protected Supplier<Boolean> checker;
	private UnaryOperator<T> postMutations = (T t) -> t;
	
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
	
	public SelectionMethod<T> withPostMutations(UnaryOperator<T> postMutations)
	{
		this.postMutations = postMutations;
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
	
	/**
	 * @param <R> Result of evaluations
	 * @param ranked A list of evaluated individuals, sorted in descending order of evaluation
	 * @return A single individual, selected according to this selection method
	 */
	protected abstract <R extends Number & Comparable<R>> T selectIndividual(List<Evaluation<T, R>> ranked);

	public final <R extends Number & Comparable<R>> T select(List<Evaluation<T, R>> ranked)
	{
		iterate();
		return postMutations.apply(selectIndividual(ranked).copy().cast());
	}
}
