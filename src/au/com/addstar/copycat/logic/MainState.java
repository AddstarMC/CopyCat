package au.com.addstar.copycat.logic;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PlayerStation;
import au.com.addstar.copycat.Util;

public abstract class MainState extends TimerState
{
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		Minigame minigame = game.getMinigame();
		game.broadcast("Start copying", null);
		
		for(PlayerStation station : game.getStations())
		{
			if(station.getPlayer() != null)
			{
				station.setCanModify(true);
				station.drawSubject();
			}
		}
		
		for(MinigamePlayer player : minigame.getPlayers())
			minigame.getDefaultPlayerLoadout().equiptLoadout(player);
		
		endTime = System.currentTimeMillis() + game.getMaxRoundTime();
	}
	
	@Override
	protected void onNotifyTimeLeft( long remaining, StateEngine<GameBoard> engine, GameBoard game )
	{
		if(remaining == 0)
			engine.setState(new PreRoundState());
		else
			game.broadcast(Util.getTimeRemainString(remaining) + " left in the round.", null);
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
}
