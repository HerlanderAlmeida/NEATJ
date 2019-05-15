package temp;

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
		// initialize population
		// define evaluator
		// do {
		// evaluator.evaluate(population)
		// selection process:
		//     newpop = repopulator.apply(population)
		// population = newpop
		// } while(!terminationCondition());
		// end loop
	}
}
