package au.com.addstar.copycat.logic;

import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.Util;

public class WaitState extends TimerState
{
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		game.broadcast("Selecting random pattern. Round starts in " + Util.getTimeRemainString(game.getWaitTime()), null);
		endTime = game.getWaitTime();
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
		else
			game.broadcast("Round starts in " + Util.getTimeRemainString(remaining), null);
	}
}
