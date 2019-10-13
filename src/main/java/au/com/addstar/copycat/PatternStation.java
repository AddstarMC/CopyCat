package au.com.addstar.copycat;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PatternStation
{
	private GameBoard mBoard;
	private BlockFace mFacing;
	private Location mLocation;
	private MinigamePlayer mPlayer;
	
	public PatternStation(GameBoard board)
	{
		mBoard = board;
	}
	
	public void setLocationAndFacing(Location location, BlockFace facing)
	{
		Validate.isTrue(facing == BlockFace.NORTH || facing == BlockFace.EAST || facing == BlockFace.SOUTH || facing == BlockFace.WEST);
		mLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
		mFacing = facing;
	}
	
	public boolean isValid()
	{
		return (mLocation != null && mFacing != null);
	}
	
	public BlockFace getFacing()
	{
		return mFacing;
	}
	
	private BlockFace getRight()
	{
		return Util.rotateRight(mFacing);
	}
	
	public MinigamePlayer getPlayer()
	{
		return mPlayer;
	}
	
	public void setPlayer(MinigamePlayer player)
	{
		mPlayer = player;
	}
	
	public Location getPatternLocation()
	{
		BlockFace right = getRight();
		double x = 1;
		
		return mLocation.clone().add(x * right.getModX() + x * mFacing.getModX(), 0, x * right.getModZ() + x * mFacing.getModZ());
	}
	
	public Location getSpawnLocation()
	{
		BlockFace right = getRight();
		double size = mBoard.getSubjectSize() / 2d;
		double x = (1 + size) * right.getModX();
		double z = (1 + size) * right.getModZ();
		
		Location loc = mLocation.clone().add(x, 1, z);
		
		loc.setYaw(Util.getYaw(mFacing));
		return loc;
	}
	
	public void clearStation()
	{
		Location corner = getPatternLocation();
		BlockFace right = getRight();
		World world = mLocation.getWorld();
		
		for(int x = 0; x < mBoard.getSubjectSize(); ++x)
		{
			for(int z = 0; z < mBoard.getSubjectSize(); ++z)
			{
				Block block = world.getBlockAt(corner.getBlockX() + x * right.getModX() + z * mFacing.getModX(), corner.getBlockY(), corner.getBlockZ() + x * right.getModZ() + z * mFacing.getModZ());
				block.setType(Material.AIR);
			}
		}
	}
	
	public void displayLocations(Player player)
	{
		Location corner = getPatternLocation();
		BlockFace right = getRight();
		
		int size = mBoard.getSubjectSize();
		
		for(int x = 0; x < size; ++x)
		{
			for(int z = 0; z < size; ++z)
			{
				player.sendBlockChange(corner.clone().add(x * right.getModX() + z * mFacing.getModX(), 0, x * right.getModZ() + z * mFacing.getModZ()),Material.YELLOW_WOOL.createBlockData());
			}
		}
	}
	
	public boolean isInPatternArea(Location location)
	{
		BlockFace right = getRight();
		int size = mBoard.getSubjectSize();
		
		if(location.getWorld() != mLocation.getWorld())
			return false;
		
		if(location.getY() != mLocation.getY())
			return false;
		
		int minX = Math.min(mLocation.getBlockX() + right.getModX() + mFacing.getModX(), mLocation.getBlockX() + (1 + size-1) * right.getModX() + (1 + size-1) * mFacing.getModX());
		int maxX = Math.max(mLocation.getBlockX() + right.getModX() + mFacing.getModX(), mLocation.getBlockX() + (1 + size-1) * right.getModX() + (1 + size-1) * mFacing.getModX());
		int minZ = Math.min(mLocation.getBlockZ() + right.getModZ() + mFacing.getModZ(), mLocation.getBlockZ() + (1 + size-1) * right.getModZ() + (1 + size-1) * mFacing.getModZ());
		int maxZ = Math.max(mLocation.getBlockZ() + right.getModZ() + mFacing.getModZ(), mLocation.getBlockZ() + (1 + size-1) * right.getModZ() + (1 + size-1) * mFacing.getModZ());
		
		return (location.getBlockX() >= minX && location.getBlockX() <= maxX && location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ);
	}
	
	public void save(ConfigurationSection section)
	{
		if(isValid())
		{
			section.set("Location", mLocation.toVector());
			section.set("World", mLocation.getWorld().getName());
			section.set("Facing", mFacing.name());
		}
	}
	
	public void read(ConfigurationSection section)
	{
		if(section.contains("Location") && section.contains("Facing"))
		{
			Vector vec = (Vector)section.get("Location");
			World world = Bukkit.getWorld(section.getString("World"));
			mLocation = vec.toLocation(world);
			mFacing = BlockFace.valueOf(section.getString("Facing"));
		}
		else
		{
			mLocation = null;
			mFacing = null;
		}
	}
}
