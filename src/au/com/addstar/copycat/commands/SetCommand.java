package au.com.addstar.copycat.commands;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.Util;
import au.com.addstar.copycat.flags.Flag;

public class SetCommand implements ICommand
{
	public SetCommand()
	{
	}
	
	@Override
	public String getName()
	{
		return "set";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "copycat.command.set";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <board> <param> <value>";
	}

	@Override
	public String getDescription()
	{
		return "Allows you to set a copycat parameter";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length < 3)
			return false;
		
		GameBoard board = CopyCatPlugin.instance.getBoard(args[0], ((Player)sender).getWorld());
		
		if(board == null)
			throw new BadArgumentException(0, "Unknown game board " + args[0] + " in " + ((Player)sender).getWorld().getName());
		
		@SuppressWarnings( "unchecked" )
		Flag<Object> flag = (Flag<Object>)board.getFlag(args[1]);
		
		if(flag == null)
		{
			BadArgumentException ex = new BadArgumentException(1, "Unknown option.");
			ex.addInfo(ChatColor.GOLD + "Available options: ");
			
			String options = "";
			
			for(String name : board.getFlags().keySet())
			{
				if(!options.isEmpty())
					options += ", ";
				options += name;
			}
			ex.addInfo(ChatColor.GRAY + options);
			
			throw ex;
		}
		
		if(args.length == 2)
		{
			sender.sendMessage(ChatColor.GREEN + args[1] + " is set to " + flag.getValueString());
			return true;
		}
		
		Object result = null;
		try
		{
			Player player = (sender instanceof Player ? (Player)sender : null);
			result = flag.parse(player, Arrays.copyOfRange(args, 2, args.length));
		}
		catch(IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + String.format("Usage: %s%s %s %s %s", parent, label, args[0], args[1], e.getMessage()));
			return true;
		}
		catch(BadArgumentException e)
		{
			String cmdString = ChatColor.GRAY + parent + label;
			for(int i = 0; i < args.length; ++i)
			{
				cmdString += " ";
				if(i == e.getArgument() + 2)
					cmdString += ChatColor.RED + args[i] + ChatColor.GRAY;
				else
					cmdString += args[i];
			}
			
			sender.sendMessage(ChatColor.RED + "Error in command: " + cmdString);
			sender.sendMessage(ChatColor.RED + " " + e.getMessage());
			return true;
		}
		
		Object lastValue = flag.getValue();
		flag.setValue(result);
		board.onFlagChanged(args[1], flag, lastValue);
		CopyCatPlugin.instance.saveBoard(args[0], ((Player)sender).getWorld());
		
		sender.sendMessage(ChatColor.GREEN + args[1] + " has been set to " + flag.getValueString());

		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length == 1)
			return CopyCatPlugin.instance.matchBoard(args[0], ((Player)sender).getWorld());
		else if(args.length > 1)
		{
			GameBoard board = CopyCatPlugin.instance.getBoard(args[0], ((Player)sender).getWorld());
			if(board == null)
				return null;
			
			if(args.length == 2)
				return Util.matchString(args[1], board.getFlags().keySet());
			else
			{
				@SuppressWarnings( "unchecked" )
				Flag<Object> flag = (Flag<Object>)board.getFlag(args[1]);
				if(flag == null)
					return null;
				
				Player player = (sender instanceof Player ? (Player)sender : null);
				return flag.tabComplete(player, Arrays.copyOfRange(args, 2, args.length));
			}
		}
		
		return null;
	}

}
