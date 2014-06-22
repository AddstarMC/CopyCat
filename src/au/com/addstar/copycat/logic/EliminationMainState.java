package au.com.addstar.copycat.logic;

import java.util.HashSet;

import org.bukkit.Sound;
import org.bukkit.util.Vector;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.addstar.copycat.GameBoard;
import au.com.addstar.copycat.PlayerStation;
import au.com.addstar.monolith.MonoWorld;
import au.com.addstar.monolith.ParticleEffect;

public class EliminationMainState extends MainState
{
	private HashSet<MinigamePlayer> mWaiting = new HashSet<MinigamePlayer>();

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
			minigame.setScore(player, minigame.getLives() - player.getDeaths());

			player.sendMessage("You did not finish in time. You have lost a life", "error");
			game.broadcast(player.getDisplayName() + " lost a life.", player);
			game.getWorld().playSound(player.getPlayer().getLocation(), Sound.IRONGOLEM_HIT, 1f, 10);
			MonoWorld.getWorld(game.getWorld()).playParticleEffect(player.getPlayer().getLocation(), ParticleEffect.VILLAGER_ANGRY, 0, 10, new Vector(1, 1, 1));
			
			if(player.getDeaths() >= minigame.getLives())
			{
				player.sendMessage("You were eliminated from the game.", "error");
				
				Minigames.plugin.pdata.quitMinigame(player, true);
				
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
				game.getBossDisplay().setText(player.getDisplayName() + " Finished");
				lastMessageTime = System.currentTimeMillis();
				
				game.getWorld().playSound(player.getPlayer().getLocation(), Sound.LEVEL_UP, 1.6f, 10);
				MonoWorld.getWorld(game.getWorld()).playParticleEffect(player.getPlayer().getLocation(), ParticleEffect.VILLAGER_HAPPY, 0, 10, new Vector(1, 1, 1));
				
				mWaiting.remove(player);
				game.getBossDisplay().setPercent(1 - (mWaiting.size() / (float)game.getMinigame().getPlayers().size()));
				
				if(mWaiting.size() <= 1)
					engine.setState(new BetweenRoundState());
			}
		}
	}
}
