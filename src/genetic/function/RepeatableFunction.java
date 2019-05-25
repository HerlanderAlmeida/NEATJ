package genetic.function;

import java.util.stream.Stream;

/**
 * A function that is repeatedly invoked on some common argument to produce a stream of outputs.
 * @author ratha
 *
 * @param <T> The input type of {@link RepeatableFunction#apply(Object) apply(T)}
 * @param <R> The output type of {@link RepeatableFunction#apply(Object) apply(T)}
 */
public interface RepeatableFunction<T, R>
{
	/**
	 * @param t
	 *            An input object (typed T)
	 * @return An output object (typed R)
	 */
	R apply(T t);
	
	/**
	 * @param t
	 *            An input object (typed T)
	 * @return A sequential, unordered stream of R as a result of applying
	 *         {@link RepeatableFunction#apply apply(t)} repeatedly to argument
	 *         {@code t}.
	 */
	default Stream<R> asStream(T t)
	{
		return Stream.generate(() -> apply(t)).sequential().unordered();
	}
	
	/**
	 * @param t
	 *            An input object (typed T)
	 * @return A parallel, unordered stream of R as a result of applying
	 *         {@link RepeatableFunction#apply apply(t)} repeatedly to argument
	 *         {@code t}.
	 */
	default Stream<R> asParallelStream(T t)
	{
		return Stream.generate(() -> apply(t)).parallel().unordered();
	}
}
