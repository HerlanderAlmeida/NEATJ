package net;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import net.connection.Connection;
import net.neuron.Neuron;

public class Network
{
	private static final Gson gson = new Gson();
	
	private List<Neuron> inputs;
	private List<Neuron> outputs;
	private List<Neuron> biases;
	private List<Neuron> hidden;
	private List<Neuron> neurons;
	private boolean recurrent;
	
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
	
	public double[] evaluate(double[] inputs)
	{
		if(recurrent)
			return evaluateRecurrent(inputs);
		else
			return evaluateFeedforward(inputs);
	}
	
	public double[] evaluateRecurrent(double[] inputs)
	{
		throw new UnsupportedOperationException("Operation not implemented yet!");
	}
	
	public double[] evaluateFeedforward(double[] inputs)
	{
		if(inputs.length != numInputs())
		{
			throw new IllegalArgumentException(String.format(
				"Invalid number of input values: %d; Expected: %d", inputs.length, numInputs()));
		}
		throw new UnsupportedOperationException("Operation not implemented yet!");
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
