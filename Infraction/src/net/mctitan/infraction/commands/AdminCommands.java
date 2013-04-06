package net.mctitan.infraction.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.mctitan.infraction.Infraction;
import net.mctitan.infraction.InfractionPerm;
import net.mctitan.infraction.InfractionType;
import net.mctitan.infraction.PlayerData;

/**
 * handles the admin commands
 * - /infracts, /check, /pardon, /delete, /deleteall
 * 
 * @author Colin
 */
public class AdminCommands extends OnlineCommands {
    //@TODO fill in error messages
    /** error message when the player tries to check anothers infractions without permission*/
    private static final String PLAYER_INFRACTS_PERM_ERROR =
            "You cannot check another's infractions";
    
    /** error message when a player checks anothers moderations without permission */
    private static final String PLAYER_CHECK_PERM_ERROR =
            "You cannot check another moderator's infractions";
    
    /** error message when a flag cannot be parsed */
    private static final String FLAG_PARSE_ERROR =
            "Flag entered is invalid";
    
    /** error message when a given is nto a number and should be */
    private static final String NOT_A_NUMBER_ERROR =
            "Number entered is not a number";
    
    /** error message when a given number is out of bounds */
    private static final String NUMBER_BOUNDS_ERROR =
            "Number given was out of bounds";
    
    /** error message when someone tries to pardon without permission */
    private static final String PARDON_PERM_ERROR =
            "You cannot pardon another's infraction";
    
    /** error message when trying to pardon a pardon */
    private static final String ALREADY_PARDONED_ERROR =
            "Infraction already pardoned or infraction is a pardon";
    
    /** error message when someone tries to delete without permission */
    private static final String DELETE_PERM_ERROR =
            "You cannot delete another's infraction";
    
    /** flag for fast output */
    private static final String FAST_FLAG = "-f";
    
    /** flag for full output */
    private static final String FULL_FLAG = "-F";
    
    /** default number to use when none is given */
    protected static final String DEFAULT_SNUMBER = "1";
    
    /** when checking infractions */
    private static final Boolean INFRACTION_CHECK = false;
    
    /** when checking moderations */
    private static final Boolean MODERATION_CHECK = true;
    
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
        //infracts command
        if(label.equals("infracts")) {
            if(args.length == 0)
                check(sender, sender.getName(), DEFAULT_SNUMBER, FULL_FLAG, INFRACTION_CHECK);
            else if(args.length == 1) {
                if(isNumber(args[0]))
                    check(sender, sender.getName(), args[0], FULL_FLAG, INFRACTION_CHECK);
                else if(checkFlag(args[0], FAST_FLAG))
                    check(sender, sender.getName(), DEFAULT_SNUMBER, args[0], INFRACTION_CHECK);
                else
                    check(sender, args[0], DEFAULT_SNUMBER, FULL_FLAG, INFRACTION_CHECK);
            } else if(args.length == 2) {
                if(isNumber(args[0]))
                    check(sender, sender.getName(), args[0], args[1], INFRACTION_CHECK);
                if(isNumber(args[1]))
                    check(sender, args[0], args[1], FULL_FLAG, INFRACTION_CHECK);
                else
                    check(sender, args[0], DEFAULT_SNUMBER, args[1], INFRACTION_CHECK);
            } else if(args.length == 3)
                check(sender, args[0], args[1], args[2], INFRACTION_CHECK);
            
        //check command
        } else if(label.equals("check")) {
            if(args.length == 0)
                check(sender, sender.getName(), DEFAULT_SNUMBER, FULL_FLAG, MODERATION_CHECK);
            else if(args.length == 1) {
                if(isNumber(args[0]))
                    check(sender, sender.getName(), args[0], FULL_FLAG, MODERATION_CHECK);
                else if(checkFlag(args[0], FAST_FLAG))
                    check(sender, sender.getName(), DEFAULT_SNUMBER, args[0], MODERATION_CHECK);
                else
                    check(sender, args[0], DEFAULT_SNUMBER, FULL_FLAG, MODERATION_CHECK);
            } else if(args.length == 2) {
                if(isNumber(args[0]))
                    check(sender, sender.getName(), args[0], args[1], MODERATION_CHECK);
                if(isNumber(args[1]))
                    check(sender, args[0], args[1], FULL_FLAG, MODERATION_CHECK);
                else
                    check(sender, args[0], DEFAULT_SNUMBER, args[1], MODERATION_CHECK);
            } else if(args.length == 3)
                check(sender, args[0], args[1], args[2], MODERATION_CHECK);
            
        //pardon command
        } else if(label.equals("pardon")) {
            if(args.length == 1)
                pardon(sender, args[0], DEFAULT_SNUMBER);
            
            else if(args.length == 2)
                pardon(sender, args[0], args[1]);
        //delete command
        } else if(label.equals("delete")) {
            if(args.length == 0)
                delete(sender, sender.getName(), DEFAULT_SNUMBER);
            if(args.length == 1) {
                if(isNumber(args[0]))
                    delete(sender, sender.getName(), args[0]);
                else
                    delete(sender, args[0], DEFAULT_SNUMBER);
            } else if(args.length == 2)
                delete(sender, args[0], args[1]);
            
        //deletall command
        } else if(label.equals("deleteall")) {
            if(args.length == 0)
                deleteall(sender, sender.getName());
            else if(args.length == 1)
                deleteall(sender, args[0]);
        }
        
        return true;
    }
    
    /**
     * called when /check is called
     * @param sender what called the command
     * @param player player caling command against
     * @param spage page number as a string
     * @param fastFlag flag used in call
     * @param moderator whether or no we are checking moderations
     */
    public void check(CommandSender sender, String player, String spage, String fastFlag, boolean moderator) {
        //local variables
        boolean fast;
        int page;
        
        //try to get the player's name based on partial matching
        player = getPlayerName(player);
        
        //check permissions, depends on moderation or infraction check
        if(!checkPerm(sender, player, moderator))
            return;
        
        //check flag
        if(!checkFlag(fastFlag, FAST_FLAG, FULL_FLAG)) {
            sender.sendMessage(FLAG_PARSE_ERROR);
            return;
        }
        fast = fastFlag.equals(FAST_FLAG);
        
        //get page number
        if((page = getNumber(sender,spage))==Integer.MIN_VALUE)
            return;
        
        //send the output to screen
        for(String s : manager.getPlayerData(player).getInfractionOutput(sender.getName(), page, moderator, fast))
            sender.sendMessage(s);
    }
    
    /**
     * called when /pardon is called
     * 
     * @param sender what called the command
     * @param player player to pardon
     * @param sid id as a string to pardon
     */
    public void pardon(CommandSender sender, String player, String sid) {
        //local variables
        int id;
        
        //check permissions
        if(!sender.hasPermission(InfractionPerm.MODERATOR.perm)) {
            sender.sendMessage(PARDON_PERM_ERROR);
            return;
        }
        
        //get player name by partial if match
        player = getPlayerName(player);
        
        //check for same player
        if(sender.getName().equals(player)) {
            sender.sendMessage(PLAYER_SAME_ERROR);
            return;
        }
        
        //get the id number
        if((id = getNumber(sender, sid)) == Integer.MIN_VALUE)
            return;
        --id; //id's are 0 based
        
        //get the player data
        PlayerData data = manager.getPlayerData(player);
        
        //check for id bounds
        if(!idInBounds(sender,id,data))
            return;
        
        //pardon the infraction
        Infraction infract = data.getInfraction(id);
        
        //test for the infraction being a pardon
        if(infract.type.equals(InfractionType.PARDON)) {
            sender.sendMessage(ALREADY_PARDONED_ERROR);
            return;
        }
        
        //create the pardon
        manager.createInfraction(sender.getName(), player, InfractionType.PARDON, infract.reason, infract);
        
        //change reason of original infraction to pardoned
        infract.reason = InfractionType.PARDON.output;
    }
    
    /**
     * called when /delete is called
     * 
     * @param sender what called the command
     * @param player player to delete against
     * @param sid infraction id to delete
     */
    public void delete(CommandSender sender, String player, String sid) {
        //local vairables
        int id;
        
        //check permissions
        if(!sender.hasPermission(InfractionPerm.ADMIN.perm)) {
            sender.sendMessage(DELETE_PERM_ERROR);
            return;
        }
        
        //get player name by partial if match
        player = getPlayerName(player);
        
        //get id number
        if((id = getNumber(sender, sid)) == Integer.MIN_VALUE)
            return;
        --id; //id's are 0 based
        
        //get the player data
        PlayerData data = manager.getPlayerData(player);
        
        //check id bounds
        if(!idInBounds(sender,id,data))
            return;
        
        //get the infraction
        Infraction infract = data.getInfraction(id);
        
        //delete the infraction
        manager.deleteInfraction(infract);
    }
    
    /**
     * called when /deleteall is used
     * 
     * @param sender what sent the command
     * @param player player to delete against
     */
    public void deleteall(CommandSender sender, String player) {
        //get player name by partial if match
        player = getPlayerName(player);
        
        //get the player data
        PlayerData data = manager.getPlayerData(player);
        
        //loop through and call delete() to delete all of the infractions
        while(data.getNumberInfractions() > 0)
            delete(sender, player, DEFAULT_SNUMBER);
    }
    
    /**
     * permissions checkign for /infracts and /check
     * 
     * @param sender what called it
     * @param player player to check
     * @param moderator whether or not it is checkign moderations
     * @return 
     */
    private boolean checkPerm(CommandSender sender, String player, boolean moderator) {
        if(moderator) {
            if(!sender.hasPermission(InfractionPerm.ADMIN.perm)) {
                sender.sendMessage(PLAYER_CHECK_PERM_ERROR);
                return false;
            }
        } else {
            //check for same player name and user permissions or moderator permissions
            if(!(sender.getName().equals(player) && sender.hasPermission(InfractionPerm.USER.perm)) &&
               !sender.hasPermission(InfractionPerm.MODERATOR.perm)) {
                sender.sendMessage(PLAYER_INFRACTS_PERM_ERROR);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * checks a string if it is a number
     * 
     * @param str string to check
     * @return true if it is a number, false otherwise
     */
    private boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
        } catch(Exception e){return false;}
        return true;
    }
    
    /**
     * changes a string into a number
     * @param sender what is trying to convert this
     * @param str string to change over
     * @return the number if successful, Integer.MIN_VALUE otherwise
     */
    private int getNumber(CommandSender sender, String str) {
        if(!isNumber(str)) {
            //send number error
            sender.sendMessage(NOT_A_NUMBER_ERROR);
            return Integer.MIN_VALUE;
        }
        
        int i = 1;
        try {
            i = Integer.parseInt(str);
        } catch(Exception e){}
        return i;
    }
    
    /**
     * gets a player name from a partial match
     * 
     * @param partial the partial name to test against
     * @return the full name if found, partial if not
     */
    private String getPlayerName(String partial) {
        Player player = plugin.getServer().getPlayer(partial);
        if(player != null)
            partial = player.getName();
        return partial;
    }
    
    /**
     * checks a singel flag agaisnt a myriad of possibilities
     * 
     * @param flag flag to check
     * @param flags arracy of flags to check against
     * @return 
     */
    private boolean checkFlag(String flag, String ... flags) {
        for(String f : flags)
            if(flag.equals(f))
                return true;
        return false;
    }
    
    /**
     * checks to see if the id is in bounds of the number of infractions a plyer has
     * 
     * @param sender what called this check
     * @param id id to check for
     * @param data player data to check against
     * @return true if in bounds, false otherwise
     */
    private boolean idInBounds(CommandSender sender, int id, PlayerData data) {
        if(id < 0 || id >= data.getNumberInfractions()) {
            sender.sendMessage(NUMBER_BOUNDS_ERROR);
            return false;
        }
        return true;
    }
}
