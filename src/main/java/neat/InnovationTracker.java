package neat;

import java.util.HashMap;
import java.util.Map;

/**
 * Innovation tracker for historical markers
 */
public class InnovationTracker
{
	private long marker = 0;
	private Map<Integer, Map<Integer, Long>> genes = new HashMap<>();

	/**
	 * For each generation, the same connection must be a different innovation.
	 * Calling this method each generation will let that be the case.
	 */
	public void reset()
	{
		this.genes.clear();
	}

	/**
	 * @return The historical marker for the given innovation
	 */
	public long getMarker(int from, int to)
	{
		if(this.genes.containsKey(from))
		{
			var map = this.genes.get(from);
			if(map.containsKey(to))
			{
				return map.get(to);
			}
			else
			{
				map.put(to, this.marker);
			}
		}
		else
		{
			this.genes.put(from, new HashMap<>());
			this.genes.get(from).put(to, this.marker);
		}
		return this.marker++;
	}
}
