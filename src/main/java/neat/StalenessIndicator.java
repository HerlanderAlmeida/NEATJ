package neat;

import java.util.OptionalDouble;
import java.util.function.Function;

public interface StalenessIndicator<T extends SpeciesIndividual<R>, R extends Number & Comparable<R>>
	extends Function<Species<T, R>, OptionalDouble>
{
}
