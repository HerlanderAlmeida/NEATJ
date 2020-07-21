package test;

import java.util.Comparator;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
import test.binary.BinaryIndividual;

public class GeneticAlgorithmTest
{
	@Test
	public void testBinaryGeneticAlgorithm()
	{
		// initialize population
		var pop = new Population<>(20, BinaryIndividual::new);
		// define evaluator
		var eval = Evaluator
			.<BinaryIndividual, Integer>of(b -> Integer.bitCount(b.genome().integer()));
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
		var generations = 0;
		do
		{
			++generations;
			selector.reset();
			var ranked = ranker.rank(eval.evaluate(pop.stream())).collect(Collectors.toList());
			// define repopulation
			var repopulator = new RepopulatorImpl<>(BinaryIndividual::copy)
			{
				@Override
				public BinaryIndividual apply(Population<BinaryIndividual> t)
				{
					return mutations.apply(selector.select(ranked).copy());
				}
			};
			// repopulate
			pop = repopulator.repopulate(pop);
			// track the best individual
			best = ranked.get(0);
		}
		while(generations < 1000 && best.result() < 30);
		Assertions.assertEquals(30, best.result());
		Assertions.assertTrue(generations < 1000);
		System.out.println("Genetic algorithm test completed in " + generations + " generations.");
	}
}
