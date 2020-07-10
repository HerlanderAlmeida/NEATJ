package test.neat;

import java.util.ArrayList;
import java.util.List;

/**
 * Innovation tracker for historical markers
 */
public class InnovationTracker
{
	private int marker = 0;
	private List<Innovation> genes = new ArrayList<>();
	
	record Innovation(int from, int to, int marker)
	{
	}
	
	/**
	 * For each generation, the same connection must be a different innovation.
	 * Calling this method each generation will let that be the case.
	 */
	public void reset()
	{
		genes.clear();
	}
	
	/**
	 * @return The historical marker for the given innovation
	 */
	public int getMarker(int from, int to)
	{
		for(var gene : genes)
			if(gene.from() == from && gene.to() == to)
				return gene.marker();
		genes.add(new Innovation(from, to, marker));
		return marker++;
	}
}
