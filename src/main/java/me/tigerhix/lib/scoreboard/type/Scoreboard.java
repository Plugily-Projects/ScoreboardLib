package me.tigerhix.lib.scoreboard.type;

import org.bukkit.entity.Player;

/**
 * Represents an advanced scoreboard that can display up to 48 characters in a single entry.
 *
 * @author TigerHix
 */
public interface Scoreboard {

    /**
     * Activate the scoreboard.
     */
    void activate();

    /**
     * Deactivate the scoreboard.
     */
    void deactivate();

    /**
     * Updates the scoreboard lines.
     */
    void update();

    /**
     * Determine if the scoreboard has been already activated.
     *
     * @return activated
     */
    boolean isActivated();

    /**
     * Returns the handler for this scoreboard.
     *
     * @return handler
     */
    ScoreboardHandler getHandler();

    /**
     * Set the handler for this scoreboard.
     *
     * @param handler handler
     */
    Scoreboard setHandler(ScoreboardHandler handler);

    /**
     * Returns the update interval (default = 10L).
     *
     * @deprecated not used anymore
     * @return update interval
     */
    @Deprecated
    long getUpdateInterval();

    /**
     * Set the update interval.
     *
     * @param updateInterval update interval
     * @deprecated not used anymore
     * @return this
     */
    @Deprecated
    Scoreboard setUpdateInterval(long updateInterval);

    /**
     * Returns the holder of this scoreboard.
     *
     * @return holder
     */
    Player getHolder();

}
