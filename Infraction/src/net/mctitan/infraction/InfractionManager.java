package net.mctitan.infraction;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.mctitan.infraction.misc.Converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Holds the infraction data for the plugin and also manages
 * the creation of infractions onto players
 * 
 * @author mindless728
 */
public class InfractionManager {
    /** the number of infractions to show per page */
    private static final int INFRACTIONS_PER_PAGE = 5;
    
    /** class is a singleton, this holds the only instance */
    private static InfractionManager instance;
    
    /** Infraction plugin, used for folder save details */
    InfractionPlugin plugin;
    
    /** mapping from a player name to a list of their infractions */
    HashMap<String, LinkedList<Infraction>> infractions;
    
    /** mapping from a moderator name to the infractions he has given */
    HashMap<String, LinkedList<Infraction>> moderators;
    
    /** set of banned players */
    HashSet<String> bans;
    
    /** default constructor makes the objects and loads from file */
    private InfractionManager() {
        plugin = InfractionPlugin.getInstance();
        infractions = new HashMap<>();
        moderators = new HashMap<>();
        bans = new HashSet<>();
        
        load();
    }
    
    /** loads teh data from the file */
    public void load() {
        File file;
        
        //try to load old save file from previous version
        file = getOldSaveFile();
        if(file.exists()) try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            HashMap<String,LinkedList<mindless782.infract.Infraction>> old_infracts;
            old_infracts = (HashMap<String,LinkedList<mindless782.infract.Infraction>>)ois.readObject();
            infractions.putAll(Converter.convertFromOldFile(old_infracts, this));
            //Converter.convertFromOldFile(old_infracts);
            sort();
            ois.close();
            file.delete();
            save();
        } catch(Exception e) {}
        
        else {
            //load in from new file
            file = getSaveFile();
            
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                //read teh infractions
                infractions = (HashMap<String,LinkedList<Infraction>>)ois.readObject();
                //read the bans
                bans = (HashSet<String>)ois.readObject();
                //read the moderators
                moderators = (HashMap<String,LinkedList<Infraction>>)ois.readObject();
                ois.close();
            } catch(Exception e) {}
        }
    }
    
    /** saves the data to the file */
    public void save() {
        File file = getSaveFile();
        
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            //save the infractions
            oos.writeObject(infractions);
            //save the ban list
            oos.writeObject(bans);
            //save the moderator list
            oos.writeObject(moderators);
            oos.close();
        } catch(Exception e) {}
    }
    
    /** sorts all of the lists */
    private void sort() {
        for(String s : infractions.keySet())
            Collections.sort(infractions.get(s));
        for(String s : moderators.keySet())
            Collections.sort(moderators.get(s));
    }
    
    /**
     * gets the file handle adssociated with the file to open
     * 
     * @return 
     */
    private File getSaveFile() {
        if(!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();
        return new File(plugin.getDataFolder().getPath()+File.separator+"Infractions");
    }
    
    private File getOldSaveFile() {
        return new File(plugin.getDataFolder().getPath()+File.separator+"Infractions.file");
    }
    
    /**
     * gets the instance from the singleton class
     * 
     * @return single instance of this class
     */
    public static synchronized InfractionManager getInstance() {
        if(instance == null) {
            instance = new InfractionManager();
        }
        return instance;
    }
    
    /**
     * creates and infraction for a player
     * 
     * @param issuer player name that issued the infraction
     * @param player player name of the player receiving the ifnraction
     * @param type type of infraction
     * @param reason reason for the infraction
     * 
     * @return infraction that is created for other code to use
     */
    public Infraction createInfraction(String issuer, String player, String type, String reason) {
        //get the list of infractions the player has
        LinkedList<Infraction> infracts = infractions.get(player);
        
        //if no such list exists, make one
        if(infracts == null)
            infracts = new LinkedList<>();
        
        //make a new infraction
        Infraction temp = new Infraction(issuer, player, type, reason);
        
        //add the infraction to the list
        infracts.addFirst(temp);
        
        //put the list back into the data store
        infractions.put(player,infracts);
        
        //add to moderators list
        addToModerator(issuer,temp);
        
        //if the infraction is a ban, add it to the ban list
        if(type.equals("banned"))
            bans.add(player);
        
        //save the changes
        save();
        
        //give the infraction back to the calling code
        return temp;
    }
    
    /**
     * pardons a players infraction
     * 
     * @param issuer player name that issued the pardon
     * @param player player name of the player receiving the pardon
     * @param id id number to pardon
     * 
     * @return infraction pardoned for calling code to use
     */
    public Infraction pardonInfraction(String issuer, String player, int id) {
        //get the list of infractions for the player
        LinkedList<Infraction> infracts = infractions.get(player);
        
        //if no list exists, do nothing
        if(infractions == null)
            return null;
        
        //make the id conform to 0 start counting code
        --id;
        
        //get the infraction
        Infraction temp = infracts.get(id);
        
        //if teh infraction doesn't exist, do nothing
        if(temp == null)
            return null;
        
        //if the infraction is already pardoned, do nothing
        if(temp.pardoned)
            return null;
        
        //set the infraction to be pardoned and who issued the pardon
        temp.pardoned = true;
        temp.pardoner = issuer;
        
        //add to moderators list if the issuer and pardoner are different
        if(!issuer.equals(temp.issuer))
            addToModerator(issuer,temp);
        
        //if the infraction is a ban, remove it to the ban list
        if(temp.type.equals("banned"))
            bans.remove(player);
        
        //save the changes
        save();
        
        //return the infraction to the calling code
        return temp;
    }
    
    /**
     * adds the infraction to the moderator data for checking them
     * 
     * @param name player name of moderator
     * @param infract infraction given to player
     */
    public void addToModerator(String name, Infraction infract) {
        //get list of moderator infractions given
        LinkedList<Infraction> infracts = moderators.get(name);
        
        //if the list doesn't exist, make it
        if(infracts == null)
            infracts = new LinkedList<>();
        
        //add the infract to their list
        infracts.addFirst(infract);
        
        //push it back to moderators
        moderators.put(name,infracts);
        
        //DO NOT SAVE, GETS DONE ELSEWHERE
    }
    
    /**
     * removes the issuer and pardoner(if one) from the infraction
     * 
     * @param infract infraction to remove against
     */
    private void removeFromModerator(Infraction infract) {
        LinkedList<Infraction> infracts;
        
        //get the issuer's list
        infracts = moderators.get(infract.issuer);
        
        //remove the infractions from the list if it exists
        if(infracts != null)
            infracts.remove(infract);
        
        //get the pardoner's list if there is a pardon
        if(!infract.pardoned)
            return;//no pardon, done
        infracts = moderators.get(infract.pardoner);
        
        //remove the infraction
        infracts.remove(infract);
    }
    
    /**
     * deletes an infraction from a player's record
     * 
     * @param issuer player name that issued the deletion
     * @param player player name having the infraction deleted
     * @param id id number to delete
     * 
     * @return infraction for calling code to use
     */
    public Infraction deleteInfraction(String issuer, String player, int id) {
        //get the list of infractions the player has
        LinkedList<Infraction> infracts = infractions.get(player);
        
        //if no such list exists, do nothing
        if(infractions == null)
            return null;
        
        //delete the infraction and get it
        Infraction temp = infracts.remove(id-1);
        
        //if the infraction does not exist, return nothing
        if(temp == null)
            return null;
        
        //remove the infraction from issuer and pardoner
        removeFromModerator(temp);
        
        //if the infraction is a ban, remove it to the ban list
        if(temp.type.equals("banned"))
            bans.remove(player);
        
        //check to see if there are no infractions left, if so delete the list
        if(infracts.size() == 0)
            infractions.remove(player);
        
        //save the changes
        save();
        
        //return the infraction to the calling code
        return temp;
    }
    
    /**
     * deletes all ifnractions a player has
     * 
     * @param issuer player name issuing the deletion
     * @param player player name getting the deletion
     * 
     * @return all of the infractions deleted
     */
    public LinkedList<Infraction> deleteAllInfractions(String issuer, String player) {
        //remove the player from the bane list
        bans.remove(player);
        
        //delete the infractions
        LinkedList<Infraction> infracts = infractions.remove(player);
        
        //go through each infract, and remove it from the issuer and pardoner
        for(Infraction temp : infracts)
            removeFromModerator(temp);
        
        //save the changes
        save();
        
        //get and delete the infractions the player has for calling code
        return infracts;
    }
    
    /**
     * get the output to give to a player about the infraction
     * @param player player name getting the output
     * @param page page number to use
     * @param moderator whether the person is a moderator or not
     * 
     * @return list of strings, each one represents an output line
     */
    public LinkedList<String> getInfractionOutput(String player, int page, boolean moderator) {
        LinkedList<String> ret = new LinkedList<>(); //the output lines to return
        LinkedList<Infraction> infracts = null;
        infracts = (moderator?moderators:infractions).get(player); //infraction a player has
        
        //if the player has no infractions, say so
        if(infracts == null || infracts.isEmpty()) {
            if(moderator)
                ret.add(ChatColor.GOLD+player+ChatColor.GREEN+" has given no infractions");
            else
                ret.add(ChatColor.AQUA+player+ChatColor.GREEN+" has no infractions :)");
            return ret;
        }
        
        //calculate the number of pages
        int pages = (infracts.size()+4)/INFRACTIONS_PER_PAGE;
        
        //do boundray testing for the page number
        if(page > pages)
            page = pages;
        else if(page <= 0)
            page = 1;
        
        //grab an iterator to look through the infractions
        Iterator<Infraction> iter = infracts.listIterator(INFRACTIONS_PER_PAGE*(page-1));
        Infraction temp = null;
        
        //add the top line of output including player, page number, and number of pages
        String header = (moderator?ChatColor.AQUA:ChatColor.RED)+player+"'s"+ChatColor.GREEN;
        if(moderator)
            header += " Given";
        header += " Infractions (Page "+ChatColor.AQUA+page+ChatColor.GREEN+"/"+
                  ChatColor.AQUA+pages+ChatColor.GREEN+")";
        ret.add(header);
        
        
        //output the number of infractions per page
        for(int i = 0; i < INFRACTIONS_PER_PAGE; ++i) {
            if(!iter.hasNext())
                break;
            temp = iter.next();
            
            //add a number plus the infraction string to the output list
            ret.add(""+ChatColor.AQUA+(INFRACTIONS_PER_PAGE*(page-1)+i+1)+") "+temp.toString());
        }
        
        
        //send the output list to the calling code
        return ret;
    }
    
    public LinkedList<String> getInfractionOutput(String player, int page) {
        return getInfractionOutput(player,page,false);
    }
    
    /**
     * returns whether someone is banned or not
     * 
     * @param name player to check against
     * @return whether or not that person is banned
     */
    public boolean isBanned(String name) {
        boolean ret = bans.contains(name);
        if(!ret) {
            LinkedList<Infraction> infracts = infractions.get(name);
            if(infracts == null)
                return ret;
            
            Infraction infract = infracts.getFirst();
            if(infract.type.equals("banned") && !infract.pardoned) {
                ret = true;
                bans.add(name);
            }
        }
        
        return ret;
    }
    
    /**
     * gets the number of infractions that a player has
     * @param name player name to check against
     * @return number of infractions associated with that player name
     */
    public int numberInfractions(String name) {
        LinkedList<Infraction> infracts = infractions.get(name);
        if(infracts == null)
            return 0;
        return infracts.size();
    }
    
    /**
     * Gets a specified infraction from a player
     * 
     * @param name player name to get
     * @param id id number of the infraction
     * @return 
     */
    public Infraction getInfraction(String name, int id) {
        LinkedList<Infraction> infracts = infractions.get(name);
        if(infracts == null)
            return null;
        return infracts.get(id);
    }
}
