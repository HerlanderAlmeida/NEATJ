package genetic.evaluate;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Evaluator<T, R>
{
	private Function<T, R> evaluator = null;
	private Function<Iterable<T>, Iterable<R>> multiEvaluator = null;
	
	public static final <T, R> Evaluator<T, R> ofMulti(
		Function<Iterable<T>, Iterable<R>> multipleEvaluator)
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
	
	public final Evaluator<T, R> withMulti(Function<Iterable<T>, Iterable<R>> multipleEvaluator)
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
		return evaluate(List.of(t)).iterator().next();
	}
	
	/**
	 * Multi-evaluators should implement sensible behavior for any 0+ Ts as
	 * input
	 */
	public final Iterable<R> evaluate(Iterable<T> ts)
	{
		if(multiEvaluator != null)
			return multiEvaluator.apply(ts);
		return StreamSupport.stream(ts.spliterator(), true).map(this::evaluate)
			.collect(Collectors.toList());
	}
	
	/**
	 * See {@link Evaluator#evaluate(Iterable)}
	 */
	@SafeVarargs
	public final Iterable<R> evaluate(T... ts)
	{
		return evaluate(List.of(ts));
	}
}