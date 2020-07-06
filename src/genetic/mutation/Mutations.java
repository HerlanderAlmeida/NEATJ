package genetic.mutation;

import java.util.ArrayList;

import genetic.Individual;

public class Mutations<T extends Individual> extends ArrayList<Mutation<T>>
{
	private static final long serialVersionUID = -2348177617279033196L;

	public T apply(T t)
	{
		for(var mutation : this)
		{
			t = mutation.apply(t);
		}
		return t;
	}
}
