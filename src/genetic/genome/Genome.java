package genetic.genome;

import genetic.generics.Castable;

public interface Genome extends Castable<Genome>
{
	public Genome copy();
}
