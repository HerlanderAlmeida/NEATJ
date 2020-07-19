package genetic.selection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import genetic.Individual;
import genetic.evaluate.Evaluation;
import genetic.selection.method.SelectionMethod;

public class Selector<T extends Individual> extends SelectionMethod<T>
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

	@Override
	public void reset()
	{
		while(!this.methods.isEmpty())
		{
			this.complete.add(this.methods.poll());
		}
		this.methods.addAll(this.complete);
		this.complete.clear();
		this.methods.forEach(SelectionMethod::reset);
	}

	/**
	 * @param <R> Result of evaluations
	 * @param ranked A list of evaluated individuals, sorted in descending order of evaluation
	 * @return A single individual, selected according to the current selection method
	 */
	@Override
	public <R extends Number & Comparable<R>> T selectIndividual(List<Evaluation<T, R>> ranked)
	{
		var selector = this.methods.peek();
		while(selector.finished())
		{
			this.complete.add(this.methods.poll());
			selector = this.methods.peek();
			if(selector == null)
			{
				reset();
				selector = this.methods.peek();
			}
		}
		return selector.select(new ArrayList<>(ranked));
	}
}
