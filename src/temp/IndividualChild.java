package temp;

import genetic.Individual;

public class IndividualChild implements Individual
{
	static int name = 0;
	int my_name = 0;
	public IndividualChild()
	{
		my_name = name++;
	}
	public String toString()
	{
		return "Child(" + my_name + ")";
	}
	
	@Override
	public IndividualChild getInstance()
	{
		return new IndividualChild();
	}
	
}
