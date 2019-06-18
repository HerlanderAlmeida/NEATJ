package temp;

import java.util.Random;

import genetic.Individual;
import genetic.genome.Genome;

public class BinaryIndividual implements Individual
{
	private static final int BITS = 10;
	private Genome genome;
	
	public BinaryIndividual(int integer)
	{
		genome = new BinaryGenome(integer);
	}
	
	public BinaryIndividual()
	{
		int randInt = new Random().nextInt((int) Math.pow(2, getBits()));
		genome = new BinaryGenome(randInt);
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
