package au.com.addstar.copycat.commands;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.copycat.CopyCatModule;
import au.com.addstar.copycat.EditSession;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;

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
		return label + " <minigame>";
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
		Minigame minigame = Minigames.getPlugin().getMinigameManager().getMinigame(args[0]);
		if(minigame == null)
			throw new BadArgumentException(1, "Unknown minigame " + args[0]);
		
		CopyCatModule module = CopyCatModule.getMinigameModule(minigame);
		
		if(!minigame.getMechanicName().equals("CopyCat") || module == null)
			throw new BadArgumentException(1, "Minigame is not a copycat game");
		
		GameBoard board = module.getGameBoard();
		
		if(board == null)
			throw new BadArgumentException(0, "Minigame has not been setup to be a copycat game. Please user /copycat create <minigame> <maxplayers> <patternsize>");
		
		
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
		{
			ArrayList<String> matches = new ArrayList<String>();
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
