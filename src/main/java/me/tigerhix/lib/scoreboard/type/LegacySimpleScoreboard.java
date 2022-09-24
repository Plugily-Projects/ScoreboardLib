package me.tigerhix.lib.scoreboard.type;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.tigerhix.lib.scoreboard.common.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public class LegacySimpleScoreboard implements Scoreboard {

  private static final String TEAM_PREFIX = "Board_";
  private static int TEAM_COUNTER = 0;

  private final org.bukkit.scoreboard.Scoreboard scoreboard;
  private final Objective objective;

  protected Player holder;

  private boolean activated;
  private ScoreboardHandler handler;
  private Map<FakePlayer, Integer> entryCache = new ConcurrentHashMap<>();
  private Table<String, Integer, FakePlayer> playerCache = HashBasedTable.create();
  private Table<Team, String, String> teamCache = HashBasedTable.create();

  public LegacySimpleScoreboard(Player holder) {
    this.holder = holder;
    // Initiate the Bukkit scoreboard
    scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    scoreboard.registerNewObjective("board", "dummy").setDisplaySlot(DisplaySlot.SIDEBAR);
    objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
  }

  @Override
  public void activate() {
    if(activated) return;
    if(handler == null) throw new IllegalArgumentException("Scoreboard handler not set");
    activated = true;
    // Set to the custom scoreboard
    holder.setScoreboard(scoreboard);
  }

  @Override
  public void deactivate() {
    if(!activated) return;
    activated = false;
    // Set to the main scoreboard
    if(holder.isOnline()) {
      synchronized(this) {
        holder.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
      }
    }
    // Unregister teams that are created for this scoreboard
    for(Team team : teamCache.rowKeySet()) {
      team.unregister();
    }
  }

  @Override
  public boolean isActivated() {
    return activated;
  }

  @Override
  public ScoreboardHandler getHandler() {
    return handler;
  }

  @Override
  public Scoreboard setHandler(ScoreboardHandler handler) {
    this.handler = handler;
    return this;
  }

  @Override
  public long getUpdateInterval() {
    throw new UnsupportedOperationException("Update interval is not supported anymore");
  }

  @Override
  public LegacySimpleScoreboard setUpdateInterval(long updateInterval) {
    throw new UnsupportedOperationException("Update interval is not supported anymore");
  }

  @Override
  public Player getHolder() {
    return holder;
  }

  @Override
  public void update() {
    if(!holder.isOnline()) {
      deactivate();
      return;
    }

    // Title
    String handlerTitle = handler.getTitle(holder);
    String finalTitle = handlerTitle != null ? Strings.format(handlerTitle) : ChatColor.BOLD.toString();

    if(!objective.getDisplayName().equals(finalTitle)) {
      objective.setDisplayName(finalTitle);
    }

    // Entries
    List<Entry> passed = handler.getEntries(holder);
    if(passed == null) {
      return;
    }

    Map<String, Integer> appeared = new HashMap<>(passed.size());
    Set<FakePlayer> current = new HashSet<>(passed.size());

    for(Entry entry : passed) {
      String key = entry.getName();
      int score = entry.getPosition();

      if(key.length() > 48) {
        key = key.substring(0, 47);
      }

      int val = appeared.computeIfAbsent(key.length() > 16 ? key.substring(16) : key, k -> -1) + 1;
      appeared.put(key, val);

      FakePlayer faker = getFakePlayer(key, val);
      Score fakePlayerScore = objective.getScore(faker);

      // Set score
      for(String ks : scoreboard.getEntries()) {
        Score sc = objective.getScore(ks);

        if(score == sc.getScore() && !sc.getEntry().equals(fakePlayerScore.getEntry())) {
          scoreboard.resetScores(ks);
          break;
        }
      }

      fakePlayerScore.setScore(score);

      // Update references
      entryCache.put(faker, score);
      current.add(faker);
    }

    appeared.clear();

    // Remove duplicated or non-existent entries
    for(FakePlayer fakePlayer : entryCache.keySet()) {
      if(!current.contains(fakePlayer)) {
        entryCache.remove(fakePlayer);
        scoreboard.resetScores(fakePlayer.toString());
      }
    }
  }

  private FakePlayer getFakePlayer(String text, int offset) {
    Team team = null;
    String name;
    int length = text.length();

    // If the text has a length less than 16, teams need not to be be created
    if(length <= 16) {
      name = text + Strings.repeat(" ", offset);
    } else {
      offset++;

      // Otherwise, iterate through the string and cut off prefix and suffix
      int index = 16 - offset;

      String prefix = text.substring(0, index);
      name = text.substring(index);

      if(name.length() > 16) {
        name = name.substring(0, 16);
      }

      String suffix = "";
      if(length > 32) {
        suffix = text.substring(32 - offset);
      }

      // If teams already exist, use them
      for(Team other : teamCache.rowKeySet()) {
        if(other.getPrefix().equals(prefix) && suffix.equals(other.getSuffix())) {
          team = other;
        }
      }

      // Otherwise create them
      if(team == null) {
        team = scoreboard.registerNewTeam(TEAM_PREFIX + TEAM_COUNTER++);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        teamCache.put(team, prefix, suffix);
      }
    }

    FakePlayer fakePlayer = playerCache.get(name, offset);

    if(fakePlayer == null) {
      fakePlayer = new FakePlayer(name, team);
      playerCache.put(name, offset, fakePlayer);
    } else {
      if(team != null && fakePlayer.team != null) {
        fakePlayer.team.removePlayer(fakePlayer);
      }

      fakePlayer.team = team;
    }

    if(fakePlayer.team != null) {
      fakePlayer.team.addPlayer(fakePlayer);
    }

    return fakePlayer;
  }

  public Objective getObjective() {
    return objective;
  }

  public org.bukkit.scoreboard.Scoreboard getScoreboard() {
    return scoreboard;
  }

  private static class FakePlayer implements OfflinePlayer {

    private final UUID randomId = UUID.randomUUID();

    private final String name;
    private Team team;

    FakePlayer(String name, Team team) {
      this.name = name;
      this.team = team;
    }

    @Override
    public boolean isOnline() {
      return true;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public UUID getUniqueId() {
      return randomId;
    }

    @Override
    public boolean isBanned() {
      return false;
    }

    @Override
    public boolean isWhitelisted() {
      return false;
    }

    @Override
    public void setWhitelisted(boolean whitelisted) {
    }

    @Override
    public Player getPlayer() {
      return null;
    }

    @Override
    public long getFirstPlayed() {
      return 0;
    }

    @Override
    public long getLastPlayed() {
      return 0;
    }

    @Override
    public boolean hasPlayedBefore() {
      return false;
    }

    @Override
    public Location getBedSpawnLocation() {
      return null;
    }

    @Override
    public Map<String, Object> serialize() {
      return null;
    }

    @Override
    public boolean isOp() {
      return false;
    }

    @Override
    public void setOp(boolean op) {
    }

    @Override
    public String toString() {
      return "FakePlayer{" +
          "name='" + name + '\'' +
          ", team=" + team
          + '}';
    }

  }

}