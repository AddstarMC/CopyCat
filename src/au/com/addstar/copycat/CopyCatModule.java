package au.com.addstar.copycat;

import java.util.Collections;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import au.com.addstar.copycat.GameBoard.GameMode;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.Flag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.menu.Menu;
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
		return Collections.emptyMap();
	}

	@Override
	public boolean useSeparateConfig()
	{
		return true;
	}

	@Override
	public void save( FileConfiguration config )
	{
		mSubjectDrawTime.saveValue("copycat", config);
		mAllowSubjectDraw.saveValue("copycat", config);
		mWaitTime.saveValue("copycat", config);
		mSaveSubjects.saveValue("copycat", config);
		mMode.saveValue("copycat", config);
		mMaxRoundTime.saveValue("copycat", config);
		mBackBoardDistance.saveValue("copycat", config);
		mBackBoardHeight.saveValue("copycat", config);
		
		if(mBoard != null)
			mBoard.write(config.createSection("board"));
	}

	@Override
	public void load( FileConfiguration config )
	{
		mSubjectDrawTime.loadValue("copycat", config);
		mAllowSubjectDraw.loadValue("copycat", config);
		mWaitTime.loadValue("copycat", config);
		mSaveSubjects.loadValue("copycat", config);
		mMode.loadValue("copycat", config);
		mMaxRoundTime.loadValue("copycat", config);
		mBackBoardDistance.loadValue("copycat", config);
		mBackBoardHeight.loadValue("copycat", config);
		
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
		return false;
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
