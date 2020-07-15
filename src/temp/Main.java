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
import test.neat.IndividualParameters;
import test.neat.InnovationTracker;
import test.neat.NetworkParameters;
import test.neat.NeuralGene;
import test.neat.NeuralIndividual;
import test.neat.Parameters;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		var networkParameters = NetworkParameters.builder()
			.withInputs(4)
			.withOutputs(2)
			.withBiases(1)
			.withRecurrency(false)
			.build();
		// initial individual parameters for each NeuralIndividual
		var individualParameters = IndividualParameters.builder()
			.withWeightMutationProbability(0.225)
			.withRandomWeightMutationProbability(0.025)
			.withLinkMutationProbability(2)
			.withBiasLinkMutationProbability(0.4)
			.withNeuronMutationProbability(0.5)
			.withEnableMutationProbability(0.4)
			.withDisableMutationProbability(0.2)
			.withDestroyMutationProbability(0.01)
			.build();
		// set parameters for algorithm
		var parameters = Parameters.builder()
			.withRange(2)
			.withStep(0.125)
			.withDisjointCoefficient(2)
			.withExcessCoefficient(1.5)
			.withWeightDifferenceCoefficient(0.4)
			.build();
		// tracker innovations
		var tracker = new InnovationTracker();
		// define parameters for individuals
		var builder = NeuralIndividual.builder()
			.withInnovationTracker(tracker)
			.withNetworkParameters(networkParameters)
			.withIndividualParameters(individualParameters)
			.withParameters(parameters);
		
		var one = builder.build();
		one.genome().genes().add(new NeuralGene(2, 7, 1, true, 0));
		one.genome().genes().add(new NeuralGene(7, 8, 1, true, 1));
		one.genome().genes().add(new NeuralGene(3, 8, 1, true, 2));
		one.genome().genes().add(new NeuralGene(8, 5, 1, true, 3));
		one.genome().genes().add(new NeuralGene(1, 4, 1, true, 4));
		one.genome().genes().add(new NeuralGene(6, 9, 1, true, 5));
		one.genome().genes().add(new NeuralGene(9, 5, 1, true, 6));
		one.genome().genes().add(new NeuralGene(8, 9, 1, true, 7));
		var two = builder.build();
		two.genome().genes().add(new NeuralGene(2, 7, 2, true, 0));
		two.genome().genes().add(new NeuralGene(7, 8, 2, true, 1));
		two.genome().genes().add(new NeuralGene(8, 5, 2, true, 3));
		two.genome().genes().add(new NeuralGene(1, 4, 2, true, 4));
		two.genome().genes().add(new NeuralGene(9, 5, 2, true, 6));
		two.genome().genes().add(new NeuralGene(8, 9, 2, true, 7));
		two.genome().genes().add(new NeuralGene(3, 9, 2, true, 8));
		two.genome().genes().add(new NeuralGene(6, 7, 2, true, 9));
		//0, 1, 3, 4, 6, 7 - joint
		//8, 9 excess unique to two
		//2, 5 disjoint unique to one
		
		// initialize population
		var pop = new Population<>(20, builder::build);
		// define evaluator
		var eval = Evaluator.<NeuralIndividual, Double>of(b ->
		{
			var evaluation = Math.random() * 12;
			b.evaluation(evaluation);
			return evaluation;
		});
		// define crossover
		CrossoverMethod<NeuralIndividual> crossover = NeuralIndividual::crossover;
		// define mutation(s)
		Mutations<NeuralIndividual> mutations = new Mutations<>();
		mutations.add(new Mutation<>(NeuralIndividual::mutateComprehensively));
		// define selection
		var selector = Selector.<NeuralIndividual>selectingBy(new ElitistSelection<>(1),
			new CrossoverSelection<NeuralIndividual>(pop.size() / 2)
				.withSelectionMethod(new RouletteSelection<>())
				.withCrossoverMethod(crossover),
			new CrossoverSelection<NeuralIndividual>()
				.withSelectionMethod(new RankSelection<>())
				.withCrossoverMethod(crossover));
		// define ranking
		var ranker = Ranker.rankingBy(
			Comparator.comparing(Evaluation<NeuralIndividual, Double>::result).reversed());
		// track the best individual
		var best = new Evaluation<>((NeuralIndividual)null, Double.MIN_VALUE);
		int generations = 0;
		do
		{
			++generations;
			selector.reset();
			var ranked = ranker.rank(eval.evaluate(pop.stream())).collect(Collectors.toList());
			// define repopulation
			var repopulator = new RepopulatorImpl<NeuralIndividual>(NeuralIndividual::copy)
			{
				public NeuralIndividual apply(Population<NeuralIndividual> t)
				{
					return mutations.apply(selector.select(ranked).copy());
				}
			};
			// repopulate
			pop = repopulator.collectN(pop, pop.size());
			// track the best individual
			best = ranked.get(0);
			System.out.println("Generation "+generations);
		}
		while(generations < 1000 && best.result() < 30);
		System.out.println("Genetic algorithm test completed in "+generations+" generations.");
	}
}
