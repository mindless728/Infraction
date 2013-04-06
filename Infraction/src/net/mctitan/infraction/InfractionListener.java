package net.mctitan.infraction;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Listens for events for Infraction for player joins
 * 
 * @author mindless728
 */
public class InfractionListener implements Listener {
    /** manages player infractions */
    InfractionManager manager;
    
    /** the infraction plugin, needed for small things */
    InfractionPlugin plugin;
    
    /** generic ban message if the player is somehow banned without an infraction */
    private static final String PLAYER_BANNED_GENERIC =
            InfractionChatColor.COLOR_BAD_PLAYER.getColor()+"You are banned from this server";
    
    /** message to display when players without infractions join */
    private static final String PLAYER_GOOD_JOINED =
            InfractionRegex.COLOR_PLAYER_REGEX+InfractionRegex.PLAYER_REGEX
            +InfractionRegex.COLOR_NORMAL_REGEX+" has no infractions :)";
    
    /** message to display when players with infractions join */
    private static final String PLAYER_BAD_JOINED =
            InfractionRegex.COLOR_PLAYER_REGEX+InfractionRegex.PLAYER_REGEX
            +InfractionRegex.COLOR_NORMAL_REGEX+" has "+InfractionRegex.COLOR_AMOUNT_REGEX
            +InfractionRegex.AMOUNT_REGEX+InfractionRegex.COLOR_NORMAL_REGEX+" infraction(s)";
    
    /** default constructor, creates/obtains objects */
    public InfractionListener() {
        manager = InfractionManager.getInstance();
        plugin = InfractionPlugin.getInstance();
    }
    
    /**
     * Handles when a player logins to the server
     * @param event event that is triggered by the player login
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLogin(PlayerLoginEvent event) {
        String name = event.getPlayer().getName();
        
        //check for whitelist, and if the player is not on it
        if(plugin.getServer().hasWhitelist() && !event.getPlayer().isWhitelisted())
            //do nothing as they can't gget in anyways
            return;
        
        //if the player isn't banned, alert moderators of infractions
        if(!manager.getPlayerData(name).isBanned()) {
            notifyAdminsOfInfractionJoin(name);
            return;
        }
        
        //get the most recent infraction if banned
        Infraction infract = manager.getPlayerData(name).getInfraction(0);
        
        //if there is no recent infraction, something is wrong, but still say banned
        if(infract == null)
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, PLAYER_BANNED_GENERIC);
        //if the infraction exists, show the message or issuer and reason
        else
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, infract.getOnFlyOutput(name));
    }
    
    /**
     * Notifies moderators when someone joins the server with/without infractions
     * 
     * @param name the name of the player joining
     */
    private void notifyAdminsOfInfractionJoin(String name) {
        //get the number of infractions the player has
        PlayerData player = manager.getPlayerData(name);
        int infracts = player.getNumberInfractions();
        
        //get the correct message
        String msg = (infracts==0?PLAYER_GOOD_JOINED:PLAYER_BAD_JOINED);
        
        //replace information in the string
        msg = msg.replace(InfractionRegex.PLAYER_REGEX,name);
        msg = msg.replace(InfractionRegex.AMOUNT_REGEX,""+infracts);
        
        //replace color information
        msg = InfractionChatColor.replaceColor(msg, player.getColor());
        msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COLOR_AMOUNT);
        msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COlOR_NORMAL);
        
        //lop through all of the online players
        for(Player p : plugin.getServer().getOnlinePlayers())
            //if the player isn't the joiner and is a moderator or admin, send the message
            if(!p.getName().equals(name) && (p.hasPermission(InfractionPerm.MODERATOR.perm) ||
                                             p.hasPermission(InfractionPerm.ADMIN.perm)))
                p.sendRawMessage(msg);
    }
}
