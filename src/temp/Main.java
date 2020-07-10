package temp;

import java.util.Comparator;
import java.util.function.Supplier;

import test.neat.InnovationTracker;
import test.neat.NeuralGene;
import test.neat.NeuralIndividual;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		var tracker = new InnovationTracker();
		var builder = NeuralIndividual.builder();
		Supplier<NeuralIndividual> supplier = builder
			.withInputs(4)
			.withOutputs(2)
			.withBiases(1)
			.withRecurrency(false)
			.withInnovationTracker(tracker)::build;
		var individual = supplier.get();
		individual.mutateLink().mutateNeuron().mutateNeuron();
		System.out.println(individual);
		for(int i = 0; i < 10000; i++)
			individual.mutateLink();
		System.out.println(individual);
	}
}
