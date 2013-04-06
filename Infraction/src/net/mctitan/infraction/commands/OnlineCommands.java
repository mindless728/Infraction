package net.mctitan.infraction.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.mctitan.infraction.InfractionChatColor;
import net.mctitan.infraction.InfractionManager;
import net.mctitan.infraction.InfractionPerm;
import net.mctitan.infraction.InfractionPlugin;
import net.mctitan.infraction.InfractionRegex;
import net.mctitan.infraction.InfractionType;

/**
 * handles the online commands
 * - /warn, /kick, /ban
 * 
 * @author Colin
 */
public class OnlineCommands implements CommandExecutor {
    /** instance to the near singleton plugin */
    protected InfractionPlugin plugin;
    
    /** manager of the player data */
    protected InfractionManager manager;
    
    /** error message when the player is not online */
    protected static final String PLAYER_NOT_ONLINE_ERROR =
            InfractionRegex.COLOR_PLAYER_REGEX+InfractionRegex.PLAYER_REGEX
            +" is not online, cannot infract";
    
    /** error message when player doesn't have basic moderator permissions */
    protected static final String SENDER_INFRACT_ANY_ERROR =
            InfractionChatColor.COLOR_BAD_PLAYER.getColor()+
            "You do not have permissions to infract anyone";
    
    /** eror message when player has moderator but sender does not have admin */
    protected static final String SENDER_INFRACT_MOD_ERROR =
            InfractionChatColor.COLOR_BAD_PLAYER.getColor()+
            "You do not have permissions to infract moderators";
    
    /** error message sent when trying to infract an admin */
    protected static final String SENDER_INFRACT_ADMIN_ERROR =
            InfractionChatColor.COLOR_BAD_PLAYER.getColor()+
            "You do not have permissions to infract administrators";
    
    /** error message sent when sender and player have same name */
    protected static final String PLAYER_SAME_ERROR =
            InfractionChatColor.COLOR_BAD_PLAYER.getColor()+"You cannot infract yourself";
    
    /** error message sent when there are not enough arguments to a command */
    protected static final String NOT_ENOUGH_ARGUMENTS =
            InfractionChatColor.COLOR_BAD_PLAYER.getColor()+"Not enough arguments given, try again";
    
    /** Default constructor taking no parameters */
    public OnlineCommands() {
        plugin = InfractionPlugin.getInstance();
        manager = InfractionManager.getInstance();
    }
    
    /**
     * executes a command that is given to it
     * 
     * @param sender what sent the command, either player or console
     * @param command command that was called
     * @param label command alias that was used
     * @param args arguments for the command
     * @return always true
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        InfractionType type;
        String reason;
        Player player;
        
        //check for correct number of arguments
        if(args.length <= 1) {
            sender.sendMessage(NOT_ENOUGH_ARGUMENTS);
            return true;
        }
        
        //check for player not online
        if((player = getPlayer(sender,args[0])) == null)
            return true;
        
        //check for permissions
        if(!checkForPermissions(sender, player))
            return true;
        
        //check for player the same
        if(sender.getName().equals(player.getName())) {
            sender.sendMessage(PLAYER_SAME_ERROR);
            return true;
        }
        
        //get the infraction type
        type = getInfractionType(label);
        
        //get the infraction reason
        reason = getReason(args,1);
        
        //create the infraction and notify
        manager.createInfraction(sender.getName(), player.getName(), type, reason);
        
        return true;
    }
    
    /**
     * Gets the player by name if online, or gives an error and returns null
     * 
     * @param sender what sent the command
     * @param name name to look for
     * @return player if found, null otherwise
     */
    protected Player getPlayer(CommandSender sender, String name) {
        Player player = plugin.getServer().getPlayer(name);
        String error;
        
        if(player == null) {
            error = PLAYER_NOT_ONLINE_ERROR;
            error = error.replace(InfractionRegex.PLAYER_REGEX,name);
            error = InfractionChatColor.replaceColor(error,InfractionChatColor.COLOR_BAD_PLAYER);
            
            sender.sendMessage(error);
            return null;
        }
        
        return player;
    }
    
    /**
     * checks permissions against the sender for the player
     * 
     * @param sender what sent the command
     * @param player player that is trying to be infracted
     * @return true if allows, false otherwise
     */
    protected boolean checkForPermissions(CommandSender sender, Player player) {
        //check if sender has basic moderators permissions
        if(!sender.hasPermission(InfractionPerm.MODERATOR.perm) &&
           !sender.hasPermission(InfractionPerm.ADMIN.perm)) {
            sender.sendMessage(SENDER_INFRACT_ANY_ERROR);
            return false;
        }
        
        //if the player is a moderator, make sure the sender is an admin
        if((player.hasPermission(InfractionPerm.MODERATOR.perm) &&
            !sender.hasPermission(InfractionPerm.ADMIN.perm))) {
            sender.sendMessage(SENDER_INFRACT_MOD_ERROR);
            return false;
        }
        
        //no infracting admins
        if(player.hasPermission(InfractionPerm.ADMIN.perm)) {
            sender.sendMessage(SENDER_INFRACT_ADMIN_ERROR);
            return false;
        }
        
        return true;
    }
    
    /**
     * gets the infraction type from the alias of the command used
     * 
     * @param label alias of the command used
     * @return infraction type
     */
    protected InfractionType getInfractionType(String label) {
        InfractionType type;
        /*switch (label) {
            case "kick":
                type = InfractionType.KICK;
                break;
            case "ban":
                type = InfractionType.BAN;
                break;
            default:
                type = InfractionType.WARN;
                break;
        }*/
        if(label.equals("kick"))
            type = InfractionType.KICK;
        else if(label.equals("ban"))
            type = InfractionType.BAN;
        else
            type = InfractionType.WARN;
        
        return type;
    }
    
    /**
     * gets the reason for an infraction fromthe argument list
     * 
     * @param args arguments given to command
     * @return reason for the infraction
     */
    protected String getReason(String[] args, int start) {
        String reason = "";
        int i;
        for(i = start; i < args.length-1; ++i)
            reason += args[i]+" ";
        reason += args[i];
        
        return reason;
    }
}
