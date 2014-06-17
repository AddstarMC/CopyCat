package au.com.addstar.copycat.logic;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import au.com.addstar.copycat.CopyCatPlugin;

public class StateEngine<T> implements Runnable
{
	private BukkitTask mTask;
	private State<T> mState;
	private T mGame;
	
	public void start(State<T> state, T game)
	{
		mGame = game;
		mTask = Bukkit.getScheduler().runTaskTimer(CopyCatPlugin.instance, this, 5, 5);
		mState = state;
		mState.onStart(this, mGame);
	}
	
	public void end()
	{
		if(mTask != null)
			mTask.cancel();
	}
	
	public void setState(State<T> state)
	{
		mState.onEnd(this, mGame);
		mState = state;
		mState.onStart(this, mGame);
	}
	
	public void abortState(State<T> next)
	{
		mState = next;
		mState.onStart(this, mGame);
	}
	
	@Override
	public void run()
	{
		mState.onTick(this, mGame);
	}
	
	public void sendEvent(String name, Object data)
	{
		mState.onEvent(name, data, this, mGame);
	}
	
	public boolean isRunning()
	{
		return mState != null;
	}
}
