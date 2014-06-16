package au.com.addstar.copycat.commands;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PatternStation;
import au.com.addstar.copycat.Util;

public class SetPatternStationCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "seteditor";
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
		return label + " <board> [show]";
	}

	@Override
	public String getDescription()
	{
		return "Sets the location of the pattern editing area for a board";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length != 1 && args.length != 2)
			return false;
		
		Location loc = ((Player)sender).getLocation();
		loc.setY(loc.getY()-1);
		BlockFace facing = Util.getFacing((Player)sender);
		
		String name = args[0];
		
		GameBoard board = CopyCatPlugin.instance.getBoard(name, loc.getWorld());
		if(board == null)
			throw new BadArgumentException(0, "Unknown game board " + name + " in " + loc.getWorld().getName());
		
		PatternStation station = board.getPatternStation();
		station.setLocationAndFacing(loc, facing);
		if(args.length == 2 && args[1].equalsIgnoreCase("show"))
			station.displayLocations((Player)sender);
		
		sender.sendMessage(ChatColor.GREEN + "Pattern editor area was sucessfully set for " + name);
		CopyCatPlugin.instance.saveBoard(name, loc.getWorld());
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length == 1)
			return CopyCatPlugin.instance.matchBoard(args[0], ((Player)sender).getWorld());
		return null;
	}

}
