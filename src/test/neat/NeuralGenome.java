package test.neat;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import genetic.genome.Genome;
import net.Network;
import net.neuron.Neuron;

public class NeuralGenome implements Genome
{
	private ArrayList<NeuralGene> genes;
	private int inputs;
	private int outputs;
	private int biases;
	private boolean recurrent;
	private int neurons;
	
	public NeuralGenome(NeuralGenome other)
	{
		this.genes = new ArrayList<>(other.genes());
		this.inputs = other.inputs;
		this.outputs = other.outputs;
		this.biases = other.biases;
		this.recurrent = other.recurrent;
		this.neurons = other.neurons;
	}
	
	public NeuralGenome(int inputs, int outputs, int biases, boolean recurrent)
	{
		this.genes = new ArrayList<>();
		this.inputs = inputs;
		this.outputs = outputs;
		this.biases = biases;
		this.recurrent = recurrent;
		this.neurons = inputs + outputs + biases;
	}
	
	public List<NeuralGene> genes()
	{
		return genes;
	}
	
	public boolean recurrent()
	{
		return recurrent;
	}
	
	public int neurons()
	{
		return neurons;
	}
	
	public int inputs()
	{
		return inputs;
	}
	
	public int outputs()
	{
		return outputs;
	}
	
	public int biases()
	{
		return biases;
	}
	
	public void neurons(int neurons)
	{
		this.neurons = neurons;
	}
	
	public NeuralGenome copy()
	{
		return new NeuralGenome(this);
	}
	
	public Network toNetwork()
	{
		var network = new Network(inputs, outputs, biases, recurrent);
		var map = new HashMap<Integer, Integer>();
		IntStream.range(0, inputs + outputs + biases).forEach(x -> map.put(x, x));
		for(var gene : genes)
		{
			if(gene.enabled())
			{
				if(!map.containsKey(gene.from()))
				{
					map.put(gene.from(),network.addNeuron(new Neuron()));
				}
				if(!map.containsKey(gene.to()))
				{
					map.put(gene.to(),network.addNeuron(new Neuron()));
				}
				network.addConnection(map.get(gene.from()), map.get(gene.to()), gene.weight());
			}
		}
		return network;
	}
	
	public String toString()
	{
		return String.format("NeuralGenome[genes=%s, inputs=%s, outputs=%s, biases=%s, neurons=%s, recurrent=%s]", genes, inputs, outputs, biases, neurons, recurrent);
	}
}