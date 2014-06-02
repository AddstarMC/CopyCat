package au.com.addstar.copycat;

import java.io.File;
import java.io.IOException;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

public class GameBoard
{
	private PlayerStation[] mStations;
	private int mSize;
	private String mMinigameId;
	private World mWorld;
	
	public GameBoard(int players, int size, String minigame)
	{
		mStations = new PlayerStation[players];
		
		for(int i = 0; i < players; ++i)
			mStations[i] = new PlayerStation(this);
		
		mSize = size;
		mMinigameId = minigame;
	}
	
	public GameBoard(File file) throws IOException, InvalidConfigurationException
	{
		read(file);
	}
	
	public void setWorld(World world)
	{
		mWorld = world;
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
		return null;
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
}

