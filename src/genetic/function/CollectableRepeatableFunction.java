package genetic.function;

import java.util.stream.Collector;

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
	 *            See usage in {@link RepeatableFunction#asStream(t)
	 *            asStream(t)}
	 * @param n
	 *            How many items will be collected
	 * @return An object of type O, generated using the Collector from
	 *         {@link CollectableRepeatableFunction#collector() collector()}
	 */
	default O collectN(T t, long n)
	{
		return asStream(t).limit(n).collect(collector());
	}
}
