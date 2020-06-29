package temp;

import java.util.Comparator;
import java.util.stream.Collectors;

import genetic.Population;
import genetic.evaluate.Evaluation;
import genetic.evaluate.Evaluator;
import genetic.repopulate.RepopulatorImpl;
import genetic.selection.Ranker;
import genetic.selection.Selector;
import genetic.selection.method.ElitistSelection;
import genetic.selection.method.RankSelection;
import genetic.selection.method.RouletteSelection;

public class Main
{
	public static void main(String[] args) throws Exception
	{
//		try
//		{
//			var prop = new Properties();
//			prop.setProperty("random", "0.1");
//			var os = new FileOutputStream("properties.prop");
////			prop.store(os, "Stored properties");
//		}
//		catch(Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// initialize population
		var pop = new Population<>(20, BinaryIndividual::new);
		System.out.println(pop);
		// define evaluator
		var eval = Evaluator.<BinaryIndividual, Integer>of(b ->
		{
			return Integer.bitCount(b.getGenome().getInt());
		});
		var ranker = Ranker.rankingBy(Comparator.comparing(Evaluation<BinaryIndividual, Integer>::result).reversed());
		var evals = eval.evaluate(pop.stream());
		var ranked = ranker.rank(evals).collect(Collectors.toList());
		var selector = Selector.<BinaryIndividual>selectingBy(new ElitistSelection<>(1), new RouletteSelection<>(pop.size()/2), new RankSelection<>());
		//pop.forEach(bi -> bi.);
		ranked.forEach((x) -> System.out.println(x));
		var repopulator = new RepopulatorImpl<BinaryIndividual>(b -> new BinaryIndividual(b.getGenome().getInt()))
		{
			@Override
			public BinaryIndividual apply(Population<BinaryIndividual> t)
			{
				System.out.println("Selected is: " + selector.select(ranked));
				return super.apply(t);
			}
		};
		var next = repopulator.collectN(pop, pop.size());
		// define repopulator
		// do {
		// evaluator.evaluate(population)
		// selection process:
		//     newpop = repopulator.apply(population)
		// population = newpop
		// } while(!terminationCondition());
		// end loop
	}
}
