package me.tigerhix.lib.scoreboard.type;

import java.util.ArrayList;
import java.util.List;

import me.tigerhix.lib.scoreboard.ScoreboardLib;
import me.tigerhix.lib.scoreboard.common.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

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
    protected long updateInterval = 10L;
    private boolean activated;
    private ScoreboardHandler handler;
    private BukkitTask updateTask;

    public SimpleScoreboard(Player holder,String board_name) {
        this.holder = holder;
        // Initiate the Bukkit scoreboard
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        scoreboard.registerNewObjective("board_"+board_name, "dummy").setDisplaySlot(DisplaySlot.SIDEBAR);
        objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        for (int i = 1; i <= 15; i++) {
            Team team = scoreboard.registerNewTeam(TEAM_PREFIX + i);
            team.addEntry(genEntry(i));
        }
    }

    @Override
    public void activate() {
        if (activated) {
            return;
        }
        if (handler == null) {
            throw new IllegalArgumentException("Scoreboard handler not set");
        }
        activated = true;
        // Set to the custom scoreboard
        holder.setScoreboard(scoreboard);
        // And start updating on a desired interval
        updateTask = Bukkit.getServer().getScheduler().runTaskTimer(ScoreboardLib.getPluginInstance(), this::update, 0, updateInterval);
    }

    @Override
    public void deactivate() {
        if (!activated) {
            return;
        }
        activated = false;
        // Set to the main scoreboard
        if (holder.isOnline()) {
            synchronized (this) {
                holder.setScoreboard((Bukkit.getScoreboardManager().getMainScoreboard()));
            }
        }
        // Unregister teams that are created for this scoreboard
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
        // Stop updating
        updateTask.cancel();
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
        return updateInterval;
    }

    @Override
    public Scoreboard setUpdateInterval(long updateInterval) {
        if (activated) {
            throw new IllegalStateException("Scoreboard is already activated");
        }
        this.updateInterval = updateInterval;
        return this;
    }

    @Override
    public Player getHolder() {
        return holder;
    }

    private void update() {
        if (!holder.isOnline()) {
            deactivate();
            return;
        }
        // Title
        String handlerTitle = handler.getTitle(holder);
        String finalTitle = Strings
                .format(handlerTitle != null ? handlerTitle : ChatColor.BOLD.toString());
        if (!objective.getDisplayName().equals(finalTitle)) {
            objective.setDisplayName(Strings.format(finalTitle));
        }
        // Entries
        List<Entry> passed = handler.getEntries(holder);
        List<Integer> current = new ArrayList<>();
        if (passed == null) {
            return;
        }
        for (Entry entry : passed) {
            // Handle the entry
            String key = entry.getName();
            int score = entry.getPosition();

            Team team = scoreboard.getTeam(TEAM_PREFIX + score);
            String temp = genEntry(score);
            if (!scoreboard.getEntries().contains(temp)) {
                objective.getScore(temp).setScore(score);
            }

            // Add prefix & suffix
            key = Strings.format(key);
            String prefix = getFirstSplit(key);
            String suffix = getFirstSplit(ChatColor.getLastColors(prefix) + (prefix.endsWith("§") ? "§" : "") + getSecondSplit(key));
            team.setPrefix(prefix);
            team.setSuffix(suffix);
            // Add to current
            current.add(score);
        }
        // Remove duplicated or non-existent entries
        for (int i = 1; i <= 15; i++) {
            if (!current.contains(i)) {
                String entry = genEntry(i);
                if (scoreboard.getEntries().contains(entry)) {
                    scoreboard.resetScores(entry);
                }
            }
        }
    }

    public Objective getObjective() {
        return objective;
    }

    private String genEntry(int slot) {
        return ChatColor.values()[slot].toString();
    }

    public org.bukkit.scoreboard.Scoreboard getScoreboard() {
        return scoreboard;
    }

    private String getFirstSplit(String s) {
        return s.length() > 64 ? s.substring(0, 64) : s;
    }

    private String getSecondSplit(String s) {
        if (s.length() > 128) {
            s = s.substring(0, 128);
        }
        return s.length() > 64 ? s.substring(64) : "";
    }
}
