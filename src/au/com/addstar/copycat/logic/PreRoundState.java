package au.com.addstar.copycat.logic;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.GameBoard;

public class PreRoundState extends State<GameBoard>
{
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		Minigame minigame = game.getMinigame();
		// More players to eliminate.
		if(minigame.getPlayers().size() > 1)
		{
			if(game.getAllowSubjectDrawing())
				engine.setState(new SubjectDrawState());
			else
			{
				game.setSubject(CopyCatPlugin.instance.getSubjectStorage().getRandomSubject(game.getSubjectSize()));
				engine.setState(new WaitState());
			}
		}
		// End of game
		else
		{
			MinigamePlayer winner = minigame.getPlayers().get(0);
			Minigames.plugin.pdata.endMinigame(winner);
			engine.end();
		}
	}
}
