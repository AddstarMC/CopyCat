package au.com.addstar.copycat.logic;

import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PlayerStation;

public class BetweenRoundState extends TimerState
{
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		endTime = System.currentTimeMillis() + 5000;
		super.onStart(engine, game);
	}
	
	@Override
	public void onEnd( StateEngine<GameBoard> engine, GameBoard game )
	{
		for(PlayerStation station : game.getStations())
		{
			station.setCanModify(false);
			station.clearStation();
		}
	}
	
	@Override
	protected void onNotifyTimeLeft( long remaining, StateEngine<GameBoard> engine, GameBoard game )
	{
		if(remaining == 0)
			engine.setState(new PreRoundState());
	}

}
