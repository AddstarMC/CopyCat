package au.com.addstar.copycat.commands;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.copycat.CopyCatPlugin;

public class DeleteCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "delete";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "copycat.command.delete";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <board>";
	}

	@Override
	public String getDescription()
	{
		return "Deletes a game board from the world";
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
		
		CopyCatPlugin.instance.deleteBoard(args[0], ((Player)sender).getWorld());
		
		sender.sendMessage(ChatColor.GREEN + args[0] + " was successfully deleted.");
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
