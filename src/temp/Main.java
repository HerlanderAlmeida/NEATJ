package temp;

import java.util.Comparator;
import java.util.stream.Collectors;

import genetic.Population;
import genetic.crossover.CrossoverMethod;
import genetic.crossover.CrossoverSelection;
import genetic.evaluate.Evaluation;
import genetic.evaluate.Evaluator;
import genetic.mutation.Mutation;
import genetic.mutation.Mutations;
import genetic.repopulate.RepopulatorImpl;
import genetic.selection.Ranker;
import genetic.selection.Selector;
import genetic.selection.method.ElitistSelection;
import genetic.selection.method.RankSelection;
import genetic.selection.method.RouletteSelection;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		// initialize population
		var pop = new Population<>(50, BinaryIndividual::new);
		// define evaluator
		var eval = Evaluator.<BinaryIndividual, Integer>of(b ->
		{
			return Integer.bitCount(b.getGenome().getInt());
		});
		// define crossover
		CrossoverMethod<BinaryIndividual> crossover = BinaryIndividual::crossover;
		// define mutation(s)
		Mutations<BinaryIndividual> mutations = new Mutations<>();
		mutations.add(new Mutation<>(0.01, BinaryIndividual::mutateIndividual));
		mutations.add(new Mutation<>(0.05, BinaryIndividual::mutatePoint));
		// define selection
		var selector = Selector.<BinaryIndividual>selectingBy(new ElitistSelection<>(1),
			new CrossoverSelection<BinaryIndividual>(pop.size() / 2)
				.withSelectionMethod(new RouletteSelection<>())
				.withCrossoverMethod(crossover),
			new CrossoverSelection<BinaryIndividual>()
				.withSelectionMethod(new RankSelection<>())
				.withCrossoverMethod(crossover));
		// define ranking
		var ranker = Ranker.rankingBy(
			Comparator.comparing(Evaluation<BinaryIndividual, Integer>::result).reversed());
		// track the best individual
		var best = new Evaluation<>(new BinaryIndividual(), Integer.MIN_VALUE);
		int generations = 0;
		do
		{
			++generations;
			selector.reset();
			var evals = eval.evaluate(pop.stream());
			var ranked = ranker.rank(evals).collect(Collectors.toList());
			best = ranked.get(0);
			var repopulator = new RepopulatorImpl<BinaryIndividual>(BinaryIndividual::copy)
			{
				public BinaryIndividual apply(Population<BinaryIndividual> t)
				{
					return mutations.apply(selector.select(ranked).copy());
				}
			};
			// repopulate
			pop = repopulator.collectN(pop, pop.size());
		}
		while(best.result() < 30);
		System.out.println(String.format("Done! Generations elapsed: %d", generations));
	}
}
