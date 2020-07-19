package neat;

import java.util.Random;

public record IndividualParameters(double metaMutationProbability, double weightMutationProbability,
	double randomWeightMutationProbability, double linkMutationProbability,
	double biasLinkMutationProbability, double sensorMutationProbability,
	double neuronMutationProbability, double enableMutationProbability,
	double disableMutationProbability, double destroyMutationProbability)
{

	public IndividualParameters mutateProbabilities(Random random)
	{
		// mutate by 0.95, or 1/0.95
		var less = 0.95;
		var more = 1.0526315789473684210526315789474;
		var metaMutationProbability = this.metaMutationProbability
			* (random.nextBoolean() ? more : less);
		var weightMutationProbability = this.weightMutationProbability
			* (random.nextBoolean() ? more : less);
		var randomWeightMutationProbability = this.randomWeightMutationProbability
			* (random.nextBoolean() ? more : less);
		var linkMutationProbability = this.linkMutationProbability
			* (random.nextBoolean() ? more : less);
		var biasLinkMutationProbability = this.biasLinkMutationProbability
			* (random.nextBoolean() ? more : less);
		var sensorMutationProbability = this.sensorMutationProbability
			* (random.nextBoolean() ? more : less);
		var neuronMutationProbability = this.neuronMutationProbability
			* (random.nextBoolean() ? more : less);
		var enableMutationProbability = this.enableMutationProbability
			* (random.nextBoolean() ? more : less);
		var disableMutationProbability = this.disableMutationProbability
			* (random.nextBoolean() ? more : less);
		var destroyMutationProbability = this.destroyMutationProbability
			* (random.nextBoolean() ? more : less);
		return new IndividualParameters(metaMutationProbability, weightMutationProbability,
			randomWeightMutationProbability, linkMutationProbability, biasLinkMutationProbability,
			sensorMutationProbability, neuronMutationProbability, enableMutationProbability,
			disableMutationProbability, destroyMutationProbability);
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public IndividualParameters copy()
	{
		return new IndividualParameters(this.metaMutationProbability, this.weightMutationProbability,
			this.randomWeightMutationProbability, this.linkMutationProbability, this.biasLinkMutationProbability,
			this.sensorMutationProbability, this.neuronMutationProbability, this.enableMutationProbability,
			this.disableMutationProbability, this.destroyMutationProbability);
	}

	public static class Builder
	{
		private double metaMutationProbability = 1;
		private double weightMutationProbability = 0.225;
		private double randomWeightMutationProbability = 0.025;
		private double linkMutationProbability = 2;
		private double biasLinkMutationProbability = 0.4;
		private double sensorMutationProbability = 0.4;
		private double neuronMutationProbability = 0.5;
		private double enableMutationProbability = 0.4;
		private double disableMutationProbability = 0.2;
		private double destroyMutationProbability = 0.01;

		private Builder()
		{
		}

		public Builder withMetaMutationProbability(double metaMutationProbability)
		{
			this.metaMutationProbability = metaMutationProbability;
			return this;
		}

		public Builder withWeightMutationProbability(double weightMutationProbability)
		{
			this.weightMutationProbability = weightMutationProbability;
			return this;
		}

		public Builder withRandomWeightMutationProbability(double randomWeightMutationProbability)
		{
			this.randomWeightMutationProbability = randomWeightMutationProbability;
			return this;
		}

		public Builder withLinkMutationProbability(double linkMutationProbability)
		{
			this.linkMutationProbability = linkMutationProbability;
			return this;
		}

		public Builder withBiasLinkMutationProbability(double biasLinkMutationProbability)
		{
			this.biasLinkMutationProbability = biasLinkMutationProbability;
			return this;
		}

		public Builder withSensorMutationProbability(double sensorMutationProbability)
		{
			this.sensorMutationProbability = sensorMutationProbability;
			return this;
		}

		public Builder withNeuronMutationProbability(double neuronMutationProbability)
		{
			this.neuronMutationProbability = neuronMutationProbability;
			return this;
		}

		public Builder withEnableMutationProbability(double enableMutationProbability)
		{
			this.enableMutationProbability = enableMutationProbability;
			return this;
		}

		public Builder withDisableMutationProbability(double disableMutationProbability)
		{
			this.disableMutationProbability = disableMutationProbability;
			return this;
		}

		public Builder withDestroyMutationProbability(double destroyMutationProbability)
		{
			this.destroyMutationProbability = destroyMutationProbability;
			return this;
		}

		public IndividualParameters build()
		{
			return new IndividualParameters(this.metaMutationProbability, this.weightMutationProbability,
				this.randomWeightMutationProbability, this.linkMutationProbability,
				this.biasLinkMutationProbability, this.sensorMutationProbability, this.neuronMutationProbability,
				this.enableMutationProbability, this.disableMutationProbability, this.destroyMutationProbability);
		}
	}
}
