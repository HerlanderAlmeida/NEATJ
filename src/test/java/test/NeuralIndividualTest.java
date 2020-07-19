package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import neat.IndividualParameters;
import neat.InnovationTracker;
import neat.NetworkParameters;
import neat.NeuralIndividual;
import network.neuron.Neuron;

public class NeuralIndividualTest
{
	@Test
	public void testConnections()
	{
		var networkParameters = NetworkParameters.builder().withInputs(3).withOutputs(1)
			.withBiases(1).withRecurrency(false).withFullConnectivity(false).build();
		var individualParameters = IndividualParameters.builder().build();
		var tracker = new InnovationTracker();
		var builder = NeuralIndividual.builder().withInnovationTracker(tracker)
			.withNetworkParameters(networkParameters)
			.withIndividualParameters(individualParameters);
		var ind = builder.build();
		for(var i = 0; i < 100_000; i++)
		{
			ind = ind.mutateLink();
		}
		// expected network setup: inputs[0, 1, 2] outputs[3] biases[4] hidden[]
		// expected connections: 0->3, 1->3, 2->3, 4->3
		var genome = ind.genome();
		var genes = genome.genes();
		Assertions.assertTrue(genome.hasConnection(0, 3));
		Assertions.assertTrue(genome.hasConnection(1, 3));
		Assertions.assertTrue(genome.hasConnection(2, 3));
		Assertions.assertTrue(genome.hasConnection(4, 3));
		Assertions.assertEquals(4, genome.genes().size());
		Assertions.assertTrue(inRange(tracker.getMarker(0, 3), 0, 3));
		Assertions.assertTrue(inRange(tracker.getMarker(1, 3), 0, 3));
		Assertions.assertTrue(inRange(tracker.getMarker(2, 3), 0, 3));
		Assertions.assertTrue(inRange(tracker.getMarker(4, 3), 0, 3));
		for(var i = 0; i < genes.size(); i++)
		{
			genes.set(i, genes.get(i).withWeight(1));
		}
		ind.mutateWeight();
		var mutated = false;
		for(var gene : genes)
		{
			if(gene.weight() != 1)
			{
				mutated = true;
			}
		}
		Assertions.assertTrue(mutated, "Weight mutation did not occur!");
		for(var i = 0; i < genes.size(); i++)
		{
			genes.set(i, genes.get(i).withWeight(1));
		}
		Assertions.assertEquals(0.8411229016,
			genome.toNetwork(Neuron::newHidden).evaluate(new double[]
			{ 1, -2, 0.5 })[0], 1e-10);
		// sum of 1 * (1 + -2 + 0.5 + 1) into sigmoid ~= 0.8411
	}

	@Test
	public void testFullyConnected()
	{
		var networkParameters = NetworkParameters.builder().withInputs(3).withOutputs(2)
			.withBiases(1).withRecurrency(false).withFullConnectivity(true).build();
		var individualParameters = IndividualParameters.builder().build();
		var tracker = new InnovationTracker();
		var builder = NeuralIndividual.builder().withInnovationTracker(tracker)
			.withNetworkParameters(networkParameters)
			.withIndividualParameters(individualParameters);
		var ind = builder.build();
		// expected network setup: inputs[0, 1, 2] outputs[3, 4] biases[5]
		// hidden[]
		// expected connections: 0->3, 1->3, 2->3, 4->3
		var genome = ind.genome();
		Assertions.assertTrue(genome.hasConnection(0, 3));
		Assertions.assertTrue(genome.hasConnection(1, 3));
		Assertions.assertTrue(genome.hasConnection(2, 3));
		Assertions.assertTrue(genome.hasConnection(5, 3));
		Assertions.assertTrue(genome.hasConnection(0, 4));
		Assertions.assertTrue(genome.hasConnection(1, 4));
		Assertions.assertTrue(genome.hasConnection(2, 4));
		Assertions.assertTrue(genome.hasConnection(5, 4));
		Assertions.assertEquals(8, genome.genes().size());
		Assertions.assertTrue(inRange(tracker.getMarker(0, 3), 0, 7));
		Assertions.assertTrue(inRange(tracker.getMarker(1, 3), 0, 7));
		Assertions.assertTrue(inRange(tracker.getMarker(2, 3), 0, 7));
		Assertions.assertTrue(inRange(tracker.getMarker(5, 3), 0, 7));
		Assertions.assertTrue(inRange(tracker.getMarker(0, 4), 0, 7));
		Assertions.assertTrue(inRange(tracker.getMarker(1, 4), 0, 7));
		Assertions.assertTrue(inRange(tracker.getMarker(2, 4), 0, 7));
		Assertions.assertTrue(inRange(tracker.getMarker(5, 4), 0, 7));
	}

	@Test
	public void testCrossover()
	{
		var networkParameters = NetworkParameters.builder().withInputs(3).withOutputs(2)
			.withBiases(1).withRecurrency(false).withFullConnectivity(true).build();
		var individualParameters = IndividualParameters.builder().build();
		var tracker = new InnovationTracker();
		var builder = NeuralIndividual.builder().withInnovationTracker(tracker)
			.withNetworkParameters(networkParameters)
			.withIndividualParameters(individualParameters);
		var ind = builder.build();
		ind = ind.crossover(ind);
		var genome = ind.genome();
		Assertions.assertEquals(8, genome.genes().size());
		networkParameters = NetworkParameters.builder().withInputs(3).withOutputs(2).withBiases(1)
			.withRecurrency(false).withFullConnectivity(false).build();
		builder = NeuralIndividual.builder().withInnovationTracker(tracker)
			.withNetworkParameters(networkParameters)
			.withIndividualParameters(individualParameters);
		var subset = builder.build();

	}

	@Test
	public void testDestroy()
	{
		var networkParameters = NetworkParameters.builder().withInputs(3).withOutputs(2)
			.withBiases(1).withRecurrency(false).withFullConnectivity(false).build();
		var individualParameters = IndividualParameters.builder().build();
		var tracker = new InnovationTracker();
		var builder = NeuralIndividual.builder().withInnovationTracker(tracker)
			.withNetworkParameters(networkParameters)
			.withIndividualParameters(individualParameters);
		var ind = builder.build();
		ind.genome().addConnection(0, 3, 1, false, tracker);
		ind.mutateDestroy();
		Assertions.assertEquals(0, ind.genome().genes().size());
	}

	private boolean inRange(int num, int low, int high)
	{
		return low <= num && num <= high;
	}
}
