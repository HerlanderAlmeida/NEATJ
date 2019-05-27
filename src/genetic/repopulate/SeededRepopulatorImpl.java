package genetic.repopulate;

import java.util.function.Function;

import genetic.Individual;

public class SeededRepopulatorImpl<T extends Individual> extends RepopulatorImpl<T>
{
	public SeededRepopulatorImpl(Function<T, T> copyFunc, long seed)
	{
		super(copyFunc);
		this.random.setSeed(seed);
	}
	
	public void reseed(long seed)
	{
		this.random.setSeed(seed);
	}
}
