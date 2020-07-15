package test.neat;

public record Parameters(double range, double step, double excessCoefficient,
	double disjointCoefficient, double weightDifferenceCoefficient)
{
	public static Builder builder()
	{
		return new Builder();
	}
	
	public static class Builder
	{
		private double range;
		private double step;
		private double excessCoefficient;
		private double disjointCoefficient;
		private double weightDifferenceCoefficient;
		
		public Builder withRange(double range)
		{
			this.range = range;
			return this;
		}
		
		public Builder withStep(double step)
		{
			this.step = step;
			return this;
		}
		
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
		
		public Parameters build()
		{
			return new Parameters(range, step, excessCoefficient, disjointCoefficient,
				weightDifferenceCoefficient);
		}
	}
}
