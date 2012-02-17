package net.mctitan.infraction.commands;

import net.mctitan.infraction.InfractionPerm;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Commands to infract players when they are offline
 * used for /owarn, /okick, and /oban
 * 
 * @author mindless728
 */
public class OfflineCommands extends OnlineCommands {
    /**
     * uses th command and gets the information out of the argument list to infract someone
     * @param sender what sent the command
     * @param command command that was called
     * @param Label alias of command used
     * @param args arguments that the command had
     * @return always true
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String Label, String [] args) {
        String name;
        String reason;
        
        if(args.length < 2) {
            sender.sendMessage(ChatColor.RED+"Not enough arguments!");
            return true;
        }
        
        name = args[0];
        reason = getReasonFromArgs(args,1);
        
        //check to see if he is infracting himself
        if(sender.getName().equals(name)) {
            sender.sendMessage(ChatColor.RED+"Cannot infract yourself");
            return true;
        }
        
        //check for permissions
        if(!sender.hasPermission(InfractionPerm.ADMIN.toString())) {
            sender.sendMessage(ChatColor.RED+"You do not have permissions to do that");
            return true;
        }
        
        //do different task for different commands
        switch (Label) {
            case "owarn":
                warn(sender, name, reason);
                break;
            case "okick":
                kick(sender, name, reason);
                break;
            case "oban":
                ban(sender, name, reason);
                break;
        }
        
        return true;
    }

    /**
     * warns a player when they are offline
     * 
     * @param sender what sent the command
     * @param name player name to warn
     * @param reason reason for the warning
     * 
     * @return true if successful
     */
    public boolean warn(CommandSender sender, String name, String reason) {
        //try to warn them normally (online)
        if(warn(sender, plugin.getServer().getPlayer(name), reason))
            return true;
        
        //if the player is banned, don't give an infraction
        if(manager.isBanned(name)) {
            sender.sendMessage(ChatColor.RED+name+ChatColor.GREEN+" is banned, cannot warn");
            return false;
        }
        
        //create the infraction for the offline player
        createInfraction(sender, name, reason, "warned");
        
        //return true
        return true;
    }
    
    /**
     * adds an infraction that says kick to an offline player
     * 
     * @param sender what sent the command
     * @param name player name to be infracted
     * @param reason reason for the infraction
     * 
     * @return true if successful
     */
    public boolean kick(CommandSender sender, String name, String reason) {
        //try to kick them normally
        if(kick(sender, plugin.getServer().getPlayer(name), reason))
            return true;
        
        //if the player is banned, don't give an infraction
        if(manager.isBanned(name)) {
            sender.sendMessage(ChatColor.RED+name+ChatColor.GREEN+" is banned, cannot kick");
            return false;
        }
        
        //create the infraction for the offline player
        createInfraction(sender, name, reason, "kicked");
        
        //return true
        return true;
    }
    
    /**
     * bans the offline player so they cannot connect
     * 
     * @param sender what sent the command
     * @param name player name to be infracted
     * @param reason reason for the infraction
     * 
     * @return true if successful
     */
    public boolean ban(CommandSender sender, String name, String reason) {
        //try to ban them normally
        if(ban(sender, plugin.getServer().getPlayer(name), reason))
            return true;
        
        //if the player is banned, don't give an infraction
        if(manager.isBanned(name)) {
            sender.sendMessage(ChatColor.RED+name+ChatColor.GREEN+" is banned, cannot ban");
            return false;
        }
        
        //create the infraction for the offline player
        createInfraction(sender, name, reason, "banned");
        
        //return true;
        return true;
    }
}
