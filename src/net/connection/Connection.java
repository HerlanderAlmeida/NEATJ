package net.connection;

import net.neuron.Neuron;

public record Connection(Neuron input, double weight)
{
	public double getValue()
	{
		return input.value() * weight;
	}
}