package genetic.selection;

public interface SelectionMethod
{
	public default boolean finished()
	{
		return false;
	}
}
