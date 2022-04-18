package network.neuron;

import java.util.HashMap;
import java.util.Map;

public class Neuron
{
	private Map<Neuron, Double> inputs;
	private Map<Neuron, Double> outputs;
	private double value;
	private boolean flagged; // relatively useful flag for any NN processing
	private Type type = Type.HIDDEN;

	public Neuron()
	{
		this.inputs = new HashMap<>();
		this.outputs = new HashMap<>();
	}

	private boolean swapFlag(boolean new_)
	{
		var old = this.flagged;
		this.flagged = new_;
		return old;
	}

	public boolean isInput()
	{
		return this.type == Type.INPUT;
	}

	public boolean isOutput()
	{
		return this.type == Type.OUTPUT;
	}

	public boolean isBias()
	{
		return this.type == Type.BIAS;
	}

	public boolean isHidden()
	{
		return this.type == Type.HIDDEN;
	}

	public static Neuron newInput()
	{
		return new Neuron().withType(Type.INPUT);
	}

	public static Neuron newOutput()
	{
		return new Neuron().withType(Type.OUTPUT);
	}

	public static Neuron newBias()
	{
		return new Neuron().withType(Type.BIAS);
	}

	public static Neuron newHidden()
	{
		return new Neuron().withType(Type.HIDDEN);
	}

	public Neuron withType(Type type)
	{
		this.type = type;
		return this;
	}

	public void addInput(Neuron input, double weight)
	{
		if(this.isBias() || this.isInput())
		{
			throw new UnsupportedOperationException(
				"addInput not supported for %s neurons!".formatted(this.type()));
		}
		if(this == input)
		{
			throw new IllegalArgumentException("Self-loops are not permitted!");
		}
		this.inputs.put(input, weight);
		input.outputs.put(this, weight);
	}

	public void addOutput(Neuron output, double weight)
	{
		if(this.isOutput())
		{
			throw new UnsupportedOperationException(
				"addOutput not supported for %s neurons!".formatted(this.type()));
		}
		if(this == output)
		{
			throw new IllegalArgumentException("Self-loops are not permitted!");
		}
		this.outputs.put(output, weight);
		output.inputs.put(this, weight);
	}

	public void dropInput(Neuron input)
	{
		this.inputs.remove(input);
		input.outputs.remove(this);
	}

	public void dropOutput(Neuron output)
	{
		this.outputs.remove(output);
		output.inputs.remove(this);
	}

	public Map<Neuron, Double> inputs()
	{
		return this.inputs;
	}

	public Map<Neuron, Double> outputs()
	{
		return this.outputs;
	}

	public void value(double value)
	{
		this.value = value;
	}

	public double value()
	{
		if(this.isHidden() || this.isOutput())
		{
			return sigmoid(this.value);
		}
		return this.value;
	}

	public void type(Type type)
	{
		this.type = type;
	}

	public Type type()
	{
		return this.type;
	}

	public void update()
	{
		var sum = 0d;
		for(var entry : this.inputs.entrySet())
		{
			sum += entry.getKey().value() * entry.getValue();
		}
		this.value = sum;
	}

	private double sigmoid(double x)
	{
		return 2d / (1d + Math.exp(-4.9 * x)) - 1;
	}

	public boolean isFlagged()
	{
		return this.flagged;
	}

	public boolean unflag()
	{
		return swapFlag(false);
	}

	public boolean flag()
	{
		return swapFlag(true);
	}

	public boolean flipFlag()
	{
		return swapFlag(!this.flagged);
	}

	@Override
	public String toString()
	{
		return String.format("Neuron[%s inputs, %s outputs, value=%s, flagged=%s]",
			this.inputs.size(), this.outputs.size(), this.value, this.flagged);
	}

	public enum Type
	{
		INPUT, OUTPUT, HIDDEN, BIAS;
	}
}
