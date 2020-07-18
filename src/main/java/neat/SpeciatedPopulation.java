package neat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import genetic.Population;
import genetic.evaluate.Evaluation;
import genetic.selection.Selector;

public class SpeciatedPopulation<T extends SpeciesIndividual<R>, R extends Number & Comparable<R>>
{
	public static final Random random = ThreadLocalRandom.current();
	private List<Species<T, R>> species = new ArrayList<>();
	private SpeciationParameters speciationParameters;
	private Selector<T> selector;
	private Supplier<T> supplier;
	private int size;
	
	private SpeciatedPopulation(int n, Supplier<T> supplier, SpeciationParameters parameters,
		Selector<T> selector)
	{
		Objects.requireNonNull(supplier);
		this.supplier = supplier;
		this.speciationParameters = parameters;
		this.selector = selector;
		this.updateSpecies(Stream.generate(supplier).limit(this.size = n));
	}
	
	public void updateRepresentatives()
	{
		for(var species : this.species)
		{
			species.updateRepresentative();
			species.perish();
		}
	}
	
	public Stream<T> individuals()
	{
		return stream().flatMap(Species::stream);
	}
	
	public Stream<Species<T, R>> stream()
	{
		return this.species.stream();
	}
	
	private void classifyIndividual(T t)
	{
		var matched = false;
		for(var species : this.species)
		{
			if(species.representative().difference(t, this.speciationParameters)
				.doubleValue() < this.speciationParameters.differenceThreshold())
			{
				var pop = species.population();
				pop.add(t);
				matched = true;
				break;
			}
		}
		if(!matched)
		{
			var newSpecies = new Species<T, R>();
			newSpecies.population(new Population<>(List.of(t)));
			newSpecies.updateRepresentative();
			this.species.add(newSpecies);
		}
	}
	
	public void updateSpecies(Stream<T> ts)
	{
		updateRepresentatives();
		ts.forEach(this::classifyIndividual);
		this.species.removeIf(Species::perished);
		if(this.species.size() > this.speciationParameters.desiredSpecies())
		{
			this.speciationParameters = this.speciationParameters
				.withDifferenceThreshold(this.speciationParameters.differenceThreshold()
					+ this.speciationParameters.differenceThresholdStep());
		}
		else if(this.species.size() < this.speciationParameters.desiredSpecies())
		{
			this.speciationParameters = this.speciationParameters
				.withDifferenceThreshold(this.speciationParameters.differenceThreshold()
					- this.speciationParameters.differenceThresholdStep());
		}
	}
	
	public Evaluation<T, R> updateFitnesses(Stream<Evaluation<T, R>> ranked)
	{
		var evals = ranked.collect(Collectors.toList());
		evals.forEach(eval -> eval.individual().fitness(eval.result()));
		this.species.forEach(species -> species.population().sort(
			Comparator.<SpeciesIndividual<R>, R>comparing(SpeciesIndividual::fitness).reversed()));
		this.species.forEach(Species::age);
		removeStaleSpecies();
		this.species.forEach(Species::adjustFitness);
		var min = this.species.stream().mapToDouble(Species::averageFitness).min();
		if(min.isPresent() && min.getAsDouble() <= 0)
		{
			for(var species : this.species)
			{
				species.averageFitness(species.averageFitness() - min.getAsDouble()
					+ this.speciationParameters.deadbeatEvaluation());
			}
		}
		return evals.stream().max(Comparator.comparing(Evaluation::result)).orElse(null);
	}
	
	public void removeStaleSpecies()
	{
		this.species
			.sort(Comparator.<Species<T, R>, Double>comparing(Species::averageFitness).reversed());
		var stagnantPopulation = true;
		for(var species : this.species)
		{
			if(species.staleness() > this.speciationParameters.staleGenerationsAllowed())
			{
				if(stagnantPopulation)
				{
					break;
				}
				species.perish();
			}
			stagnantPopulation = false;
		}
		if(stagnantPopulation)
		{
			var index = 0;
			for(var species : this.species)
			{
				if(index < this.speciationParameters.preservedSpecies())
				{
					species.revive(); // fountain of youth? revival herb?
				}
				else
				{
					species.perish();
				}
				index++;
			}
		}
		this.species.removeIf(Species::perished);
	}
	
	/**
	 * Assumption: species populations are sorted highest to lowest
	 */
	public Stream<T> repopulate(UnaryOperator<T> mutations)
	{
		if(this.species.size() == 0)
		{
			System.out.println("Hard reset!");
			this.updateSpecies(Stream.generate(this.supplier).limit(this.size));
		}
		var totalAverageFitness = this.species.stream().mapToDouble(Species::averageFitness).sum();
		var slotsRemaining = this.size;
		for(var species : this.species)
		{
			species.capacity((int) (this.size * (species.averageFitness() / totalAverageFitness)));
			slotsRemaining -= species.capacity();
		}
		// we'll be generous with the remaining slots, logarithmically
		while(slotsRemaining > 0)
		{
			var lottery = slotsRemaining - slotsRemaining / 2;
			slotsRemaining -= lottery;
			var species = this.species.get(random.nextInt(this.species.size()));
			species.capacity(species.capacity() + lottery);
		}
		for(var species : this.species)
		{
			if(species.capacity() == 0)
			{
				species.perish();
			}
			else
			{
				// assumption necessary for this part
				species.eliminate(this.speciationParameters.eliminationRate());
			}
		}
		this.species.removeIf(Species::perished);
		var ret = Stream.<T>builder();
		for(var species : this.species)
		{
			var fitnessList = species.fitnessList();
			var capacity = species.capacity();
			for(var i = 0; i < capacity; i++)
			{
				ret.accept(this.selector.select(fitnessList));
			}
		}
		return ret.build();
	}
	
	public static <T extends SpeciesIndividual<R>, R extends Number & Comparable<R>> Builder<T, R> builder()
	{
		return new Builder<>();
	}
	
	public static class Builder<T extends SpeciesIndividual<R>, R extends Number & Comparable<R>>
	{
		private int size;
		private Supplier<T> generator;
		private SpeciationParameters parameters;
		private Selector<T> selector;
		
		private Builder()
		{
		}
		
		public Builder<T, R> withSize(int size)
		{
			this.size = size;
			return this;
		}
		
		public Builder<T, R> withGenerator(Supplier<T> generator)
		{
			this.generator = generator;
			return this;
		}
		
		public Builder<T, R> withParameters(SpeciationParameters parameters)
		{
			this.parameters = parameters;
			return this;
		}
		
		public Builder<T, R> withSelector(Selector<T> selector)
		{
			this.selector = selector;
			return this;
		}
		
		public SpeciatedPopulation<T, R> build()
		{
			return new SpeciatedPopulation<>(this.size, this.generator, this.parameters, this.selector);
		}
	}
}
