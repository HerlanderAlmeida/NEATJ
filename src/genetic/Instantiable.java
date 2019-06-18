package genetic;

/**
 * Implementers will provide functionality for returning an object of type
 * {@code Instantiable}. The nature of this functionality is free
 * to be defined by implementing classes.
 * 
 * @see {@link java.util.function.Supplier}
 */
public interface Instantiable
{
	/**
	 * @return An instance of this Instantiable type
	 */
	Instantiable getInstance();
}
/*
 * Another potential design of Instantiable. Eschewed in favor of the less
 * type-intensive approach, because it generated less compiler warnings, allowed
 * for clearer code, and allowed equally strong type bounds.
 */
/*
 * public interface Instantiable<T> { public default <S extends Instantiable<T>>
 * T getInstance() { return this.<T, S>getProvider().get(); }
 * 
 * abstract <S, U extends Instantiable<T>> Supplier<S> getProvider(); }
 */
/*
 * One potential design of Instantiable. Eschewed in favor of the interface
 * approach, which allows for much greater flexibility for users to extend their
 * own classes.
 */
/*
 * public class Instantiable<T> { private Instantiable() { //only subclasses are
 * truly instantiable }
 * 
 * public <S extends Instantiable<T>> T getInstance() { return
 * this.getProvider().get(); }
 * 
 * protected Supplier<T> getProvider() { return () -> null; } }
 */