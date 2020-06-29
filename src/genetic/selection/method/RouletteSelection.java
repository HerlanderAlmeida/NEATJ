package genetic.selection.method;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import genetic.Individual;
import genetic.evaluate.Evaluation;

public class RouletteSelection<T extends Individual> extends SelectionMethod<T>
{
	public RouletteSelection(int iterations)
	{
		this.withIterations(iterations);
	}
	
	public RouletteSelection()
	{
		this.checker = () -> false;
	}
	
	@Override
	public <R extends Number & Comparable<R>> T select(List<Evaluation<T, R>> ranked)
	{
		var converted = ranked.stream().map(eval -> new Evaluation<>(eval.individual(), eval.result().doubleValue())).collect(Collectors.toList());
		var least = converted.stream().min(Comparator.comparing(Evaluation::result)).get().result();
		if(least < 0)
			converted = converted.stream().map(eval -> new Evaluation<>(eval.individual(), eval.result() - least + 1.0)).collect(Collectors.toList());
		else if(least == 0)
			converted = converted.stream().map(eval -> new Evaluation<>(eval.individual(), eval.result() + 1.0)).collect(Collectors.toList());
		var sum = converted.stream().collect(Collectors.summingDouble(Evaluation::result));
		var differentiator = random.nextDouble(0, sum);
		for(var eval : converted)
		{
			differentiator -= eval.result();
			if(differentiator < 0)
				return eval.individual();
		}
		return converted.get(converted.size() - 1).individual();
	}
}
