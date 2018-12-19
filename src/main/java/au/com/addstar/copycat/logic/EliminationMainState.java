package au.com.addstar.copycat.logic;

import java.util.HashSet;

import au.com.mineauz.minigames.MinigameMessageType;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;

import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PlayerStation;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;

public class EliminationMainState extends MainState
{
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
		super.onEnd(engine, game);
		
		Minigame minigame = game.getMinigame();
		
		for(MinigamePlayer player : mWaiting)
		{
			player.addDeath();
			minigame.setScore(player, Math.round(minigame.getLives() - player.getDeaths()));

			player.sendMessage("You did not finish in time. You have lost a life", MinigameMessageType.ERROR);
			game.broadcast(player.getDisplayName() + " lost a life.", player);
			player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1f, 10);
			player.getPlayer().getWorld().spawnParticle(Particle.VILLAGER_ANGRY, player.getPlayer().getLocation(), 10, 1, 1, 1, 0);
			
			if(player.getDeaths() >= minigame.getLives())
			{
				player.sendMessage("You were eliminated from the game.", MinigameMessageType.ERROR);
				PlayerStation station = game.getStation(player);
				station.setPlayer(null);
				
				Minigames.getPlugin().getPlayerManager().quitMinigame(player, true);
				
				if(minigame.getPlayers().size() > 1)
					game.broadcast(player.getDisplayName() + " was eliminated. Only " + minigame.getPlayers().size() + " players remain.", player);
				else
					game.broadcast(player.getDisplayName() + " was eliminated.", player);
			}

		}
	}
	
	@Override
	public void onEvent( String name, Object data, StateEngine<GameBoard> engine, GameBoard game )
	{
		if(name.equals("leave"))
		{
			MinigamePlayer player = (MinigamePlayer)data;
			mWaiting.remove(player);
		}
		else if(name.equals("place"))
		{
			MinigamePlayer player = (MinigamePlayer)data;
			
			PlayerStation station = game.getStation(player);
			if(game.getSubject().matches(station.getPlayLocation(), station.getFacing()))
			{
				station.setCanModify(false);
				game.broadcast(player.getDisplayName() + " has completed the pattern!", null);
				BossBar bar = game.getBossDisplay();
				bar.setTitle(player.getDisplayName() + " Finished");
				lastMessageTime = System.currentTimeMillis();
				
				player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.6f, 10);
				player.getPlayer().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getPlayer().getLocation(), 10, 1, 1, 1, 0);
				
				mWaiting.remove(player);
				bar.setProgress(1 - (mWaiting.size() / (float)game.getMinigame().getPlayers().size()));
				bar.setColor(BarColor.PURPLE);
				
				if(mWaiting.size() <= 1)
					engine.setState(new BetweenRoundState());
			}
		}
	}
}
