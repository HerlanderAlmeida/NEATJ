package temp;

import java.util.Random;

import genetic.Individual;
import genetic.genome.Genome;

public class BinaryIndividual implements Individual
{
	private static final int BITS = 30;
	private static final Random RNG = new Random();
	private Genome genome;
	
	public BinaryIndividual(Object[] o)
	{
		this();
	}
	
	public BinaryIndividual(int integer)
	{
		genome = new BinaryGenome(integer);
	}
	
	public BinaryIndividual()
	{
		this(RNG.nextInt((int) Math.pow(2, getBits())));
	}
	
	public String toString()
	{
		return "BI(" + getGenome().getInt() + ")";
	}
	
	@Override
	public BinaryIndividual getInstance()
	{
		return new BinaryIndividual();
	}
	
	private BinaryIndividual withInt(int i)
	{
		getGenome().setInt(i);
		return this;
	}
	
	@Override
	public BinaryIndividual copy()
	{
		return new BinaryIndividual().withInt(getGenome().getInt());
	}
	
	public static int getBits()
	{
		return BITS;
	}
	
	@Override
	public BinaryGenome getGenome()
	{
		return this.genome.cast();
	}
}
