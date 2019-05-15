package genetic;

import java.util.function.Function;

/**
 * Implements a mechanism by which an old population of Individuals is replaced
 * with a new population.
 * 
 * @author ratha
 *
 */
@FunctionalInterface
public interface Repopulator<T extends Individual<? super T>> 
	extends Function<Population<? extends T>, Population<T>>
{
}
