package au.com.addstar.copycat;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.events.JoinMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.QuitMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.StartMinigameEvent;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;
import com.pauldavdesign.mineauz.minigames.scoring.ScoreTypeBase;

public class CopyCatLogic extends ScoreTypeBase
{
	@Override
	public void balanceTeam( List<MinigamePlayer> players, Minigame minigame )
	{
		System.out.println("Balancing team");
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
	
	@EventHandler
	public void onMinigameStart(StartMinigameEvent event)
	{
		GameBoard board = CopyCatPlugin.instance.getBoardByGame(event.getMinigame().getName(false));
		if(board != null)
			board.beginGame();
	}
	
	@EventHandler
	public void onMinigameQuit(QuitMinigameEvent event)
	{
		GameBoard board = CopyCatPlugin.instance.getBoardByGame(event.getMinigame().getName(false));
		if(board != null)
			board.onPlayerLeave(event.getMinigamePlayer());
	}

	private GameBoard getBoard(MinigamePlayer player)
	{
		if(!player.isInMinigame())
			return null;
			
		return CopyCatPlugin.instance.getBoardByGame(player.getMinigame().getName(false));
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onBlockPlace(BlockPlaceEvent event)
	{
		MinigamePlayer player = Minigames.plugin.pdata.getMinigamePlayer(event.getPlayer());
		GameBoard board = getBoard(player);
		if(board != null)
		{
			PlayerStation station = board.getStation(player);
			if(!station.isInPlayArea(event.getBlock().getLocation()) || !station.getCanModify())
				event.setCancelled(true);
			else
				board.onPlaceBlock(player);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onBlockBreak(BlockBreakEvent event)
	{
		MinigamePlayer player = Minigames.plugin.pdata.getMinigamePlayer(event.getPlayer());
		GameBoard board = getBoard(player);
		if(board != null)
		{
			PlayerStation station = board.getStation(player);
			if(!station.isInPlayArea(event.getBlock().getLocation()) || !station.getCanModify())
				event.setCancelled(true);
		}
	}
}
