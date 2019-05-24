package genetic.function;

import java.util.stream.Stream;

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
	 * @return A parallel, unordered stream of R as a result of applying
	 *         {@link RepeatableFunction#apply apply(t)} repeatedly to argument
	 *         {@code t}.
	 */
	default Stream<R> asStream(T t)
	{
		return Stream.generate(() -> apply(t)).sequential().unordered();
	}
}
