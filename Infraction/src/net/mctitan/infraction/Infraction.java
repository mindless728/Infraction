package net.mctitan.infraction;

import org.bukkit.ChatColor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a single infraction on a player
 * 
 * @author mindless728
 */
public class Infraction implements Serializable, Comparable {
    /** the player name who issued the command */
    public String issuer;
    
    /** the player that was infracted */
    public String player;
    
    /** the type of infraction */
    public String type;
    
    /** the reason for the infractions */
    public String reason;
    
    /** the date and time of the infraction */
    public String datetime;
    
    /** the moderator+ that pardoned the player */
    public String pardoner;
    
    /** if the infraction is pardoned */
    public boolean pardoned;
    
    /** the formatter for the date and time */
    private static transient final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /** chat color for normal test */
    private static transient final ChatColor NORMAL = ChatColor.WHITE;
    
    /** chat color for the date and time test */
    private static transient final ChatColor DATETIME = ChatColor.LIGHT_PURPLE;
    
    /** chat color for the issuer text */
    private static transient final ChatColor ISSUER = ChatColor.GREEN;
    
    /** chat color for the type text */
    private static transient final ChatColor TYPE = ChatColor.GOLD;
    
    /** chat color for the player text*/
    private static transient final ChatColor PLAYER = ChatColor.RED;
    
    /** chat color for the reason text */
    private static transient final ChatColor REASON = ChatColor.DARK_GRAY;
    
    /** chat color for the pardon text */
    private static transient final ChatColor PARDON = ChatColor.AQUA;
    
    /**
     * Constructor that takes in the issuer, player, type, and reason
     * 
     * @param i issuer's player name
     * @param p player's player name
     * @param t type of infraction
     * @param r reason for the infraction
     */
    public Infraction(String i, String p, String t, String r) {
        this(i,p,t,r,format.format(new Date()));
    }
    
    /**
     * Constructor that takes in all information for creating an infraction
     * 
     * @param i issuer's player name
     * @param p player's player name
     * @param t type of infraction
     * @param r reason for infraction
     * @param dt date and time of the infraction
     */
    public Infraction(String i, String p, String t, String r, String dt) {
        issuer = i;
        player = p;
        type = t;
        reason = r;
        datetime = dt;
    }
    
    /**
     * gets the output for the infraction for a given player, text changes depending
     * on player name this output goed to
     * 
     * @param name the reveicing player's name
     * @return the msg to be sent
     */
    public String getOutput(String name) {
        String itest = (pardoned?pardoner:issuer); //use pardoner or issuer
        String i = (name.equals(itest)?"You":issuer); //use itest or "You"
        String p = (name.equals(player)?"You":player); //use player name or "You"
        String t = (pardoned?"pardoned":type); //use the type or pardoned
        ChatColor CT = (pardoned?PARDON:TYPE); //yse type or pardon for coloring
        
        String ret = ISSUER+i+" "+CT+t+" "+PLAYER+p+NORMAL+" for "+REASON+reason;
        
        return ret;
    }
    
    /**
     * gets the output for the console, has no colored text
     * 
     * @return the msg for the console
     */
    public String getConsoleOutput() {
        String itest = (pardoned?pardoner:issuer);
        String t = (pardoned?"pardoned":type);
        
        String ret = itest+" "+t+" "+player+" for "+reason;
        
        return ret;
    }
    
    /**
     * the information needed toshow the infraction when checking it
     * 
     * @return the infractions text formatted for the minecraft screen
     */
    @Override
    public String toString() {
        String ret = NORMAL+"["+DATETIME+datetime+NORMAL+"] "+ISSUER+issuer+" "+
                     TYPE+type+" "+PLAYER+player+NORMAL+" for "+REASON+"\""+reason+"\"";
        if(pardoned)
            ret += PARDON+" - Pardoned By: "+pardoner;
        
        return ret;
    }
    
    @Override
    public int compareTo(Object o) {
        if(o == null || !(o instanceof Infraction))
            return 0;
        Infraction infract = (Infraction)o;
        return infract.datetime.compareTo(this.datetime);
    }
}
