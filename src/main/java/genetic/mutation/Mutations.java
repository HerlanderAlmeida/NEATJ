package genetic.mutation;

import java.util.ArrayList;
import java.util.Collection;

import genetic.Individual;

public class Mutations<T extends Individual> extends ArrayList<Mutation<T>>
{
	private static final long serialVersionUID = -2348177617279033196L;

	public Mutations()
	{
	}

	public Mutations(Collection<Mutation<T>> mutations)
	{
		super(mutations);
	}

	public T apply(T t)
	{
		for(var mutation : this)
		{
			t = mutation.apply(t);
		}
		return t;
	}
}
