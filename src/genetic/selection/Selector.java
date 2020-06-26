package genetic.selection;

public class Selector
{
	private SelectionMethod[] methods;
	
	public static Selector selectingBy(SelectionMethod... methods)
	{
		return new Selector().withSelectionMethods(methods);
	}
	
	private Selector()
	{
	}
	
	public Selector withSelectionMethods(SelectionMethod... methods)
	{
		this.methods = methods;
		return this;
	}
}
