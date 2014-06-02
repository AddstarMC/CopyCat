package au.com.addstar.copycat;

import java.util.List;

import org.bukkit.event.EventHandler;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.events.JoinMinigameEvent;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;
import com.pauldavdesign.mineauz.minigames.scoring.ScoreTypeBase;

public class CopyCatLogic extends ScoreTypeBase
{
	@Override
	public void balanceTeam( List<MinigamePlayer> players, Minigame minigame )
	{
		
	}

	@Override
	public String getType()
	{
		return "CopyCat";
	}
	
	@EventHandler
	
	public void onMinigameJoin(JoinMinigameEvent event)
	{
		
	}
	
}
