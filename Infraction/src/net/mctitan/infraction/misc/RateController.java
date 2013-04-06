package net.mctitan.infraction.misc;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;

import net.mctitan.infraction.Infraction;
import net.mctitan.infraction.InfractionManager;
import net.mctitan.infraction.InfractionType;

import java.util.HashMap;

/**
 * Generic controller for player rates, extended by other classes
 * 
 * @author mindless728
 */
public abstract class RateController implements Runnable, Listener {
    /** tracks on a per player name basis */
    HashMap<String,Integer> tracker;
    
    /** infraction manager for creating new infractions */
    InfractionManager manager;
    
    /** default contructor that creates/obtains objects */
    public RateController() {
        tracker = new HashMap<String,Integer>();
        manager = InfractionManager.getInstance();
    }
    
    /**
     * registers a player with the given rate controller, if too high a ban ensues
     * @param player 
     */
    protected boolean register(Player player) {
        //get the current amount
        Integer i = tracker.get(player.getName());
        
        //if it doesn't exist, make one
        if(i == null)
            i = new Integer(0);
        
        //increment said amount
        ++i;
        
        //if the amount id too high, create an infraction to ban and kick the player
        if(i.compareTo(getLimiter()) >= 0) {
            manager.createInfraction(getIssuer(), player.getName(), InfractionType.BAN, getReason());
            
            //player was banned
            return true;
        }
        
        //push the amount back into the tracker
        tracker.put(player.getName(), i);
        
        //player was not banned
        return false;
    }
    
    /** the tracker needs to be cleared every so often */
    @Override
    public void run() {
        tracker.clear();
    }
    
    /**
     * registers player events, and canceles them if player is banned
     * 
     * @param event 
     */
    protected void genericEvent(PlayerEvent event) {
        Player player = event.getPlayer();
        
        //if the player is banned, cancel the event, and return
        if(manager.getPlayerData(player.getName()).isBanned()) {
            if(event instanceof Cancellable)
                ((Cancellable)event).setCancelled(true);
            return;
        }
         
        
        //register this event to the player
        if(register(player))
            if(event instanceof Cancellable)
                ((Cancellable)event).setCancelled(true);
    }
    
    /**
     * gets the limiting amount beore a ban
     * @return the limiting amount
     */
    protected abstract int getLimiter();
    
    /**
     * gets the string that shows as who banned the person
     * 
     * @return string that created the infraction
     */
    protected abstract String getIssuer();
    
    /**
     * gets the reason for the ban
     * 
     * @return bane reason
     */
    protected abstract String getReason();
}
