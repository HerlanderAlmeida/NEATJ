package genetic;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * Individuals are members of a population.
 */
public abstract class Individual implements Instantiable
{
	@Override
	public abstract Individual getInstance();
}
