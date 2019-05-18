package genetic;

/**
 * It is possible, but not recommended, to use the types here to confuse people.
 * Individuals are members of a population.
 */
public interface Individual extends Instantiable
{
	@Override
	Individual getInstance();
	
	default <T extends Individual> T as(Class<T> cls)
	{
		if(cls.isInstance(this))
			return cls.cast(this);
		throw new IllegalArgumentException("Params must be of actual given type!");
	}
}
