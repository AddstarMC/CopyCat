package au.com.addstar.copycat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.copycat.commands.CopyCatCommand;

import com.google.common.collect.HashBiMap;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.scoring.ScoreType;

public class CopyCatPlugin extends JavaPlugin
{
	private HashMap<World, HashMap<String, GameBoard>> mBoards = new HashMap<World, HashMap<String, GameBoard>>();
	private HashBiMap<String, GameBoard> mMinigameToBoard = HashBiMap.create();
	
	public static CopyCatPlugin instance;
	public static final Pattern validNamePattern = Pattern.compile("^[a-zA-Z0-9_]+$");
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		if(!getDataFolder().exists())
			getDataFolder().mkdirs();
		
		new CopyCatCommand().registerAs(getCommand("copycat"));
		
		ScoreType.addScoreType(new CopyCatLogic());
		Minigames.plugin.mdata.addPreset(new CopyCatPreset());
		
		for(World world : Bukkit.getWorlds())
			loadWorld(world);
		
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
	}
	
	@Override
	public void onDisable()
	{
	}
	
	public void loadWorld(World world)
	{
		File folder = new File(getDataFolder(), "boards");
		if(!folder.exists())
			return;
		
		HashMap<String, GameBoard> boards = new HashMap<String, GameBoard>();
		mBoards.put(world, boards);
		
		for(File file : folder.listFiles())
		{
			if(file.isFile() && file.getName().toLowerCase().endsWith(".yml") && file.getName().startsWith(world.getName().toLowerCase() + "-"))
			{
				String name = file.getName().split("\\-")[1].toLowerCase();
				name = name.substring(0, name.indexOf(".yml"));
				
				try
				{
					GameBoard board = new GameBoard(file);
					boards.put(name, board);
					mMinigameToBoard.put(board.getMinigameId(), board);
				}
				catch(IOException e)
				{
					getLogger().severe("Failed to load GameBoard " + name + " in world " + world.getName());
					e.printStackTrace();
				}
				catch(InvalidConfigurationException e)
				{
					getLogger().severe("Failed to load GameBoard " + name + " in world " + world.getName());
					e.printStackTrace();
				}
			}
		}
	}
	
	public void unloadWorld(World world)
	{
		HashMap<String, GameBoard> boards = mBoards.remove(world);
		if(boards == null)
			return;
		
		for(GameBoard board : boards.values())
			mMinigameToBoard.inverse().remove(board);
		
		boards.clear();
	}
	
	public boolean registerGame(GameBoard board, String name, World world)
	{
		name = name.toLowerCase();
		
		HashMap<String, GameBoard> boards = mBoards.get(world);
		if(boards == null)
		{
			boards = new HashMap<String, GameBoard>();
			mBoards.put(world, boards);
		}
		
		if(boards.containsKey(name))
			return false;
		
		boards.put(name, board);
		mMinigameToBoard.put(board.getMinigameId(), board);
		
		File folder = new File(getDataFolder(), "boards");
		if(!folder.exists())
			folder.mkdirs();
		
		try
		{
			board.write(new File(folder, world.getName().toLowerCase() + "-" + name + ".yml"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public GameBoard getBoard(String name, World world)
	{
		HashMap<String, GameBoard> boards = mBoards.get(world);
		if(boards == null)
			return null;
		
		return boards.get(name.toLowerCase());
	}
}
