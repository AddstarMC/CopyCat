package au.com.addstar.copycat.logic;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PatternStation;
import au.com.addstar.copycat.PlayerStation;
import au.com.addstar.copycat.Subject;
import au.com.addstar.copycat.Util;

public class SubjectDrawState extends TimerState
{
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		Minigame minigame = game.getMinigame();
		MinigamePlayer player = game.selectNextDrawer();
		PatternStation station = game.getPatternStation();
		
		game.broadcast(player.getDisplayName() + " is drawing the pattern.", player);
		player.teleport(station.getSpawnLocation());
		station.setPlayer(player);
		player.sendMessage("Draw the pattern in the area in front of you. You have " + Util.getTimeRemainString(game.getSubjectDrawTime()) + ". If you do not fill every block, a random pattern will be used.", "win");
		minigame.getDefaultPlayerLoadout().equiptLoadout(player);
		
		endTime = System.currentTimeMillis() + game.getSubjectDrawTime();
	}
	
	@Override
	public void onEnd( StateEngine<GameBoard> engine, GameBoard game )
	{
		MinigamePlayer player = game.getDrawer();
		PlayerStation playerStation = game.getStation(player);
		PatternStation station = game.getPatternStation();
		station.setPlayer(null);
		player.teleport(playerStation.getSpawnLocation());

		Subject subject = Subject.from(station.getPatternLocation(), station.getFacing(), game.getSubjectSize());
		if(subject == null)
		{
			game.broadcast("Pattern was not completed in time. Selecting a random pattern.", null);
			game.setSubject(CopyCatPlugin.instance.getSubjectStorage().getRandomSubject(game.getSubjectSize()));
		}
		else
		{
			game.broadcast("Using pattern created by " + player.getDisplayName(), null);
			game.setSubject(subject);
			
			if(game.getSaveSubjects())
				CopyCatPlugin.instance.getSubjectStorage().add(subject);
		}
		
		station.clearStation();
		game.setDrawer(null);
	}
	
	@Override
	protected void onNotifyTimeLeft( long remaining, StateEngine<GameBoard> engine, GameBoard game )
	{
		if(remaining > 0)
		{
			game.broadcast("Round starts in " + Util.getTimeRemainString(remaining), game.getDrawer());
			game.getDrawer().sendMessage("You have " + Util.getTimeRemainString(remaining) + " to complete your drawing", null);
		}
		else
			engine.setState(game.getMainState());
	}
	
	@Override
	public void onEvent( String name, Object data, StateEngine<GameBoard> engine, GameBoard game )
	{
		if(name.equals("leave"))
		{
			MinigamePlayer player = (MinigamePlayer)data;
			if(player == game.getDrawer())
			{
				game.setDrawer(null);
				engine.abortState(this);
			}
		}
	}
}
