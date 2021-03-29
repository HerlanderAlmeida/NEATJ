package neat;

public record SpeciationParameters(double excessCoefficient, double disjointCoefficient,
	double weightDifferenceCoefficient, int desiredSpecies, double differenceThreshold,
	double differenceThresholdStep, double crossoverProbability, double eliminationRate,
	int staleGenerationsAllowed, double deadbeatEvaluation, int preservedSpecies,
	boolean preservingLifetimeMaxFitness)
{

	public static Builder builder()
	{
		return new Builder();
	}

	public SpeciationParameters withDifferenceThreshold(double differenceThreshold)
	{
		return new SpeciationParameters(this.excessCoefficient, this.disjointCoefficient,
			this.weightDifferenceCoefficient, this.desiredSpecies, differenceThreshold,
			this.differenceThresholdStep, this.crossoverProbability, this.eliminationRate,
			this.staleGenerationsAllowed, this.deadbeatEvaluation, this.preservedSpecies,
			this.preservingLifetimeMaxFitness);
	}

	public static class Builder
	{
		private double excessCoefficient;
		private double disjointCoefficient;
		private double weightDifferenceCoefficient;
		private int desiredSpecies;
		private double differenceThreshold;
		private double differenceThresholdStep;
		private double crossoverProbability;
		private double eliminationRate;
		private int staleGenerationsAllowed;
		private double deadbeatEvaluation;
		private int preservedSpecies;
		private boolean preservingLifetimeMaxFitness;

		public Builder withExcessCoefficient(double excessCoefficient)
		{
			this.excessCoefficient = excessCoefficient;
			return this;
		}

		public Builder withDisjointCoefficient(double disjointCoefficient)
		{
			this.disjointCoefficient = disjointCoefficient;
			return this;
		}

		public Builder withWeightDifferenceCoefficient(double weightDifferenceCoefficient)
		{
			this.weightDifferenceCoefficient = weightDifferenceCoefficient;
			return this;
		}

		public Builder withDesiredSpecies(int desiredSpecies)
		{
			this.desiredSpecies = desiredSpecies;
			return this;
		}

		public Builder withDifferenceThreshold(double differenceThreshold)
		{
			this.differenceThreshold = differenceThreshold;
			return this;
		}

		public Builder withDifferenceThresholdStep(double differenceThresholdStep)
		{
			this.differenceThresholdStep = differenceThresholdStep;
			return this;
		}

		public Builder withCrossoverProbability(double crossoverProbability)
		{
			this.crossoverProbability = crossoverProbability;
			return this;
		}

		public Builder withEliminationRate(double eliminationRate)
		{
			this.eliminationRate = eliminationRate;
			return this;
		}

		public Builder withStaleGenerationsAllowed(int staleGenerationsAllowed)
		{
			this.staleGenerationsAllowed = staleGenerationsAllowed;
			return this;
		}

		public Builder withDeadbeatEvaluation(double deadbeatEvaluation)
		{
			this.deadbeatEvaluation = deadbeatEvaluation;
			return this;
		}

		public Builder withPreservedSpecies(int preservedSpecies)
		{
			this.preservedSpecies = preservedSpecies;
			return this;
		}

		public Builder withPreservingLifetimeMaxFitness(boolean preservingLifetimeMaxFitness)
		{
			this.preservingLifetimeMaxFitness = preservingLifetimeMaxFitness;
			return this;
		}

		public SpeciationParameters build()
		{
			return new SpeciationParameters(this.excessCoefficient, this.disjointCoefficient,
				this.weightDifferenceCoefficient, this.desiredSpecies, this.differenceThreshold,
				this.differenceThresholdStep, this.crossoverProbability, this.eliminationRate,
				this.staleGenerationsAllowed, this.deadbeatEvaluation, this.preservedSpecies,
				this.preservingLifetimeMaxFitness);
		}
	}
}
