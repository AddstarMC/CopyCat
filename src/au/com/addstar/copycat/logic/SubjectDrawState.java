package au.com.addstar.copycat.logic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.LoadoutModule;
import au.com.addstar.copycat.CopyCatPlugin;
import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PatternStation;
import au.com.addstar.copycat.PlayerStation;
import au.com.addstar.copycat.Subject;
import au.com.addstar.copycat.Util;

public class SubjectDrawState extends TimerState
{
	private Conversation mConversation;
	private MinigamePlayer mPlayer;
	
	private boolean mSkip;
	
	private StateEngine<GameBoard> mEngine;
	private GameBoard mBoard;
	
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		mEngine = engine;
		mBoard = game;
		Minigame minigame = game.getMinigame();
		MinigamePlayer player = mPlayer = game.selectNextDrawer();
		PatternStation station = game.getPatternStation();
		
		game.broadcast(player.getDisplayName() + " is drawing the pattern.", player);
		player.teleport(station.getSpawnLocation());
		station.setPlayer(player);
		player.sendMessage("Draw the pattern in the area in front of you. You have " + Util.getTimeRemainString(game.getSubjectDrawTime()) + ". If you do not fill every block, a random pattern will be used.", "win");
		
		LoadoutModule module = LoadoutModule.getMinigameModule(minigame);
		PlayerLoadout defaultLoadout = module.getLoadout("default");
		defaultLoadout.equiptLoadout(player);
		
		endTime = System.currentTimeMillis() + game.getSubjectDrawTime();
		
		mConversation = new ConversationFactory(CopyCatPlugin.instance)
			.withFirstPrompt(new EditConvo())
			.withModality(false)
			.buildConversation(player.getPlayer());
		
		mConversation.begin();
	}
	
	@Override
	public void onEnd( StateEngine<GameBoard> engine, GameBoard game )
	{
		mConversation.abandon();
		
		MinigamePlayer player = game.getDrawer();
		PlayerStation playerStation = game.getStation(player);
		PatternStation station = game.getPatternStation();
		station.setPlayer(null);
		player.teleport(playerStation.getSpawnLocation());

		if(mSkip)
		{
			game.broadcast("Selecting a random pattern.", null);
			game.setSubject(CopyCatPlugin.instance.getSubjectStorage().getRandomSubject(game.getSubjectSize()));
		}
		else
		{
			Subject subject = Subject.from(station.getPatternLocation(), station.getFacing(), game.getSubjectSize());
			if(subject == null)
			{
				game.broadcast("Pattern was not completed in time. Selecting a random pattern.", null);
				game.setSubject(CopyCatPlugin.instance.getSubjectStorage().getRandomSubject(game.getSubjectSize()));
			}
			else
			{
				game.broadcast("Using pattern created by " + player.getDisplayName(), null);
				game.setSubject(subject);
				
				if(game.getSaveSubjects())
					CopyCatPlugin.instance.getSubjectStorage().add(subject);
			}
		}
		
		station.clearStation();
		game.setDrawer(null);
	}
	
	@Override
	protected void onNotifyTimeLeft( long remaining, StateEngine<GameBoard> engine, GameBoard game )
	{
		if(remaining > 0)
		{
			game.broadcast("Round starts in " + Util.getTimeRemainString(remaining), game.getDrawer());
			game.getDrawer().sendMessage("You have " + Util.getTimeRemainString(remaining) + " to complete your drawing", null);
		}
		else
			engine.setState(game.getMainState());
	}
	
	@Override
	public void onEvent( String name, Object data, StateEngine<GameBoard> engine, GameBoard game )
	{
		if(name.equals("leave"))
		{
			MinigamePlayer player = (MinigamePlayer)data;
			if(player == game.getDrawer())
			{
				mConversation.abandon();
				game.setDrawer(null);
				engine.abortState(this);
			}
		}
	}
	
	private void onInput(String input)
	{
		if(input.equalsIgnoreCase("skip"))
		{
			mPlayer.sendMessage("You have elected to use a random pattern.", null);
			mSkip = true;
			mEngine.setState(mBoard.getMainState());
		}
		else if(input.equalsIgnoreCase("done"))
		{
			PatternStation station = mBoard.getPatternStation();
			Subject subject = Subject.from(station.getPatternLocation(), station.getFacing(), mBoard.getSubjectSize());
			if(subject == null)
				mPlayer.sendMessage("Your pattern is incomplete. All spots must be used.");
			else
				mEngine.setState(mBoard.getMainState());
		}
	}
	
	private class EditConvo extends StringPrompt
	{
		@Override
		public Prompt acceptInput( ConversationContext context, final String input )
		{
			Bukkit.getScheduler().runTask(CopyCatPlugin.instance, new Runnable()
			{
				@Override
				public void run()
				{
					onInput(input);
				}
			});
			return this;
		}
		
		@Override
		public String getPromptText( ConversationContext context )
		{
			return ChatColor.translateAlternateColorCodes('&', "You are drawing. Type &eskip &fto use a random pattern. Type &edone&f to say finish drawing");
		}
	}
}
