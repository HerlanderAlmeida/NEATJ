package genetic.evaluate;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;

import genetic.Individual;

/**
 * Evaluator for Individuals
 * 
 * @author ratha
 *
 * @param <T>
 *            The type of individual
 * @param <R>
 *            The result of an evaluation
 */
public class Evaluator<T extends Individual, R>
{
	/**
	 * Evaluator for one Individual at a time
	 */
	private Function<T, R> evaluator = null;
	/**
	 * Evaluator for multiple Individuals at a time
	 */
	private Function<Stream<T>, Stream<Evaluation<T, R>>> multiEvaluator = null;
	
	public static final <T extends Individual, R> Evaluator<T, R> ofMulti(
		Function<Stream<T>, Stream<Evaluation<T, R>>> multipleEvaluator)
	{
		return new Evaluator<T, R>().withMulti(multipleEvaluator);
	}
	
	public static final <T extends Individual, R> Evaluator<T, R> of(Function<T, R> singleEvaluator)
	{
		return new Evaluator<T, R>().with(singleEvaluator);
	}
	
	private Evaluator()
	{
	}
	
	public final Evaluator<T, R> withMulti(
		Function<Stream<T>, Stream<Evaluation<T, R>>> multipleEvaluator)
	{
		if(this.multiEvaluator == null)
			this.multiEvaluator = multipleEvaluator;
		else
			throw new IllegalStateException(
				"An evaluator may only set its multiple evaluation method once!");
		return this;
	}
	
	public final Evaluator<T, R> with(Function<T, R> singleEvaluator)
	{
		if(this.evaluator == null)
			this.evaluator = singleEvaluator;
		else
			throw new IllegalStateException(
				"An evaluator may only set its single evaluation method once!");
		return this;
	}
	
	/**
	 * {@link Evaluator#evaluator Single evaluators} should be able to apply to
	 * many {@link Evaluator#T Ts} without order dependencies
	 */
	public final Evaluation<T, R> evaluate(T t)
	{
		if(evaluator != null)
			return new Evaluation<>(t, evaluator.apply(t));
		return evaluate(Stream.of(t)).findFirst()
			.orElseThrow(() -> new NoSuchElementException("No result of Evaluator::evaluate!"));
	}
	
	/**
	 * {@link Evaluator#multiEvaluator Multi-evaluators} should implement
	 * sensible behavior for any 0+ {@link Evaluator#T Ts} as input
	 */
	public final Stream<Evaluation<T, R>> evaluate(Stream<T> ts)
	{
		if(multiEvaluator != null)
			return multiEvaluator.apply(ts);
		return ts.map(this::evaluate);
	}
	
	/**
	 * Multi-evaluation on 0+ {@link Evaluator#T Ts} as an array. See
	 * {@link Evaluator#evaluate(Stream)}
	 */
	@SafeVarargs
	public final Stream<Evaluation<T, R>> evaluate(T... ts)
	{
		return evaluate(Stream.of(ts));
	}
}