package genetic.generics;

public interface Castable<UPPER_BOUND>
{
	@SuppressWarnings( "unchecked" )
	public default <T extends UPPER_BOUND> T cast()
	{
		return (T) self();
	}
	
	
	public default <T> T cast(Class<T> cls)
	{
		var rep = self();
		if(cls.isInstance(rep))
			return cls.cast(rep);
		throw new IllegalArgumentException("Representation can't be cast to "+cls);
	}
	
	public default Object self()
	{
		return this;
	}
}
