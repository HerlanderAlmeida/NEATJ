package genetic.selection;

import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import genetic.Individual;
import genetic.evaluate.Evaluation;
import genetic.evaluate.Evaluator;

/**
 * A ranker for Individuals based on the result of an evaluation
 * 
 * @author ratha
 *
 * @param <T>
 * @param <R>
 */
public final class Ranker<T extends Individual, R>
{
	private Comparator<Evaluation<T, R>> order;
	
	public static <T extends Individual, R> Ranker<T, R> rankingBy(
		Comparator<Evaluation<T, R>> ranking)
	{
		return new Ranker<T, R>().withRanking(ranking);
	}
	
	private Ranker()
	{
	}
	
	/**
	 * @return A Ranker with the given ranking order
	 */
	public Ranker<T, R> withRanking(Comparator<Evaluation<T, R>> ranking)
	{
		if(this.order == null)
			this.order = ranking;
		else
			throw new IllegalStateException("A ranker may only set its order once!");
		return this;
	}
	
	/**
	 * Ranks evaluations
	 * 
	 * @param toRank
	 *            A {@link Stream} of evaluations to be ranked
	 * @return The {@link Stream} resulting from a ranking operation on multiple
	 *         {@link Evaluation Evaluations}
	 */
	public Stream<Evaluation<T, R>> rank(Stream<Evaluation<T, R>> toRank)
	{
		return toRank.sorted(order);
	}
	
	/**
	 * Ranks evaluations
	 * 
	 * @param toRank
	 *            An {@link Iterable} for evaluations to be ranked
	 * @return The {@link Stream} resulting from a ranking operation on multiple
	 *         {@link Evaluation Evaluations}
	 */
	public Stream<Evaluation<T, R>> rank(Iterable<Evaluation<T, R>> toRank)
	{
		return StreamSupport.stream(toRank.spliterator(), false).sorted(order);
	}
	
	/**
	 * Multi-evaluation on 0+ {@link Evaluator#T Ts} as an array, followed by a
	 * ranking operation. See {@link Evaluator#evaluate(T[])
	 * Evaluator.evaluate(T...)} and {@link Ranker#rank(Stream)}
	 * 
	 * @return A ranked {@link Stream} of evaluations
	 */
	@SafeVarargs
	public final Stream<Evaluation<T, R>> rank(Evaluator<T, R> evaluator, T... ts)
	{
		return rank(evaluator.evaluate(ts));
	}
	
	/**
	 * Multi-evaluation on 0+ {@link Evaluator#T Ts} as a {@link Stream},
	 * followed by a ranking operation. See {@link Evaluator#evaluate(Stream)}
	 * and {@link Ranker#rank(Stream)}
	 * 
	 * @return A ranked {@link Stream} of evaluations
	 */
	public final Stream<Evaluation<T, R>> rank(Evaluator<T, R> evaluator, Stream<T> ts)
	{
		return rank(evaluator.evaluate(ts));
	}
}
