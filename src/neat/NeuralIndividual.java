package neat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NeuralIndividual extends SpeciesIndividual<Double>
{
	private static final Random RNG = new Random();
	private NeuralGenome genome;
	private InnovationTracker tracker;
	// per-individual parameter values
	private IndividualParameters individualParameters;
	// generic parameter values
	private double fitness;
	
	private NeuralIndividual(Object[] o)
	{
		this((InnovationTracker) o[0], (NetworkParameters) o[1], (IndividualParameters) o[2]);
	}
	
	private NeuralIndividual(InnovationTracker tracker, NetworkParameters networkParameters,
		IndividualParameters individualParameters)
	{
		genome = new NeuralGenome(networkParameters);
		this.tracker = tracker;
		this.individualParameters = individualParameters;
	}
	
	private NeuralIndividual(NeuralIndividual other)
	{
		this.genome = new NeuralGenome(other.genome().copy());
		this.tracker = other.tracker;
		this.individualParameters = other.individualParameters.copy();
		this.fitness = other.fitness;
	}
	
	@Override
	public NeuralIndividual copy()
	{
		return new NeuralIndividual(this);
	}
	
	@Override
	public NeuralGenome genome()
	{
		return this.genome.cast();
	}
	
	public Double fitness()
	{
		return this.fitness;
	}
	
	public void fitness(Double fitness)
	{
		this.fitness = fitness;
	}
	
	public void divideFitness(double sharers)
	{
		this.fitness /= sharers;
	}
	
	public IndividualParameters individualParameters()
	{
		return this.individualParameters;
	}
	
	public Double difference(SpeciesIndividual<Double> di, SpeciationParameters parameters)
	{
		if(di instanceof NeuralIndividual other)
		{
			var genome1 = this.genome;
			var genome2 = other.genome;
			Collections.sort(genome1.genes(), Comparator.comparingInt(NeuralGene::marker));
			Collections.sort(genome2.genes(), Comparator.comparingInt(NeuralGene::marker));
			var iter1 = genome1.genes().iterator();
			var iter2 = genome2.genes().iterator();
			NeuralGene gene1 = iter1.hasNext() ? iter1.next() : null;
			NeuralGene gene2 = iter2.hasNext() ? iter2.next() : null;
			var disjoint = 0d;
			var excess = 0d;
			var weightDiffs = 0d;
			var joint = 0d;
			while(gene1 != null && gene2 != null)
			{
				if(gene1.marker() < gene2.marker())
				{
					disjoint++;
					gene1 = iter1.hasNext() ? iter1.next() : null;
				}
				else if(gene1.marker() > gene2.marker())
				{
					disjoint++;
					gene2 = iter2.hasNext() ? iter2.next() : null;
				}
				else if(gene1.marker() == gene2.marker())
				{
					weightDiffs += Math.abs(gene1.weight() - gene2.weight());
					joint++;
					gene1 = iter1.hasNext() ? iter1.next() : null;
					gene2 = iter2.hasNext() ? iter2.next() : null;
				}
			}
			if(gene1 != null || gene2 != null)
				excess++;
			while(iter1.hasNext())
			{
				excess++;
				iter1.next();
			}
			while(iter2.hasNext())
			{
				excess++;
				iter2.next();
			}
			double largerGenomeSize = Math.max(1,
				Math.max(genome1.genes().size(), genome2.genes().size()));
			return parameters.excessCoefficient() * excess / largerGenomeSize
				+ parameters.disjointCoefficient() * disjoint / largerGenomeSize
				+ parameters.weightDifferenceCoefficient() * weightDiffs / Math.max(1, joint);
		}
		else
			throw new IllegalArgumentException(
				"Arbitrary DifferentialIndividual distance not supported!");
	}
	
	public NeuralIndividual crossover(NeuralIndividual other)
	{
		var genome1 = this.genome;
		var genome2 = other.genome;
		var evaluation1 = this.fitness;
		var evaluation2 = other.fitness;
		var individualParameters = this.fitness > other.fitness ? this.individualParameters
			: this.fitness < other.fitness ? other.individualParameters
				: RNG.nextBoolean() ? this.individualParameters : other.individualParameters;
		var child = new NeuralIndividual(tracker, genome.networkParameters(), individualParameters);
		Collections.sort(genome1.genes(), Comparator.comparingInt(NeuralGene::marker));
		Collections.sort(genome2.genes(), Comparator.comparingInt(NeuralGene::marker));
		var iter1 = genome1.genes().iterator();
		var iter2 = genome2.genes().iterator();
		NeuralGene gene1 = iter1.hasNext() ? iter1.next() : null;
		NeuralGene gene2 = iter2.hasNext() ? iter2.next() : null;
		var exclusive1 = new ArrayList<NeuralGene>();
		var exclusive2 = new ArrayList<NeuralGene>();
		var joint = child.genome.genes();
		while(gene1 != null && gene2 != null)
		{
			if(gene1.marker() < gene2.marker())
			{
				exclusive1.add(gene1);
				gene1 = iter1.hasNext() ? iter1.next() : null;
			}
			else if(gene1.marker() > gene2.marker())
			{
				exclusive2.add(gene2);
				gene2 = iter2.hasNext() ? iter2.next() : null;
			}
			else if(gene1.marker() == gene2.marker())
			{
				var next = RNG.nextBoolean() ? gene1 : gene2;
				var enabled = RNG.nextBoolean() ? gene1.enabled() : gene2.enabled();
				joint.add(next.withEnabled(enabled));
				gene1 = iter1.hasNext() ? iter1.next() : null;
				gene2 = iter2.hasNext() ? iter2.next() : null;
			}
		}
		if(gene1 != null)
		{
			exclusive1.add(gene1);
		}
		while(iter1.hasNext())
		{
			exclusive1.add(iter1.next());
		}
		if(gene2 != null)
		{
			exclusive2.add(gene2);
		}
		while(iter2.hasNext())
		{
			exclusive2.add(iter2.next());
		}
		if(evaluation1 > evaluation2)
		{
			joint.addAll(exclusive1);
		}
		else if(evaluation1 < evaluation2)
		{
			joint.addAll(exclusive2);
		}
		else
		{
			if(RNG.nextBoolean())
			{
				joint.addAll(exclusive1);
			}
			else
			{
				joint.addAll(exclusive2);
			}
		}
		child.genome.neurons(Math.max(this.genome.neurons(), other.genome.neurons()));
		return child;
	}
	
	public NeuralIndividual mutateComprehensively()
	{
		individualParameters = individualParameters.mutateProbabilities(RNG);
		for(var meta = individualParameters.metaMutationProbability(); meta > 0; meta--)
		{
			if(meta >= 1 || RNG.nextDouble() < meta)
			{
				for(var chance = individualParameters
					.weightMutationProbability(); chance > 0; chance--)
					if(chance >= 1 || RNG.nextDouble() < chance)
						mutateWeight();
				for(var chance = individualParameters
					.randomWeightMutationProbability(); chance > 0; chance--)
					if(chance >= 1 || RNG.nextDouble() < chance)
						mutateRandomWeight();
				for(var chance = individualParameters
					.linkMutationProbability(); chance > 0; chance--)
					if(chance >= 1 || RNG.nextDouble() < chance)
						mutateLink();
				for(var chance = individualParameters
					.biasLinkMutationProbability(); chance > 0; chance--)
					if(chance >= 1 || RNG.nextDouble() < chance)
						mutateBiasLink();
				for(var chance = individualParameters
					.sensorMutationProbability(); chance > 0; chance--)
					if(chance >= 1 || RNG.nextDouble() < chance)
						mutateSensor();
				for(var chance = individualParameters
					.neuronMutationProbability(); chance > 0; chance--)
					if(chance >= 1 || RNG.nextDouble() < chance)
						mutateNeuron();
				for(var chance = individualParameters
					.enableMutationProbability(); chance > 0; chance--)
					if(chance >= 1 || RNG.nextDouble() < chance)
						mutateEnable();
				for(var chance = individualParameters
					.disableMutationProbability(); chance > 0; chance--)
					if(chance >= 1 || RNG.nextDouble() < chance)
						mutateDisable();
				for(var chance = individualParameters
					.destroyMutationProbability(); chance > 0; chance--)
					if(chance >= 1 || RNG.nextDouble() < chance)
						mutateDestroy();
			}
		}
		return this;
	}
	
	/**
	 * Adds a link
	 */
	public NeuralIndividual mutateLink()
	{
		var neurons = genome.neurons();
		var inputs = genome.inputs();
		var outputs = genome.outputs();
		var biases = genome.biases();
		var unchanging = inputs + biases;
		// always input, bias, or hidden
		var first = RNG.nextInt(neurons - outputs);
		// always output or hidden
		var second = RNG.nextInt(neurons - unchanging);
		var inputEdge = inputs;
		var outputEdge = inputs + outputs;
		var biasEdge = inputs + outputs + biases;
		// network=[inputs|outputs|biases|hidden]
		if(first >= inputs)
		{
			first += outputs;
		}
		if(second >= outputs)
		{
			// translate non-outputs to be hidden
			second += biases;
		}
		second += inputs;
		Predicate<Integer> isInput = x -> x < inputEdge;
		Predicate<Integer> isOutput = x -> x >= inputEdge && x < outputEdge;
		Predicate<Integer> isBias = x -> x >= outputEdge && x < biasEdge;
		if(isOutput.test(first))
			throw new IllegalStateException(
				"Unexpectly, output node selected as link source: " + first);
		if(isBias.test(second))
			throw new IllegalStateException(
				"Unexpectly, bias node selected as link destination: " + second);
		if(isInput.test(second))
			throw new IllegalStateException(
				"Unexpectly, input node selected as link destination: " + second);
		if(first == second)
		{
			return this;
		}
		if(!genome.recurrent() && !isInput.test(first) && !isBias.test(first)
			&& !isOutput.test(second))
		{
			// need to preserve DAG-ness of the neural network
			if(isRecurrent(first, second))
			{
				var temp = first;
				first = second;
				second = temp;
			}
		}
		for(var gene : genome.genes())
		{
			if(gene.from() == first && gene.to() == second)
			{
				return this;
			}
		}
		genome.genes()
			.add(
				new NeuralGene(first, second,
					RNG.nextDouble() * genome.networkParameters().range() * 2
						- genome.networkParameters().range(),
					true, tracker.getMarker(first, second)));
		return this;// .9, try twice
	}
	
	// help, this function is extremely expensive!!!
	public boolean isRecurrent(int from, int to)
	{
		var sources = genome.genes().stream().collect(Collectors.groupingBy(NeuralGene::to));
		var previous = new ArrayDeque<Integer>();
		Optional.ofNullable(sources.get(from)).orElseGet(ArrayList::new).stream()
			.map(NeuralGene::from).forEach(previous::add);
		var visited = new HashSet<Integer>();
		while(!previous.isEmpty())
		{
			var current = previous.poll();
			if(current == to)
			{
				return true;
			}
			if(!visited.add(current))
			{
				continue;
			}
			var currentSources = sources.get(current);
			if(currentSources != null)
			{
				for(var source : currentSources)
				{
					if(!visited.contains(source.from()))
					{
						previous.offer(source.from());
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Adds a link starting with a bias node
	 */
	public NeuralIndividual mutateBiasLink()
	{
		var neurons = genome.neurons();
		var inputs = genome.inputs();
		var outputs = genome.outputs();
		var biases = genome.biases();
		var unchanging = inputs + biases;
		// always bias
		var first = RNG.nextInt(biases);
		// always output or hidden
		var second = RNG.nextInt(neurons - unchanging);
		var inputEdge = inputs;
		var outputEdge = inputs + outputs;
		var biasEdge = inputs + outputs + biases;
		// network=[inputs|outputs|biases|hidden]
		// translate non-outputs to be hidden
		if(second >= outputs)
		{
			second += biases;
		}
		first += inputs + outputs;
		second += inputs;
		Predicate<Integer> isInput = x -> x < inputEdge;
		Predicate<Integer> isBias = x -> x >= outputEdge && x < biasEdge;
		if(!isBias.test(first))
			throw new IllegalStateException(
				"Unexpectedly, non-bias node selected as link source: " + first);
		if(isBias.test(second))
			throw new IllegalStateException(
				"Unexpectly, bias node selected as link destination: " + second);
		if(isInput.test(second))
			throw new IllegalStateException(
				"Unexpectly, input node selected as link destination: " + second);
		for(var gene : genome.genes())
		{
			if(gene.from() == first && gene.to() == second)
			{
				return this;
			}
		}
		genome.genes()
			.add(
				new NeuralGene(first, second,
					RNG.nextDouble() * genome.networkParameters().range() * 2
						- genome.networkParameters().range(),
					true, tracker.getMarker(first, second)));
		return this;// .4
	}
	
	public NeuralIndividual mutateSensor()
	{
		var neurons = genome.neurons();
		var inputs = genome.inputs();
		var outputs = genome.outputs();
		// always input, bias, or hidden
		var first = RNG.nextInt(neurons - outputs);
		// always output
		var second = RNG.nextInt(outputs);
		var inputEdge = inputs;
		var outputEdge = inputs + outputs;
		// network=[inputs|outputs|biases|hidden]
		if(first >= inputs)
		{
			first += outputs;
		}
		second += inputs;
		Predicate<Integer> isOutput = x -> x >= inputEdge && x < outputEdge;
		if(isOutput.test(first))
			throw new IllegalStateException(
				"Unexpectly, output node selected as link source: " + first);
		if(!isOutput.test(second))
			throw new IllegalStateException(
				"Unexpectly, non-output node selected as link destination: " + second);
		for(var gene : genome.genes())
		{
			if(gene.from() == first && gene.to() == second)
			{
				return this;
			}
		}
		genome.genes()
			.add(
				new NeuralGene(first, second,
					RNG.nextDouble() * genome.networkParameters().range() * 2
						- genome.networkParameters().range(),
					true, tracker.getMarker(first, second)));
		return this; // .4
	}
	
	/**
	 * Changes a weight
	 */
	public NeuralIndividual mutateWeight()
	{
		var genes = this.genome.genes();
		if(genes.size() == 0)
			return this;
		var index = RNG.nextInt(genes.size());
		var gene = genes.get(index);
		genes.set(index, gene
			.withWeight(gene.weight() + RNG.nextGaussian() * genome.networkParameters().step()));
		return this;// 0.225
	}
	
	/**
	 * Randomizes a weight
	 */
	public NeuralIndividual mutateRandomWeight()
	{
		var genes = this.genome.genes();
		if(genes.size() == 0)
			return this;
		var index = RNG.nextInt(genes.size());
		var gene = genes.get(index);
		genes.set(index, gene.withWeight(RNG.nextDouble() * genome.networkParameters().range() * 2
			- genome.networkParameters().range()));
		return this;// .025
	}
	
	/**
	 * Adds a neuron
	 */
	public NeuralIndividual mutateNeuron()
	{
		var genes = genome.genes();
		if(genes.size() == 0)
			return this;
		var index = RNG.nextInt(genes.size());
		var gene = genes.get(index);
		if(!gene.enabled())
		{
			return this;
		}
		// disconnect the old link and invent a new node into existence
		genes.set(index, gene.withEnabled(false));
		var neurons = genome.neurons();
		genome.neurons(neurons + 1);
		
		var firstEnd = new NeuralGene(gene.from(), neurons, 1, true,
			tracker.getMarker(gene.from(), neurons));
		var secondEnd = new NeuralGene(neurons, gene.to(), gene.weight(), true,
			tracker.getMarker(neurons, gene.to()));
		genes.add(firstEnd);
		genes.add(secondEnd);
		return this;// 0.5
	}
	
	/**
	 * Enables a link
	 */
	public NeuralIndividual mutateEnable()
	{
		return mutateActivity(true);// .2
	}
	
	/**
	 * Disables a link
	 */
	public NeuralIndividual mutateDisable()
	{
		return mutateActivity(false);// .4
	}
	
	private NeuralIndividual mutateActivity(boolean target)
	{
		record Location(int index, NeuralGene gene)
		{
		}
		var pairs = new ArrayList<Location>();
		var genes = this.genome.genes();
		var iter = genes.iterator();
		int idx = 0;
		// Just in case the backing for genes() ever changes to LinkedList
		while(iter.hasNext())
		{
			var gene = iter.next();
			if(gene.enabled() != target)
			{
				pairs.add(new Location(idx, gene));
			}
			idx++;
		}
		if(pairs.isEmpty())
		{
			return this;
		}
		var flipping = pairs.get(RNG.nextInt(pairs.size()));
		genes.set(flipping.index, flipping.gene.withEnabled(target));
		return this;
	}
	
	private NeuralIndividual mutateDestroy()
	{
		var disabled = new ArrayList<Integer>();
		var genes = this.genome.genes();
		var iter = genes.iterator();
		int idx = 0;
		// Just in case the backing for genes() ever changes to LinkedList
		while(iter.hasNext())
		{
			var gene = iter.next();
			if(!gene.enabled() || gene.enabled())
			{
				disabled.add(idx);
			}
			idx++;
		}
		if(disabled.isEmpty())
		{
			return this;
		}
		genes.remove((int) disabled.get(RNG.nextInt(disabled.size())));
		return this;
	}
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	public String toString()
	{
		return String.format("NeuralIndividual[genome=%s, fitness=%s]", genome, fitness);
	}
	
	public static class Builder
	{
		private int set = 0b000;
		private InnovationTracker tracker;
		private NetworkParameters networkParameters;
		private IndividualParameters individualParameters;
		
		private Builder()
		{
		}
		
		public Builder withInnovationTracker(InnovationTracker tracker)
		{
			set |= 0b100;
			this.tracker = tracker;
			return this;
		}
		
		public Builder withNetworkParameters(NetworkParameters networkParameters)
		{
			set |= 0b010;
			this.networkParameters = networkParameters;
			return this;
		}
		
		public Builder withIndividualParameters(IndividualParameters individualParameters)
		{
			set |= 0b001;
			this.individualParameters = individualParameters;
			return this;
		}
		
		public NeuralIndividual build()
		{
			if((set & 0b100) != 0b100)
				throw new IllegalStateException("Innovation tracker not set!");
			if((set & 0b010) != 0b010)
				throw new IllegalStateException("Network parameters not set!");
			if((set & 0b001) != 0b001)
				throw new IllegalStateException("Individual parameters not set!");
			return new NeuralIndividual(tracker, networkParameters, individualParameters);
		}
	}
}
