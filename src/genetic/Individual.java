package genetic;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * Individuals are members of a population.
 */
public interface Individual extends Instantiable
{
	@Override
	public abstract Individual getInstance();
}
