package net.mctitan.infraction;

import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
/**
 * Singleton class handles infraction creation and player data
 * 
 * @author Colin
 */
public class InfractionManager {
    /** singleton instance object */
    private static InfractionManager instance;
    
    /** file name that the player data is stored in */
    private static final String fileName = "Infractions";
    
    /** holds all of the player data */
    private HashMap<String,PlayerData> players;
    
    /** initializes all data structures and loads them from disk */
    private InfractionManager() {
        //create the hshmap of names to player data
        players = new HashMap<String,PlayerData>();
        
        //load data from disk
        load();
    }
    
    /**
     * gets the single instance of the manager
     * 
     * @return instance of this singleton
     */
    public static InfractionManager getInstance() {
        if(instance == null)
            instance = new InfractionManager();
        return instance;
    }
    
    /** loads the data from the disk */
    public final void load() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getSaveFile()));
            players = (HashMap<String,PlayerData>)ois.readObject();
            ois.close();
        } catch(Exception e) {
            save();
        }
    }
    
    /** saves the data to the disk */
    public final void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSaveFile()));
            oos.writeObject(players);
            oos.close();
        } catch(Exception e) {}
    }
    
    /**
     * gets the save file and creates the plugin folder if it doesn't exist
     * 
     * @return the save file for the infractions
     */
    private File getSaveFile() {
        File folder = InfractionPlugin.getInstance().getDataFolder();
        if(!folder.exists())
            folder.mkdir();
        
        File file = new File(folder.getPath()+File.separator+fileName);
        
        return file;
    }
    
    /**
     * Gets the player data associated with a player name
     * 
     * @param player name of the player
     * @return data for the player, creates new if one doesn't exist
     */
    public PlayerData getPlayerData(String player) {
        PlayerData data = players.get(player);
        if(data == null)
            data = createNewPlayerData(player);
        
        return data;
    }
    
    /**
     * creates a new player data and stores it
     * 
     * @param player name of the player
     * @return the newly created player data
     */
    private PlayerData createNewPlayerData(String player) {
        PlayerData data = new PlayerData(player);
        
        players.put(data.name, data);
        
        return data;
    }
    
    /**
     * creates an infraction based on player names
     * 
     * @param issuer name of issuing player
     * @param player name of receiving player
     * @param type type of infraction
     * @param reason reason for infraction
     * 
     * @return newly created infraction
     */
    public Infraction createInfraction(String issuer, String player, InfractionType type, String reason) {
        return createInfraction(getPlayerData(issuer),getPlayerData(player),type,reason);
    }
    
    /**
     * creates an infraction based on player names
     * 
     * @param issuer player data of issuing player
     * @param player player data of receiving player
     * @param type type of infraction
     * @param reason reason for infraction
     * 
     * @return newly created infraction
     */
    public Infraction createInfraction(PlayerData issuer, PlayerData player, InfractionType type, String reason) {
        return createInfraction(issuer,player,type,reason,null);
    }
    
    /**
     * creates an infraction given player names and an infraction to link to
     * 
     * @param issuer player name that issued the infraction
     * @param player player name receiving the infraction
     * @param type type of infractions
     * @param reason reason for infraction
     * @param infract infraction to link against
     * 
     * @return newly created infraction
     */
    public Infraction createInfraction(String issuer, String player, InfractionType type, String reason, Infraction infract) {
        return createInfraction(getPlayerData(issuer),getPlayerData(player),type,reason,infract);
    }
    
    /**
     * create an infraction using the player data and an infraction to link to
     * 
     * @param issuer player that issued the infraction
     * @param player player receiving the infraction
     * @param type type of infraction
     * @param reason reason for infraction
     * @param infract infraction to link against
     * 
     * @return newly created infraction
     */
    public Infraction createInfraction(PlayerData issuer, PlayerData player, InfractionType type, String reason, Infraction infract) {
        //create the new infraction
        Infraction i = new Infraction(player,issuer,type,reason,infract);
        
        //save the state of the player data
        save();
        
        //notify moderators of the infraction
        notify(i);
        
        //return the infraction
        return i;
    }
    
    /**
     * deletes an infraction from the player and issuer, if there is linked infraction
     * deletes that as well
     * 
     * @param infract infraction to delete
     */
    public void deleteInfraction(Infraction infract) {
        while(infract != null) {
            infract.player.rmInfraction(infract);
            infract.issuer.rmModeration(infract);
            infract = infract.infract;
            if(infract != null)
                infract.infract = null;
        }
        
        //save the state of the player data
        save();
    }
    
    /**
     * notifies moderators, admins, player being infracted, and the console about infraction
     * 
     * @param infract infraction to notify about
     */
    private void notify(Infraction infract) {
        //loop through the players
        for(Player p : InfractionPlugin.getInstance().getServer().getOnlinePlayers())
            //if the player has permission or is the receiver, send the output
            if(p.hasPermission(InfractionPerm.MODERATOR.perm) ||
               p.hasPermission(InfractionPerm.ADMIN.perm) ||
               (p.hasPermission(InfractionPerm.USER.perm) && p.getName().equals(infract.player.name)))
                p.sendRawMessage(infract.getOnFlyOutput(p.getName()));
        
        //send the output to the console
        //@TODO make this so it isn't color coded
        System.out.println(infract.getOnFlyOutput("").replaceAll("§.", ""));
    }
}
