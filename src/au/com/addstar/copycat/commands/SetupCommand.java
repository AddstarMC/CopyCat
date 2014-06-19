package au.com.addstar.copycat.commands;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;

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
		return label + " <board>";
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
		if(args.length != 1)
			return false;
		
		World world = ((Player)sender).getWorld();
		String name = args[0];
		GameBoard board = CopyCatPlugin.instance.getBoard(name, world);
		
		if(board == null)
			throw new BadArgumentException(0, "Unknown board " + name);
		
		List<String> errors = board.getErrors();
		
		if(!errors.isEmpty())
		{
			sender.sendMessage(ChatColor.RED + "The board is incomplete. Cannot setup the game for play:");
			for(String error : errors)
				sender.sendMessage(ChatColor.GRAY + "* " + error);
			
			return true;
		}

		CopyCatPlugin.applyDefaultsForGame(board);
		sender.sendMessage(ChatColor.GREEN + name + " is now ready to play!");
		
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
