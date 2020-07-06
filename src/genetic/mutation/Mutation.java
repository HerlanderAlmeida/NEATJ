package genetic.mutation;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;

import genetic.Individual;

public class Mutation<T extends Individual>
{
	protected static final Random random = ThreadLocalRandom.current();
	private double probability;
	private UnaryOperator<T> mutation;
	
	public Mutation(double probability, UnaryOperator<T> mutation)
	{
		this.withProbability(probability).withMutation(mutation);
	}
	
	public Mutation(UnaryOperator<T> mutation)
	{
		this.withProbability(1).withMutation(mutation);
	}
	
	public T apply(T t)
	{
		if(random.nextDouble() < probability)
			return mutation.apply(t);
		else
			return t;
	}
	
	public Mutation<T> withProbability(double probability)
	{
		if(probability < 0 || probability > 1)
			throw new IllegalArgumentException("Probability must be within range [0, 1]!");
		this.probability = probability;
		return this;
	}
	
	public Mutation<T> withMutation(UnaryOperator<T> mutation)
	{
		this.mutation = mutation;
		return this;
	}
}
