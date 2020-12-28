package me.tigerhix.lib.scoreboard;

import me.tigerhix.lib.scoreboard.type.SimpleScoreboard;
import me.tigerhix.lib.scoreboard.type.Scoreboard;
import me.tigerhix.lib.scoreboard.type.LegacySimpleScoreboard;
import me.tigerhix.lib.scoreboard.util.ServerVersion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class ScoreboardLib extends JavaPlugin {

    private static Plugin instance;

    public static Plugin getPluginInstance() {
        return instance;
    }

    public static void setPluginInstance(Plugin instance) {
        if (ScoreboardLib.instance != null) return;
        ScoreboardLib.instance = instance;
    }

    public static Scoreboard createScoreboard(Player holder) {
        if (ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_14_R1)){
            return new SimpleScoreboard(holder);
        }
        return new LegacySimpleScoreboard(holder);
    }

    @Override
    public void onEnable() {
        setPluginInstance(this);
    }

}