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
        
        //if the player isn't banned, alert moderators of infractions
        if(!manager.isBanned(name)) {
            notifyAdminsOfInfractionJoin(name);
            return;
        }
        
        //get the most recent infraction if banned
        Infraction infract = manager.getInfraction(name, 0);
        
        //if there is no recent infraction, something is wrong, but still say banned
        if(infract == null)
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "You are banned from this server");
        //if the infraction exists, show the message or issuer and reason
        else
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, infract.getOutput(name));
    }
    
    /**
     * Notifies moderators when someone joins the server with/without infractions
     * 
     * @param name the name of the player joining
     */
    private void notifyAdminsOfInfractionJoin(String name) {
        //get the number of infractions the player has
        int infracts = manager.numberInfractions(name);
        
        String msg;
        //if there are no infractions, say so
        if(infracts == 0)
            msg = ChatColor.AQUA+name+ChatColor.GREEN+" has no infractions :)";
        //if there are infractions say how many
        else
            msg = ChatColor.RED+name+ChatColor.GREEN+" has "+ChatColor.GOLD+infracts+
                  ChatColor.GREEN+" infraction"+(infracts>1?"s":"");
        
        //lop through all of the online players
        for(Player p : plugin.getServer().getOnlinePlayers())
            //if the player isn't the joiner and is a moderator or admin, send the message
            if(!p.getName().equals(name) && (p.hasPermission(InfractionPerm.MODERATOR.toString()) ||
                                             p.hasPermission(InfractionPerm.ADMIN.toString())))
                p.sendRawMessage(msg);
    }
}
