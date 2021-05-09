package me.tigerhix.lib.scoreboard;

import me.tigerhix.lib.scoreboard.type.LegacySimpleScoreboard;
import me.tigerhix.lib.scoreboard.type.Scoreboard;
import me.tigerhix.lib.scoreboard.type.SimpleScoreboard;
import me.tigerhix.lib.scoreboard.util.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

public final class ScoreboardLib extends JavaPlugin {

  private static Plugin instance;

  public static Plugin getPluginInstance() {
    return instance;
  }

  public static void setPluginInstance(Plugin instance) {
    if(ScoreboardLib.instance != null) return;
    ScoreboardLib.instance = instance;
  }

  public static Scoreboard createScoreboard(Player holder) {
    if(Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")) {
      return new LegacySimpleScoreboard(holder);
    }
    if(Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
      ViaAPI api = Via.getAPI(); // Get the API
      int version = api.getPlayerVersion(holder); // Get the protocol version
      if(version > 404) {
        return new SimpleScoreboard(holder);
      }
    } else if(ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_14_R1)) {
      return new SimpleScoreboard(holder);
    }
    return new LegacySimpleScoreboard(holder);
  }

  @Override
  public void onEnable() {
    setPluginInstance(this);
  }

}