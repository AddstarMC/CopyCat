package au.com.addstar.copycat;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.copycat.commands.CopyCatCommand;

import com.google.common.collect.ImmutableList;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.mechanics.GameMechanics;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.LobbySettingsModule;

public class CopyCatPlugin extends JavaPlugin
{
	private SubjectStorage mStorage;
	
	public static CopyCatPlugin instance;
	static final Random rand = new Random();
	static final List<ItemStack> blockTypes;
	
	static
	{
		blockTypes = ImmutableList.<ItemStack>builder()
			.add(new ItemStack(Material.WHITE_TERRACOTTA, 64))
			.add(new ItemStack(Material.YELLOW_TERRACOTTA, 64))
			.add(new ItemStack(Material.LIGHT_BLUE_TERRACOTTA, 64))
			.add(new ItemStack(Material.CYAN_TERRACOTTA, 64))
			.add(new ItemStack(Material.BROWN_TERRACOTTA, 64))
			.add(new ItemStack(Material.GREEN_TERRACOTTA, 64))
			.add(new ItemStack(Material.RED_TERRACOTTA, 64))
			.add(new ItemStack(Material.BLACK_TERRACOTTA, 64))
			.build();
	}
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		if(!getDataFolder().exists())
			if(!getDataFolder().mkdirs())
			  getLogger().warning("DataFolder unable to be created check file permissions");
		mStorage = new SubjectStorage(new File(getDataFolder(), "subjects"));
    PluginCommand c = getCommand("copycat");
    if(c != null) {
      c.setExecutor(new CopyCatCommand());
    }

		GameMechanics.addGameMechanic(new CopyCatLogic());
		Minigames.getPlugin().getMinigameManager().addModule(CopyCatModule.class);
		
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
	}
	
	@Override
	public void onDisable()
	{
		GameMechanics.removeGameMechanic("CopyCat");
		Minigames.getPlugin().getMinigameManager().removeModule("CopyCat", CopyCatModule.class);
	}
	
	public static void applyDefaultsForGame(GameBoard board)
	{
		Minigame game = board.getMinigame();
		if(game == null)
			return;
		
		game.setMaxPlayers(board.getStationCount());
		
		game.getStartLocations().clear();
	}
	
	public static void applyDefaults(Minigame minigame)
	{
		minigame.setMechanic("CopyCat");
		minigame.setBlocksdrop(true);
		minigame.setCanBlockBreak(true);
		minigame.setCanBlockPlace(true);
		LoadoutModule module = LoadoutModule.getMinigameModule(minigame);
		PlayerLoadout loadout = module.getLoadout("default");
		loadout.clearLoadout();
		loadout.addItem(new ItemStack(Material.DIAMOND_PICKAXE), 0);
		for(int i = 0; i < blockTypes.size(); ++i)
			loadout.addItem(blockTypes.get(i).clone(), i+1);
		
		minigame.setDefaultGamemode(GameMode.SURVIVAL);
		minigame.setGametypeName("Copy Cat");
		minigame.setObjective("Copy the shown pattern");
		LobbySettingsModule.getMinigameModule(minigame).setTeleportOnStart(false);
		minigame.setType(MinigameType.MULTIPLAYER);
		minigame.setLives(3);
	}
	
	public SubjectStorage getSubjectStorage()
	{
		return mStorage;
	}
	
	static boolean isValidBlockType(Material data)
	{
		for(ItemStack item : blockTypes)
		{
			if(item.getType() == data)
				return true;
		}
		return false;
	}
}
