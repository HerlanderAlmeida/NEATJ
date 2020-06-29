package genetic.selection.method;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import genetic.Individual;
import genetic.evaluate.Evaluation;

public class RankSelection<T extends Individual> extends RouletteSelection<T>
{
	public RankSelection(int iterations)
	{
		this.withIterations(iterations);
	}
	
	public RankSelection()
	{
		this.checker = () -> false;
	}
	
	@Override
	public <R extends Number & Comparable<R>> T select(List<Evaluation<T, R>> ranked)
	{
		ranked.sort(Comparator.comparing(Evaluation::result));
		var altered = new ArrayList<Evaluation<T, Double>>();
		double rank = 0;
		for(var eval : ranked)
		{
			altered.add(new Evaluation<>(eval.individual(), ++rank));
		}
		return super.select(altered);
	}
}
