package au.com.addstar.copycat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;

@SuppressWarnings( "deprecation" )
public class Subject
{
	private int mSize;
	private MaterialData[] mData;
	
	public Subject(int size, MaterialData[] data)
	{
		mSize = size;
		mData = data;
	}
	
	public int getSize()
	{
		return mSize;
	}
	
	public void placeAt(Location location, BlockFace facing)
	{
		Validate.isTrue(facing == BlockFace.NORTH || facing == BlockFace.EAST || facing == BlockFace.SOUTH || facing == BlockFace.WEST);
		
		int bx = location.getBlockX();
		int by = location.getBlockY();
		int bz = location.getBlockZ();
		
		for(int x = 0; x < mSize; ++x)
		{
			for(int y = 0; y < mSize; ++y)
			{
				Block block = location.getWorld().getBlockAt(bx + facing.getModX() * x, by + y, bz + facing.getModZ() * x);
				MaterialData data = mData[x + (y * mSize)];
				
				block.setType(data.getItemType());
				block.setData(data.getData());
			}
		}
	}
	
	public boolean matches(Location location, BlockFace facing)
	{
		Validate.isTrue(facing == BlockFace.NORTH || facing == BlockFace.EAST || facing == BlockFace.SOUTH || facing == BlockFace.WEST);
		
		BlockFace right = Util.rotateRight(facing);
		
		int bx = location.getBlockX();
		int by = location.getBlockY();
		int bz = location.getBlockZ();
		
		for(int x = 0; x < mSize; ++x)
		{
			for(int y = 0; y < mSize; ++y)
			{
				Block block = location.getWorld().getBlockAt(bx + right.getModX() * x + facing.getModX() * y, by, bz + right.getModZ() * x + facing.getModZ() * y);
				MaterialData data = mData[x + (y * mSize)];
				if(block.getType() != data.getItemType() || block.getData() != data.getData())
					return false;
			}
		}
		
		return true;
	}
	
	public void save(File file) throws IOException
	{
		YamlConfiguration config = new YamlConfiguration();
		config.set("size", mSize);
		ArrayList<String> mats = new ArrayList<String>(mData.length);
		for(int i = 0; i < mData.length; ++i)
		{
			MaterialData data = mData[i];
			mats.add(String.format("%s:%d", data.getItemType().name(), data.getData()));
		}
		
		config.set("data", mats);
		config.save(file);
	}
	
	public static Subject from(Location location, BlockFace facing, int size)
	{
		Validate.isTrue(facing == BlockFace.NORTH || facing == BlockFace.EAST || facing == BlockFace.SOUTH || facing == BlockFace.WEST);
		
		BlockFace right = Util.rotateRight(facing);
		
		int bx = location.getBlockX();
		int by = location.getBlockY();
		int bz = location.getBlockZ();
		
		MaterialData[] data = new MaterialData[size * size];
		for(int x = 0; x < size; ++x)
		{
			for(int y = 0; y < size; ++y)
			{
				Block block = location.getWorld().getBlockAt(bx + x * right.getModX() + y * facing.getModX(), by, bz + x * right.getModZ() + y * facing.getModZ());
				if(block.isEmpty())
					return null;
				data[x + (y * size)] = block.getType().getNewData(block.getData());
			}
		}
		
		return new Subject(size, data);
	}
	
	public static Subject from(File file) throws FileNotFoundException, IOException, InvalidConfigurationException
	{
		YamlConfiguration config = new YamlConfiguration();
		config.load(file);
		
		int size = config.getInt("size");
		MaterialData[] data = new MaterialData[size * size];
		
		List<String> dataStrings = config.getStringList("data");
		if(dataStrings.size() != data.length)
			throw new InvalidConfigurationException("Data size is incorrect. " + dataStrings.size() + " != " + (size * size));
		
		for(int i = 0; i < data.length; ++i)
		{
			String str = dataStrings.get(i);
			Material mat = Material.valueOf(str.split(":")[0]);
			int dataVal = Integer.valueOf(str.split(":")[1]);
			data[i] = mat.getNewData((byte)dataVal);
		}
		
		return new Subject(size, data);
	}
}
