package net.mctitan.infraction.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.mctitan.infraction.Infraction;
import net.mctitan.infraction.InfractionManager;
import net.mctitan.infraction.InfractionPerm;
import net.mctitan.infraction.InfractionPlugin;

/**
 * Commands to infract players when they are online
 * used for /warn, /kick, /ban
 * 
 * @author mindless728
 */
public class OnlineCommands implements CommandExecutor {
    /** reference to the plugin */
    InfractionPlugin plugin;
    
    /** reerence to the infraction manager */
    InfractionManager manager;
    
    /** constructor, gets the objects */
    public OnlineCommands() {
        plugin = InfractionPlugin.getInstance();
        manager = InfractionManager.getInstance();
    }
    
    /**
     * parses the command if it is a /warn, /kick, or a /ban
     * 
     * @param sender what sent the command
     * @param command command that was called
     * @param Label //alias used to call command
     * @param args //arguments the command has
     * 
     * @return always true
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String Label, String [] args) {
        String reason;
        
        if(args.length < 2) {
            sender.sendMessage(ChatColor.RED+"Not enough arguments!");
            return true;
        }
        
        Player player = plugin.getServer().getPlayer(args[0]);
        reason = getReasonFromArgs(args,1);
        
        //if the player isn't on, send a notification
        if(player == null) {
            playerNotOnlineNotification(sender, args[0]);
            return false;
        }
        
        //check to see if the moderator is infracting himself
        if(sender.getName().equals(player.getName())) {
            sender.sendMessage(ChatColor.RED+"Cannot infract yourself");
            return true;
        }
        
        //check for permissions
        if(!sender.hasPermission(InfractionPerm.MODERATOR.toString()) || 
           (!sender.hasPermission(InfractionPerm.ADMIN.toString()) &&
             player.hasPermission(InfractionPerm.MODERATOR.toString()))) {
            sender.sendMessage(ChatColor.RED+"You do not have permissions to do that");
            return true;
        }
        
        //do different task for different commands
        switch (Label) {
            case "warn":
                warn(sender, player, reason);
                break;
            case "kick":
                kick(sender, player, reason);
                break;
            case "ban":
                ban(sender, player, reason);
                break;
        }
        
        return true;
    }
    
    /**
     * warns a player by adding an infraction
     * 
     * @param sender what sent the command
     * @param name player name to warn
     * @param reason reason for the warning
     * 
     * @return whether it was succesful or not
     */
    public boolean warn(CommandSender sender, Player player, String reason) {
        //make sure the player is not null
        if(player == null)
            return false;
        
        //create the warning infraction
        Infraction infract = createInfraction(sender, player.getName(), reason, "warned");
        
        //send the receiving player a message about being infracted
        player.sendRawMessage(infract.getOutput(player.getName()));
        
        //success!
        return true;
    }
    
    /**
     * kicks a player by adding an infraction and removing them from the server
     * 
     * @param sender what sent the command
     * @param name player name to kick
     * @param reason reason for the kick
     * 
     * @return whether it was succesful or not
     */
    public boolean kick(CommandSender sender, Player player, String reason) {
        //make sure the player is not null
        if(player == null)
            return false;
        
        //create the kick infraction
        Infraction infract = createInfraction(sender, player.getName(), reason, "kicked");
        
        //kick the player from the server
        player.kickPlayer(infract.getOutput(player.getName()));
        
        //success!
        return true;
    }
    
    /**
     * bans a player by adding an infraction, removes them from the server until pardoned
     * 
     * @param sender what sent the command
     * @param name player name to ban
     * @param reason reason for the ban
     * 
     * @return whether it was successful or not
     */
    public boolean ban(CommandSender sender, Player player, String reason) {
        //make sure the player is not null
        if(player == null)
            return false;
        
        //create the ban infraction
        Infraction infract = createInfraction(sender, player.getName(), reason, "banned");
        
        //kick the player from the server
        player.kickPlayer(infract.getOutput(player.getName()));
        
        //success!
        return true;
    }
    
    /**
     * creates the underlying infraction that is to be added
     * 
     * @param sender what sent the commad
     * @param player player being infracted
     * @param reason reason for the infraction
     * @param type type of infraction
     */
    protected Infraction createInfraction(CommandSender sender, String name, String reason, String type) {
        //create the ifnraction via the manager, store it for now
        Infraction infract = manager.createInfraction(sender.getName(), name, type, reason);
        
        //notify all moderators and admins of the infraction
        notifyOfInfract(infract);
        
        //send the infraction to the calling code
        return infract;
    }
    
    /**
     * notifies the sender if the player isn't on at the time of infraction
     * 
     * @param sender what sent the command
     * @param name player name that was tried
     */
    private void playerNotOnlineNotification(CommandSender sender, String name) {
        String msg = "Player "+name+" is not online";
        if(sender instanceof Player)
            ((Player)sender).sendRawMessage(ChatColor.RED+msg);
        else
            sender.sendMessage(msg);
    }
    
    /**
     * notifies all moderators and admins of the infraction that happened
     * 
     * @param infract infraction that was given out
     */
    private void notifyOfInfract(Infraction infract) {
        //go through an notify all moderators and admins
        for(Player player : plugin.getServer().getOnlinePlayers())
            //make sure the player is a moderator or admin
            if(player.hasPermission(InfractionPerm.MODERATOR.toString()) ||
               player.hasPermission(InfractionPerm.ADMIN.toString()))
                //if the current player equals the infracted player, don't send the message
                //as he/she would already have it
                if(!infract.player.equals(player.getName()))
                    //send the notification
                    player.sendRawMessage(infract.getOutput(player.getName()));
        
        //send notification to the console
        System.out.println(infract.getConsoleOutput());
    }
    
    /**
     * grabs the reason from the argument array, for the infraction
     * 
     * @param args arguments passed to the command
     * @param start starting index to use
     * 
     * @return reason for infraction with spaces between words
     */
    protected String getReasonFromArgs(String [] args, int start) {
        String ret = "";
        
        if(start >= args.length)
            return ret;
        
        int i;
        for(i = start; i < args.length-1; ++i)
            ret += args[i]+" ";
        ret += args[i];
        
        return ret;
    }
}
