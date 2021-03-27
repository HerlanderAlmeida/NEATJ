package test;

import java.io.IOException;
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
import genetic.selection.Ranker;
import genetic.selection.Selector;
import genetic.selection.method.ElitistSelection;
import genetic.selection.method.RankSelection;
import genetic.selection.method.RouletteSelection;
import neat.IndividualParameters;
import neat.InnovationTracker;
import neat.NetworkParameters;
import neat.NeuralIndividual;
import neat.SpeciatedPopulation;
import neat.SpeciationParameters;
import neat.Species;
import neat.StalenessIndicator;
import network.neuron.Neuron;
import utils.GsonUtils;
import utils.RecordDeserializer;
import utils.ResourceUtils;

public class PersistenceTest
{
	@Test
	public void testPersistenceWithNEAT() throws IOException
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
			.withDesiredSpecies(15)
			.withDifferenceThreshold(1.4)
			.withDifferenceThresholdStep(0.05)
			.withCrossoverProbability(0.5)
			.withEliminationRate(0.8)
			.withStaleGenerationsAllowed(15)
			.withDeadbeatEvaluation(0)
			.withPreservedSpecies(1)
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
			return 8 - (int)(sse * 1e4)/1e4;
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
		StalenessIndicator<NeuralIndividual, Double> stalenessIndicator = species -> species
			.fitnesses().max();
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
			.build();

		// track the best individual
		var best = new Evaluation<>((NeuralIndividual)null, Double.NEGATIVE_INFINITY);
		// starting from generation 100, will write generations 100 + 1 to 100 + 5
		var firstGeneration = 100;
		var numGenerations = 5;
		var thisGeneration = firstGeneration;

		// set our serialization/deserialization
		var gsonBuilder = GsonUtils.gsonBuilder()
			.registerTypeAdapter(Population.class, Population.deserializer(NeuralIndividual.class))
			.registerTypeAdapter(PersistentRecord.class,
				RecordDeserializer.forClass(PersistentRecord.class));
		var gson = GsonUtils.INSTANCE.swapGson(gsonBuilder.create());

		ResourceUtils.deleteFile("/persistence/gen101_record.json");
		ResourceUtils.deleteFile("/persistence/gen102_record.json");
		ResourceUtils.deleteFile("/persistence/gen103_record.json");
		ResourceUtils.deleteFile("/persistence/gen104_record.json");
		ResourceUtils.deleteFile("/persistence/gen105_record.json");

		do
		{
			if(thisGeneration == firstGeneration && thisGeneration != 0)
			{
				var persisted = ResourceUtils.readObjectFromFile(
					"/persistence/gen" + thisGeneration + "_record.json", PersistentRecord.class);
				pop = persisted.pop();
				pop.selector(selector);
				pop.generator(builder::build);
				pop.stalenessIndicator(stalenessIndicator);
				tracker = persisted.tracker();
				pop.individuals().forEach(ni -> ni.tracker(persisted.tracker()));
			}
			++thisGeneration;
			// we want parallel and unordered for evaluation, but not for rank
			var evals = eval
				.evaluate(
					pop.species().parallelStream().flatMap(Species::parallelStream).unordered())
				.collect(Collectors.toList());
			var top = pop.updateFitnesses(ranker.rank(evals));
			//*
			System.out.println("Generation " + thisGeneration + ", Size: "
				+ top.get().individual().genome().genes().size() + ", Neurons: "
				+ top.get().individual().genome().neurons() + ", best fitness: "
				+ top.get().result());// */
			best = top.orElse(best).result() < best.result() ? best : top.get();
			pop.updateSpecies(pop.repopulate());
			tracker.reset();
			ResourceUtils.writeObjectToFile("/persistence/gen" + thisGeneration + "_record.json",
				new PersistentRecord(pop, tracker));
		} while(thisGeneration < firstGeneration + numGenerations);
		System.out.println("Persistence test completed in " + numGenerations + " generations.");
		Assertions.assertTrue(pop.individuals().count() > 0, "All individuals perished");
		var network = best.individual().genome().toNetwork(Neuron::newHidden);
		var inputs = new double[][] {
			{ 0, 0, 0 }, { -1.0 },
			{ 0, 0, 1 }, { 1.0 },
			{ 0, 1, 0 }, { 1.0 },
			{ 0, 1, 1 }, { -1.0 },
			{ 1, 0, 0 }, { 1.0 },
			{ 1, 0, 1 }, { -1.0 },
			{ 1, 1, 0 }, { -1.0 },
			{ 1, 1, 1 }, { 1.0 }
		};

		for(var i = 0; i < inputs.length; i += 2)
		{
			Assertions.assertEquals(inputs[i + 1][0], Math.signum(network.evaluate(inputs[i])[0]));
		}

		// reset state
		ResourceUtils.deleteFile("/persistence/gen101_record.json");
		ResourceUtils.deleteFile("/persistence/gen102_record.json");
		ResourceUtils.deleteFile("/persistence/gen103_record.json");
		ResourceUtils.deleteFile("/persistence/gen104_record.json");
		ResourceUtils.deleteFile("/persistence/gen105_record.json");
		GsonUtils.INSTANCE.setGson(gson);
	}

	// defined separately (important!) for purposes of persistence. The GSON library doesn't like local records
	private record PersistentRecord(SpeciatedPopulation<NeuralIndividual, Double> pop,
		InnovationTracker tracker)
	{
	}
}