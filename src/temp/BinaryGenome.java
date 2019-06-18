package temp;

import genetic.genome.Genome;

public class BinaryGenome implements Genome
{
	private int my_int;
	
	public BinaryGenome(int i)
	{
		setInt(i);
	}
	
	public int getInt()
	{
		return my_int;
	}
	
	public void setInt(int integer)
	{
		this.my_int = integer;
	}

	public BinaryGenome copy()
	{
		return new BinaryGenome(getInt());
	}
}
