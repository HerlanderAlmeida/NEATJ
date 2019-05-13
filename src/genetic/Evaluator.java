package genetic;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Evaluator<T, R>
{
	private Function<T, R> evaluator = null;
	private Function<Iterable<T>, Iterable<R>> multiEvaluator = null;
	
	public Evaluator()
	{
	}
	
	public final Evaluator<T, R> asMulti(Function<Iterable<T>, Iterable<R>> multipleEvaluator)
	{
		if(this.multiEvaluator == null)
			this.multiEvaluator = multipleEvaluator;
		else
			throw new IllegalStateException(
				"An evaluator may only set its multiple evaluation method once!");
		return this;
	}
	
	public final Evaluator<T, R> as(Function<T, R> singleEvaluator)
	{
		if(this.evaluator == null)
			this.evaluator = singleEvaluator;
		else
			throw new IllegalStateException(
				"An evaluator may only set its evaluation method once!");
		return this;
	}
	
	public final R evaluate(T t)
	{
		if(evaluator != null)
			return evaluator.apply(t);
		return evaluate(List.of(t)).iterator().next();
	}
	
	public final Iterable<R> evaluate(Iterable<T> ts)
	{
		if(multiEvaluator != null)
			return multiEvaluator.apply(ts);
		return StreamSupport.stream(ts.spliterator(), true).map(this::evaluate)
			.collect(Collectors.toList());
	}
	
	@SafeVarargs
	public final Iterable<R> evaluate(T... ts)
	{
		return evaluate(List.of(ts));
	}
}