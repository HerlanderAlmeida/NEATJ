package neat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import genetic.Population;
import genetic.evaluate.Evaluation;
import genetic.selection.Selector;
import utils.Exclude;

public class SpeciatedPopulation<T extends SpeciesIndividual<R>, R extends Number & Comparable<R>>
{
	public static final Random random = ThreadLocalRandom.current();
	private List<Species<T, R>> species = new ArrayList<>();
	private SpeciationParameters speciationParameters;
	@Exclude
	private Selector<T> selector;
	@Exclude
	private Supplier<T> generator;
	@Exclude
	private StalenessIndicator<T, R> stalenessIndicator;
	private int size;

	private SpeciatedPopulation(int size, Supplier<T> generator, SpeciationParameters parameters,
		Selector<T> selector, StalenessIndicator<T, R> stalenessIndicator)
	{
		Objects.requireNonNull(generator);
		this.generator = generator;
		this.speciationParameters = parameters;
		this.selector = selector;
		this.stalenessIndicator = stalenessIndicator;
		this.repopulate(size);
	}

	public Selector<T> selector()
	{
		return this.selector;
	}

	public void selector(Selector<T> selector)
	{
		this.selector = selector;
	}

	public Supplier<T> generator()
	{
		return this.generator;
	}

	public void generator(Supplier<T> supplier)
	{
		this.generator = supplier;
	}

	public void repopulate(int size)
	{
		this.updateSpecies(Stream.generate(this.generator).limit(this.size = size));
	}

	public StalenessIndicator<T, R> stalenessIndicator()
	{
		return this.stalenessIndicator;
	}

	public void stalenessIndicator(StalenessIndicator<T, R> stalenessIndicator)
	{
		this.stalenessIndicator = stalenessIndicator;
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
		return this.species.stream().map(Species::stream).reduce(Stream::concat)
			.orElse(Stream.empty());
	}

	public List<Species<T, R>> species()
	{
		return this.species;
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
		this.updateRepresentatives();
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

	public Optional<Evaluation<T, R>> updateFitnesses(Stream<Evaluation<T, R>> ranked)
	{
		var evals = ranked.collect(Collectors.toList());
		evals.forEach(eval -> eval.individual().fitness(eval.result()));
		this.species.forEach(species -> species.population().sort(
			Comparator.<SpeciesIndividual<R>, R>comparing(SpeciesIndividual::fitness).reversed()));
		this.species.forEach(species -> species.age(this.stalenessIndicator.apply(species)));
		this.species.forEach(Species::adjustFitness);
		this.removeStaleSpecies();
		var min = this.species.stream().mapToDouble(Species::averageFitness).min();
		if(min.isPresent() && min.getAsDouble() <= 0)
		{
			for(var species : this.species)
			{
				species.averageFitness(species.averageFitness() - min.getAsDouble()
					+ this.speciationParameters.deadbeatEvaluation());
			}
		}
		return evals.stream().max(Comparator.comparing(Evaluation::result));
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
	public Stream<T> repopulate()
	{
		if(this.species.size() == 0)
		{
			this.repopulate(this.size);
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
			/*
			 * Reset must be per species rather than per generation, because
			 * each species is treated as a unique sub-population
			 */
			this.selector.reset();
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
		private StalenessIndicator<T, R> stalenessIndicator;

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

		/**
		 * Staleness is measured for each species, using this indicator as the measure of comparison
		 */
		public Builder<T, R> withStalenessIndicator(StalenessIndicator<T, R> stalenessIndicator)
		{
			this.stalenessIndicator = stalenessIndicator;
			return this;
		}

		public SpeciatedPopulation<T, R> build()
		{
			return new SpeciatedPopulation<>(this.size, this.generator, this.parameters,
				this.selector, this.stalenessIndicator);
		}
	}
}
