package genetic.selection;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import genetic.Individual;
import genetic.evaluate.Evaluation;
import genetic.selection.method.SelectionMethod;

public class Selector<T extends Individual>
{
	private Queue<SelectionMethod<T>> methods;
	private Queue<SelectionMethod<T>> complete = new ArrayDeque<>();
	
	/**
	 * @return A {@link Selector} with the given {@link SelectionMethod SelectionMethod(s)}
	 */
	@SafeVarargs
	public static <S extends Individual> Selector<S> selectingBy(SelectionMethod<S>... methods)
	{
		return new Selector<S>().withSelectionMethods(methods);
	}
	
	private Selector()
	{
	}
	
	@SuppressWarnings( "unchecked" )
	private Selector<T> withSelectionMethods(SelectionMethod<T>... methods)
	{
		this.methods = new ArrayDeque<>(List.of(methods));
		return this;
	}
	
	public void reset()
	{
		while(!methods.isEmpty())
		{
			complete.add(methods.poll());
		}
		methods.addAll(complete);
		complete.clear();
		methods.forEach(SelectionMethod::reset);
	}

	public <R extends Number & Comparable<R>> T select(List<Evaluation<T, R>> ranked)
	{
		var selector = methods.peek();
		while(selector.finished())
		{
			complete.add(methods.poll());
			selector = methods.peek();
			if(selector == null)
				throw new NullPointerException("No selection methods remain to be executed in the selector!");
		}
		return selector.select(ranked);
	}
}
