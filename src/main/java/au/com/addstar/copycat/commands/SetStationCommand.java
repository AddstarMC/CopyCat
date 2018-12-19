package au.com.addstar.copycat.commands;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.copycat.CopyCatModule;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PlayerStation;
import au.com.addstar.copycat.Util;
import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;

public class SetStationCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "setstation";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "copycat.command.create";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <minigame> <number> [show]";
	}

	@Override
	public String getDescription()
	{
		return "Sets the location of a player station for a board";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length != 2 && args.length != 3)
			return false;
		
		Location loc = ((Player)sender).getLocation();
		loc.setY(loc.getY()-1);
		BlockFace facing = Util.getFacing((Player)sender);
		
		String name = args[0];
		
		Minigame minigame = Minigames.getPlugin().getMinigameManager().getMinigame(args[0]);
		if(minigame == null)
			throw new BadArgumentException(1, "Unknown minigame " + args[0]);
		
		CopyCatModule module = CopyCatModule.getMinigameModule(minigame);
		
		if(!minigame.getMechanicName().equals("CopyCat") || module == null)
			throw new BadArgumentException(1, "Minigame is not a copycat game");
		
		GameBoard board = module.getGameBoard();
		
		if(board == null)
			throw new BadArgumentException(0, "Minigame has not been setup to be a copycat game. Please user /copycat setup <minigame> <maxplayers> <patternsize>");
		
		int number = 1;
		
		try
		{
			number = Integer.parseInt(args[1]);
			if(number <= 0 || number > board.getStationCount())
				throw new BadArgumentException(1, "Invalid station number. Must be between 1 and " + board.getStationCount());
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(1, "Expected station number. Must be between 1 and " + board.getStationCount());
		}
		
		PlayerStation station = board.getStation(number-1);
		station.setLocationAndFacing(loc, facing);
		if(args.length == 3 && args[2].equalsIgnoreCase("show"))
			station.displayLocations((Player)sender);
		
		sender.sendMessage(ChatColor.GREEN + "Station " + number + " was sucessfully set for " + name);
		
		minigame.saveMinigame();
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length == 1)
		{
			ArrayList<String> matches = new ArrayList<>();
			String toMatch = args[0].toLowerCase();
			for(String name : Minigames.getPlugin().getMinigameManager().getAllMinigames().keySet())
			{
				if(name.toLowerCase().startsWith(toMatch))
					matches.add(name);
			}
			
			return matches;
		}
		return null;
	}

}
