package au.com.addstar.copycat.logic;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;

import au.com.addstar.copycat.GameBoard;

public class WaitState extends TimerState
{
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		endTime = System.currentTimeMillis() + game.getModule().getWaitTime();
		BossBar bar = game.getBossDisplay();
		bar.setTitle("Selecting Pattern");
		bar.setProgress(0);
		bar.setColor(BarColor.PURPLE);
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
