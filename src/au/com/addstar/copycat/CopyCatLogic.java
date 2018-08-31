package au.com.addstar.copycat;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import au.com.mineauz.minigames.MinigameMessageType;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.events.EndMinigameEvent;
import au.com.mineauz.minigames.events.StartMinigameEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.mechanics.GameMechanicBase;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;

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
	public List<MinigamePlayer> balanceTeam( List<MinigamePlayer> players, Minigame minigame )
	{
		return Collections.EMPTY_LIST;
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
				board.getBossDisplay().removePlayer(player.getPlayer());
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
		MinigamePlayer player = Minigames.getPlugin().getPlayerManager().getMinigamePlayer(event.getPlayer());
		
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
		MinigamePlayer player = Minigames.getPlugin().getPlayerManager().getMinigamePlayer(event.getPlayer());
		
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
			player.sendMessage(ChatColor.RED + "No CopyCat board is linked with this minigame", MinigameMessageType.ERROR);
			return false;
		}
		
		List<String> errors = board.getErrors();
		if(!errors.isEmpty())
		{
			for(String error : errors)
				player.sendMessage(error,MinigameMessageType.ERROR);

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
				board.getBossDisplay().removePlayer(player.getPlayer());
		}
	}


	@Override
	public void quitMinigame( Minigame minigame, MinigamePlayer player, boolean forced )
	{
		GameBoard board = getBoard(minigame);
		if(board != null)
		{
			if(!forced)
				board.onPlayerLeave(player);
			
			board.getBossDisplay().removePlayer(player.getPlayer());
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
	public void onJoinMinigame(Minigame minigame, MinigamePlayer player) {
		GameBoard board = getBoard(minigame);
		if(player.isInMinigame())
			board.getBossDisplay().addPlayer(player.getPlayer());
	}

	@Override
	public EnumSet<MinigameType> validTypes()
	{
		return EnumSet.of(MinigameType.MULTIPLAYER);
	}
}
