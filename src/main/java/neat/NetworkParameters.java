package neat;

/**
 * @param fullyConnected
 *            Whether the network starts fully connected or not
 */
public record NetworkParameters(int inputs, int outputs, int biases, boolean recurrent,
	double range, double step, boolean fullyConnected, boolean arbitrarilyConnected)
{

	public static Builder builder()
	{
		return new Builder();
	}
	public static class Builder
	{
		private int inputs = 1;
		private int outputs = 1;
		private int biases = 1;
		private boolean recurrent = false;
		private double range = 2;
		private double step = 0.01;
		private boolean fullyConnected = false;
		private boolean arbitrarilyConnected = false;

		private Builder()
		{
		}

		public Builder withInputs(int inputs)
		{
			this.inputs = inputs;
			return this;
		}

		public Builder withOutputs(int outputs)
		{
			this.outputs = outputs;
			return this;
		}

		public Builder withBiases(int biases)
		{
			this.biases = biases;
			return this;
		}

		public Builder withRecurrency(boolean recurrency)
		{
			this.recurrent = recurrency;
			return this;
		}

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

		public Builder withFullConnectivity(boolean fullyConnected)
		{
			this.fullyConnected = fullyConnected;
			return this;
		}

		public Builder withArbitraryConnectivity(boolean arbitrarilyConnected)
		{
			this.arbitrarilyConnected = arbitrarilyConnected;
			return this;
		}

		public NetworkParameters build()
		{
			return new NetworkParameters(this.inputs, this.outputs, this.biases, this.recurrent,
				this.range, this.step, this.fullyConnected, this.arbitrarilyConnected);
		}
	}
}
