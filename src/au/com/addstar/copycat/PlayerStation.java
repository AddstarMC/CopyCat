package au.com.addstar.copycat;

import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import au.com.mineauz.minigames.MinigamePlayer;

public class PlayerStation
{
	private GameBoard mBoard;
	private BlockFace mFacing;
	private Location mLocation;
	
	private MinigamePlayer mPlayer;
	private boolean mCanModify;
	
	public PlayerStation(GameBoard board)
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
	
	public void setPlayer(MinigamePlayer player)
	{
		mPlayer = player;
	}
	
	public MinigamePlayer getPlayer()
	{
		return mPlayer;
	}
	
	public BlockFace getFacing()
	{
		return mFacing;
	}
	
	public void setCanModify(boolean canModify)
	{
		mCanModify = canModify;
	}
	
	public boolean getCanModify()
	{
		return mCanModify;
	}
	
	public void clearStation()
	{
		Location corner = getSubjectLocation();
		Location cornerPlay = getPlayLocation();
		BlockFace right = getRight();
		World world = mLocation.getWorld();
		
		for(int x = 0; x < mBoard.getSubjectSize(); ++x)
		{
			for(int z = 0; z < mBoard.getSubjectSize(); ++z)
			{
				// Subject
				Block block = world.getBlockAt(corner.getBlockX() + x * right.getModX(), corner.getBlockY() + z, corner.getBlockZ() + x * right.getModZ());
				block.setType(Material.AIR);
				
				// Play area
				block = world.getBlockAt(cornerPlay.getBlockX() + x * right.getModX() + z * mFacing.getModX(), cornerPlay.getBlockY(), cornerPlay.getBlockZ() + x * right.getModZ() + z * mFacing.getModZ());
				block.setType(Material.AIR);
			}
		}
		
		Collection<Item> items = world.getEntitiesByClass(Item.class);
		
		float offsetC = mBoard.getSubjectSize() / 2f; 
		float maxSize = offsetC + 4; 
		Location center = cornerPlay.clone().add(right.getModX() * offsetC + mFacing.getModX() * offsetC, 0, right.getModZ() * offsetC + mFacing.getModZ() * offsetC);
		Location temp = new Location(null, 0, 0, 0);
		
		for(Item item : items)
		{
			item.getLocation(temp);
			
			if(Math.abs(temp.getX() - center.getX()) < maxSize &&
			   Math.abs(temp.getZ() - center.getZ()) < maxSize &&
			   Math.abs(temp.getY() - center.getY()) < 3)
			{
				item.remove();
			}
		}
	}
	
	public void displayLocations(Player player)
	{
		Location corner = getSubjectLocation();
		Location cornerPlay = getPlayLocation();
		BlockFace right = getRight();
		
		int size = mBoard.getSubjectSize();
		
		for(int x = 0; x < size; ++x)
		{
			for(int z = 0; z < size; ++z)
			{
				// Subject
				player.sendBlockChange(corner.clone().add(x * right.getModX(), z, x * right.getModZ()), Material.WOOL, (byte)14);
				
				// Play area
				player.sendBlockChange(cornerPlay.clone().add(x * right.getModX() + z * mFacing.getModX(), 0, x * right.getModZ() + z * mFacing.getModZ()), Material.WOOL, (byte)4);
			}
		}
	}
	
	public void drawSubject()
	{
		Subject subject = mBoard.getSubject();
		if(subject != null)
			subject.placeAt(getSubjectLocation(), getRight());
	}
	
	private BlockFace getRight()
	{
		return Util.rotateRight(mFacing);
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
	
	public Location getSubjectLocation()
	{
		BlockFace right = getRight();
		double size = mBoard.getSubjectSize();
		double x = 1;
		double z = mBoard.getBackboardDistance() + size;
		
		return mLocation.clone().add(x * right.getModX() + z * mFacing.getModX(), mBoard.getBackboardHeight(), x * right.getModZ() + z * mFacing.getModZ());
	}
	
	public Location getPlayLocation()
	{
		BlockFace right = getRight();
		double x = 1;
		
		return mLocation.clone().add(x * right.getModX() + x * mFacing.getModX(), 0, x * right.getModZ() + x * mFacing.getModZ());
	}
	
	public boolean isInPlayArea(Location location)
	{
		BlockFace right = getRight();
		int size = mBoard.getSubjectSize();
		
		if(location.getWorld() != mLocation.getWorld())
			return false;
		
		if(location.getY() != mLocation.getY())
			return false;
		
		int minX = Math.min(mLocation.getBlockX() + 1 * right.getModX() + 1 * mFacing.getModX(), mLocation.getBlockX() + (1 + size-1) * right.getModX() + (1 + size-1) * mFacing.getModX());
		int maxX = Math.max(mLocation.getBlockX() + 1 * right.getModX() + 1 * mFacing.getModX(), mLocation.getBlockX() + (1 + size-1) * right.getModX() + (1 + size-1) * mFacing.getModX());
		int minZ = Math.min(mLocation.getBlockZ() + 1 * right.getModZ() + 1 * mFacing.getModZ(), mLocation.getBlockZ() + (1 + size-1) * right.getModZ() + (1 + size-1) * mFacing.getModZ());
		int maxZ = Math.max(mLocation.getBlockZ() + 1 * right.getModZ() + 1 * mFacing.getModZ(), mLocation.getBlockZ() + (1 + size-1) * right.getModZ() + (1 + size-1) * mFacing.getModZ());
		
		return (location.getBlockX() >= minX && location.getBlockX() <= maxX && location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ);
	}
	
	public void save(ConfigurationSection section)
	{
		if(isValid())
		{
			section.set("Location", mLocation.toVector());
			section.set("Facing", mFacing.name());
		}
	}
	
	public void read(ConfigurationSection section)
	{
		if(section.contains("Location") && section.contains("Facing"))
		{
			mLocation = ((Vector)section.get("Location")).toLocation(mBoard.getWorld());
			mFacing = BlockFace.valueOf(section.getString("Facing"));
		}
		else
		{
			mLocation = null;
			mFacing = null;
		}
	}
}
