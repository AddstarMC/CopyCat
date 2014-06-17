package au.com.addstar.copycat;

import java.util.HashMap;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EditSession
{
	public static HashMap<Player, EditSession> activeSessions = new HashMap<Player, EditSession>();
	
	private Player mPlayer;
	private GameBoard mBoard;
	private PatternStation mStation;
	
	private Subject mSubject;
	
	private GameMode mMode;
	private ItemStack[] mInventory;
	
	public EditSession(GameBoard board, Player player)
	{
		Validate.isTrue(board.getPatternStation().isValid(), "That board has no defined pattern editor area.");
		mPlayer = player;
		mBoard = board;
		
		mStation = mBoard.getPatternStation();
	}
	
	@SuppressWarnings( "deprecation" )
	public void begin() throws IllegalArgumentException
	{
		if(mBoard.getEditSession() != null)
			throw new IllegalArgumentException("An edit session is already in progress for that board.");
		
		if(mBoard.isInProgress())
			throw new IllegalArgumentException("A game is in progress on that board.");
		
		if(!mPlayer.teleport(mStation.getSpawnLocation()))
			throw new IllegalArgumentException("Could not teleport to the pattern editor.");
		
		mMode = mPlayer.getGameMode();
		mPlayer.setGameMode(GameMode.CREATIVE);
		PlayerInventory inv = mPlayer.getInventory();
		mInventory = inv.getContents().clone();
		inv.clear();
		
		int slot = 0;
		for(ItemStack item : CopyCatPlugin.blockTypes)
			inv.setItem(++slot, item.clone());
		
		inv.setItem(0, new ItemStack(Material.DIAMOND_PICKAXE, 1));
		mPlayer.updateInventory();
		
		mStation.clearStation();
		mBoard.setEditSession(this);
		activeSessions.put(mPlayer, this);
		
		new ConversationFactory(CopyCatPlugin.instance)
			.withEscapeSequence("exit")
			.withLocalEcho(false)
			.withModality(false)
			.withFirstPrompt(new EditContext())
			.addConversationAbandonedListener(new EndHandler())
			.buildConversation(mPlayer)
			.begin();
		showHelp(mPlayer);
	}
	
	public boolean isLocationOk(Location loc)
	{
		return mStation.isInPatternArea(loc);
	}
	
	@SuppressWarnings( "deprecation" )
	public void end()
	{
		PlayerInventory inv = mPlayer.getInventory();
		inv.setContents(mInventory);
		mPlayer.updateInventory();
		mPlayer.setGameMode(mMode);
		mStation.clearStation();
		mBoard.setEditSession(null);
		activeSessions.remove(mPlayer);
	}
	
	private void showHelp(Conversable player)
	{
		player.sendRawMessage(ChatColor.translateAlternateColorCodes('&', "&6[CopyCat Editor] &fYou are in edit mode. Type &eexit &fto leave. Type &esave &fto save the pattern. Type &eclear &fto clear the pattern area. Type &eload &fto load an existing pattern. Type &edelete &fto delete the loaded pattern."));
	}
	
	private void onCommand(String command, Conversable player)
	{
		if(command.equalsIgnoreCase("save"))
		{
			Subject subject = mSubject;
			boolean ok = false;
			if(subject != null)
				ok = Subject.from(mStation.getPatternLocation(), mStation.getFacing(), mBoard.getSubjectSize(), subject);
			else
			{
				subject = Subject.from(mStation.getPatternLocation(), mStation.getFacing(), mBoard.getSubjectSize());
				ok = (subject != null);
			}
			
			if(ok)
			{
				if(CopyCatPlugin.instance.getSubjectStorage().add(subject))
				{
					player.sendRawMessage(ChatColor.GREEN + "Pattern saved");
					mSubject = null;
					mStation.clearStation();
				}
				else
					player.sendRawMessage(ChatColor.RED + "Could not save the pattern, an error occured");
			}
			else
				player.sendRawMessage(ChatColor.RED + "The pattern is not valid. It either contains gaps, or invalid blocks");
		}
		else if(command.equalsIgnoreCase("clear"))
		{
			mStation.clearStation();
			mSubject = null;
			player.sendRawMessage(ChatColor.GREEN + "Pattern editor cleared");
		}
		else if(command.equalsIgnoreCase("load"))
		{
			mSubject = CopyCatPlugin.instance.getSubjectStorage().getRandomSubject(mBoard.getSubjectSize());
			if(mSubject != null)
			{
				mSubject.placeAtFlat(mStation.getPatternLocation(), mStation.getFacing());
				player.sendRawMessage(ChatColor.GREEN + "Loaded a random pattern");
			}
			else
				player.sendRawMessage(ChatColor.RED + "There are no patterns available to load.");
		}
		else if(command.equalsIgnoreCase("delete"))
		{
			if(mSubject == null)
				player.sendRawMessage(ChatColor.RED + "You have no pattern loaded");
			else
			{
				if(CopyCatPlugin.instance.getSubjectStorage().remove(mSubject))
				{
					player.sendRawMessage(ChatColor.GREEN + "Pattern deleted");
					mStation.clearStation();
					mSubject = null;
				}
				else
					player.sendRawMessage(ChatColor.RED + "Could not delete pattern");
			}
		}
		else if(command.equalsIgnoreCase("help"))
		{
			showHelp(player);
		}
	}
	
	private class EndHandler implements ConversationAbandonedListener
	{
		@Override
		public void conversationAbandoned( ConversationAbandonedEvent event )
		{
			event.getContext().getForWhom().sendRawMessage(ChatColor.GOLD + "You are no longer in edit mode");
			end();
		}
	}
	
	private class EditContext extends StringPrompt
	{
		@Override
		public Prompt acceptInput( final ConversationContext context, final String input )
		{
			Bukkit.getScheduler().runTask(CopyCatPlugin.instance, new Runnable()
			{
				@Override
				public void run()
				{
					onCommand(input, context.getForWhom());
				}
			});
			return this;
		}

		@Override
		public String getPromptText( ConversationContext context )
		{
			return ChatColor.GOLD + "[CopyCat Editor] " + ChatColor.WHITE + "Enter a command. Type " + ChatColor.YELLOW + "exit" + ChatColor.WHITE + " to exit the editor.";
		}
	}
}
