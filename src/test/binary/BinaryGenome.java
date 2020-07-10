package test.binary;

import genetic.genome.Genome;

public class BinaryGenome implements Genome
{
	private int my_int;
	
	public BinaryGenome(int i)
	{
		integer(i);
	}
	
	public int integer()
	{
		return my_int;
	}
	
	public void integer(int integer)
	{
		this.my_int = integer;
	}

	public BinaryGenome copy()
	{
		return new BinaryGenome(integer());
	}
}
