package me.tigerhix.lib.scoreboard.type;

import me.tigerhix.lib.scoreboard.common.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 28.12.2020
 */
public class SimpleScoreboard implements Scoreboard {

  private static final String TEAM_PREFIX = "Board_";
  private final org.bukkit.scoreboard.Scoreboard scoreboard;
  private final Objective objective;
  protected Player holder;
  private boolean activated;
  private ScoreboardHandler handler;

  public SimpleScoreboard(Player holder) {
    this.holder = holder;
    // Initiate the Bukkit scoreboard
    scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    scoreboard.registerNewObjective("board", "dummy").setDisplaySlot(DisplaySlot.SIDEBAR);
    objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
    for(int i = 1; i <= 15; i++) {
      scoreboard.registerNewTeam(TEAM_PREFIX + i).addEntry(getEntry(i));
    }
  }

  @Override
  public void activate() {
    if(activated) {
      return;
    }
    if(handler == null) {
      throw new IllegalArgumentException("Scoreboard handler not set");
    }
    activated = true;
    // Set to the custom scoreboard
    holder.setScoreboard(scoreboard);
  }

  @Override
  public void deactivate() {
    if(!activated) {
      return;
    }
    activated = false;
    // Set to the main scoreboard
    if(holder.isOnline()) {
      synchronized(this) {
        holder.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
      }
    }
    // Unregister teams that are created for this scoreboard
    for(Team team : scoreboard.getTeams()) {
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
    if (!activated) {
      return;
    }

    if(!holder.isOnline()) {
      deactivate();
      return;
    }

    String handlerTitle = handler.getTitle(holder);
    String finalTitle = handlerTitle != null ? Strings.format(handlerTitle) : ChatColor.BOLD.toString();

    if(!objective.getDisplayName().equals(finalTitle)) {
      objective.setDisplayName(finalTitle);
    }

    List<Entry> passed = handler.getEntries(holder);

    if(passed == null) {
      return;
    }

    List<Integer> current = new ArrayList<>(passed.size());

    for(Entry entry : passed) {
      int score = entry.getPosition();
      Team team = scoreboard.getTeam(TEAM_PREFIX + score);
      String temp = getEntry(score);

      if(!scoreboard.getEntries().contains(temp)) {
        objective.getScore(temp).setScore(score);
      }

      String key = Strings.format(entry.getName());
      int length = key.length();

      String prefix = length > 64 ? key.substring(0, 64) : key;
      String suffix = ChatColor.getLastColors(prefix) + (prefix.charAt(prefix.length() - 1) == '§' ? "§" : "") + limitKey(length, key);

      team.setPrefix(prefix);
      team.setSuffix(suffix.length() > 64 ? suffix.substring(0, 64) : suffix);

      current.add(score);
    }

    // Remove duplicated or non-existent entries
    for(int i = 1; i <= 15; i++) {
      if(!current.contains(i)) {
        String entry = getEntry(i);

        if(scoreboard.getEntries().contains(entry)) {
          scoreboard.resetScores(entry);
        }
      }
    }
  }

  public Objective getObjective() {
    return objective;
  }

  private final ChatColor[] values = ChatColor.values();

  private String getEntry(int slot) {
    return values[slot].toString();
  }

  public org.bukkit.scoreboard.Scoreboard getScoreboard() {
    return scoreboard;
  }

  private String limitKey(int length, String str) {
    if(length > 128) {
      return str.substring(0, 128);
    }

    return length > 64 ? str.substring(64) : "";
  }
}
