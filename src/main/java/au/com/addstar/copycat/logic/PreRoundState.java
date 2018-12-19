package au.com.addstar.copycat.logic;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;

import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.Util;

public class PreRoundState extends State<GameBoard>
{
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		Minigame minigame = game.getMinigame();
		// More players to eliminate.
		if(minigame.getPlayers().size() > 1)
		{
			BossBar bar = game.getBossDisplay();
			bar.setTitle("Waiting");
			bar.setProgress(1);
			bar.setColor(BarColor.PURPLE);

			if(game.getModule().getAllowSubjectDraw())
				engine.setState(new SubjectDrawState());
			else
			{
				game.setSubject(CopyCatPlugin.instance.getSubjectStorage().getRandomSubject(game.getSubjectSize()));
				game.broadcast("Selecting random pattern. Round starts in " + Util.getTimeRemainString(game.getModule().getWaitTime()), null);
				engine.setState(new WaitState());
			}
		}
		// End of game
		else
		{
			Minigames.getPlugin().getPlayerManager().endMinigame(minigame, new ArrayList<MinigamePlayer>(minigame.getPlayers()), Collections.<MinigamePlayer>emptyList());
			game.endGame();
		}
	}
}
