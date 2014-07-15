package au.com.addstar.copycat.commands;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.mineauz.minigames.Minigames;

import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;

public class CreateCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "create";
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
		return label + " <name> <minigame> <players> <boardsize>";
	}

	@Override
	public String getDescription()
	{
		return "Creates a CopyCat game board. Once it has been created, you need to run /copycat setstation <board> <number>";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length != 4)
			return false;
		
		World world = ((Player)sender).getWorld();
		
		String name = args[0];
		if(!CopyCatPlugin.validNamePattern.matcher(name).matches())
			throw new BadArgumentException(0, "Name contains invalid characters");
		
		if(CopyCatPlugin.instance.getBoard(name, world) != null)
			throw new BadArgumentException(0, name + " already exists in " + world.getName());
		
		int players = 0;
		int size = 0;
		
		String minigame = args[1];
		if(Minigames.plugin.mdata.getMinigame(minigame) == null)
			throw new BadArgumentException(1, "Unknown minigame");
		
		try
		{
			players = Integer.parseInt(args[2]);
			if(players < 2)
				throw new BadArgumentException(2, "You need at least 2 players");
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(2, "Expected player count.");
		}
		
		try
		{
			size = Integer.parseInt(args[3]);
			if(size < 4)
				throw new BadArgumentException(3, "Size should be at least 4");
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(3, "Expected board size. 8 is the normal size.");
		}
		
		GameBoard board = new GameBoard(players, size, minigame, world);
		if(CopyCatPlugin.instance.registerGame(board, name))
		{
			CopyCatPlugin.applyDefaults(board.getMinigame());
			
			int dimX = size + 4;
			int dimZ = size + 6;
			int dimY = size + 5;
			
			sender.sendMessage(ChatColor.GREEN + "Game board '" + name + "' created in " + world.getName());
			sender.sendMessage(ChatColor.GREEN + "This game will support up to " + players + " players.");
			sender.sendMessage(ChatColor.YELLOW + "You now need to set up the stations where players can play. Stand in the lower left corner of where you want the board to be and face the direciton you want. Then use " + ChatColor.RED + "/copycat setstation " + name + " <number>" + ChatColor.YELLOW + " to set the station. The stations dimentions will be " + dimX + "x" + dimY + "x" + dimZ + " in the direciton you face.");
		}
		else
			sender.sendMessage(ChatColor.RED + "Failed to add game board. An error occured.");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
