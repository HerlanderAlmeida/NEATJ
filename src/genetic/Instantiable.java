package genetic;

import java.util.function.Supplier;

/**
 * Implementers will provide functionality for returning an object 
 * of type {@code T}, freshly generated from a {@code Supplier<? super T>}. The nature
 * of this {@code Supplier} is free to be defined by implementing classes.
 * Instantiables must provide some sort of {@link genetic.Individual Individual} 
 * from their provider.
 * @see {@link java.util.function.Supplier}
 */
public interface Instantiable<T extends Individual<?>>
{
	/**
	 * @return An instance of this Instantiable
	 */
	public default Object getInstance() 
	{
		return this.getProvider().get();
	}
	
	/**
	 * @return A provider that gives instances of this Instantiable
	 */
	abstract Supplier<? super T> getProvider();
}
/*
 * Another potential design of Instantiable. Eschewed in favor of the
 * less type-intensive approach, because it generated less compiler warnings,
 * allowed for clearer code, and allowed equally strong type bounds.
 */
/*
public interface Instantiable<T>
{
	public default <S extends Instantiable<T>> T getInstance()
	{
		return this.<T, S>getProvider().get();
	}
	
	abstract <S, U extends Instantiable<T>> Supplier<S> getProvider();
}
*/
/*
 * One potential design of Instantiable. Eschewed in favor of the 
 * interface approach, which allows for much greater flexibility
 * for users to extend their own classes. 
 */
/*
public class Instantiable<T>
{
	private Instantiable()
	{
		//only subclasses are truly instantiable
	}
	
	public <S extends Instantiable<T>> T getInstance()
	{
		return this.getProvider().get();
	}
	
	protected Supplier<T> getProvider()
	{
		return () -> null;
	}
}
*/