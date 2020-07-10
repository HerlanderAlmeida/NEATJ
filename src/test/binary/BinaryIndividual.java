package test.binary;

import java.util.Random;

import genetic.Individual;
import genetic.genome.Genome;

public class BinaryIndividual extends Individual
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
		return String.format("BI(%"+BITS+"s)", Integer.toBinaryString(genome().integer())).replaceAll(" ", "0");
	}
	
	private BinaryIndividual withInt(int i)
	{
		genome().integer(i);
		return this;
	}
	
	@Override
	public BinaryIndividual copy()
	{
		return new BinaryIndividual().withInt(genome().integer());
	}
	
	public static int getBits()
	{
		return BITS;
	}
	
	@Override
	public BinaryGenome genome()
	{
		return this.genome.cast();
	}
	
	public BinaryIndividual crossover(BinaryIndividual other)
	{
		int first = RNG.nextInt(BITS);
		int second = RNG.nextInt(BITS);
		if(first >= second)
		{
			int temp = first;
			first = second;
			second = temp;
		}
		int flag = -1 >>> first + 32-BITS;
		int flag2 = -1 >>> second + 33-BITS;
		flag ^= flag2;
		var ret = new BinaryIndividual((this.genome().integer() & flag) | (other.genome().integer() & ~flag));
		return ret;
	}
	
	public BinaryIndividual mutateIndividual()
	{
		return new BinaryIndividual();
	}
	
	public BinaryIndividual mutatePoint()
	{
		int flag = (1 << RNG.nextInt(BITS));
		return this.withInt(genome().integer() ^ flag);
	}
}
