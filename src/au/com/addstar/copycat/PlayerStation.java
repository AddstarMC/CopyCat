package au.com.addstar.copycat;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

public class PlayerStation
{
	private GameBoard mBoard;
	private BlockFace mFacing;
	private Location mLocation;
	
	public PlayerStation(GameBoard board)
	{
		mBoard = board;
	}
	
	public void setLocationAndFacing(Location location, BlockFace facing)
	{
		Validate.isTrue(facing == BlockFace.NORTH || facing == BlockFace.EAST || facing == BlockFace.SOUTH || facing == BlockFace.WEST);
		mLocation = location;
		mFacing = facing;
	}
	
	public boolean isValid()
	{
		return (mLocation != null && mFacing != null);
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
				Block block = world.getBlockAt(corner.getBlockX() + x * mFacing.getModX(), corner.getBlockY() + z, corner.getBlockZ() + x * mFacing.getModZ());
				block.setType(Material.AIR);
				
				// Play area
				block = world.getBlockAt(cornerPlay.getBlockX() + x * right.getModX() + z + mFacing.getModX(), cornerPlay.getBlockY(), cornerPlay.getBlockZ() + x * right.getModZ() + z + mFacing.getModZ());
				block.setType(Material.AIR);
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
		double x = (2 + size) * right.getModX();
		double z = (2 + size) * right.getModZ();
		
		return mLocation.clone().add(x, 1, z);
	}
	
	public Location getSubjectLocation()
	{
		BlockFace right = getRight();
		double size = mBoard.getSubjectSize();
		double x = 2;
		double z = 5 + size;
		
		return mLocation.clone().add(x * right.getModX() + z * mFacing.getModX(), 3, x * right.getModZ() + z * mFacing.getModZ());
	}
	
	public Location getPlayLocation()
	{
		BlockFace right = getRight();
		double x = 2;
		
		return mLocation.clone().add(x * right.getModX() + x * mFacing.getModX(), 3, x * right.getModZ() + x * mFacing.getModZ());
	}
	
	public boolean isInPlayArea(Location location)
	{
		BlockFace right = getRight();
		int size = mBoard.getSubjectSize();
		
		if(location.getWorld() != mLocation.getWorld())
			return false;
		
		if(location.getY() != mLocation.getY())
			return false;
		
		int minX = Math.min(mLocation.getBlockX() + 2 * right.getModX() + 2 * mFacing.getModX(), mLocation.getBlockX() + (2 + size-1) * right.getModX() + (2 + size-1) * mFacing.getModX());
		int maxX = Math.max(mLocation.getBlockX() + 2 * right.getModX() + 2 * mFacing.getModX(), mLocation.getBlockX() + (2 + size-1) * right.getModX() + (2 + size-1) * mFacing.getModX());
		int minZ = Math.min(mLocation.getBlockZ() + 2 * right.getModZ() + 2 * mFacing.getModZ(), mLocation.getBlockZ() + (2 + size-1) * right.getModZ() + (2 + size-1) * mFacing.getModZ());
		int maxZ = Math.max(mLocation.getBlockZ() + 2 * right.getModZ() + 2 * mFacing.getModZ(), mLocation.getBlockZ() + (2 + size-1) * right.getModZ() + (2 + size-1) * mFacing.getModZ());
		
		return (location.getBlockX() >= minX && location.getBlockX() <= maxX && location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ);
	}
	
	public void save(ConfigurationSection section)
	{
		if(isValid())
		{
			section.set("Location", mLocation);
			section.set("Facing", mFacing.name());
		}
	}
	
	public void read(ConfigurationSection section)
	{
		if(section.contains("Location") && section.contains("Facing"))
		{
			mLocation = (Location)section.get("Location");
			mFacing = BlockFace.valueOf(section.getString("Facing"));
		}
		else
		{
			mLocation = null;
			mFacing = null;
		}
	}
}
