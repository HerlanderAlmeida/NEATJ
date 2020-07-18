package neat;

import java.util.HashMap;
import java.util.Map;

/**
 * Innovation tracker for historical markers
 */
public class InnovationTracker
{
	private int marker = 0;
	private Map<Integer, Map<Integer, Integer>> genes = new HashMap<>();
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
		if(genes.containsKey(from))
		{
			var map = genes.get(from);
			if(map.containsKey(to))
			{
				return map.get(to);
			}
			else
			{
				map.put(to, marker);
			}
		}
		else
		{
			genes.put(from, new HashMap<>());
			genes.get(from).put(to, marker);
		}
		return marker++;
	}
}
