package au.com.addstar.copycat.commands;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import au.com.addstar.copycat.CopyCatModule;
import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;

public class SetupCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "setup";
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
		return label + " <board> <players> <boardsize>";
	}

	@Override
	public String getDescription()
	{
		return "Sets up the board and minigame ready to play";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length != 3)
			return false;
		
		Minigame minigame = Minigames.plugin.mdata.getMinigame(args[0]);
		if(minigame == null)
			throw new BadArgumentException(1, "Unknown minigame " + args[0]);
		
		int players;
		int size;
		
		try
		{
			players = Integer.parseInt(args[1]);
			if(players < 2)
				throw new BadArgumentException(1, "You need at least 2 players");
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(1, "Expected player count.");
		}
		
		try
		{
			size = Integer.parseInt(args[2]);
			if(size < 4)
				throw new BadArgumentException(2, "Size should be at least 4");
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(2, "Expected board size. 8 is the normal size.");
		}
		
		CopyCatModule module = CopyCatModule.getMinigameModule(minigame);
		GameBoard board = null;
		
		if(module != null)
			board = module.getGameBoard();
		
		CopyCatPlugin.applyDefaults(minigame);
		
		if(board == null)
		{
			board = new GameBoard(players, size);
			board.initialize(CopyCatModule.getMinigameModule(minigame));
			module.setGameBoard(board);
		}
		else
		{
			board.setStationCount(players);
			board.setSubjectSize(size);
		}
		
		if(board.getStation(0).isValid())
			CopyCatPlugin.applyDefaultsForGame(board);
		
		minigame.saveMinigame();
		
		sender.sendMessage(ChatColor.GREEN + minigame.getName(false) + " has been setup to be a CopyCat game.");
		sender.sendMessage(ChatColor.GREEN + "This game will support up to " + players + " players.");
		sender.sendMessage(ChatColor.YELLOW + "You now need to set up the stations where players can play. Stand in the lower left corner of where you want the board to be and face the direction you want. Then use " + ChatColor.RED + "/copycat setstation " + args[0] + " <number>" + ChatColor.YELLOW + " to set the station.");
		sender.sendMessage(ChatColor.YELLOW + "If pattern drawing is enabled, you will need to setup a pattern drawing area. Stand in the lower left corner of where you want the area to be and face the direction you want. Then use " + ChatColor.RED + "/copycat seteditor " + args[0] + "" + ChatColor.YELLOW + " to set the editor.");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length == 1)
		{
			ArrayList<String> matches = new ArrayList<String>();
			String toMatch = args[0].toLowerCase();
			for(String name : Minigames.plugin.mdata.getAllMinigames().keySet())
			{
				if(name.toLowerCase().startsWith(toMatch))
					matches.add(name);
			}
			
			return matches;
		}
		return null;
	}

}
