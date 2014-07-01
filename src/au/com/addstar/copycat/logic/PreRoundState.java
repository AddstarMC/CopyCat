package au.com.addstar.copycat.logic;

import java.util.ArrayList;
import java.util.Collections;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

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
			game.getBossDisplay().setText("Waiting");
			game.getBossDisplay().setPercent(1);
			if(game.getAllowSubjectDrawing())
				engine.setState(new SubjectDrawState());
			else
			{
				game.setSubject(CopyCatPlugin.instance.getSubjectStorage().getRandomSubject(game.getSubjectSize()));
				game.broadcast("Selecting random pattern. Round starts in " + Util.getTimeRemainString(game.getWaitTime()), null);
				engine.setState(new WaitState());
			}
		}
		// End of game
		else
		{
			Minigames.plugin.pdata.endMinigame(minigame, new ArrayList<MinigamePlayer>(minigame.getPlayers()), Collections.<MinigamePlayer>emptyList());
			game.endGame();
		}
	}
}
