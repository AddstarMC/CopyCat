package au.com.addstar.copycat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

public class GameBoard
{
	private enum GameState
	{
		Initial,
		Waiting,
		Main,
		SubjectDraw
	}
	
	private PlayerStation[] mStations;
	private int mSize;
	private String mMinigameId;
	private World mWorld;
	private int mSubjectDrawTime = 60;
	private boolean mAllowSubjectDraw = true;
	private int mWaitTime = 10;
	private boolean mSaveSubjects = true;
	
	// In game vars
	
	private GameState mState;
	private GameState mNextState;
	private long mStateEnd;
	private int mNextToDraw;
	private long mLastNotify;
	private Subject mSubject;
	
	private HashSet<MinigamePlayer> mWaiting = new HashSet<MinigamePlayer>();
	
	private MinigamePlayer mSubjectDrawer;
	private BukkitTask mTask;
	
	public GameBoard(int players, int size, String minigame, World world)
	{
		mStations = new PlayerStation[players];
		
		for(int i = 0; i < players; ++i)
			mStations[i] = new PlayerStation(this);
		
		mSize = size;
		mMinigameId = minigame;
		mWorld = world;
	}
	
	public GameBoard(File file, World world) throws IOException, InvalidConfigurationException
	{
		mWorld = world;
		read(file);
	}
	
	public World getWorld()
	{
		return mWorld;
	}
	
	public int getSubjectSize()
	{
		return mSize;
	}
	
	public Subject getSubject()
	{
		return mSubject;
	}
	
	public boolean isValid()
	{
		return getErrors().isEmpty();
	}
	
	public List<String> getErrors()
	{
		ArrayList<String> errors = new ArrayList<String>();
		if(getMinigame() == null)
			errors.add("Minigame " + mMinigameId + " does not exist");
		
		for(int i = 0; i < mStations.length; ++i)
		{
			if(!mStations[i].isValid())
				errors.add("Player station " + (i+1) + " is not set");
		}
		
		return errors;
	}
	
	public String getMinigameId()
	{
		return mMinigameId;
	}
	
	public Minigame getMinigame()
	{
		return Minigames.plugin.mdata.getMinigame(mMinigameId);
	}
	
	public int getStationCount()
	{
		return mStations.length;
	}
	
	public PlayerStation getStation(int number)
	{
		return mStations[number];
	}
	
	public PlayerStation getStation(MinigamePlayer player)
	{
		for(PlayerStation station : mStations)
		{
			if(station.getPlayer() == player)
				return station;
		}
		
		return null;
	}
	
	public void write(File file) throws IOException
	{
		YamlConfiguration config = new YamlConfiguration();
		
		config.set("Minigame", mMinigameId);
		config.set("Size", mSize);
		config.set("StationCount", mStations.length);
		for(int i = 0; i < mStations.length; ++i)
		{
			ConfigurationSection section = config.createSection("Station" + i);
			mStations[i].save(section);
		}
		
		config.save(file);
	}
	
	public void read(File file) throws IOException, InvalidConfigurationException
	{
		YamlConfiguration config = new YamlConfiguration();
		config.load(file);
		
		mMinigameId = config.getString("Minigame");
		mSize = config.getInt("Size");
		int count = config.getInt("StationCount");
		mStations = new PlayerStation[count];
		for(int i = 0; i < count; ++i)
		{
			ConfigurationSection section = config.getConfigurationSection("Station" + i);
			mStations[i] = new PlayerStation(this);
			mStations[i].read(section);
		}
	}
	
	public void broadcast(String message, MinigamePlayer except)
	{
		for(MinigamePlayer player : getMinigame().getPlayers())
		{
			if(except != player)
				player.sendMessage(message, null);
		}
	}
	
	private void startSubjectDraw()
	{
		Minigame minigame = getMinigame();
		
		mState = GameState.SubjectDraw;
		mSubjectDrawer = minigame.getPlayers().get(mNextToDraw);
		broadcast(mSubjectDrawer.getDisplayName() + " is drawing the pattern.", mSubjectDrawer);
		mSubjectDrawer.sendMessage("Draw the pattern in your play area. You have " + Util.getTimeRemainString(mSubjectDrawTime * 1000) + ". If you do not fill every block, a random pattern will be used.", "win");
		minigame.getDefaultPlayerLoadout().equiptLoadout(mSubjectDrawer);
		
		mStateEnd = System.currentTimeMillis() + mSubjectDrawTime * 1000;
		
		++mNextToDraw;
		if(mNextToDraw >= minigame.getPlayers().size())
			mNextToDraw = 0;
	}
	
	private void selectSubject()
	{
		SubjectStorage storage = CopyCatPlugin.instance.getSubjectStorage();
		mSubject = storage.getRandomSubject(mSize);
	}
	
	private void waitFor(long time, GameState next)
	{
		mState = GameState.Waiting;
		mStateEnd = System.currentTimeMillis() + time;
		mLastNotify = System.currentTimeMillis();
		mNextState = next;
	}
	
	public void beginGame()
	{
		Minigame minigame = getMinigame();
		mNextToDraw = CopyCatPlugin.rand.nextInt(minigame.getPlayers().size());
		mState = GameState.Initial;
		
		// Assign stations
		ArrayList<MinigamePlayer> excess = new ArrayList<MinigamePlayer>(); 
		for(int i = 0; i < minigame.getPlayers().size(); ++i)
		{
			MinigamePlayer player = minigame.getPlayers().get(i);
			
			if(i > mStations.length)
				excess.add(player);
			else
			{
				mStations[i].setPlayer(player);
				player.teleport(mStations[i].getSpawnLocation());
			}
		}
		
		// Remove excess
		for(MinigamePlayer player : excess)
		{
			player.sendMessage("There was not enough spots to place players", "error");
			Minigames.plugin.pdata.quitMinigame(player, true);
		}
		
		for(PlayerStation station : mStations)
			station.clearStation();
		
		// Begin the game
		mTask = Bukkit.getScheduler().runTaskTimer(CopyCatPlugin.instance, new GameTimer(), 5, 5);
		doLogic();
	}
	
	public void onPlayerLeave(MinigamePlayer player)
	{
		Minigame minigame = getMinigame();
		PlayerStation station = getStation(player);
		station.clearStation();
		
		if(mNextToDraw >= minigame.getPlayers().size())
			mNextToDraw = 0;
		
		if(mState == GameState.SubjectDraw && player == mSubjectDrawer)
			startSubjectDraw();
		
		if(minigame.getPlayers().size() <= 2)
		{
			System.out.println("Game end");
			endGame();
		}
	}
	
	private void handleWaitLogic()
	{
		long left = mStateEnd - System.currentTimeMillis();;
		if(left <= 0)
		{
			mState = mNextState;
			mNextState = null;
			mStateEnd = 0;
		}
		else
		{
			if(left >= 30000)
			{
				if(System.currentTimeMillis() - mLastNotify >= 15000)
				{
					broadcast("Round starts in " + Util.getTimeRemainString(left), null);
					mLastNotify = System.currentTimeMillis();
				}
			}
			else if(left >= 10000)
			{
				if(System.currentTimeMillis() - mLastNotify >= 5000)
				{
					broadcast("Round starts in " + Util.getTimeRemainString(left), null);
					mLastNotify = System.currentTimeMillis();
				}
			}
			else
			{
				if(System.currentTimeMillis() - mLastNotify >= 1000)
				{
					broadcast("Round starts in " + Util.getTimeRemainString(left), null);
					mLastNotify = System.currentTimeMillis();
				}
			}
		}
	}
	
	private void handleSubjectDrawLogic()
	{
		long left = mStateEnd - System.currentTimeMillis();;
		if(left <= 0)
		{
			PlayerStation station = getStation(mSubjectDrawer);
			Subject subject = Subject.from(station.getPlayLocation(), station.getFacing(), mSize);
			if(subject == null)
			{
				broadcast("Pattern was not completed in time. Selecting a random pattern.", null);
				selectSubject();
			}
			else
			{
				broadcast("Using pattern created by " + mSubjectDrawer.getDisplayName(), null);
				mSubject = subject;
				
				if(mSaveSubjects)
					CopyCatPlugin.instance.getSubjectStorage().add(mSubject);
			}
			
			station.clearStation();
			mSubjectDrawer = null;
			mState = GameState.Main;
			mStateEnd = 0;
		}
		else
		{
			if(left >= 30000)
			{
				if(System.currentTimeMillis() - mLastNotify >= 15000)
				{
					broadcast("Round starts in " + Util.getTimeRemainString(left), mSubjectDrawer);
					mSubjectDrawer.sendMessage("You have " + Util.getTimeRemainString(left) + " to complete your drawing", "win");
					mLastNotify = System.currentTimeMillis();
				}
			}
			else if(left >= 10000)
			{
				if(System.currentTimeMillis() - mLastNotify >= 5000)
				{
					broadcast("Round starts in " + Util.getTimeRemainString(left), mSubjectDrawer);
					mSubjectDrawer.sendMessage("You have " + Util.getTimeRemainString(left) + " to complete your drawing", "win");
					mLastNotify = System.currentTimeMillis();
				}
			}
			else
			{
				if(System.currentTimeMillis() - mLastNotify >= 1000)
				{
					broadcast("Round starts in " + Util.getTimeRemainString(left), mSubjectDrawer);
					mSubjectDrawer.sendMessage("You have " + Util.getTimeRemainString(left) + " to complete your drawing", "win");
					mLastNotify = System.currentTimeMillis();
				}
			}
		}
	}
	
	private void handleMain()
	{
		if(mStateEnd == 0)
		{
			Minigame minigame = getMinigame();
			broadcast("Start copying", null);
			mWaiting.clear();
			mWaiting.addAll(minigame.getPlayers());
			
			for(PlayerStation station : mStations)
			{
				if(station.getPlayer() != null)
					station.drawSubject();
			}
			
			for(MinigamePlayer player : minigame.getPlayers())
				minigame.getDefaultPlayerLoadout().equiptLoadout(player);
			mStateEnd = 1;
		}
	}

	private void eliminateLast()
	{
		for(PlayerStation station : mStations)
		{
			station.clearStation();
		}
		
		Minigame minigame = getMinigame();
		MinigamePlayer player = mWaiting.iterator().next();
		player.addDeath();

		player.sendMessage("You did not finish in time. You have lost a life", "error");
		broadcast(player.getDisplayName() + " lost a life.", player);
		
		if(player.getDeaths() >= minigame.getLives())
		{
			player.sendMessage("You were eliminated from the game.", "error");
			
			Minigames.plugin.pdata.quitMinigame(player, true);
			if(minigame.getPlayers().size() > 1)
				broadcast(player.getDisplayName() + " was eliminated. Only " + (minigame.getPlayers().size() - 1) + " players remain.", player);
			else
				broadcast(player.getDisplayName() + " was eliminated.", player);
		}
		
		// More players to eliminate.
		if(minigame.getPlayers().size() > 1)
		{
			if(mAllowSubjectDraw)
				startSubjectDraw();
			else
			{
				waitFor(mWaitTime / 2 * 1000, GameState.Main);
				selectSubject();
				broadcast("Selecting random pattern. Round starts in " + Util.getTimeRemainString(mWaitTime * 1000), null);
			}
		}
		// End of game
		else
		{
			MinigamePlayer winner = minigame.getPlayers().get(0);
			
			Minigames.plugin.pdata.endMinigame(winner);
			mState = GameState.Initial;
			endGame();
		}
	}
	
	public boolean canModify(MinigamePlayer player)
	{
		if(mState == GameState.Main)
		{
			if(mWaiting.contains(player))
				return true;
			return false;
		}
		else if(mState == GameState.SubjectDraw)
			return (mSubjectDrawer == player);
		
		return false;
	}
	
	public void onPlaceBlock(MinigamePlayer player)
	{
		if(mState == GameState.Main)
		{
			if(mWaiting.contains(player))
			{
				PlayerStation station = getStation(player);
				if(mSubject.matches(station.getPlayLocation(), station.getFacing()))
				{
					broadcast(player.getDisplayName() + " has completed the pattern!", null);
					mWaiting.remove(player);
					
					if(mWaiting.size() <= 1)
						eliminateLast();
				}
			}
		}
	}
	
	public void doLogic()
	{
		switch(mState)
		{
		case Initial:
		{
			if(mAllowSubjectDraw)
				startSubjectDraw();
			else
			{
				waitFor(mWaitTime / 2 * 1000, GameState.Main);
				selectSubject();
				broadcast("Selecting random pattern. Round starts in " + Util.getTimeRemainString(mWaitTime * 1000), null);
			}
			break;
		}
		case Waiting:
			handleWaitLogic();
			break;
		case SubjectDraw:
			handleSubjectDrawLogic();
			break;
		case Main:
			handleMain();
			break;
		}
	}
	
	public void endGame()
	{
		mTask.cancel();
	}
	
	private class GameTimer implements Runnable
	{
		@Override
		public void run()
		{
			doLogic();
		}
	}
}

