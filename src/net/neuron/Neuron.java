package net.neuron;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.connection.MultiplierConnection;

public class Neuron
{
	private Collection<MultiplierConnection> inputs;
	private double value;
	
	public Neuron()
	{
	}
	
	public Neuron(MultiplierConnection... inputs)
	{
		this(List.of(inputs));
	}
	
	public Neuron(Iterable<MultiplierConnection> inputs)
	{
		this.inputs = new LinkedList<MultiplierConnection>();
		inputs.forEach(this.inputs::add);
	}
	
	public void setValue(double value)
	{
		this.value = value;
	}
	
	public double getValue()
	{
		return value;
	}
	
	public void update()
	{
		value = 0;
		for(MultiplierConnection c : inputs)
		{
			value += c.getValue();
		}
		// apply sigmoid function or other things
	}
	
}
