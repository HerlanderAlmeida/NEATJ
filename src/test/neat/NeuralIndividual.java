package test.neat;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;

import genetic.Individual;
import genetic.genome.Genome;
import net.Network;

public class NeuralIndividual extends Individual
{
	private static final double RANGE = 2;
	private static final double STEP = RANGE / 16.0;
	private static final Random RNG = new Random();
	private Genome genome;
	private InnovationTracker tracker;
	private double evaluation;
	
	public NeuralIndividual(Object[] o)
	{
		this((int) o[0], (int) o[1], (int) o[2], (boolean) o[3], (InnovationTracker) o[4]);
	}
	
	private NeuralIndividual(int inputs, int outputs, int biases, boolean recurrent, InnovationTracker tracker)
	{
		genome = new NeuralGenome(inputs, outputs, biases, recurrent);
		this.tracker = tracker;
	}
	
	public NeuralIndividual(NeuralIndividual other)
	{
		this.genome = new NeuralGenome(other.genome().copy());
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
	
	public double evaluation()
	{
		return this.evaluation;
	}
	
	public void evaluation(double evaluation)
	{
		this.evaluation = evaluation;
	}
	
	public NeuralIndividual crossover(NeuralIndividual other)
	{
		
		return null;
	}
	
	/**
	 * Adds a link
	 */
	public NeuralIndividual mutateLink()
	{
		var genome = genome();
		var neurons = genome.neurons();
		var inputs = genome.inputs();
		var outputs = genome.outputs();
		var biases = genome.biases();
		var unchanging = inputs + biases;
		var first = RNG.nextInt(neurons - outputs);// always input, bias, or hidden
		var second = RNG.nextInt(neurons - unchanging); // always output or hidden
		var inputEdge = inputs;
		var outputEdge = inputs + outputs;
		var biasEdge = inputs + outputs + biases;
		//network=[inputs|outputs|biases|hidden]
		if(first >= inputs)
		{
			first += outputs;
		}
		if(second >= outputs)
		{
			second += biases;//translate non-outputs to be hidden
		}
		second += inputs;
		Predicate<Integer> isInput = x -> x < inputEdge;
		Predicate<Integer> isOutput = x -> x >= inputEdge && x < outputEdge;
		Predicate<Integer> isBias = x -> x >= outputEdge && x < biasEdge;
		if(isOutput.test(first))
			throw new IllegalStateException("Unexpectly, output node selected as link source: "+first);
		if(isBias.test(second))
			throw new IllegalStateException("Unexpectly, bias node selected as link destination: "+second);
		if(isInput.test(second))
			throw new IllegalStateException("Unexpectly, input node selected as link destination: "+second);
		if(first == second)
		{
			return this;
		}
//		System.out.print("First("+first+") is "+(isInput.test(first)?"input":isBias.test(first)?"bias":isOutput.test(first)?"output":"hidden"));
//		System.out.println(", second("+second+") is "+(isInput.test(second)?"input":isBias.test(second)?"bias":isOutput.test(second)?"output":"hidden"));
		if(!genome.recurrent() && !isInput.test(first) && !isBias.test(first) && !isOutput.test(second))
		{
			if(first > second) // need to preserve DAG-ness of the NN
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
		System.out.println("Successfully added: "+first+"->"+second);
		genome.genes().add(new NeuralGene(first, second, RNG.nextDouble() * RANGE * 2 - RANGE, true, tracker.getMarker(first, second)));
		return this;//.9, try twice
	}
	
	/**
	 * Adds a link starting with a bias node
	 */
	public NeuralIndividual mutateBiasLink()
	{
		var genome = genome();
		var neurons = genome.neurons();
		var inputs = genome.inputs();
		var outputs = genome.outputs();
		var biases = genome.biases();
		var unchanging = inputs + biases;
		var first = RNG.nextInt(biases);// always bias
		var second = RNG.nextInt(neurons - unchanging); // always output or hidden
		var inputEdge = inputs;
		var outputEdge = inputs + outputs;
		var biasEdge = inputs + outputs + biases;
		//network=[inputs|outputs|biases|hidden]
		if(second >= outputs)
		{
			second += biases;//translate non-outputs to be hidden
		}
		first += inputs + outputs;
		second += inputs;
		Predicate<Integer> isInput = x -> x < inputEdge;
		Predicate<Integer> isBias = x -> x >= outputEdge && x < biasEdge;
		if(!isBias.test(first))
			throw new IllegalStateException("Unexpectedly, non-bias node selected as link source: "+first);
		if(isBias.test(second))
			throw new IllegalStateException("Unexpectly, bias node selected as link destination: "+second);
		if(isInput.test(second))
			throw new IllegalStateException("Unexpectly, input node selected as link destination: "+second);
//		System.out.print("First("+first+") is "+(isInput.test(first)?"input":isBias.test(first)?"bias":isOutput.test(first)?"output":"hidden"));
//		System.out.println(", second("+second+") is "+(isInput.test(second)?"input":isBias.test(second)?"bias":isOutput.test(second)?"output":"hidden"));
		for(var gene : genome.genes())
		{
			if(gene.from() == first && gene.to() == second)
			{
				return this;
			}
		}
		genome.genes().add(new NeuralGene(first, second, RNG.nextDouble() * RANGE * 2 - RANGE, true, tracker.getMarker(first, second)));
		return this;//.4
	}
	
	/**
	 * Changes a weight
	 */
	public NeuralIndividual mutateWeight()
	{
		var genes = genome().genes();
		var index = RNG.nextInt(genes.size());
		var gene = genes.get(index);
		genes.set(index, gene.withWeight(gene.weight() + RNG.nextGaussian() * STEP));
		return this;//0.225
	}
	
	/**
	 * Randomizes a weight
	 */
	public NeuralIndividual mutateRandomWeight()
	{
		var genes = genome().genes();
		var index = RNG.nextInt(genes.size());
		var gene = genes.get(index);
		genes.set(index, gene.withWeight(RNG.nextDouble() * RANGE * 2 - RANGE));
		return this;//.025
	}

	/**
	 * Adds a neuron
	 */
	public NeuralIndividual mutateNeuron()
	{
		var genome = genome();
		var genes = genome.genes();
		if(genes.size() == 0)
			return this;
		var index = RNG.nextInt(genes.size());
		var gene = genes.get(index);
		if(!gene.enabled())
		{
			System.out.println("Gene wasn't enabled!");
			return this;
		}
		//disconnect the old link and invent a new node into existence
		genes.set(index,gene.withEnabled(false));
		var neurons = genome.neurons();
		genome.neurons(neurons + 1);
		
		var firstEnd = new NeuralGene(gene.from(), neurons, 1, true, tracker.getMarker(gene.from(), neurons));
		var secondEnd = new NeuralGene(neurons, gene.to(), gene.weight(), true, tracker.getMarker(neurons, gene.to()));
		genes.add(firstEnd);
		genes.add(secondEnd);
		return this;//0.5
	}
	
	/**
	 * Enables a link
	 */
	public NeuralIndividual mutateEnable()
	{
		return mutateActivity(true);//.2
	}
	
	/**
	 * Disables a link
	 */
	public NeuralIndividual mutateDisable()
	{
		return mutateActivity(false);//.4
	}
	
	private NeuralIndividual mutateActivity(boolean target)
	{
		record Location(int index, NeuralGene gene){}
		var pairs = new ArrayList<Location>();
		var genes = genome().genes();
		var iter = genes.iterator();
		int idx = 0;
		while(iter.hasNext()) //Just in case the backing for genes() ever changes to LinkedList
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
		genes.set(flipping.index(), flipping.gene().withEnabled(target));
		return this;
	}
	
	public Network phenotype()
	{
		return genome().toNetwork();
	}
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	public String toString()
	{
		return String.format("NeuralIndividual[genome=%s, evaluation=%s]", genome, evaluation);
	}
	
	public static class Builder
	{
		private int set = 0b00000;
		private int inputs;
		private int outputs;
		private int biases;
		private boolean recurrent;
		private InnovationTracker tracker;
		
		public Builder withInputs(int inputs)
		{
			set |= 0b00001; 
			this.inputs = inputs;
			return this;
		}
		
		public Builder withOutputs(int outputs)
		{
			set |= 0b00010;
			this.outputs = outputs;
			return this;
		}
		
		public Builder withBiases(int biases)
		{
			set |= 0b00100;
			this.biases = biases;
			return this;
		}
		
		public Builder withRecurrency(boolean recurrency)
		{
			set |= 0b01000; 
			this.recurrent = recurrency;
			return this;
		}
		
		public Builder withInnovationTracker(InnovationTracker tracker)
		{
			set |= 0b10000;
			this.tracker = tracker;
			return this;
		}
		
		public NeuralIndividual build()
		{
			if((set & 0b10000) != 0b10000)
				throw new IllegalStateException("Innovation tracker not set!");
			if((set & 0b01000) != 0b01000)
				throw new IllegalStateException("Recurrency not set!");
			if((set & 0b00100) != 0b00100)
				throw new IllegalStateException("Number of biases not set!");
			if((set & 0b00010) != 0b00010)
				throw new IllegalStateException("Number of outputs not set!");
			if((set & 0b00001) != 0b00001)
				throw new IllegalStateException("Number of inputs not set!");
			return new NeuralIndividual(inputs, outputs, biases, recurrent, tracker);
		}
	}
}
