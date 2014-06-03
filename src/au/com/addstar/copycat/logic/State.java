package au.com.addstar.copycat.logic;

public abstract class State<T>
{
	public void onStart(StateEngine<T> engine, T game)
	{
	}
	
	public void onEnd(StateEngine<T> engine, T game)
	{
	}
	
	public void onTick(StateEngine<T> engine, T game)
	{
	}
	
	public void onEvent(String name, Object data, StateEngine<T> engine, T game)
	{
	}
}
