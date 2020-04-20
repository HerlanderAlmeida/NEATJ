package genetic.evaluate;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;

public class Evaluator<T, R>
{
	private Function<T, R> evaluator = null;
	private Function<Stream<T>, Stream<R>> multiEvaluator = null;
	
	public static final <T, R> Evaluator<T, R> ofMulti(
		Function<Stream<T>, Stream<R>> multipleEvaluator)
	{
		return new Evaluator<T, R>().withMulti(multipleEvaluator);
	}
	
	public static final <T, R> Evaluator<T, R> of(Function<T, R> singleEvaluator)
	{
		return new Evaluator<T, R>().with(singleEvaluator);
	}
	
	private Evaluator()
	{
	}
	
	public final Evaluator<T, R> withMulti(Function<Stream<T>, Stream<R>> multipleEvaluator)
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
				"An evaluator may only set its evaluation method once!");
		return this;
	}
	
	/**
	 * Single evaluators should be able to apply to many Ts without order
	 * dependencies
	 */
	public final R evaluate(T t)
	{
		if(evaluator != null)
			return evaluator.apply(t);
		return evaluate(Stream.of(t)).findFirst().orElseThrow(() -> new NoSuchElementException("No result of Evaluator::evaluate!"));
	}
	
	/**
	 * Multi-evaluators should implement sensible behavior for any 0+ Ts as
	 * input
	 */
	public final Stream<R> evaluate(Stream<T> ts)
	{
		if(multiEvaluator != null)
			return multiEvaluator.apply(ts);
		return ts.map(this::evaluate);
	}
	
	/**
	 * See {@link Evaluator#evaluate(Stream)}
	 */
	@SafeVarargs
	public final Stream<R> evaluate(T... ts)
	{
		return evaluate(ts);
	}
}