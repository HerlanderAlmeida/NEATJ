package neat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import utils.Exclude;

public class NeuralIndividual extends SpeciesIndividual<Double>
{
	private static final Random random = new Random();
	private NeuralGenome genome;
	@Exclude
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
		this.genome = new NeuralGenome(networkParameters);
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

	@Override
	public Double fitness()
	{
		return this.fitness;
	}

	@Override
	public void fitness(Double fitness)
	{
		this.fitness = fitness;
	}

	@Override
	public void divideFitness(double sharers)
	{
		this.fitness /= sharers;
	}

	public IndividualParameters individualParameters()
	{
		return this.individualParameters;
	}

	public InnovationTracker tracker()
	{
		return this.tracker;
	}

	public void tracker(InnovationTracker tracker)
	{
		this.tracker = tracker;
	}

	/**
	 * Computes genomic distance between two NeuralIndividuals
	 */
	@Override
	public Double difference(SpeciesIndividual<Double> di, SpeciationParameters parameters)
	{
		if(di instanceof NeuralIndividual other)
		{
			var genome1 = this.genome;
			var genome2 = other.genome;
			Collections.sort(genome1.genes(), Comparator.comparingLong(NeuralGene::marker));
			Collections.sort(genome2.genes(), Comparator.comparingLong(NeuralGene::marker));
			var iter1 = genome1.genes().iterator();
			var iter2 = genome2.genes().iterator();
			var gene1 = iter1.hasNext() ? iter1.next() : null;
			var gene2 = iter2.hasNext() ? iter2.next() : null;
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
			{
				excess++;
			}
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
				+ parameters.weightDifferenceCoefficient()
					* Math.pow(weightDiffs, parameters.weightDifferencePower())
					/ Math.max(1, joint);
		}
		else
		{
			throw new IllegalArgumentException(
				"Arbitrary DifferentialIndividual distance not supported!");
		}
	}

	public NeuralIndividual crossover(NeuralIndividual other)
	{
		var genome1 = this.genome;
		var genome2 = other.genome;
		var evaluation1 = this.fitness;
		var evaluation2 = other.fitness;
		var individualParameters = this.fitness > other.fitness ? this.individualParameters
			: this.fitness < other.fitness ? other.individualParameters
				: random.nextBoolean() ? this.individualParameters : other.individualParameters;
		var child = new NeuralIndividual(this.tracker, this.genome.networkParameters(),
			individualParameters.copy());
		Collections.sort(genome1.genes(), Comparator.comparingLong(NeuralGene::marker));
		Collections.sort(genome2.genes(), Comparator.comparingLong(NeuralGene::marker));
		var iter1 = genome1.genes().iterator();
		var iter2 = genome2.genes().iterator();
		var gene1 = iter1.hasNext() ? iter1.next() : null;
		var gene2 = iter2.hasNext() ? iter2.next() : null;
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
				var next = random.nextBoolean() ? gene1 : gene2;
				var enabled = random.nextBoolean() ? gene1.enabled() : gene2.enabled();
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
		else if(evaluation2 > evaluation1)
		{
			joint.addAll(exclusive2);
		}
		else if(this.genome.recurrent())
		{
			for(var gene : exclusive1)
			{
				if(random.nextBoolean())
				{
					joint.add(gene);
				}
			}
			for(var gene : exclusive2)
			{
				if(random.nextBoolean())
				{
					joint.add(gene);
				}
			}
		}
		else if(random.nextBoolean())
		{
			joint.addAll(exclusive1);
		}
		else
		{
			joint.addAll(exclusive2);
		}
		// max known input/output/bias node
		var maxIOB = this.genome.inputs() + this.genome.outputs() + this.genome.biases() - 1;
		// scan all nodes, finding useless ones, and throw them away
		var froms = new HashSet<Integer>();
		var tos = new HashSet<Integer>();
		for(var gene : joint)
		{
			if(gene.enabled() && gene.from() > maxIOB)
			{
				froms.add(gene.from());
			}
			if(gene.enabled() && gene.to() > maxIOB)
			{
				tos.add(gene.to());
			}
		}
		froms.retainAll(tos);
		joint.removeIf(gene -> gene.from() > maxIOB && !froms.contains(gene.from())
			|| gene.to() > maxIOB && !froms.contains(gene.to()));
		// max known input/output/bias/hidden node
		var maxIOBH = maxIOB;
		for(var gene : joint)
		{
			maxIOBH = Math.max(maxIOBH, Math.max(gene.from(), gene.to()));
		}
		child.genome.neurons(maxIOBH + 1);
		return child;
	}

	public NeuralIndividual mutateCrossover()
	{
		this.applyMutation(this.individualParameters::crossoverMutationProbability,
			NeuralIndividual::mutateComprehensively);
		return this;
	}

	public NeuralIndividual mutateCloning()
	{
		this.applyMutation(this.individualParameters::cloningMutationProbability,
			NeuralIndividual::mutateComprehensively);
		return this;
	}

	public NeuralIndividual mutateComprehensively()
	{
		if(this.individualParameters.canMutate())
		{
			this.individualParameters = this.individualParameters.mutateProbabilities(random);
		}
		this.applyMutation(this.individualParameters::weightMutationProbability,
			NeuralIndividual::mutateWeight);
		this.applyMutation(this.individualParameters::randomWeightMutationProbability,
			NeuralIndividual::mutateRandomWeight);
		this.applyMutation(this.individualParameters::linkMutationProbability,
			NeuralIndividual::mutateLink);
		this.applyMutation(this.individualParameters::biasLinkMutationProbability,
			NeuralIndividual::mutateBiasLink);
		this.applyMutation(this.individualParameters::sensorMutationProbability,
			NeuralIndividual::mutateSensor);
		this.applyMutation(this.individualParameters::neuronMutationProbability,
			NeuralIndividual::mutateNeuron);
		this.applyMutation(this.individualParameters::enableMutationProbability,
			NeuralIndividual::mutateEnable);
		this.applyMutation(this.individualParameters::disableMutationProbability,
			NeuralIndividual::mutateDisable);
		this.applyMutation(this.individualParameters::destroyMutationProbability,
			NeuralIndividual::mutateDestroy);
		return this;
	}

	private void applyMutation(Supplier<Double> probability, Consumer<NeuralIndividual> mutation)
	{
		for(var chance = probability.get(); chance > 0; chance--)
		{
			if(chance >= 1 || random.nextDouble() < chance)
			{
				mutation.accept(this);
			}
		}
	}

	/**
	 * Adds a link
	 */
	public NeuralIndividual mutateLink()
	{
		var neurons = this.genome.neurons();
		var inputs = this.genome.inputs();
		var outputs = this.genome.outputs();
		var biases = this.genome.biases();
		var unchanging = inputs + biases;
		// always input, bias, or hidden
		var first = random.nextInt(neurons - outputs);
		// always output or hidden
		var second = random.nextInt(neurons - unchanging);
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
		{
			throw new IllegalStateException(
				"Unexpectly, output node selected as link source: " + first);
		}
		if(isBias.test(second))
		{
			throw new IllegalStateException(
				"Unexpectly, bias node selected as link destination: " + second);
		}
		if(isInput.test(second))
		{
			throw new IllegalStateException(
				"Unexpectly, input node selected as link destination: " + second);
		}
		if(first == second)
		{
			return this;
		}
		/*
		 * Putting this check first can help mitigate the slow recurrent check.
		 * It's far more likely to hit if the network gets dense, which is
		 * exactly when checking recurrency is slow.
		 */
		if(this.genome.hasConnection(second, first))
		{
			var temp = first;
			first = second;
			second = temp;
		}
		var genes = this.genome.getConnections(first, second);
		if(genes.size() != 0)
		{
			var index = random.nextInt(genes.size());
			var gene = genes.get(index);
			genes.set(index,
				gene.withWeight(random.nextDouble() * this.genome.networkParameters().range() * 2
					- this.genome.networkParameters().range()));
			return this;
		}
		if(!this.genome.recurrent() && !isInput.test(first) && !isBias.test(first)
			&& !isOutput.test(second))
		{
			// need to preserve DAG-ness of the neural network
			if(this.isRecurrent(first, second))
			{
				var temp = first;
				first = second;
				second = temp;
			}
		}
		this.genome.addConnection(first, second, this.tracker);
		return this;// .9, try twice
	}

	// help, this function is extremely expensive!!!
	public boolean isRecurrent(int from, int to)
	{
		var sources = this.genome.genes().stream().collect(Collectors.groupingBy(NeuralGene::to));
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
		var neurons = this.genome.neurons();
		var inputs = this.genome.inputs();
		var outputs = this.genome.outputs();
		var biases = this.genome.biases();
		var unchanging = inputs + biases;
		if(biases == 0)
		{
			return this;
		}
		// always bias
		var first = random.nextInt(biases);
		// always output or hidden
		var second = random.nextInt(neurons - unchanging);
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
		{
			throw new IllegalStateException(
				"Unexpectedly, non-bias node selected as link source: " + first);
		}
		if(isBias.test(second))
		{
			throw new IllegalStateException(
				"Unexpectly, bias node selected as link destination: " + second);
		}
		if(isInput.test(second))
		{
			throw new IllegalStateException(
				"Unexpectly, input node selected as link destination: " + second);
		}
		if(this.genome.hasConnection(first, second))
		{
			return this;
		}
		this.genome.addConnection(first, second, this.tracker);
		return this;// .4
	}

	public NeuralIndividual mutateSensor()
	{
		var neurons = this.genome.neurons();
		var inputs = this.genome.inputs();
		var outputs = this.genome.outputs();
		// always input, bias, or hidden
		var first = random.nextInt(neurons - outputs);
		// always output
		var second = random.nextInt(outputs);
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
		{
			throw new IllegalStateException(
				"Unexpectly, output node selected as link source: " + first);
		}
		if(!isOutput.test(second))
		{
			throw new IllegalStateException(
				"Unexpectly, non-output node selected as link destination: " + second);
		}
		if(this.genome.hasConnection(first, second))
		{
			return this;
		}
		this.genome.addConnection(first, second, this.tracker);
		return this; // .4
	}

	/**
	 * Changes a weight
	 */
	public NeuralIndividual mutateWeight()
	{
		var genes = this.genome.genes();
		for(var index = 0; index < genes.size(); index++)
		{
			var gene = genes.get(index);
			genes.set(index,
				gene.withWeight(
					gene.weight() + random.nextDouble() * this.genome.networkParameters().step() * 2
						- this.genome.networkParameters().step()));
		}
		return this;// 0.225
	}

	/**
	 * Randomizes a weight
	 */
	public NeuralIndividual mutateRandomWeight()
	{
		var genes = this.genome.genes();
		if(genes.size() == 0)
		{
			return this;
		}
		var index = random.nextInt(genes.size());
		var gene = genes.get(index);
		genes.set(index,
			gene.withWeight(random.nextDouble() * this.genome.networkParameters().range() * 2
				- this.genome.networkParameters().range()));
		return this;// .025
	}

	/**
	 * Adds a neuron
	 */
	public NeuralIndividual mutateNeuron()
	{
		var genes = this.genome.genes();
		if(genes.size() == 0)
		{
			return this;
		}
		var index = random.nextInt(genes.size());
		var gene = genes.get(index);
		if(!gene.enabled())
		{
			return this;
		}
		// disconnect the old link and invent a new node into existence
		genes.set(index, gene.withEnabled(false));
		var neurons = this.genome.neurons();
		this.genome.neurons(neurons + 1);
		this.genome.addConnection(gene.from(), neurons, 1, this.tracker);
		this.genome.addConnection(neurons, gene.to(), gene.weight(), this.tracker);
		return this;// 0.5
	}

	private record Location(int index, NeuralGene gene)
	{
	}

	/**
	 * Enables a link
	 */
	public NeuralIndividual mutateEnable()
	{
		var pairs = new ArrayList<Location>();
		var genes = this.genome.genes();
		var iter = genes.iterator();
		var idx = 0;
		// Just in case the backing for genes() ever changes to LinkedList
		while(iter.hasNext())
		{
			var gene = iter.next();
			if(!gene.enabled())
			{
				pairs.add(new Location(idx, gene));
			}
			idx++;
		}
		if(pairs.isEmpty())
		{
			return this;
		}
		var flipping = pairs.get(random.nextInt(pairs.size()));
		genes.set(flipping.index, flipping.gene.withEnabled(true));
		return this;
	}

	/**
	 * Disables a link. We don't want to disable a link if there are no other
	 * outputs taken from the node.
	 */
	public NeuralIndividual mutateDisable()
	{
		var pairs = new ArrayList<Location>();
		var genes = this.genome.genes();
		var iter = genes.iterator();
		var idx = 0;
		// Just in case the backing for genes() ever changes to LinkedList
		while(iter.hasNext())
		{
			var gene = iter.next();
			if(gene.enabled())
			{
				pairs.add(new Location(idx, gene));
			}
			idx++;
		}
		if(pairs.isEmpty())
		{
			return this;
		}
		var flipping = pairs.get(random.nextInt(pairs.size()));
		for(var gene : genes)
		{
			if(gene.enabled() && gene.from() == flipping.gene.from()
				&& gene.marker() != flipping.gene.marker())
			{
				genes.set(flipping.index, flipping.gene.withEnabled(false));
				return this;
			}
		}
		return this;
	}

	public NeuralIndividual mutateDestroy()
	{
		var disabled = new ArrayList<Integer>();
		var genes = this.genome.genes();
		var iter = genes.iterator();
		var idx = 0;
		// Just in case the backing for genes() ever changes to LinkedList
		while(iter.hasNext())
		{
			var gene = iter.next();
			if(!gene.enabled())
			{
				disabled.add(idx);
			}
			idx++;
		}
		if(disabled.isEmpty())
		{
			return this;
		}
		genes.remove((int) disabled.get(random.nextInt(disabled.size())));
		return this;
	}

	public static Builder builder()
	{
		return new Builder();
	}

	@Override
	public String toString()
	{
		return String.format("NeuralIndividual[genome=%s, fitness=%s]", this.genome, this.fitness);
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
			this.set |= 0b100;
			this.tracker = tracker;
			return this;
		}

		public Builder withNetworkParameters(NetworkParameters networkParameters)
		{
			this.set |= 0b010;
			this.networkParameters = networkParameters;
			return this;
		}

		public Builder withIndividualParameters(IndividualParameters individualParameters)
		{
			this.set |= 0b001;
			this.individualParameters = individualParameters;
			return this;
		}

		public NeuralIndividual build()
		{
			if((this.set & 0b100) != 0b100)
			{
				throw new IllegalStateException("Innovation tracker not set!");
			}
			if((this.set & 0b010) != 0b010)
			{
				throw new IllegalStateException("Network parameters not set!");
			}
			if((this.set & 0b001) != 0b001)
			{
				throw new IllegalStateException("Individual parameters not set!");
			}
			var ret = new NeuralIndividual(this.tracker, this.networkParameters,
				this.individualParameters);
			if(this.networkParameters.fullyConnected()
				|| this.networkParameters.arbitrarilyConnected() && random.nextBoolean())
			{
				ret.genome().becomeFullyConnected(this.tracker);
			}
			return ret;
		}
	}
}
