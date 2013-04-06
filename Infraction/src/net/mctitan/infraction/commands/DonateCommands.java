package net.mctitan.infraction.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.mctitan.infraction.InfractionRegex;
import net.mctitan.infraction.PlayerData;

public class DonateCommands extends AdminCommands {
    private static final String PLAYER_COMMAND_USE =
            "Player's cannot use this command";
    
    private static final String WRONG_NUMBER_ARGUMENTS =
            "Wrong number of arguments given";
    
    private static final String COMMAND_ALREADY_USED =
            "Command already used for "+InfractionRegex.PLAYER_REGEX;
    
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
        //check if command sender is a player
        if(sender instanceof Player) {
            //players cannot use this command
            sender.sendMessage(PLAYER_COMMAND_USE);
            
            return true;
        }
        
        //check number of arguments, there should only be one
        if(args.length != 1) {
            //send error message
            sender.sendMessage(WRONG_NUMBER_ARGUMENTS);
            
            return true;
        }
        
        //get the player data
        PlayerData data = manager.getPlayerData(args[0]);
        
        //check the data against the command being used
        if(label.equals("donate_pardon") || label.equals("donate_delete")) {
            //check for command already used
            if(data.getDonations(label) >= 1) {
                //send error message and return
                sender.sendMessage(COMMAND_ALREADY_USED.replace(InfractionRegex.PLAYER_REGEX,args[0]));
                
                return true;
            }
            
            //add one to the command used
            data.addDonation(label);
        }
        
        //do the appropriate command from AdminCommands
        if(label.equals("donate_pardon"))
            pardon(sender, args[0], DEFAULT_SNUMBER);
        else if(label.equals("donate_delete"))
            delete(sender, args[0], DEFAULT_SNUMBER);
        else
            deleteall(sender,args[0]);
        
        return true;
    }
}
