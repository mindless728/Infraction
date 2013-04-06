package net.mctitan.infraction.misc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.EventPriority;

/**
 * Controls how fast people can chat and auto bans auto spammers (both chat and command)
 * 
 * @author Colin
 */
public class SpamControl extends RateController {
    /** how many chats/commands allowed per second*/
    public static int limiter = 5;
    
    /** the name of the issuer when this bans someone */
    public static String issuer = "StopSpam";
    
    /** the reason for banning when this bans someone */
    public static String reason = "Auto Spamming Bitch";
    
    @Override
    protected int getLimiter() {
        return limiter;
    }
    
    @Override
    protected String getIssuer() {
        return issuer;
    }
    
    @Override
    protected String getReason() {
        return reason;
    }
    
    /**
     * listens for player chat events
     * 
     * @param event event triggered by player chat
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(PlayerChatEvent event) {
        genericEvent(event);
    }
    
    /**
     * listens for player command events
     * 
     * @param event event triggered when a player issues a command
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        genericEvent(event);
    }
}
