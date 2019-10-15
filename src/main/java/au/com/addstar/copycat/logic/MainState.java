package au.com.addstar.copycat.logic;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.LoadoutModule;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;

import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PlayerStation;
import au.com.addstar.copycat.Util;

public abstract class MainState extends TimerState
{
	protected long startTime;
	protected long lastMessageTime = 0;
	protected long lastTimeOutput = 0;
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		startTime = System.currentTimeMillis();
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
		BossBar bar = game.getBossDisplay();
		bar.setTitle("Start Copying");
		bar.setColor(BarColor.GREEN);
		bar.setProgress(0);
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
				double progress = (double)left / (double)(endTime - startTime);
				progress = Math.min(Math.max(progress, 0), 1);
				
				BossBar bar = game.getBossDisplay();
				bar.setTitle("Time left: " + Util.getTimeRemainString(left));
				bar.setProgress(progress);
				
				if (progress > 0.3)
					bar.setColor(BarColor.GREEN);
				else if (progress > 0.1)
					bar.setColor(BarColor.YELLOW);
				else
					bar.setColor(BarColor.RED);
					
				lastTimeOutput = current;
			}
		}
		
	}
}
