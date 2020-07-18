package network.neuron;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import network.connection.Connection;

public class Neuron
{
	private Collection<Connection> inputs;
	private double value;
	private boolean flagged; // relatively useful flag for any NN processing
	private Type type = Type.HIDDEN;
	
	public Neuron()
	{
		this(new Connection[0]);
	}
	
	public Neuron(Connection... inputs)
	{
		this(Stream.of(inputs).collect(Collectors.toCollection(ArrayList::new)));
	}
	
	public Neuron(Iterable<Connection> inputs)
	{
		this.inputs = new LinkedList<Connection>();
		inputs.forEach(this.inputs::add);
	}
	
	private boolean swapFlag(boolean new_)
	{
		var old = flagged;
		flagged = new_;
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
	
	public void addConnection(Connection input)
	{
		inputs.add(input);
	}
	
	public Collection<Connection> allInputs()
	{
		return inputs;
	}
	
	public Collection<Connection> inputs()
	{
		return inputs;
	}
	
	public void value(double value)
	{
		this.value = value;
	}
	
	public double value()
	{
		return value;
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
		value = 0;
		for(var c : inputs)
		{
			value += c.getValue();
		}
		// apply sigmoid function or other things
		value = sigmoid(value);
	}
	
	private double sigmoid(double x)
	{
		return 2d / (1d + Math.exp(-4.9 * x)) - 1;
	}
	
	public boolean isFlagged()
	{
		return flagged;
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
		return swapFlag(!flagged);
	}
	
	public String toString()
	{
		return String.format("Neuron[inputs=%s, value=%s, flagged=%s]", inputs, value, flagged);
	}
	
	public enum Type
	{
		INPUT, OUTPUT, HIDDEN, BIAS;
	}
}
