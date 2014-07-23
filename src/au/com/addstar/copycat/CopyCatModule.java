package au.com.addstar.copycat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import au.com.addstar.copycat.GameBoard.GameMode;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.Flag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemNewLine;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.MinigameModule;

public class CopyCatModule extends MinigameModule
{
	private IntegerFlag mSubjectDrawTime;
	private BooleanFlag mAllowSubjectDraw;
	private IntegerFlag mWaitTime;
	private BooleanFlag mSaveSubjects;
	private EnumFlag<GameMode> mMode;
	private IntegerFlag mMaxRoundTime;
	private IntegerFlag mBackBoardDistance;
	private IntegerFlag mBackBoardHeight;
	
	private GameBoard mBoard;
	private boolean mHasInit;
	
	public CopyCatModule( Minigame mgm )
	{
		super(mgm);
		
		mSubjectDrawTime = new IntegerFlag(60, "patternDrawTime");
		mAllowSubjectDraw = new BooleanFlag(true, "allowPatternDraw");
		mWaitTime = new IntegerFlag(10, "waitTime");
		mSaveSubjects = new BooleanFlag(true, "savePatterns");
		mMode = new EnumFlag<GameMode>(GameMode.Elimination, "copycatMode");
		mMaxRoundTime = new IntegerFlag(120, "maxRoundTime");
		mBackBoardDistance = new IntegerFlag(4, "backboardDistance");
		mBackBoardHeight = new IntegerFlag(3, "backboardHeight");
		
		mHasInit = false;
	}

	@Override
	public String getName()
	{
		return "CopyCat";
	}
	
	public static CopyCatModule getMinigameModule(Minigame minigame)
	{
		return (CopyCatModule)minigame.getModule("CopyCat");
	}

	@Override
	public Map<String, Flag<?>> getFlags()
	{
		HashMap<String, Flag<?>> flags = new HashMap<String, Flag<?>>();
		flags.put(mSubjectDrawTime.getName(), mSubjectDrawTime);
		flags.put(mAllowSubjectDraw.getName(), mAllowSubjectDraw);
		flags.put(mWaitTime.getName(), mWaitTime);
		flags.put(mSaveSubjects.getName(), mSaveSubjects);
		flags.put(mMode.getName(), mMode);
		flags.put(mMaxRoundTime.getName(), mMaxRoundTime);
		flags.put(mBackBoardDistance.getName(), mBackBoardDistance);
		flags.put(mBackBoardHeight.getName(), mBackBoardHeight);
		
		return flags;
	}

	@Override
	public boolean useSeparateConfig()
	{
		return true;
	}

	@Override
	public void save( FileConfiguration config )
	{
		if(mBoard != null)
			mBoard.write(config.createSection("board"));
	}

	@Override
	public void load( FileConfiguration config )
	{
		if(config.isConfigurationSection("board"))
			mBoard = new GameBoard(config.getConfigurationSection("board"));
		else
			mBoard = null;
	}

	@Override
	public void addMenuOptions( Menu menu )
	{
	}

	@Override
	public boolean getMenuOptions( Menu previous )
	{
		Menu menu = new Menu(6, getMinigame().getName(false), previous.getViewer());
		menu.addItem(mWaitTime.getMenuItem("Wait Time", Material.WATCH, Arrays.asList("The time in between rounds in seconds.")));
		menu.addItem(mMaxRoundTime.getMenuItem("Max Round Time", Material.WATCH, Arrays.asList("The maximum amount of time one round","can run for")));
		menu.addItem(getMenuItem("Mode", Material.PAPER, mMode, GameMode.class, Arrays.asList("The CopyCat game mode to use for","this minigame.")));
		menu.addItem(new MenuItemNewLine());
		menu.addItem(mAllowSubjectDraw.getMenuItem("Allow Pattern Draw", Material.LEVER, Arrays.asList("When true, before the round starts,","a player will be selected to draw the pattern","for the others")));
		menu.addItem(mSubjectDrawTime.getMenuItem("Pattern Draw Time", Material.WATCH, Arrays.asList("The time allowed for drawing the pattern")));
		menu.addItem(mSaveSubjects.getMenuItem("Save Patterns", Material.REDSTONE_TORCH_ON, Arrays.asList("When true, patterns created by players","during pattern drawing time will be","saved and used during random pattern loading")));
		menu.addItem(new MenuItemNewLine());
		menu.addItem(mBackBoardDistance.getMenuItem("Backboard Distance", Material.IRON_BOOTS, Arrays.asList("The distance from the end of the ","play area where the backboard will be located.")));
		menu.addItem(mBackBoardHeight.getMenuItem("Backboard Height", Material.FEATHER, Arrays.asList("The height offset for the backboard")));
		
		menu.addItem(new MenuItemPage("Back", Material.REDSTONE_TORCH_ON, previous), menu.getSize() - 9);
		menu.displayMenu(previous.getViewer());
		
		return true;
	}
	
	private <T extends Enum<T>> MenuItemEnum<T> getMenuItem(String name, Material material, final EnumFlag<T> flag, Class<T> enumClass, List<String> description)
	{
		MenuItemEnum<T> item = new MenuItemEnum<T>(enumClass, name, description, new Callback<T>()
		{
			@Override
			public void setValue( T value )
			{
				flag.setFlag(value);
			}

			@Override
			public T getValue()
			{
				return flag.getFlag();
			}
		}, material);
		
		return item;
	}
	
	public int getSubjectDrawTime()
	{
		return mSubjectDrawTime.getFlag() * 1000;
	}
	
	public int getWaitTime()
	{
		return mWaitTime.getFlag() * 1000;
	}
	
	public int getMaxRoundTime()
	{
		return mMaxRoundTime.getFlag() * 1000;
	}
	
	public int getBackboardDistance()
	{
		return mBackBoardDistance.getFlag();
	}
	
	public int getBackboardHeight()
	{
		return mBackBoardHeight.getFlag();
	}
	
	public boolean getAllowSubjectDraw()
	{
		return mAllowSubjectDraw.getFlag();
	}
	
	public boolean getSaveSubjects()
	{
		return mSaveSubjects.getFlag();
	}
	
	public GameMode getMode()
	{
		return mMode.getFlag();
	}

	public GameBoard getGameBoard()
	{
		if(!getMinigame().getMechanicName().equals("CopyCat"))
			return null;
		
		if(!mHasInit && mBoard != null)
		{
			mBoard.initialize(this);
			mHasInit = true;
		}
		
		return mBoard;
	}
	
	public void setGameBoard(GameBoard board)
	{
		mBoard = board;
	}

}
