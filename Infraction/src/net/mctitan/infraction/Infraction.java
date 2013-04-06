package net.mctitan.infraction;

import org.bukkit.entity.Player;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents an infraction given
 * 
 * @author Colin
 */
public class Infraction implements Serializable {
    /** player that received the infraction */
    public PlayerData player;
    
    /** player that issued the infraction */
    public PlayerData issuer;
    
    /** type of infraction */
    public InfractionType type;
    
    /** reason for the infraction */
    public String reason;
    
    /** date of the infraction */
    public String date;
    
    /** time of the infraction */
    public String time;
    
    /** used to link infractions with a pardon if it exists, null otherwise */
    public Infraction infract;
    
    /** how the date gets formatted */
    private static transient final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /** how the time gets formatted */
    private static transient final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    
    /** generic output sent on the fly (also known as fast) */
    private static transient final String ON_FLY_OUTPUT =
            InfractionRegex.COLOR_ISSUER_REGEX+InfractionRegex.ISSUER_REGEX+" "+InfractionRegex.COLOR_TYPE_REGEX+InfractionRegex.TYPE_REGEX
            +" "+InfractionRegex.COLOR_PLAYER_REGEX+InfractionRegex.PLAYER_REGEX+" "+InfractionRegex.COLOR_NORMAL_REGEX+"for "
            +InfractionRegex.COLOR_REASON_REGEX+"\""+InfractionRegex.REASON_REGEX+"\"";
    
    /** generic full output of the infraction */
    private static transient final String FULL_OUTPUT =
            InfractionRegex.COLOR_DATETIME_REGEX+"["+InfractionRegex.DATE_REGEX+" "+InfractionRegex.TIME_REGEX+"] "+InfractionRegex.COLOR_ISSUER_REGEX
            +InfractionRegex.ISSUER_REGEX+ " "+InfractionRegex.COLOR_TYPE_REGEX+InfractionRegex.TYPE_REGEX+" "+InfractionRegex.COLOR_PLAYER_REGEX
            +InfractionRegex.PLAYER_REGEX+" "+InfractionRegex.COLOR_NORMAL_REGEX+"for "+InfractionRegex.COLOR_REASON_REGEX+"\""
            +InfractionRegex.REASON_REGEX+"\"";
    
    /** you, duh! */
    private static transient final String YOU = "You";
    
    /**
     * Constructor uses the minimal amount of information needed to make one
     * 
     * @param player player that is receiving the infraction
     * @param issuer player that issued the infraction
     * @param type type of infraction
     * @param reason reason for the infraction
     */
    public Infraction(PlayerData player,
                      PlayerData issuer,
                      InfractionType type,
                      String reason) {
        this(player,issuer,type,reason,null);
    }
    
    /**
     * Constructor that is only missing the date and time
     * 
     * @param player player that is receiving the infraction
     * @param issuer player that issued the infraction
     * @param type type of infraction
     * @param reason reason for the infraction
     * @param infract if this is a pardon, the linking of the infractions
     */
    public Infraction(PlayerData player,
                      PlayerData issuer,
                      InfractionType type,
                      String reason,
                      Infraction infract) {
        this(player,issuer,type,reason,infract,DATE_FORMAT.format(new Date()),TIME_FORMAT.format(new Date()));
    }
    
    /**
     * Constructor that is only missing the link to another infraction
     * 
     * @param player player that is receiving the infraction
     * @param issuer player that issued the infraction
     * @param type type of infraction
     * @param reason reason for the infractions
     * @param date date of the infraction
     * @param time time of infraction
     */
    public Infraction(PlayerData player,
                      PlayerData issuer,
                      InfractionType type,
                      String reason,
                      String date,
                      String time) {
        this(player,issuer,type,reason,null,date,time);
    }
    
    /**
     * Constructor that has every bit of information needed to make the infraction
     * 
     * @param player player receiving the infraction
     * @param issuer player that issued the infraction
     * @param type type of infraction
     * @param reason reason for the infraction
     * @param infract if this is a pardon, the linking of the infractions
     * @param date date of the infraction
     * @param time time of the infraction
     */
    public Infraction(PlayerData player,
                      PlayerData issuer,
                      InfractionType type,
                      String reason,
                      Infraction infract,
                      String date,
                      String time) {
        //initialize the data in this object
        this.player = player;
        this.issuer = issuer;
        this.type = type;
        this.reason = reason;
        this.infract = infract;
        this.date = date;
        this.time = time;
        
        //add the infractions to the player and issuer
        addInfractions();
        
        //link this to the other infraction
        linkInfractions();
        
        //see if the player should be kicked
        kickPlayer();
    }
    
    /** adds the infraction to the player and issuer */
    private void addInfractions() {
        //add infraction to player infractions
        player.addPlayerInfraction(this);
        
        //add infraction to issueer moderations
        issuer.addModeratorInfraction(this);
    }
    
    /** links the original infraction if this is a pardon, nothing otherwise */
    private void linkInfractions() {
        if(infract == null)
            return;
        
        infract.infract = this;
    }
    
    /** if the infraction is a kick or ban, try to kick the player */
    private void kickPlayer() {
        if(type.equals(InfractionType.KICK) || type.equals(InfractionType.BAN)) {
            Player p = InfractionPlugin.getInstance().getServer().getPlayer(player.name);
            if(p != null)
                p.kickPlayer(getOnFlyOutput(p.getName()));
        }
    }
    
    /**
     * calculates the on the fly aka fast output.  This output is used to tell
     * the issuer, player, and other moderators about what happened.
     * 
     * @param name player name that is receiving the notification
     * @return the output that the player will receive
     */
    public String getOnFlyOutput(String name) {
        //get outputed player and issuer name
        String pname = (name.equals(player.name)?YOU:player.name);
        String iname = (name.equals(issuer.name)?YOU:issuer.name);
        
        String msg = ON_FLY_OUTPUT;
        //replace all of the data in the string
        msg = msg.replace(InfractionRegex.ISSUER_REGEX,iname);
        msg = msg.replace(InfractionRegex.TYPE_REGEX,type.output);
        msg = msg.replace(InfractionRegex.PLAYER_REGEX,pname);
        msg = msg.replace(InfractionRegex.REASON_REGEX,reason);
        
        //replace all color in output
        msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COLOR_ISSUER);
        msg = InfractionChatColor.replaceColor(msg, type.color);
        msg = InfractionChatColor.replaceColor(msg, player.getColor());
        msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COlOR_NORMAL);
        msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COLOR_REASON);
        
        return msg;
    }
    
    /**
     * calculates the on the full output.  This output is used to tell
     * the issuer, player, and other moderators about what happened.
     * 
     * @param name player name that is receiving the notification
     * @return the output that the player will receive
     */
    public String getFullOutput(String name) {
        //get outputed player and issuer name
        String pname = (name.equals(player.name)?YOU:player.name);
        String iname = (name.equals(issuer.name)?YOU:issuer.name);
        
        String msg = FULL_OUTPUT;
        
        //replace all data in the string
        msg = msg.replace(InfractionRegex.DATE_REGEX,date);
        msg = msg.replace(InfractionRegex.TIME_REGEX,time);
        msg = msg.replace(InfractionRegex.ISSUER_REGEX,iname);
        msg = msg.replace(InfractionRegex.TYPE_REGEX,type.output);
        msg = msg.replace(InfractionRegex.PLAYER_REGEX,pname);
        msg = msg.replace(InfractionRegex.REASON_REGEX,reason);
        
        //replace all color codes in string
        msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COLOR_DATETIME);
        msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COLOR_ISSUER);
        msg = InfractionChatColor.replaceColor(msg, type.color);
        msg = InfractionChatColor.replaceColor(msg, player.getColor());
        msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COlOR_NORMAL);
        msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COLOR_REASON);
        
        return msg;
    }
    
    /**
     * Used to test this class without a server running
     * @param args not used
     */
    public static void main(String [] args) {
        PlayerData player = new PlayerData("HerbieVersmells");
        PlayerData issuer = new PlayerData("mindless728");
        Infraction infract = new Infraction(player, issuer, InfractionType.WARN, "being a dick");
        
        System.out.println(infract.getOnFlyOutput(player.name));
        System.out.println(infract.getFullOutput(issuer.name));
    }
}
