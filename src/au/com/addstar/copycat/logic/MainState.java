package au.com.addstar.copycat.logic;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.LoadoutModule;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PlayerStation;
import au.com.addstar.copycat.Util;

public abstract class MainState extends TimerState
{
	protected long lastMessageTime = 0;
	protected long lastTimeOutput = 0;
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
		
		LoadoutModule module = LoadoutModule.getMinigameModule(minigame);
		PlayerLoadout defaultLoadout = module.getLoadout("default");
		
		for(MinigamePlayer player : minigame.getPlayers())
			defaultLoadout.equiptLoadout(player);
		
		endTime = System.currentTimeMillis() + game.getModule().getMaxRoundTime();
		game.getBossDisplay().setText("Start Copying");
		game.getBossDisplay().setPercent(0);
		lastMessageTime = System.currentTimeMillis();
	}
	
	@Override
	protected void onNotifyTimeLeft( long remaining, StateEngine<GameBoard> engine, GameBoard game )
	{
		if(remaining == 0)
			engine.setState(new BetweenRoundState());
		else
			game.broadcast(Util.getTimeRemainString(remaining) + " left in the round.", null);
	}
	
	@Override
	public void onEnd( StateEngine<GameBoard> engine, GameBoard game )
	{
		
	}
	
	@Override
	public void onTick( StateEngine<GameBoard> engine, GameBoard game )
	{
		super.onTick(engine, game);

		long current = System.currentTimeMillis();
		long left = endTime - current;
		
		if(current - lastMessageTime >= 5000)
		{
			if(current - lastTimeOutput >= 1000)
			{
				game.getBossDisplay().setText("Time left: " + Util.getTimeRemainString(left));
				lastTimeOutput = current;
			}
		}
		
	}
}
