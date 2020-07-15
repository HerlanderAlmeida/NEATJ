package genetic.evaluate;

import genetic.Individual;

/**
 * Post-evaluation data for an Individual
 * 
 * @author ratha
 *
 * @param <T>
 *            The type of Individual being evaluated
 * @param <R>
 *            The type of the result of the evaluation
 */
public record Evaluation<T extends Individual, R> (T individual, R result)
{
	public Evaluation
	{
	}
}
