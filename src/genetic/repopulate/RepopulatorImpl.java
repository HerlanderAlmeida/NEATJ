package genetic.repopulate;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collector;

import genetic.Individual;
import genetic.Population;

public abstract class RepopulatorImpl<T extends Individual> implements Repopulator<T>
{
	private Random random;
	private Function<T, T> copier;
	
	public RepopulatorImpl(Function<T, T> copyFunc)
	{
		random = new Random();
		copier = copyFunc;
	}
	
	@Override
	public Population<T> repopulate(Population<T> pop)
	{
		return collectN(pop, pop.size());
	}
	
	@Override
	public T apply(Population<T> t)
	{
		return copier.apply(t.get(random.nextInt((t.size()))));
	}
	
	@Override
	public Collector<? super T, ?, Population<T>> collector()
	{
		return Collector.of(() -> new Population<T>(0, () -> null),
			(p, i) -> p.add(i),
			(a, b) -> a.join(b),
			new Collector.Characteristics[0]);
	}
}