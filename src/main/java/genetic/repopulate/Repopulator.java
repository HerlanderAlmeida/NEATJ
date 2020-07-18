package genetic.repopulate;

import java.util.stream.Collector;

import genetic.Individual;
import genetic.Population;
import genetic.function.CollectableRepeatableFunction;

/**
 * Implements a mechanism by which an old population of Individuals is replaced
 * with a new population.
 * 
 * @author ratha
 *
 */
public interface Repopulator<T extends Individual>
	extends CollectableRepeatableFunction<Population<T>, T, Population<T>>
{
	/**
	 * Some sort of repopulation function that defines the transition between
	 * one population and the next. </br>
	 * Example:
	 * 
	 * <pre>
	 * {@code
	 * 	return collectN(pop, pop.size());
	 * }
	 * </pre>
	 * 
	 * @param pop
	 *            The previous population
	 * @return The next population
	 */
	Population<T> repopulate(Population<T> pop);
	
	/**
	 * Some sort of generation function that produces instances of {@code T}
	 * given some {@code Population<T>}. Implementing this method enables
	 * {@link CollectableRepeatableFunction#collectN(T, long) collectN(T, long)}
	 * 
	 * @param t
	 *            The given {@code Population}
	 * @return The next T
	 */
	@Override
	default T apply(Population<T> t)
	{
		throw new UnsupportedOperationException(
			"Repopulator::apply(Population<T>) was invoked without being implemented!");
	}
	
	/**
	 * Defines the collector used by this function. </br>
	 * Example (beware, not thread-safe by default {@code Population}
	 * implementation):
	 * 
	 * <pre>
	 * {@code
	 * return Collector.of(() -> new Population<T>(0, () -> null),
	 *	(p, i) -> p.add(i),
	 *	(a, b) -> a.join(b),
	 *	new Characteristics[0]);
	 * }
	 * </pre>
	 * 
	 * @return The collector used by this function
	 */
	@Override
	default Collector<? super T, ?, Population<T>> collector()
	{
		throw new UnsupportedOperationException(
			"Repopulator::apply(Population<T>) was invoked without being implemented!");
	}
}
