package genetic.selection.method;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import genetic.Individual;
import genetic.evaluate.Evaluation;

public class TournamentSelection<T extends Individual> extends SelectionMethod<T>
{
	private int tournamentSize;

	public TournamentSelection(int tournamentSize)
	{
		this.tournamentSize = tournamentSize;
	}

	public TournamentSelection(int tournamentSize, int iterations)
	{
		super(iterations);
		this.tournamentSize = tournamentSize;
	}

	@Override
	public <R extends Number & Comparable<R>> T selectIndividual(List<Evaluation<T, R>> ranked)
	{
		var evals = new HashSet<Evaluation<T, R>>();
		for(var members = 0; members < this.tournamentSize; members++)
		{
			evals.add(ranked.get(random.nextInt(ranked.size())));
		}
		return evals.stream().max(Comparator.comparing(Evaluation::result))
			.orElseGet(() -> ranked.get(random.nextInt(ranked.size()))).individual();
	}
}
