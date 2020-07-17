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
		for(var species : species)
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
		return species.stream();
	}
	
	private void classifyIndividual(T t)
	{
		var matched = false;
		for(var species : species)
		{
			if(species.representative().difference(t, speciationParameters)
				.doubleValue() < speciationParameters.differenceThreshold())
			{
				var<T> pop = species.population();
				pop.add(t);
				matched = true;
				break;
			}
		}
		if(!matched)
		{
			var<T, R> newSpecies = new Species<T, R>();
			newSpecies.population(new Population<>(List.of(t)));
			newSpecies.updateRepresentative();
			species.add(newSpecies);
		}
	}
	
	public void updateSpecies(Stream<T> ts)
	{
		updateRepresentatives();
		ts.forEach(this::classifyIndividual);
		species.removeIf(Species::perished);
		if(species.size() > speciationParameters.desiredSpecies())
		{
			speciationParameters = speciationParameters
				.withDifferenceThreshold(speciationParameters.differenceThreshold()
					+ speciationParameters.differenceThresholdStep());
		}
		else if(species.size() < speciationParameters.desiredSpecies())
		{
			speciationParameters = speciationParameters
				.withDifferenceThreshold(speciationParameters.differenceThreshold()
					- speciationParameters.differenceThresholdStep());
		}
	}
	
	public Evaluation<T, R> updateFitnesses(Stream<Evaluation<T, R>> ranked)
	{
		var evals = ranked.collect(Collectors.toList());
		evals.forEach(eval -> eval.individual().fitness(eval.result()));
		species.forEach(species -> species.population().sort(
			Comparator.<SpeciesIndividual<R>, R>comparing(SpeciesIndividual::fitness).reversed()));
		species.forEach(Species::age);
		removeStaleSpecies();
		species.forEach(Species::adjustFitness);
		var min = species.stream().mapToDouble(Species::averageFitness).min();
		if(min.isPresent() && min.getAsDouble() <= 0)
		{
			for(var species : species)
			{
				species.averageFitness(species.averageFitness() - min.getAsDouble()
					+ speciationParameters.deadbeatEvaluation());
			}
		}
		return evals.stream().max(Comparator.comparing(Evaluation::result)).orElse(null);
	}
	
	public void removeStaleSpecies()
	{
		species
			.sort(Comparator.<Species<T, R>, Double>comparing(Species::averageFitness).reversed());
		var stagnantPopulation = true;
		for(var species : species)
		{
			if(species.staleness() > speciationParameters.staleGenerationsAllowed())
			{
				if(stagnantPopulation)
					break;
				species.perish();
			}
			stagnantPopulation = false;
		}
		if(stagnantPopulation)
		{
			var index = 0;
			for(var species : species)
			{
				if(index < speciationParameters.preservedSpecies())
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
		species.removeIf(Species::perished);
	}
	
	/**
	 * Assumption: species populations are sorted highest to lowest
	 */
	public Stream<T> repopulate(UnaryOperator<T> mutations)
	{
		if(species.size() == 0)
		{
			System.out.println("Hard reset!");
			this.updateSpecies(Stream.generate(supplier).limit(this.size));
		}
		var totalAverageFitness = species.stream().mapToDouble(Species::averageFitness).sum();
		var slotsRemaining = size;
		for(var species : species)
		{
			species.capacity((int) (size * (species.averageFitness() / totalAverageFitness)));
			slotsRemaining -= species.capacity();
		}
		// we'll be generous with the remaining slots, logarithmically
		while(slotsRemaining > 0)
		{
			var lottery = slotsRemaining - (slotsRemaining / 2);
			slotsRemaining -= lottery;
			var species = this.species.get(random.nextInt(this.species.size()));
			species.capacity(species.capacity() + lottery);
		}
		for(var species : species)
		{
			if(species.capacity() == 0)
			{
				species.perish();
			}
			else
			{
				// assumption necessary for this part
				species.eliminate(speciationParameters.eliminationRate());
			}
		}
		species.removeIf(Species::perished);
		var ret = Stream.<T>builder();
		for(var species : species)
		{
			var fitnessList = species.fitnessList();
			var capacity = species.capacity();
			for(var i = 0; i < capacity; i++)
			{
				ret.accept(selector.select(fitnessList));
			}
		}
		return ret.build();
	}
	
	public static <T extends SpeciesIndividual<R>, R extends Number & Comparable<R>> Builder<T, R> builder()
	{
		return new Builder<T, R>();
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
			return new SpeciatedPopulation<>(size, generator, parameters, selector);
		}
	}
}
