package au.com.addstar.copycat;

import java.util.concurrent.TimeUnit;

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
	
	public static float getYaw(BlockFace face)
	{
		switch(face)
		{
		case NORTH:
			return 180;
		case EAST:
			return -90;
		case SOUTH:
			return 0;
		default:
			return 90;
		}
	}
	
	public static String getTimeRemainString(long time)
	{
		StringBuilder text = new StringBuilder();
		if(time > TimeUnit.MINUTES.toMillis(1))
		{
			if(text.length() != 0)
				text.append(" ");
			
			long value = time / TimeUnit.MINUTES.toMillis(1);
			text.append(String.valueOf(value));
			text.append(" ");
			if(value != 1)
				text.append("Minutes");
			else
				text.append("Minute");
			time -= (value * TimeUnit.MINUTES.toMillis(1));
		}
		
		if(text.length() != 0)
			text.append(" ");
		
		long value = time / TimeUnit.SECONDS.toMillis(1);
		text.append(String.valueOf(value));
		text.append(" ");
		if(value != 1)
			text.append("Seconds");
		else
			text.append("Second");
		time -= (value * TimeUnit.SECONDS.toMillis(1));
		
		return text.toString();
	}
}
