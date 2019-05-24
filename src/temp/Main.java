package temp;

import genetic.Population;
import genetic.repopulate.Repopulator;
import genetic.repopulate.RepopulatorImpl;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		Population<IndividualChild> p = new Population<IndividualChild>(10, false, IndividualChild.class, "bleh", 7);
		boolean b = p.remove("blah");
		System.out.println(b);
		b = p.remove(p.get(0));
		System.out.println(b);
		for(int i = 0; i < 1; ++i)
			p.remove(0);
		System.out.println(p);
		p.forEach(System.out::println);
		Repopulator<IndividualChild> repop = new RepopulatorImpl<>(IndividualChild::copy){};
		System.out.println("*********");
		long l = System.currentTimeMillis();
		p = repop.repopulate(p);
		System.out.println(p);
		p.forEach(System.out::println);
		System.out.println(p.size());
		System.out.println((System.currentTimeMillis() - l)/1000.0 + " seconds elapsed!");
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
		// define evaluator
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
