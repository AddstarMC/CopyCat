package au.com.addstar.copycat;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import au.com.addstar.monolith.MonoPlayer;
import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.events.EndMinigameEvent;
import au.com.mineauz.minigames.events.StartMinigameEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.mechanics.GameMechanicBase;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.MinigameModule;

public class CopyCatLogic extends GameMechanicBase
{
	public CopyCatLogic()
	{
	}
	
	private GameBoard getBoard(Minigame minigame)
	{
		CopyCatModule module = CopyCatModule.getMinigameModule(minigame);
		
		if(module == null)
			return null;
		
		if(minigame.getMechanic().equals(this))
			return module.getGameBoard();
		return null;
	}
	
	@Override
	public void balanceTeam( List<MinigamePlayer> players, Minigame minigame )
	{
	}

	@Override
	public String getMechanic()
	{
		return "CopyCat";
	}
	
	@EventHandler
	public void onMinigameStart(StartMinigameEvent event)
	{
		GameBoard board = getBoard(event.getMinigame());
		if(board != null)
			board.beginGame();
	}
	
	@EventHandler
	public void onMinigameEnd(EndMinigameEvent event)
	{
		GameBoard board = getBoard(event.getMinigame());
		if(board != null)
		{
			for(MinigamePlayer player : event.getWinners())
				MonoPlayer.getPlayer(player.getPlayer()).setBossBarDisplay(null);
		}
	}

	private GameBoard getBoard(MinigamePlayer player)
	{
		if(player == null || !player.isInMinigame())
			return null;
			
		return getBoard(player.getMinigame());
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onBlockPlace(BlockPlaceEvent event)
	{
		MinigamePlayer player = Minigames.plugin.pdata.getMinigamePlayer(event.getPlayer());
		
		GameBoard board = getBoard(player);
		if(board != null)
		{
			if(!board.canModify(player, event.getBlock().getLocation()))
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
			if(!board.canModify(player, event.getBlock().getLocation()))
				event.setCancelled(true);
		}
	}

	@Override
	public boolean checkCanStart( Minigame minigame, MinigamePlayer player )
	{
		GameBoard board = getBoard(minigame);
		
		if(board == null)
		{
			player.sendMessage(ChatColor.RED + "No CopyCat board is linked with this minigame");
			return false;
		}
		
		List<String> errors = board.getErrors();
		if(!errors.isEmpty())
		{
			for(String error : errors)
				player.sendMessage(ChatColor.RED + error);

			return false;
		}
		
		if(minigame.getStartLocations().isEmpty())
			minigame.addStartLocation(board.getStation(0).getSpawnLocation());
		
		return true;
	}

	@Override
	public MinigameModule displaySettings( Minigame minigame )
	{
		return CopyCatModule.getMinigameModule(minigame);
	}

	@Override
	public void endMinigame( Minigame minigame, List<MinigamePlayer> winners, List<MinigamePlayer> losers )
	{
		GameBoard board = getBoard(minigame);
		if(board != null)
		{
			for(MinigamePlayer player : winners)
				MonoPlayer.getPlayer(player.getPlayer()).setBossBarDisplay(null);
		}
	}

	@Override
	public void joinMinigame( Minigame minigame, MinigamePlayer player )
	{
		GameBoard board = getBoard(minigame);
		if(player.isInMinigame())
			MonoPlayer.getPlayer(player.getPlayer()).setBossBarDisplay(board.getBossDisplay());
	}

	@Override
	public void quitMinigame( Minigame minigame, MinigamePlayer player, boolean forced )
	{
		GameBoard board = getBoard(minigame);
		if(board != null)
		{
			if(!forced)
				board.onPlayerLeave(player);
			
			MonoPlayer.getPlayer(player.getPlayer()).setBossBarDisplay(null);
		}
	}

	@Override
	public void startMinigame( Minigame minigame, MinigamePlayer player )
	{
		// Only for global type
	}

	@Override
	public void stopMinigame( Minigame minigame, MinigamePlayer player )
	{
		// Only for global type
	}

	@Override
	public EnumSet<MinigameType> validTypes()
	{
		return EnumSet.of(MinigameType.MULTIPLAYER);
	}
}
