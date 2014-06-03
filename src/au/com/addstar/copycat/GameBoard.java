package au.com.addstar.copycat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import au.com.addstar.copycat.logic.EliminationMainState;
import au.com.addstar.copycat.logic.PreRoundState;
import au.com.addstar.copycat.logic.ScoringMainState;
import au.com.addstar.copycat.logic.State;
import au.com.addstar.copycat.logic.StateEngine;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

public class GameBoard
{
	public enum GameMode
	{
		Elimination,
		Score
	}
	
	private PlayerStation[] mStations;
	private int mSize;
	private String mMinigameId;
	private World mWorld;
	private int mSubjectDrawTime = 60;
	private boolean mAllowSubjectDraw = true;
	private int mWaitTime = 10;
	private boolean mSaveSubjects = true;
	private GameMode mMode = GameMode.Score;
	
	// In game vars
	
	private int mNextToDraw;
	private Subject mSubject;
	
	private MinigamePlayer mSubjectDrawer;
	private StateEngine<GameBoard> mEngine = new StateEngine<GameBoard>();
	
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
	
	public void setSubject(Subject subject)
	{
		mSubject = subject;
	}
	
	public void setDrawer(MinigamePlayer player)
	{
		mSubjectDrawer = player;
	}
	
	public MinigamePlayer getDrawer()
	{
		return mSubjectDrawer;
	}
	
	public MinigamePlayer selectNextDrawer()
	{
		Minigame minigame = getMinigame();
		mSubjectDrawer = minigame.getPlayers().get(mNextToDraw);
		++mNextToDraw;
		if(mNextToDraw >= minigame.getPlayers().size())
			mNextToDraw = 0;
		
		return mSubjectDrawer;
	}
	
	public long getSubjectDrawTime()
	{
		return mSubjectDrawTime * 1000;
	}
	
	public long getWaitTime()
	{
		return mWaitTime * 1000;
	}
	
	public boolean getSaveSubjects()
	{
		return mSaveSubjects;
	}
	
	public boolean getAllowSubjectDrawing()
	{
		return mAllowSubjectDraw;
	}
	
	public GameMode getGameMode()
	{
		return mMode;
	}
	
	public void setGameMode(GameMode mode)
	{
		mMode = mode;
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
	
	public PlayerStation[] getStations()
	{
		return mStations;
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
	
	public void beginGame()
	{
		Minigame minigame = getMinigame();
		mNextToDraw = CopyCatPlugin.rand.nextInt(minigame.getPlayers().size());
		
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
				mStations[i].setCanModify(false);
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
		mEngine.start(new PreRoundState(), this);
	}
	
	public void onPlayerLeave(MinigamePlayer player)
	{
		Minigame minigame = getMinigame();
		PlayerStation station = getStation(player);
		station.clearStation();
		station.setPlayer(null);
		station.setCanModify(false);
		
		if(minigame.getPlayers().size() <= 2)
			endGame();
		
		mEngine.sendEvent("leave", player);
	}
	
	public void onPlaceBlock(MinigamePlayer player)
	{
		mEngine.sendEvent("place", player);
	}
	
	public void endGame()
	{
		mEngine.end();
	}
	
	public State<GameBoard> getMainState()
	{
		switch(mMode)
		{
		default:
		case Elimination:
			return new EliminationMainState();
		case Score:
			return new ScoringMainState();
		}
	}
}

