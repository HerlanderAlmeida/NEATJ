package temp;

import java.io.FileOutputStream;
import java.util.Properties;

import genetic.Population;

public class Main
{
	public static void main(String[] args)
	{
		Population<IndividualChild> p = new Population<IndividualChild>(10, IndividualChild::new);
		boolean b = p.remove("blah");
		System.out.println(b);
		b = p.remove(p.get(0));
		System.out.println(b);
		for(int i = 0; i < 1; ++i)
			p.remove(0);
		System.out.println(p);
		p.forEach(System.out::println);
		try
		{
			var prop = new Properties();
			prop.setProperty("random", "0.1");
			var os = new FileOutputStream("properties.prop");
			prop.store(os, "Stored properties");
		}
		catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
