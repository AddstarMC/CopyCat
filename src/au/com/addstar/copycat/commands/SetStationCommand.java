package au.com.addstar.copycat.commands;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PlayerStation;
import au.com.addstar.copycat.Util;

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
		return label + " <board> <number> [show]";
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
		
		GameBoard board = CopyCatPlugin.instance.getBoard(name, loc.getWorld());
		if(board == null)
			throw new BadArgumentException(0, "Unknown game board " + name + " in " + loc.getWorld().getName());
		
		int number = 0;
		
		try
		{
			number = Integer.parseInt(args[1]);
			if(number < 0 || number > board.getStationCount())
				throw new BadArgumentException(1, "Invalid station number. Must be between 0 and " + board.getStationCount());
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(1, "Expected station number. Must be between 0 and " + board.getStationCount());
		}
		
		PlayerStation station = board.getStation(number);
		station.setLocationAndFacing(loc, facing);
		if(args.length == 3 && args[2].equalsIgnoreCase("show"))
			station.displayLocations((Player)sender);
		
		CopyCatPlugin.instance.saveBoard(name, loc.getWorld());
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
