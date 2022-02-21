package me.tigerhix.lib.scoreboard;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import me.tigerhix.lib.scoreboard.type.LegacySimpleScoreboard;
import me.tigerhix.lib.scoreboard.type.Scoreboard;
import me.tigerhix.lib.scoreboard.type.SimpleScoreboard;
import me.tigerhix.lib.scoreboard.util.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import protocolsupport.api.ProtocolSupportAPI;

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
      try {
        int version = ProtocolSupportAPI.getProtocolVersion(holder).getId();
        if(version >= 401 && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_13_R1)) {
          //only give player & server higher 1.12 the better scoreboard
          return new SimpleScoreboard(holder);
        }
      } catch(Exception ignored) {
        //Can't interact with protocol api
      }
      return new LegacySimpleScoreboard(holder);
    }
    if(Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
      try {
        ViaAPI api = Via.getAPI(); // Get the API
        int version = api.getPlayerVersion(holder); // Get the protocol version
        if(version > 404 && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_14_R1)) {
          //only give player & server higher 1.13 the better scoreboard
          return new SimpleScoreboard(holder);
        }
      } catch(Exception ignored) {
        //Not using ViaVersion 4 or unable to get ViaVersion return LegacyBoard!
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