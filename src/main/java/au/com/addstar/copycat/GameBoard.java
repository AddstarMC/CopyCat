package au.com.addstar.copycat;

import au.com.addstar.copycat.logic.EliminationMainState;
import au.com.addstar.copycat.logic.PreRoundState;
import au.com.addstar.copycat.logic.ScoringMainState;
import au.com.addstar.copycat.logic.State;
import au.com.addstar.copycat.logic.StateEngine;
import au.com.mineauz.minigames.MinigameMessageType;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class GameBoard {
  private PlayerStation[] mStations;
  private PatternStation mPatternStation;
  private int mSize;
  private CopyCatModule mModule;
  private int mNextToDraw;

  // In game vars
  private Subject mSubject;
  private MinigamePlayer mSubjectDrawer;
  private StateEngine<GameBoard> mEngine = new StateEngine<>();
  private BossBar mBossDisplay;
  private EditSession mEditSession;

  private GameBoard() {
    mBossDisplay = Bukkit.createBossBar("Waiting for players", BarColor.PURPLE, BarStyle.SOLID);
    mBossDisplay.setProgress(1);
  }

  /**
   * @param players number of players
   * @param size size of board
   */
  public GameBoard(int players, int size) {
    this();

    mStations = new PlayerStation[players];

    for (int i = 0; i < players; ++i) {
      mStations[i] = new PlayerStation(this);
    }

    mPatternStation = new PatternStation(this);

    mSize = size;
  }

  public GameBoard(ConfigurationSection section) {
    this();

    read(section);
  }

  public void initialize(CopyCatModule module) {
    mModule = module;
  }

  public CopyCatModule getModule() {
    return mModule;
  }

  public int getSubjectSize() {
    return mSize;
  }

  public void setSubjectSize(int size) {
    mSize = size;
  }

  public Subject getSubject() {
    return mSubject;
  }

  public void setSubject(Subject subject) {
    mSubject = subject;
  }

  public MinigamePlayer getDrawer() {
    return mSubjectDrawer;
  }

  public void setDrawer(MinigamePlayer player) {
    mSubjectDrawer = player;
  }

  public MinigamePlayer selectNextDrawer() {
    Minigame minigame = getMinigame();
    mSubjectDrawer = minigame.getPlayers().get(mNextToDraw);
    ++mNextToDraw;
    if (mNextToDraw >= minigame.getPlayers().size()) {
      mNextToDraw = 0;
    }

    return mSubjectDrawer;
  }

  public boolean isValid() {
    return getErrors().isEmpty();
  }

  public List<String> getErrors() {
    ArrayList<String> errors = new ArrayList<>();

    for (int i = 0; i < mStations.length; ++i) {
      if (!mStations[i].isValid()) {
        errors.add("Player station " + (i + 1) + " is not set");
      }
    }

    if (mModule.getAllowSubjectDraw() && !mPatternStation.isValid()) {
      errors.add("Pattern drawing is allowed but the pattern drawing area has not been set.");
    }

    if (mEditSession != null) {
      errors.add("The game is being edited currently, please try again later.");
    }

    return errors;
  }

  public boolean isInProgress() {
    Minigame minigame = getMinigame();
    if (minigame == null) {
      return false;
    }

    return (minigame.hasPlayers());
  }

  public Minigame getMinigame() {
    return mModule.getMinigame();
  }

  public int getStationCount() {
    return mStations.length;
  }

  public void setStationCount(int count) {
    mStations = Arrays.copyOf(mStations, count);
  }

  public PlayerStation getStation(int number) {
    return mStations[number];
  }

  public PlayerStation getStation(MinigamePlayer player) {
    for (PlayerStation station : mStations) {
      if (station.getPlayer() == player) {
        return station;
      }
    }

    return null;
  }

  public PlayerStation[] getStations() {
    return mStations;
  }

  public PatternStation getPatternStation() {
    return mPatternStation;
  }

  public BossBar getBossDisplay() {
    return mBossDisplay;
  }

  public boolean canModify(MinigamePlayer player, Location location) {
    if (mPatternStation.getPlayer() == player) {
      return mPatternStation.isInPatternArea(location);
    }

    PlayerStation station = getStation(player);
    return (station.getCanModify() && station.isInPlayArea(location));
  }

  public void write(ConfigurationSection config) {
    config.set("Size", mSize);
    config.set("StationCount", mStations.length);
    for (int i = 0; i < mStations.length; ++i) {
      ConfigurationSection section = config.createSection("Station" + i);
      mStations[i].save(section);
    }

    mPatternStation.save(config.createSection("PatternStation"));
  }

  public void read(ConfigurationSection config) {
    mSize = config.getInt("Size");
    int count = config.getInt("StationCount");
    mStations = new PlayerStation[count];
    for (int i = 0; i < count; ++i) {
      ConfigurationSection section = config.getConfigurationSection("Station" + i);
      mStations[i] = new PlayerStation(this);
      mStations[i].read(section);
    }

    mPatternStation = new PatternStation(this);
    if (config.isConfigurationSection("PatternStation")) {
      ConfigurationSection section = config.getConfigurationSection("PatternStation");
      if (section != null) {
        mPatternStation.read(section);
      } else {
        Minigames.log(Level.WARNING,"PatternStation is has null config for: " + config.getCurrentPath() );
      }
    }
  }

  /**
   * @param message The message
   * @param except Except this player
   */
  public void broadcast(String message, MinigamePlayer except) {
    for (MinigamePlayer player : getMinigame().getPlayers()) {
      if (except != player) {
        player.sendMessage(message, MinigameMessageType.INFO);
      }
    }
  }

  public void beginGame() {
    Minigame minigame = getMinigame();
    mNextToDraw = CopyCatPlugin.rand.nextInt(minigame.getPlayers().size());

    // Assign stations
    ArrayList<MinigamePlayer> excess = new ArrayList<>();
    for (int i = 0; i < minigame.getPlayers().size(); ++i) {
      MinigamePlayer player = minigame.getPlayers().get(i);

      if (i > mStations.length) {
        excess.add(player);
      } else {
        mStations[i].setPlayer(player);
        mStations[i].setCanModify(false);
        player.teleport(mStations[i].getSpawnLocation());
      }
    }

    // Remove excess
    for (MinigamePlayer player : excess) {
      player.sendMessage("There was not enough spots to place players", MinigameMessageType.ERROR);
      Minigames.getPlugin().getPlayerManager().quitMinigame(player, true);
    }

    for (PlayerStation station : mStations) {
      station.clearStation();
    }

    Objective objective = minigame.getScoreboardManager().getObjective(minigame.getName(false));
    if (objective != null) {
      objective.setDisplayName("\u21D0  " + minigame.getName(true) + "  \u21D2");
    }

    Bukkit.getScheduler().runTask(CopyCatPlugin.instance, () -> {
      if (mModule.getMode() == GameMode.Elimination) {
        Minigame minigame1 = getMinigame();
        for (MinigamePlayer player : minigame1.getPlayers()) {
          minigame1.setScore(player, Math.round(minigame1.getLives()));
        }
      }
    });

    // Begin the game
    mEngine.start(new PreRoundState(), this);
  }

  /**
   * @param player {@link MinigamePlayer }
   */
  public void onPlayerLeave(MinigamePlayer player) {
    Minigame minigame = getMinigame();
    PlayerStation station = getStation(player);
    if (station != null) {
      station.clearStation();
      station.setPlayer(null);
      station.setCanModify(false);
    }

    if (mEngine.isRunning()) {
      if (minigame.getPlayers().size() <= 2) {
        endGame();
      }

      mEngine.sendEvent("leave", player);
    }
  }

  public void onPlaceBlock(MinigamePlayer player) {
    if (mEngine.isRunning()) {
      mEngine.sendEvent("place", player);
    }
  }

  public void endGame() {
    mEngine.end();

    for (PlayerStation station : mStations) {
      station.clearStation();
      station.setPlayer(null);
    }

    mBossDisplay.setTitle("Waiting for players");
    mBossDisplay.setStyle(BarStyle.SOLID);
    mBossDisplay.setColor(BarColor.PURPLE);
    mBossDisplay.setProgress(1);
  }

  /**
   * @return {@link State} the game board state
   */
  public State<GameBoard> getMainState() {
    switch (mModule.getMode()) {
      default:
      case Elimination:
        return new EliminationMainState();
      case Score:
        return new ScoringMainState();
    }
  }

  EditSession getEditSession() {
    return mEditSession;
  }

  void setEditSession(EditSession session) {
    mEditSession = session;
  }

  public enum GameMode {
    Elimination,
    Score
  }
}

