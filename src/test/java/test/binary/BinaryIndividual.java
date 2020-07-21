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
		this.genome = new BinaryGenome(integer);
	}

	public BinaryIndividual()
	{
		this(RNG.nextInt((int) Math.pow(2, getBits())));
	}

	@Override
	public String toString()
	{
		return String.format("BI(%" + BITS + "s)", Integer.toBinaryString(genome().integer()))
			.replaceAll(" ", "0");
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
		var first = RNG.nextInt(BITS);
		var second = RNG.nextInt(BITS);
		if(first >= second)
		{
			var temp = first;
			first = second;
			second = temp;
		}
		var flag = -1 >>> first + 32 - BITS;
		var flag2 = -1 >>> second + 33 - BITS;
		flag ^= flag2;
		var ret = new BinaryIndividual(
			genome().integer() & flag | other.genome().integer() & ~flag);
		return ret;
	}

	public BinaryIndividual mutateIndividual()
	{
		return new BinaryIndividual();
	}

	public BinaryIndividual mutatePoint()
	{
		var flag = 1 << RNG.nextInt(BITS);
		return withInt(genome().integer() ^ flag);
	}
}
