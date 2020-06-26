package temp;

import java.util.Comparator;
import java.util.stream.Collectors;

import genetic.Population;
import genetic.evaluate.Evaluation;
import genetic.evaluate.Evaluator;
import genetic.repopulate.RepopulatorImpl;
import genetic.selection.Ranker;

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
//		var pop = new Population<>(20, BinaryIndividual::new);
		var pop = new Population<>(20,BinaryIndividual::new);
		System.out.println(pop);
		// define evaluator
		var eval = Evaluator.<BinaryIndividual, Integer>of(b ->
		{
			return Integer.bitCount(b.getGenome().getInt());
		});
		var ranker = Ranker.rankingBy(Comparator.comparing(Evaluation<BinaryIndividual, Integer>::result).reversed());
		var evals = eval.evaluate(pop.stream());
		var ranked = ranker.rank(evals).collect(Collectors.toList());

		//pop.forEach(bi -> bi.);
		ranked.forEach((x) -> System.out.println(x));
		var repopulator = new RepopulatorImpl<BinaryIndividual>(b -> new BinaryIndividual(b.getGenome().getInt()))
		{
			@Override
			public BinaryIndividual apply(Population<BinaryIndividual> t)
			{
				// TODO Auto-generated method stub
				return super.apply(t);
			}
		};
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
