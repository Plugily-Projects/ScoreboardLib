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
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public final class ScoreboardLib extends JavaPlugin {

  private static Plugin instance;

  public static Plugin getPluginInstance() {
    return instance;
  }

  public static void setPluginInstance(Plugin instance) {
    if(ScoreboardLib.instance != null) return;
    ScoreboardLib.instance = instance;
  }

  public static Scoreboard createScoreboard(Player holder, @Nullable String new_board_name) {
    String board_name;
    if (new_board_name!=null && new_board_name.length()>0) {
      board_name=new_board_name;
    } else {
      int leftLimit = 48; // numeral '0'
      int rightLimit = 122; // letter 'z'
      int targetStringLength = 6;
      Random random = new Random();

      //Unique String Generator
      String generatedString = random.ints(leftLimit, rightLimit + 1)
              .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
              .limit(targetStringLength)
              .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
              .toString();

      board_name=generatedString;
    }
    if(Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")) {
      return new LegacySimpleScoreboard(holder,board_name);
    }
    if(Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
      try {
        ViaAPI api = Via.getAPI(); // Get the API
        int version = api.getPlayerVersion(holder); // Get the protocol version
        if(version > 404 && ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_14_R1)) {
          //only give player & server higher 1.13 the better scoreboard
          return new SimpleScoreboard(holder,board_name);
        }
      } catch(Exception ignored) {
        //Not using ViaVersion 4 or unable to get ViaVersion return LegacyBoard!
      }
    } else if(ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_14_R1)) {
      return new SimpleScoreboard(holder,board_name);
    }
    return new LegacySimpleScoreboard(holder,board_name);
  }

  @Override
  public void onEnable() {
    setPluginInstance(this);
  }

}