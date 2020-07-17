package neat;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import genetic.genome.Genome;
import network.Network;
import network.neuron.Neuron;

public class NeuralGenome implements Genome
{
	private ArrayList<NeuralGene> genes;
	private NetworkParameters networkParameters;
	private int neurons;
	
	public NeuralGenome(NeuralGenome other)
	{
		this.genes = new ArrayList<>(other.genes);
		this.networkParameters = other.networkParameters;
		this.neurons = other.neurons;
	}
	
	public NeuralGenome(NetworkParameters networkParameters)
	{
		this.genes = new ArrayList<>();
		this.networkParameters = networkParameters;
		this.neurons = inputs() + outputs() + biases();
	}
	
	public List<NeuralGene> genes()
	{
		return genes;
	}
	
	public int inputs()
	{
		return this.networkParameters.inputs();
	}
	
	public int outputs()
	{
		return this.networkParameters.outputs();
	}
	
	public int biases()
	{
		return this.networkParameters.biases();
	}
	
	public boolean recurrent()
	{
		return this.networkParameters.recurrent();
	}
	
	public int neurons()
	{
		return neurons;
	}
	
	public void neurons(int neurons)
	{
		this.neurons = neurons;
	}
	
	public NetworkParameters networkParameters()
	{
		return networkParameters;
	}
	
	public NeuralGenome copy()
	{
		return new NeuralGenome(this);
	}
	
	public Network toNetwork(Supplier<Neuron> supplier)
	{
		var network = new Network(inputs(), outputs(), biases(), recurrent());
		var map = new HashMap<Integer, Integer>();
		IntStream.range(0, inputs() + outputs() + biases()).forEach(x -> map.put(x, x));
		genes.stream().filter(NeuralGene::enabled).forEach(gene -> 
		{
			if(gene.enabled())
			{
				if(!map.containsKey(gene.from()))
				{
					map.put(gene.from(),network.addNeuron(supplier.get()));
				}
				if(!map.containsKey(gene.to()))
				{
					map.put(gene.to(),network.addNeuron(supplier.get()));
				}
				network.addConnection(map.get(gene.from()), map.get(gene.to()), gene.weight());
			}
		});
		return network;
	}
	
	public String toString()
	{
		return String.format("NeuralGenome[genes=%s, inputs=%s, outputs=%s, biases=%s, neurons=%s, recurrent=%s]", genes, inputs(), outputs(), biases(), neurons, recurrent());
	}
}