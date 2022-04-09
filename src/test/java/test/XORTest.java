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
import genetic.selection.method.RankSelection;
import genetic.selection.method.RouletteSelection;
import neat.FitnessMeasure;
import neat.IndividualParameters;
import neat.InnovationTracker;
import neat.NetworkParameters;
import neat.NeuralIndividual;
import neat.SpeciatedPopulation;
import neat.SpeciationParameters;
import neat.SpeciesIndividual;
import neat.StalenessIndicator;
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
			.withStep(0.05)
			.withFullConnectivity(false)
			.withArbitraryConnectivity(false)
			.build();
		// initial individual parameters for each NeuralIndividual
		var individualParameters = IndividualParameters.builder()
			.withParameterEvolution(true)
			.withCloningMutationProbability(1.1)
			.withCrossoverMutationProbability(0.1)
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
			.withWeightDifferencePower(1.0)
			.withDesiredSpecies(15)
			.withDifferenceThreshold(1.4)
			.withDifferenceThresholdStep(0.05)
			.withCrossoverProbability(0.5)
			.withEliminationRate(0.8)
			.withStaleGenerationsAllowed(15)
			.withDeadbeatEvaluation(0)
			.withPreservedSpecies(1)
			.withPreservingLifetimeMaxFitness(false)
			.build();
		// define tracker for innovations
		var tracker = new InnovationTracker();
		// define parameters for individuals
		var builder = NeuralIndividual.builder()
			.withInnovationTracker(tracker)
			.withNetworkParameters(networkParameters)
			.withIndividualParameters(individualParameters);
		// define evaluator
		var eval = Evaluator.<NeuralIndividual, Double>of(b ->
		{
			var network = b.genome().toNetwork(Neuron::newHidden);
			var expected = new double[][] {
					{ 0, 0, 0 }, { 0 },
					{ 0, 0, 1 }, { 1 },
					{ 0, 1, 0 }, { 1 },
					{ 0, 1, 1 }, { 0 },
					{ 1, 0, 0 }, { 1 },
					{ 1, 0, 1 }, { 0 },
					{ 1, 1, 0 }, { 0 },
					{ 1, 1, 1 }, { 1 }
			};
			var sse = 0d;
			for(var i = 0; i < expected.length; i += 2)
			{
				var evaluation = network.evaluate(expected[i]);
				var target = expected[i + 1];
				for(var j = 0; j < target.length; j++)
				{
					target[j] -= (evaluation[j] + 1) / 2.0; // calculate errors
					target[j] *= target[j]; // square errors
					sse += target[j];
				}
			}
			return 8 - sse;
		});
		// define crossover
		var crossover = (CrossoverMethod<NeuralIndividual>) NeuralIndividual::crossover;
		// define mutation(s)
		// this mutation is likely to mutate generations more stably
//		var crossedMutation = new Mutation<NeuralIndividual>(t -> t);
		// this mutation can make smaller networks, but may
		// also spiral network size out of control
		var noMutation = new Mutation<NeuralIndividual>(t -> t);
		var crossedMutation = new Mutation<>(NeuralIndividual::mutateCrossover);
		var uncrossedMutation = new Mutation<>(NeuralIndividual::mutateCloning);
		// define selection
		var selector = Selector.<NeuralIndividual>selectingBy(
			new ElitistSelection<NeuralIndividual>(1)
				.withPostMutations(noMutation::apply),
			new CrossoverSelection<NeuralIndividual>()
				.withFirstSelector(new RouletteSelection<>())
				.withSecondSelector(new RankSelection<>())
				.withCrossoverMethod(crossover)
				.withCrossoverProbability(speciationParameters.crossoverProbability())
				.withCrossedMutation(crossedMutation::apply)
				.withUncrossedMutation(uncrossedMutation::apply)
		);
		// define species' measure of staleness
		var stalenessIndicator = (StalenessIndicator<NeuralIndividual, Double>) species -> species
			.fitnesses().max();
		// define species' measure of fitness
		var fitnessMeasure = (FitnessMeasure<NeuralIndividual, Double>) species -> species
			.population().stream().mapToDouble(SpeciesIndividual::fitness).average().orElse(0);
		// define ranking
		var ranker = Ranker.rankingBy(
			Comparator.comparing(Evaluation<NeuralIndividual, Double>::result).reversed());
		// initialize population
		var pop = SpeciatedPopulation.<NeuralIndividual, Double>builder()
			.withSize(100)
			.withGenerator(builder::build)
			.withParameters(speciationParameters)
			.withSelector(selector)
			.withStalenessIndicator(stalenessIndicator)
			.withFitnessMeasure(fitnessMeasure)
			.build();

		// track the best individual
		var best = new Evaluation<>((NeuralIndividual)null, Double.NEGATIVE_INFINITY);
		var generations = 0;
		do
		{
			++generations;
			var top = pop.updateFitnesses(ranker.rank(eval.evaluate(pop.individuals())));
			/*
			System.out.println("Generation " + generations + ", Size: "
				+ top.get().individual().genome().genes().size() + ", Neurons: "
				+ top.get().individual().genome().neurons() + ", best fitness: " + top.get().result());//*/
			best = top.orElse(best).result() < best.result() ? best : top.get();
			pop.updateSpecies(pop.repopulate());
			tracker.reset();
		}
		while(!this.isVerified3Input(best.individual()) && pop.individuals().count() > 0 && generations < 1000);
		System.out.println("3-input XOR test completed in "+generations+" generations.");
		Assertions.assertFalse(generations == 1000, "Failed to converge in 1000 generations");
		Assertions.assertTrue(pop.individuals().count() > 0, "All individuals perished");
		var network = best.individual().genome().toNetwork(Neuron::newHidden);
		Assertions.assertTrue(network.evaluate(new double[]	{ 0, 0, 0 })[0] < 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 0, 0, 1 })[0] > 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 0, 1, 0 })[0] > 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 0, 1, 1 })[0] < 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 1, 0, 0 })[0] > 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 1, 0, 1 })[0] < 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 1, 1, 0 })[0] < 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 1, 1, 1 })[0] > 0);
	}

	public boolean isVerified3Input(NeuralIndividual individual)
	{
		var network = individual.genome().toNetwork(Neuron::newHidden);
		return network.evaluate(new double[] { 0, 0, 0 })[0] < 0
			&& network.evaluate(new double[] { 0, 0, 1 })[0] > 0
			&& network.evaluate(new double[] { 0, 1, 0 })[0] > 0
			&& network.evaluate(new double[] { 0, 1, 1 })[0] < 0
			&& network.evaluate(new double[] { 1, 0, 0 })[0] > 0
			&& network.evaluate(new double[] { 1, 0, 1 })[0] < 0
			&& network.evaluate(new double[] { 1, 1, 0 })[0] < 0
			&& network.evaluate(new double[] { 1, 1, 1 })[0] > 0;
	}

	/**
	 * Traditional XOR test
	 */
	@Test
	public void testXOR2InputWithNEAT()
	{
		var networkParameters = NetworkParameters.builder()
			.withInputs(2)
			.withOutputs(1)
			.withBiases(1)
			.withRecurrency(false)
			.withRange(2)
			.withStep(0.05)
			.withFullConnectivity(false)
			.withArbitraryConnectivity(false)
			.build();
		// initial individual parameters for each NeuralIndividual
		var individualParameters = IndividualParameters.builder()
			.withParameterEvolution(true)
			.withCloningMutationProbability(1.1)
			.withCrossoverMutationProbability(0.1)
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
			.withWeightDifferencePower(1.0)
			.withDesiredSpecies(15)
			.withDifferenceThreshold(1.4)
			.withDifferenceThresholdStep(0.05)
			.withCrossoverProbability(0.5)
			.withEliminationRate(0.8)
			.withStaleGenerationsAllowed(15)
			.withDeadbeatEvaluation(0)
			.withPreservedSpecies(1)
			.withPreservingLifetimeMaxFitness(false)
			.build();
		// define tracker for innovations
		var tracker = new InnovationTracker();
		// define parameters for individuals
		var builder = NeuralIndividual.builder()
			.withInnovationTracker(tracker)
			.withNetworkParameters(networkParameters)
			.withIndividualParameters(individualParameters);
		// define evaluator
		var eval = Evaluator.<NeuralIndividual, Double>of(b ->
		{
			var network = b.genome().toNetwork(Neuron::newHidden);
			var expected = new double[][] {
					{ 0, 0 }, { 0 },
					{ 0, 1 }, { 1 },
					{ 1, 0 }, { 1 },
					{ 1, 1 }, { 0 }
			};
			var sse = 0d;
			for(var i = 0; i < expected.length; i += 2)
			{
				var evaluation = network.evaluate(expected[i]);
				var target = expected[i + 1];
				for(var j = 0; j < target.length; j++)
				{
					target[j] -= (evaluation[j] + 1) / 2.0; // calculate errors
					target[j] *= target[j]; // square errors
					sse += target[j];
				}
			}
			return 4 - sse;
		});
		// define crossover
		var crossover = (CrossoverMethod<NeuralIndividual>) NeuralIndividual::crossover;
		// define mutation(s)
		// this mutation is likely to mutate generations more stably
//		var crossedMutation = new Mutation<NeuralIndividual>(t -> t);
		// this mutation can make smaller networks, but may
		// also spiral network size out of control
		var noMutation = new Mutation<NeuralIndividual>(t -> t);
		var crossedMutation = new Mutation<>(NeuralIndividual::mutateCrossover);
		var uncrossedMutation = new Mutation<>(NeuralIndividual::mutateCloning);
		// define selection
		var selector = Selector.<NeuralIndividual>selectingBy(
			new ElitistSelection<NeuralIndividual>(1)
				.withPostMutations(noMutation::apply),
			new CrossoverSelection<NeuralIndividual>()
				.withFirstSelector(new RouletteSelection<>())
				.withSecondSelector(new RankSelection<>())
				.withCrossoverMethod(crossover)
				.withCrossoverProbability(speciationParameters.crossoverProbability())
				.withCrossedMutation(crossedMutation::apply)
				.withUncrossedMutation(uncrossedMutation::apply)
		);
		// define species' measure of staleness
		var stalenessIndicator = (StalenessIndicator<NeuralIndividual, Double>) species -> species
			.fitnesses().max();
		// define species' measure of fitness
		var fitnessMeasure = (FitnessMeasure<NeuralIndividual, Double>) species -> species
			.population().stream().mapToDouble(SpeciesIndividual::fitness).average().orElse(0);
		// define ranking
		var ranker = Ranker.rankingBy(
			Comparator.comparing(Evaluation<NeuralIndividual, Double>::result).reversed());
		// initialize population
		var pop = SpeciatedPopulation.<NeuralIndividual, Double>builder()
			.withSize(100)
			.withGenerator(builder::build)
			.withParameters(speciationParameters)
			.withSelector(selector)
			.withStalenessIndicator(stalenessIndicator)
			.withFitnessMeasure(fitnessMeasure)
			.build();

		// track the best individual
		var best = new Evaluation<>((NeuralIndividual)null, Double.NEGATIVE_INFINITY);
		var generations = 0;
		do
		{
			++generations;
			var top = pop.updateFitnesses(ranker.rank(eval.evaluate(pop.individuals())));
			/*
			System.out.println("Generation " + generations + ", Size: "
				+ top.get().individual().genome().genes().size() + ", Neurons: "
				+ top.get().individual().genome().neurons() + ", best fitness: " + top.get().result());//*/
			best = top.orElse(best).result() < best.result() ? best : top.get();
			pop.updateSpecies(pop.repopulate());
			tracker.reset();
		}
		while(!this.isVerified2Input(best.individual()) && pop.individuals().count() > 0 && generations < 100);
		System.out.println("2-input XOR test completed in "+generations+" generations.");
		Assertions.assertFalse(generations == 100, "Failed to converge in 100 generations");
		Assertions.assertTrue(pop.individuals().count() > 0, "All individuals perished");
		var network = best.individual().genome().toNetwork(Neuron::newHidden);
		Assertions.assertTrue(network.evaluate(new double[]	{ 0, 0 })[0] < 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 0, 1 })[0] > 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 1, 0 })[0] > 0);
		Assertions.assertTrue(network.evaluate(new double[]	{ 1, 1 })[0] < 0);
	}

	public boolean isVerified2Input(NeuralIndividual individual)
	{
		var network = individual.genome().toNetwork(Neuron::newHidden);
		return network.evaluate(new double[] { 0, 0 })[0] < 0
			&& network.evaluate(new double[] { 0, 1 })[0] > 0
			&& network.evaluate(new double[] { 1, 0 })[0] > 0
			&& network.evaluate(new double[] { 1, 1 })[0] < 0;
	}
}