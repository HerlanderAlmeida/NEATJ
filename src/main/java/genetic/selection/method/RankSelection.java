package genetic.selection.method;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import genetic.Individual;
import genetic.evaluate.Evaluation;

public class RankSelection<T extends Individual> extends RouletteSelection<T>
{
	public RankSelection()
	{
		super();
	}

	public RankSelection(int iterations)
	{
		super(iterations);
	}

	@Override
	public <R extends Number & Comparable<R>> T selectIndividual(List<Evaluation<T, R>> ranked)
	{
		var sorted = new ArrayList<>(ranked); // we don't want to alter our inputs
		sorted.sort(Comparator.comparing(Evaluation::result));
		var altered = new ArrayList<Evaluation<T, Double>>();
		double rank = 0;
		for(var eval : sorted)
		{
			altered.add(new Evaluation<>(eval.individual(), ++rank));
		}
		return super.selectIndividual(altered);
	}
}
