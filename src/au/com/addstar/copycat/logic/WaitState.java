package au.com.addstar.copycat.logic;

import au.com.addstar.copycat.GameBoard;

public class WaitState extends TimerState
{
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		endTime = System.currentTimeMillis() + game.getWaitTime();
	}
	
	@Override
	public void onEnd( StateEngine<GameBoard> engine, GameBoard game )
	{
		
	}
	
	@Override
	protected void onNotifyTimeLeft( long remaining, StateEngine<GameBoard> engine, GameBoard game )
	{
		if(remaining == 0)
			engine.setState(game.getMainState());
	}
}
