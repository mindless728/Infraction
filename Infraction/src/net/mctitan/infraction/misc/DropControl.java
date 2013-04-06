package net.mctitan.infraction.misc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.EventPriority;

/**
 * Controls how fast people can drop items, if too fast it bans them
 * 
 * @author mindless728
 */
public class DropControl extends RateController {
    /** how many chats/commands allowed per second*/
    public static int limiter = 20;
    
    /** the name of the issuer when this bans someone */
    public static String issuer = "StopDrop";
    
    /** the reason for banning when this bans someone */
    public static String reason = "Auto Dropping Bitch";
    
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
     * listens for players dropping items
     * 
     * @param event event that is triggered for dropped items
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if(event.isCancelled())
            return;
        
        genericEvent(event);
    }
}
