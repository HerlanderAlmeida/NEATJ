package temp;

import genetic.Individual;

public class IndividualChild implements Individual
{
	String data;
	int numData;
	private static int name = 0;
	private int my_name;
	
	public IndividualChild(String s, Integer n)
	{
		my_name = name++;
		data = s;
		numData = n;
	}
	
	public String toString()
	{
		return data + numData + "(" + my_name + ")";
	}
	
	@Override
	public IndividualChild getInstance()
	{
		return new IndividualChild("blah", 5);
	}

	@Override
	public IndividualChild copy()
	{
		return new IndividualChild("blah", my_name);
	}
}
