package network;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import network.connection.Connection;
import network.neuron.Neuron;
import utils.GsonUtils;

public class Network
{
	private List<Neuron> inputs;
	private List<Neuron> outputs;
	private List<Neuron> biases;
	private List<Neuron> hidden;
	private List<Neuron> neurons;
	private boolean recurrent;

	/**
	 * @param inputs
	 *            Number of input neurons for the network
	 * @param outputs
	 *            Number of output neurons for the network
	 * @param biases
	 *            Number of bias neurons for the network
	 * @param recurrent
	 *            Whether the network supports recurrent evaluation
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

	public Network(String json)
	{
		var net = GsonUtils.fromJson(json, Network.class);
		this.inputs = net.inputs;
		this.outputs = net.outputs;
		this.biases = net.biases;
		this.hidden = net.hidden;
		this.recurrent = net.recurrent;
	}

	public double[] evaluate(int[] inputs)
	{
		return evaluate(Arrays.stream(inputs).asDoubleStream().toArray());
	}

	public double[] evaluate(double[] inputs)
	{
		if(this.recurrent)
		{
			return evaluateRecurrent(inputs);
		}
		else
		{
			return evaluateOnce(inputs);
		}
	}

	private double[] evaluateRecurrent(double[] inputValues)
	{
		if(inputValues.length != numInputs())
		{
			throw new IllegalArgumentException(
				String.format("Invalid number of input values: expected %d, found %d", numInputs(),
					inputValues.length));
		}
		var index = 0;
		for(var input : this.inputs)
		{
			input.value(inputValues[index++]);
		}
		for(var bias : this.biases)
		{
			bias.value(1);
		}
		for(var hidden : this.hidden)
		{
			hidden.update();
		}
		for(var output : this.outputs)
		{
			output.update();
		}
		var outputValues = new double[this.outputs.size()];
		index = 0;
		for(var output : this.outputs)
		{
			outputValues[index++] = output.value();
		}
		return outputValues;
	}

	private double[] evaluateOnce(double[] inputValues)
	{
		if(inputValues.length != numInputs())
		{
			throw new IllegalArgumentException(
				String.format("Invalid number of input values: expected %d, found %d", numInputs(),
					inputValues.length));
		}
		var index = 0;
		reset(); // for safety
		for(var input : this.inputs)
		{
			input.value(inputValues[index++]);
			input.flag();
		}
		for(var bias : this.biases)
		{
			bias.value(1);
			bias.flag();
		}
		var waiting = new ArrayDeque<Neuron>();
		for(var output : this.outputs)
		{
			waiting.offer(output);
		}
		while(!waiting.isEmpty())
		{
			var current = waiting.poll();
			if(current.isFlagged())
			{
				continue;
			}
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
		var outputValues = new double[this.outputs.size()];
		index = 0;
		for(var output : this.outputs)
		{
			outputValues[index++] = output.value();
		}
		reset(); // don't leave behind residue
		return outputValues;
	}

	public void reset()
	{
		for(var neuron : this.neurons)
		{
			neuron.unflag();
			neuron.value(0);
		}
	}

	public int numInputs()
	{
		return this.inputs.size();
	}

	public int numOutputs()
	{
		return this.outputs.size();
	}

	public int numBiases()
	{
		return this.biases.size();
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
		return GsonUtils.toJson(this);
	}

	@Override
	public String toString()
	{
		return String.format(
			"Network[inputs=%s, outputs=%s, biases=%s, hidden=%s, neurons=%s, recurrent=%s]",
			this.inputs.size(), this.outputs.size(), this.biases.size(), this.hidden.size(),
			this.neurons.size(), this.recurrent);
	}
}
