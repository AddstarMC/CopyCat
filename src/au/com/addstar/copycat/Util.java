package au.com.addstar.copycat;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class Util
{
	public static BlockFace rotateRight(BlockFace face)
	{
		switch(face)
		{
		case NORTH:
			return BlockFace.EAST;
		case EAST:
			return BlockFace.SOUTH;
		case SOUTH:
			return BlockFace.WEST;
		default:
		case WEST:
			return BlockFace.NORTH;
		}
	}
	
	public static BlockFace getFacing(Player player)
	{
		float yaw = player.getLocation().getYaw();
		
		if(yaw <= -180)
			yaw += 360;

		if(yaw >= 180)
			yaw -= 360;

		if(yaw >= -45 && yaw <= 45)
			return BlockFace.SOUTH;
		else if(yaw > 45 && yaw < 135)
			return BlockFace.WEST;
		else if(yaw > -135 && yaw < -45)
			return BlockFace.EAST;
		else
			return BlockFace.NORTH;
	}
}
