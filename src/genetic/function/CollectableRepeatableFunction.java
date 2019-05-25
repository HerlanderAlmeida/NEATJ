package genetic.function;

import java.util.stream.Collector;

/**
 * A function that is repeatedly invoked on some common argument and collected at some point.
 * @author ratha
 *
 * @param <T> The input type of {@link RepeatableFunction#apply(Object) apply(T)}
 * @param <R> The output type of {@link RepeatableFunction#apply(Object) apply(T)}
 * @param <O> The output type after the collection operation is complete
 */
public interface CollectableRepeatableFunction<T, R, O> extends RepeatableFunction<T, R>
{
	/**
	 * @return The collector used by this function
	 */
	Collector<? super R, ?, O> collector();
	
	/**
	 * Collects n items from the stream
	 * 
	 * @param t
	 *            See usage in {@link RepeatableFunction#asStream(T)
	 *            asStream(t)}
	 * @param n
	 *            How many items will be collected
	 * @param parallel
	 *            Whether the stream to be collected from is parallel
	 * @return An object of type O, generated using the Collector from
	 *         {@link CollectableRepeatableFunction#collector() collector()}
	 */
	default O collectN(T t, long n, boolean parallel)
	{
		return ( parallel ? asParallelStream(t) : asStream(t) ).limit(n).collect(collector());
	}
	
	/**
	 * Collects n items from the stream. Assume the default implementation is
	 * equivalent to
	 * {@link CollectableRepeatableFunction#collectN(T, long, boolean)
	 * collectN(t, n, false)}
	 * 
	 * @param t
	 *            See usage in {@link RepeatableFunction#asStream(t)
	 *            asStream(t)}
	 * @param n
	 *            How many items will be collected
	 * @return An object of type O, generated using the Collector from
	 *         {@link CollectableRepeatableFunction#collector() collector()}
	 */
	default O collectN(T t, long n)
	{
		return collectN(t, n, false);
	}
}
