package net.mctitan.infraction.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.mctitan.infraction.InfractionChatColor;
import net.mctitan.infraction.InfractionPerm;
import net.mctitan.infraction.InfractionRegex;

/**
 * handles the offline commands
 * - /owarn, /okick, /oban
 * 
 * @author Colin
 */
public class OfflineCommands extends OnlineCommands {
    /** message sent if the sender is not an admin and tries the offline command */
    private static final String SENDER_NOT_ADMIN_ERROR =
            InfractionChatColor.COLOR_BAD_PLAYER.getColor()+"Only admins can offline ban players";
    
    /** message when infracting an already banned player */
    private static final String PLAYER_BANNED_ERROR =
            InfractionChatColor.COLOR_BAD_PLAYER.getColor()+"Player "+InfractionRegex.PLAYER_REGEX
            +" is banned, cannot infract";
    
    /**
     * executes a command that is given to it
     * 
     * @param sender what sent the command, either player or console
     * @param command command that was called
     * @param label command alias that was used
     * @param args arguments for the command
     * @return 
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //check for correct number of arguments
        if(args.length <= 1) {
            sender.sendMessage(NOT_ENOUGH_ARGUMENTS);
            return true;
        }
        
        //try to get the player
        Player player = plugin.getServer().getPlayer(args[0]);
        
        //get rid of the o in the alias
        label = label.substring(1);
        
        //if the player doesn't have permissions, do nothing more
        if(!checkForPermissions(sender))
            return true;
        
        //if the player is online, use the online version
        if(player != null) {
            super.onCommand(sender, command, label, args);
            return true;
        }
        
        //if the player is banned, do nothing
        if(checkForBan(sender,args[0]))
            return true;
        
        //check for correct number of arguments
        if(args.length <= 1) {
            sender.sendMessage(NOT_ENOUGH_ARGUMENTS);
            return true;
        }
        
        //get the reason
        String reason = getReason(args,1);
        
        //get a new infraction and notify
        manager.createInfraction(sender.getName(), args[0], getInfractionType(label), reason);
        
        return true;
    }
    
    /**
     * checks permissions against the sender for the player
     * 
     * @param sender what sent the command
     * @param player player that is trying to be infracted
     * @return true if allows, false otherwise
     */
    protected boolean checkForPermissions(CommandSender sender) {
        if(!sender.hasPermission(InfractionPerm.ADMIN.perm)) {
            sender.sendMessage(SENDER_NOT_ADMIN_ERROR);
            return false;
        }
        
        return true;
    }
    
    /**
     * checks the player for a ban
     * 
     * @param sender what is checking for a ban
     * @param name player name to check agaisnt
     * @return true if banned, false otherwise
     */
    protected boolean checkForBan(CommandSender sender, String name) {
        boolean ret = manager.getPlayerData(name).isBanned();
        
        if(ret)
            sender.sendMessage(PLAYER_BANNED_ERROR.replace(InfractionRegex.PLAYER_REGEX,name));
        
        return ret;
    }
}
