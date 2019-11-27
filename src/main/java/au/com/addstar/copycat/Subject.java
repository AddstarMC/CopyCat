package au.com.addstar.copycat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import au.com.mineauz.minigames.Minigames;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Subject {
  private File mFile;
  private int mSize;
  private Material[] mData;

  private Subject(int size, Material[] data) {
    this(size, data, null);
  }

  private Subject(int size, Material[] data, File file) {
    mSize = size;
    mData = data;
    mFile = file;
  }

  private static void checkValidFacing(BlockFace facing) {
    Validate.isTrue(facing == BlockFace.NORTH || facing == BlockFace.EAST || facing == BlockFace.SOUTH || facing == BlockFace.WEST);
  }

  static boolean from(Location location, BlockFace facing, int size, Subject destination) {
    checkValidFacing(facing);

    BlockFace right = Util.rotateRight(facing);

    int bx = location.getBlockX();
    int by = location.getBlockY();
    int bz = location.getBlockZ();

    Material[] data = new Material[size * size];
    for (int x = 0; x < size; ++x) {
      for (int y = 0; y < size; ++y) {
        Block block = Objects.requireNonNull(location.getWorld()).getBlockAt(bx + x * right.getModX() + y * facing.getModX(), by, bz + x * right.getModZ() + y * facing.getModZ());
        if (block.isEmpty())
          return false;
        data[x + (y * size)] = block.getType();
        if (!CopyCatPlugin.isValidBlockType(data[x + (y * size)]))
          return false;
      }
    }

    destination.mData = data;
    destination.mSize = size;
    return true;
  }

  public static Subject from(Location location, BlockFace facing, int size) {
    Subject subject = new Subject(size, null);
    if (from(location, facing, size, subject))
      return subject;
    return null;
  }

  static Subject from(File file) throws IOException, InvalidConfigurationException {
    YamlConfiguration config = new YamlConfiguration();
    config.load(file);

    int size = config.getInt("size");
    Material[] data = new Material[size * size];

    List<String> dataStrings = config.getStringList("data");
    if (dataStrings.size() != data.length)
      throw new InvalidConfigurationException("Data size is incorrect. " + dataStrings.size() + " != " + (size * size));
    for (int i = 0; i < data.length; ++i) {
      String str = dataStrings.get(i);
      Material mat;
      try {
        if (str.contains(":")) {
          String[] out = str.split(":", 2);
          mat = Material.valueOf(out[0]);
        } else {
          mat = Material.valueOf(str);
        }
        data[i] = mat;
      } catch (IllegalArgumentException e) {
        Minigames.log(Level.WARNING, e.getMessage());
        throw new InvalidConfigurationException("Data size is incorrect. File: " + file.getName() + " Data: " + dataStrings.size() + " != " + (size * size));
      }
    }
    return new Subject(size, data, file);
  }

  public int getSize() {
    return mSize;
  }

  File getFile() {
    return mFile;
  }

  void placeAt(Location location, BlockFace facing) {
    checkValidFacing(facing);
    int bx = location.getBlockX();
    int by = location.getBlockY();
    int bz = location.getBlockZ();

    for (int x = 0; x < mSize; ++x) {
      for (int y = 0; y < mSize; ++y) {
        Block block = Objects.requireNonNull(location.getWorld()).getBlockAt(bx + facing.getModX() * x, by + y, bz + facing.getModZ() * x);
        Material data = mData[x + (y * mSize)];

        block.setBlockData(data.createBlockData());
      }
    }
  }

  void placeAtFlat(Location location, BlockFace facing) {
    checkValidFacing(facing);
    int bx = location.getBlockX();
    int by = location.getBlockY();
    int bz = location.getBlockZ();

    BlockFace right = Util.rotateRight(facing);

    for (int x = 0; x < mSize; ++x) {
      for (int y = 0; y < mSize; ++y) {

        Block block = Objects.requireNonNull(location.getWorld()).getBlockAt(bx + right.getModX() * x + facing.getModX() * y, by, bz + right.getModZ() * x + facing.getModZ() * y);
        Material data = mData[x + (y * mSize)];

        block.setBlockData(data.createBlockData());
      }
    }
  }

  public boolean matches(Location location, BlockFace facing) {
    checkValidFacing(facing);
    BlockFace right = Util.rotateRight(facing);

    int bx = location.getBlockX();
    int by = location.getBlockY();
    int bz = location.getBlockZ();

    for (int x = 0; x < mSize; ++x) {
      for (int y = 0; y < mSize; ++y) {
        Block block = Objects.requireNonNull(location.getWorld()).getBlockAt(bx + right.getModX() * x + facing.getModX() * y, by, bz + right.getModZ() * x + facing.getModZ() * y);
        Material data = mData[x + (y * mSize)];
        if (block.getType() != data)
          return false;
      }
    }

    return true;
  }

  void save(File file) throws IOException {
    YamlConfiguration config = new YamlConfiguration();
    config.set("size", mSize);
    List<String> mats = new ArrayList<>(mData.length);
    for (Material data : mData) {
      mats.add(data.name());
    }

    config.set("data", mats);
    config.save(file);
  }
}
