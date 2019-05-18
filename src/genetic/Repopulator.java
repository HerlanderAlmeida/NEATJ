package genetic;

import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

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
	@Override
	T apply(Population<T> t);
	
	@Override
	default Collector<? super T, ?, Population<T>> collector()
	{
		return Collector.<T,Population<T>>of(() -> new Population<T>(0, () -> null),
			(p, i) -> p.add(i),
			(a, b) -> a.join(b),
			new Characteristics[0]);
	}
}
