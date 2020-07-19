package test;

import java.util.Comparator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import genetic.crossover.CrossoverMethod;
import genetic.crossover.CrossoverSelection;
import genetic.evaluate.Evaluation;
import genetic.evaluate.Evaluator;
import genetic.mutation.Mutation;
import genetic.selection.Ranker;
import genetic.selection.Selector;
import genetic.selection.method.ElitistSelection;
import genetic.selection.method.RandomSelection;
import genetic.selection.method.RankSelection;
import genetic.selection.method.RouletteSelection;
import neat.IndividualParameters;
import neat.InnovationTracker;
import neat.NetworkParameters;
import neat.NeuralIndividual;
import neat.SpeciatedPopulation;
import neat.SpeciationParameters;
import network.neuron.Neuron;

public class XORTest
{
	@Test
	public void testXORwithNEAT()
	{
		var networkParameters = NetworkParameters.builder()
			.withInputs(3)
			.withOutputs(1)
			.withBiases(1)
			.withRecurrency(false)
			.withRange(2)
			.withStep(0.125)
			.withFullConnectivity(false)
			.build();
		// initial individual parameters for each NeuralIndividual
		var individualParameters = IndividualParameters.builder()
			.withMetaMutationProbability(1)
			.withWeightMutationProbability(0.225)
			.withRandomWeightMutationProbability(0.025)
			.withLinkMutationProbability(2)
			.withBiasLinkMutationProbability(0.4)
			.withSensorMutationProbability(0.1)
			.withNeuronMutationProbability(0.5)
			.withEnableMutationProbability(0.4)
			.withDisableMutationProbability(0.2)
			.withDestroyMutationProbability(0.01)
			.build();
		// set parameters for algorithm
		var speciationParameters = SpeciationParameters.builder()
			.withDisjointCoefficient(2)
			.withExcessCoefficient(1.5)
			.withWeightDifferenceCoefficient(0.4)
			.withDesiredSpecies(15)
			.withDifferenceThreshold(1.4)
			.withDifferenceThresholdStep(0.05)
			.withCrossoverProbability(0.75)
			.withEliminationRate(0.8)
			.withStaleGenerationsAllowed(15)
			.withDeadbeatEvaluation(0)
			.withPreservedSpecies(2)
			.build();
		// tracker innovations
		var tracker = new InnovationTracker();
		// define parameters for individuals
		var builder = NeuralIndividual.builder()
			.withInnovationTracker(tracker)
			.withNetworkParameters(networkParameters)
			.withIndividualParameters(individualParameters);
		// define evaluator
		var eval = Evaluator.<NeuralIndividual, Double>of(b ->
		{
			var network = b.genome().toNetwork(Neuron::new);
			var expected = new double[][] {
					{ 0, 0, 0 }, { 0 },
					{ 0, 0, 1 }, { 1 },
					{ 0, 1, 0 }, { 1 },
					{ 0, 1, 1 }, { 0 },
					{ 1, 0, 0 }, { 1 },
					{ 1, 0, 1 }, { 0 },
					{ 1, 1, 0 }, { 0 },
					{ 1, 1, 1 }, { 1 },
			};
			var sse = 0d;
			for(var i = 0; i < expected.length / 2; i++)
			{
				var evaluation = network.evaluate(expected[i * 2]);
				var target = expected[i * 2 + 1];
				for(var j = 0; j < target.length; j++)
				{
					target[j] -= (evaluation[j] + 1) / 2.0; // calculate errors
					target[j] *= target[j]; // square errors
					sse += target[j];
				}
			}
			return (8 - sse) * (8 - sse);
		});
		// define crossover
		var crossover = (CrossoverMethod<NeuralIndividual>) NeuralIndividual::crossover;
		// define mutation(s)
		var mutation = new Mutation<>(NeuralIndividual::mutateComprehensively);
		// define selection
		var selector = Selector.<NeuralIndividual>selectingBy(new ElitistSelection<>(1),
			Selector.<NeuralIndividual>selectingBy(
				new CrossoverSelection<NeuralIndividual>(6)
					.withSelectionMethod(new RandomSelection<>())
					.withCrossoverMethod(crossover)
					.withCrossoverProbability(speciationParameters.crossoverProbability())
					.withPostMutations(mutation::apply),
				new CrossoverSelection<NeuralIndividual>(1)
					.withSelectionMethod(new RouletteSelection<>())
					.withCrossoverMethod(crossover)
					.withCrossoverProbability(speciationParameters.crossoverProbability())
					.withPostMutations(mutation::apply),
				new CrossoverSelection<NeuralIndividual>(1)
					.withSelectionMethod(new RankSelection<>())
					.withCrossoverMethod(crossover)
					.withCrossoverProbability(speciationParameters.crossoverProbability())
					.withPostMutations(mutation::apply)
			));
		// define ranking
		var ranker = Ranker.rankingBy(
			Comparator.comparing(Evaluation<NeuralIndividual, Double>::result).reversed());
		// initialize population
		var pop = SpeciatedPopulation.<NeuralIndividual, Double>builder()
			.withSize(100)
			.withGenerator(builder::build)
			.withParameters(speciationParameters)
			.withSelector(selector)
			.build();

		// track the best individual
		var best = new Evaluation<>((NeuralIndividual)null, Double.NEGATIVE_INFINITY);
		var generations = 0;
		do
		{
			++generations;
			tracker.reset();
			var top = pop.updateFitnesses(ranker.rank(eval.evaluate(pop.individuals())));
			System.out.println("Generation " + generations + ", Size: "
				+ top.individual().genome().genes().size() + ", best fitness: " + top.result());
			best = top == null || top.result() < best.result() ? best : top;
			pop.updateSpecies(pop.repopulate(mutation::apply));
		}
		while(!isVerified(best.individual()) && pop.individuals().count() > 0);
		System.out.println("NEAT test completed in "+generations+" generations.");
		if(pop.individuals().count() <= 0)
		{
			System.out.println("All individuals perished");
		}
		Assertions.assertTrue(pop.individuals().count() > 0);
		var network = best.individual().genome().toNetwork(Neuron::new);
		Assertions.assertTrue(network.evaluate(new double[]	{ 0, 0, 0 })[0] < 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 0, 0, 1 })[0] > 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 0, 1, 0 })[0] > 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 0, 1, 1 })[0] < 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 1, 0, 0 })[0] > 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 1, 0, 1 })[0] < 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 1, 1, 0 })[0] < 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 1, 1, 1 })[0] > 0);
	}

	public boolean isVerified(NeuralIndividual individual)
	{
		var network = individual.genome().toNetwork(Neuron::new);
		return network.evaluate(new double[] { 0, 0, 0 })[0] < 0
			&& network.evaluate(new double[] { 0, 0, 1 })[0] > 0
			&& network.evaluate(new double[] { 0, 1, 0 })[0] > 0
			&& network.evaluate(new double[] { 0, 1, 1 })[0] < 0
			&& network.evaluate(new double[] { 1, 0, 0 })[0] > 0
			&& network.evaluate(new double[] { 1, 0, 1 })[0] < 0
			&& network.evaluate(new double[] { 1, 1, 0 })[0] < 0
			&& network.evaluate(new double[] { 1, 1, 1 })[0] > 0;
	}
}
