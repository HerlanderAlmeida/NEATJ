package neat;

import java.util.function.Function;

public interface FitnessMeasure<T extends SpeciesIndividual<R>, R extends Number & Comparable<R>>
	extends Function<Species<T, R>, Double>
{

}
