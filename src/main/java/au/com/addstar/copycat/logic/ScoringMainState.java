package au.com.addstar.copycat.logic;

import java.util.HashSet;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;

import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PlayerStation;

public class ScoringMainState extends MainState
{
	private int mNextComplete = 1;
	
	private HashSet<MinigamePlayer> mWaiting = new HashSet<>();
	
	@Override
	public void onStart( StateEngine<GameBoard> engine, GameBoard game )
	{
		Minigame minigame = game.getMinigame();
		mWaiting.clear();
		mWaiting.addAll(minigame.getPlayers());
		
		super.onStart(engine, game);
	}
	
	@Override
	public void onEnd( StateEngine<GameBoard> engine, GameBoard game )
	{
		Minigame minigame = game.getMinigame();
		super.onEnd(engine, game);
		
		MinigamePlayer winner = null;
		for(MinigamePlayer player : minigame.getPlayers())
		{
			if(player.getScore() >= minigame.getMaxScore())
			{
				winner = player;
				break;
			}
		}
		
		if(winner != null)
		{
			HashSet<MinigamePlayer> players = new HashSet<>(minigame.getPlayers());
			players.remove(winner);
			
			for(MinigamePlayer player : players)
			{
				PlayerStation station = game.getStation(player);
				station.setPlayer(null);
				Minigames.getPlugin().getPlayerManager().quitMinigame(player, true);
			}
		}
	}
	
	@Override
	public void onEvent( String name, Object data, StateEngine<GameBoard> engine, GameBoard game )
	{
		if(name.equals("place"))
		{
			MinigamePlayer player = (MinigamePlayer)data;
			
			PlayerStation station = game.getStation(player);
			if(game.getSubject().matches(station.getPlayLocation(), station.getFacing()))
			{
				station.setCanModify(false);
				player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.6f, 10);
				player.getPlayer().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getPlayer().getLocation(), 10, 1, 1, 1, 0);
				
				game.broadcast(player.getDisplayName() + " has completed the pattern!", null);
				BossBar bar = game.getBossDisplay();
				bar.setTitle(player.getDisplayName() + " Finished");
				bar.setColor(BarColor.PURPLE);
				bar.setProgress(1 - (mWaiting.size() / (float)game.getMinigame().getPlayers().size()));
				
				lastMessageTime = System.currentTimeMillis();
				
				int points = 0;
				switch(mNextComplete++)
				{
				case 1:
					points = 3;
					break;
				case 2:
					points = 2;
					break;
				case 3:
					points = 1;
					break;
				}

				if(points > 0)
				{
					player.addScore(points);
					game.getMinigame().setScore(player, player.getScore());
					if(points != 1)
					{
						game.broadcast(player.getDisplayName() + " was awarded " + points + " points.", player);
						player.sendMessage("You were awarded " + points + " points.", null);
					}
					else
					{
						game.broadcast(player.getDisplayName() + " was awarded " + points + " point.", player);
						player.sendMessage("You were awarded " + points + " point.", null);
					}
					
					if(player.getScore() >= game.getMinigame().getMaxScore())
					{
						engine.setState(new PreRoundState());
						return;
					}
				}
				
				mWaiting.remove(player);
				
				if(mWaiting.isEmpty())
					engine.setState(new BetweenRoundState());
			}
		}
	}
}
