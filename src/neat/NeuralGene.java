package neat;

public record NeuralGene(int from, int to, double weight, boolean enabled, /* historical marker */ int marker)
{
	public String toString()
	{
		return String.format("[%s%s%s(%s)#%s]", from, enabled?"->":"X",to,weight,marker);
	}
	
	public NeuralGene withEnabled(boolean enabled)
	{
		return new NeuralGene(this.from, this.to, this.weight, enabled, this.marker);
	}
	
	public NeuralGene withMarker(int marker)
	{
		return new NeuralGene(this.from, this.to, this.weight, this.enabled, marker);
	}
	
	public NeuralGene withWeight(double weight)
	{
		return new NeuralGene(this.from, this.to, weight, this.enabled, this.marker);
	}
}