package test.neat;

public record NetworkParameters(int inputs, int outputs, int biases, boolean recurrent)
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
		
		public NetworkParameters build()
		{
			return new NetworkParameters(inputs, outputs, biases, recurrent);
		}
	}
}
