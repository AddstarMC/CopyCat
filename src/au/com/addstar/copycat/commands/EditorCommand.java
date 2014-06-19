package au.com.addstar.copycat.commands;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.EditSession;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;

public class EditorCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "editor";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "patterneditor", "patternedit" };
	}

	@Override
	public String getPermission()
	{
		return "copycat.command.editor";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <board>";
	}

	@Override
	public String getDescription()
	{
		return "Enters pattern editing mode on the specified board.";
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
		
		Player player = (Player)sender;
		GameBoard board = CopyCatPlugin.instance.getBoard(args[0], player.getWorld());
		
		if(board == null)
			throw new BadArgumentException(0, "Unknown board");
		
		if(EditSession.activeSessions.containsKey(player))
			throw new IllegalArgumentException("You are already in an edit session");
		
		EditSession session = new EditSession(board, player);
		session.begin();
		
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
