package mindless782.infract;

import org.bukkit.ChatColor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Infraction implements Serializable {
    public String issuer;
    public String player;
    public String type;
    public String reason;
    public String datetime;
    public String pardoner;
    public boolean pardoned;
    
    private static final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
    
    public Infraction(String i, String p, String t, String r) {
        this(i,p,t,r,format.format(new Date()));
    }
    
    public Infraction(String i, String p, String t, String r, String dt) {
        issuer = i;
        player = p;
        type = t;
        reason = r;
        datetime = dt;
    }
    
    @Override
    public String toString() {
        String ret = ChatColor.WHITE+"["+ChatColor.LIGHT_PURPLE+datetime+ChatColor.WHITE+"] "+
                ChatColor.GREEN+issuer+" "+ChatColor.GOLD+type+" "+ChatColor.RED+player+
                ChatColor.WHITE+" with reason: "+ChatColor.GRAY+"\""+reason+"\"";
        if(pardoned)
            ret += ChatColor.AQUA+" - Pardoned By: "+pardoner;
        return ret;
    }
    
    public String getFileOutput() {
        String ret = issuer+" "+type+" "+reason+" !end "+datetime+" "+pardoned;
        if(pardoned)
            ret += " "+pardoner;
        return ret;
    }
}
