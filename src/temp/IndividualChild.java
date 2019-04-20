package temp;

import java.util.function.Supplier;

import genetic.Individual;

public class IndividualChild extends Individual<IndividualChild>
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
	public Supplier<? super Individual<?>> getProvider()
	{
		return IndividualChild::new;
	}
	
}
