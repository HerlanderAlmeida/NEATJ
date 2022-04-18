package network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
		throw new UnsupportedOperationException("Recurrent evaluation not yet supported!");
	}

	private Set<Neuron> disconnectChildren(Neuron neuron)
	{
		var outputs = neuron.outputs().keySet();
		var result = Set.copyOf(outputs);
		for(var output : result)
		{
			neuron.dropOutput(output);
		}
		outputs.clear();
		return result;
	}

	private double[] evaluateOnce(double[] inputValues)
	{
		if(inputValues.length != numInputs())
		{
			throw new IllegalArgumentException(
				String.format("Invalid number of input values: expected %d, found %d", numInputs(),
					inputValues.length));
		}
		record Connection(Neuron input, Neuron output, double weight)
		{
		}

		record NeuronDegree(Neuron neuron, int degree)
		{
			public NeuronDegree(Neuron neuron)
			{
				this(neuron, neuron.inputs().size());
			}
		}

		reset(); // for safety
		var indices = new HashMap<Neuron, Integer>();
		var index = 0;
		for(var neuron : this.neurons)
		{
			indices.put(neuron, index++);
		}
		var knownConnections = new HashSet<Connection>();
		for(var input : this.neurons)
		{
			for(var entry : input.outputs().entrySet())
			{
				knownConnections.add(new Connection(input, entry.getKey(), entry.getValue()));
			}
		}
		var order = new LinkedList<Neuron>();
		var indeterminate = new TreeSet<Neuron>(Comparator.comparing(indices::get));
		var nextQ = new TreeSet<NeuronDegree>(
			Comparator.<NeuronDegree, Integer>comparing(n -> n.degree())
				.thenComparing(n -> indices.get(n.neuron())));
		var next = new HashSet<Neuron>();
		index = 0;
		for(var input : this.inputs)
		{
			input.value(inputValues[index++]);
			input.flag();
			indeterminate.addAll(this.disconnectChildren(input));
		}
		for(var bias : this.biases)
		{
			bias.value(1);
			bias.flag();
			indeterminate.addAll(this.disconnectChildren(bias));
		}
		indeterminate.forEach(
			neuron -> neuron.inputs().keySet().removeIf(Predicate.not(indeterminate::contains)));
		while(!indeterminate.isEmpty())
		{
			nextQ.clear();
			for(var neuron : indeterminate)
			{
				neuron.outputs().keySet().removeIf(Neuron::isFlagged);
				nextQ.add(new NeuronDegree(neuron));
			}
			while(!nextQ.isEmpty())
			{
				var bestNeuron = nextQ.pollFirst();
				if(bestNeuron.neuron().isFlagged())
				{
					continue;
				}
				if(bestNeuron != null)
				{
					var neuron = bestNeuron.neuron();
					order.offer(neuron);
					neuron.flag();
					var outputs = neuron.outputs().keySet();
					for(var iter = outputs.iterator(); iter.hasNext();)
					{
						var output = iter.next();
						output.inputs().remove(neuron);
						if(indeterminate.contains(output))
						{
							nextQ.add(new NeuronDegree(output));
						}
						else
						{
							next.add(output);
						}
						iter.remove();
					}
				}
			}
			indeterminate.clear();
			indeterminate.addAll(next);
			next.clear();
		}
		for(var connection : knownConnections)
		{
			connection.input().addOutput(connection.output(), connection.weight());
		}
		order.forEach(Neuron::update);
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
		if(from == to)
		{
			throw new IllegalArgumentException(
				"Connection from %s to %s is not allowed!".formatted(from, to));
		}
		var source = this.neurons.get(from);
		var dest = this.neurons.get(to);
		dest.addInput(source, weight);
		source.addOutput(dest, weight);
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
