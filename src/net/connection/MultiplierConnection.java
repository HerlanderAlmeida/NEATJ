package net.connection;

import net.neuron.Neuron;

public class MultiplierConnection extends Connection
{
	MultiplierConnection(Neuron input, double weight)
	{
		super(input, weight);
	}
	
	@Override
	public double getValue()
	{
		return getInput().getValue() * getWeight();
	}
}