package net;

class Link
{
	int from;
	int to;
	double weight;
	
	public String toString()
	{
		return String.format("Link[from=%d, to=%d, weight=%f]", from, to, weight);
	}
}
