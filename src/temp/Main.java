package temp;

import genetic.Population;
import genetic.evaluate.Evaluator;
import genetic.repopulate.RepopulatorImpl;

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
			return b.getGenome().getInt();
		});
		var evals = eval.evaluate(pop);
		//pop.forEach(bi -> bi.);
//		evals.forEach(System.out::println);
		var repopulator = new RepopulatorImpl<BinaryIndividual>(b -> new BinaryIndividual(b.getGenome().getInt()))
		{
			
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
