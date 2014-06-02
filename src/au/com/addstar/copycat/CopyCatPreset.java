package au.com.addstar.copycat;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.pauldavdesign.mineauz.minigames.PlayerLoadout;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;
import com.pauldavdesign.mineauz.minigames.presets.BasePreset;

public class CopyCatPreset implements BasePreset
{

	@Override
	public void execute( Minigame minigame )
	{
		minigame.setScoreType("copycat");
		minigame.setBlocksdrop(false);
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
		
		minigame.setCanMovePlayerWait(false);
		minigame.setCanMoveStartWait(false);
		minigame.setDefaultGamemode(GameMode.SURVIVAL);
		minigame.setGametypeName("Copy Cat");
		minigame.setObjective("Copy the shown pattern");
		minigame.setTeleportOnStart(false);
	}

	@Override
	public String getInfo()
	{
		return "Sets everything up for a copycat game";
	}

	@Override
	public String getName()
	{
		return "copycat";
	}

}
