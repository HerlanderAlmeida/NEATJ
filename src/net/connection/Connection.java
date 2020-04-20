package net.connection;

import net.neuron.Neuron;

public abstract class Connection
{
	private Neuron input;
	private double weight;
	
	public Connection(Neuron input, double weight)
	{
		this.input = input;
		this.weight = weight;
	}
	
	public abstract double getValue();

	public double getWeight()
	{
		return weight;
	}

	public Neuron getInput()
	{
		return input;
	}
	
	public static MultiplierConnection multiplier(Neuron input, double weight)
	{
		return new MultiplierConnection(input, weight);
	}
}