package network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import network.connection.Connection;
import network.neuron.Neuron;

public class Network
{
	private static final Gson gson = new Gson();
	
	private List<Neuron> inputs;
	private List<Neuron> outputs;
	private List<Neuron> biases;
	private List<Neuron> hidden;
	private List<Neuron> neurons;
	private boolean recurrent;
	
	/**
	 * @param inputs Number of input neurons for the network
	 * @param outputs Number of output neurons for the network
	 * @param biases Number of bias neurons for the network
	 * @param recurrent Whether the network supports recurrent evaluation
	 */
	public Network(int inputs, int outputs, int biases, boolean recurrent)
	{
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.biases = new ArrayList<>();
		this.hidden = new ArrayList<>();
		this.neurons = new ArrayList<>();
		Stream.generate(Neuron::newInput).limit(inputs).forEach(this::addNeuron);
		Stream.generate(Neuron::newOutput).limit(outputs).forEach(this::addNeuron);
		Stream.generate(Neuron::newBias).limit(biases).forEach(this::addNeuron);
		this.recurrent = recurrent;
	}
	
	public Network(String filename)
	{
		try (var reader = new JsonReader(new BufferedReader(new FileReader(filename))))
		{
			var net = (Network) gson.fromJson(reader, Network.class);
			this.inputs = net.inputs;
			this.outputs = net.outputs;
			this.biases = net.biases;
			this.hidden = net.hidden;
			this.recurrent = net.recurrent;
		}
		catch(IOException e)
		{
			var exc = new IllegalStateException("Failed to initialize network!");
			exc.initCause(e);
			throw exc;
		}
	}
	
	public double[] evaluate(int[] inputs)
	{
		return evaluate(Arrays.stream(inputs).asDoubleStream().toArray());
	}
	
	public double[] evaluate(double[] inputs)
	{
		if(recurrent)
			return evaluateRecurrent(inputs);
		else
			return evaluateOnce(inputs);
	}
	
	private double[] evaluateRecurrent(double[] inputs)
	{
		throw new UnsupportedOperationException("Operation not implemented yet!");
	}
	
	private double[] evaluateOnce(double[] inputValues)
	{
		if(inputValues.length != numInputs())
		{
			throw new IllegalArgumentException(String.format(
				"Invalid number of input values: expected %d, found %d", numInputs(), inputValues.length));
		}
		var index = 0;
		reset(); // for safety
		for(var input : inputs)
		{
			input.value(inputValues[index++]);
			input.flag();
		}
		for(var bias : biases)
		{
			bias.value(1);
			bias.flag();
		}
		var waiting = new ArrayDeque<Neuron>();
		for(var output : outputs)
		{
			waiting.offer(output);
		}
		while(!waiting.isEmpty())
		{
			var current = waiting.poll();
			if(current.isFlagged())
				continue;
			var size = waiting.size();
			for(var connection : current.allInputs())
			{
				var input = connection.input();
				if(!input.isFlagged())
				{
					waiting.offer(input);
				}
			}
			if(size == waiting.size())
			{
				current.update();
				current.flag();
			}
			else
			{
				waiting.offer(current);
			}
		}
		var outputValues = new double[outputs.size()];
		index = 0;
		for(var output : outputs)
		{
			outputValues[index++] = output.value();
		}
		reset(); // don't leave behind residue
		return outputValues;
	}
	
	public void reset()
	{
		for(var neuron : neurons)
		{
			neuron.unflag();
			neuron.value(0);
		}
	}
	
	public int numInputs()
	{
		return inputs.size();
	}
	
	public int numOutputs()
	{
		return outputs.size();
	}
	
	public int numBiases()
	{
		return biases.size();
	}
	
	public int addNeuron(Neuron n)
	{
		switch (n.type())
		{
		case BIAS:
			this.biases.add(n);
			break;
		case HIDDEN:
			this.hidden.add(n);
			break;
		case INPUT:
			this.inputs.add(n);
			break;
		case OUTPUT:
			this.outputs.add(n);
			break;
		default:
			throw new IllegalArgumentException("Unsupported neuron type: " + n.type());
		}
		this.neurons.add(n);
		return this.neurons.size() - 1;
	}
	
	public void addConnection(int from, int to, double weight)
	{
		this.neurons.get(to).addConnection(new Connection(this.neurons.get(from), weight));
	}
	
	public String toJson()
	{
		return gson.toJson(this);
	}
	
	public String toString()
	{
		return String.format("Network[inputs=%s, outputs=%s, biases=%s, hidden=%s, neurons=%s, recurrent=%s]", inputs.size(), outputs.size(), biases.size(), hidden.size(), neurons.size(), recurrent);
	}
}
