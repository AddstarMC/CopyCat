package au.com.addstar.copycat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.copycat.commands.CopyCatCommand;

import com.google.common.collect.HashBiMap;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.PlayerLoadout;
import com.pauldavdesign.mineauz.minigames.gametypes.MinigameType;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;
import com.pauldavdesign.mineauz.minigames.scoring.ScoreType;

public class CopyCatPlugin extends JavaPlugin
{
	private HashMap<World, HashMap<String, GameBoard>> mBoards = new HashMap<World, HashMap<String, GameBoard>>();
	private HashBiMap<String, GameBoard> mMinigameToBoard = HashBiMap.create();
	private SubjectStorage mStorage;
	
	public static CopyCatPlugin instance;
	public static final Pattern validNamePattern = Pattern.compile("^[a-zA-Z0-9_]+$");
	public static final Random rand = new Random();
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		if(!getDataFolder().exists())
			getDataFolder().mkdirs();
		
		mStorage = new SubjectStorage(new File(getDataFolder(), "subjects"));
		
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
					GameBoard board = new GameBoard(file, world);
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
	
	public boolean registerGame(GameBoard board, String name)
	{
		name = name.toLowerCase();
		
		HashMap<String, GameBoard> boards = mBoards.get(board.getWorld());
		if(boards == null)
		{
			boards = new HashMap<String, GameBoard>();
			mBoards.put(board.getWorld(), boards);
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
			board.write(new File(folder, board.getWorld().getName().toLowerCase() + "-" + name + ".yml"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean saveBoard(String name, World world)
	{
		GameBoard board = getBoard(name, world);
		if(board == null)
			return false;

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
	
	public GameBoard getBoardByGame(String minigame)
	{
		return mMinigameToBoard.get(minigame);
	}
	
	public static void applyDefaultsForGame(GameBoard board)
	{
		Minigame game = board.getMinigame();
		if(game == null)
			return;
		
		applyDefaults(game);
		game.setMaxPlayers(board.getStationCount());
		
		game.getStartLocations().clear();
		game.addStartLocation(board.getStation(0).getSpawnLocation());
		game.saveMinigame();
	}
	
	public static void applyDefaults(Minigame minigame)
	{
		minigame.setScoreType("copycat");
		minigame.setBlocksdrop(true);
		minigame.setCanBlockBreak(true);
		minigame.setCanBlockPlace(true);
		PlayerLoadout loadout = minigame.getDefaultPlayerLoadout();
		loadout.clearLoadout();
		loadout.addItem(new ItemStack(Material.DIAMOND_PICKAXE), 0);
		loadout.addItem(new ItemStack(Material.STAINED_CLAY, 64, (short)0), 1);
		loadout.addItem(new ItemStack(Material.STAINED_CLAY, 64, (short)4), 2);
		loadout.addItem(new ItemStack(Material.STAINED_CLAY, 64, (short)3), 3);
		loadout.addItem(new ItemStack(Material.STAINED_CLAY, 64, (short)9), 4);
		loadout.addItem(new ItemStack(Material.STAINED_CLAY, 64, (short)8), 5);
		loadout.addItem(new ItemStack(Material.STAINED_CLAY, 64, (short)13), 6);
		loadout.addItem(new ItemStack(Material.STAINED_CLAY, 64, (short)14), 7);
		loadout.addItem(new ItemStack(Material.STAINED_CLAY, 64, (short)15), 8);
		
		minigame.setDefaultGamemode(GameMode.SURVIVAL);
		minigame.setGametypeName("Copy Cat");
		minigame.setObjective("Copy the shown pattern");
		minigame.setTeleportOnStart(false);
		minigame.setType(MinigameType.FREE_FOR_ALL);
	}
	
	public SubjectStorage getSubjectStorage()
	{
		return mStorage;
	}
}
