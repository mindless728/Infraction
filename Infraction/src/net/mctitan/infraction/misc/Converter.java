package net.mctitan.infraction.misc;

import net.mctitan.infraction.Infraction;
import net.mctitan.infraction.InfractionManager;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Contains code that converts the old infraction file into the new system
 * might not ever be used for me
 * 
 * @author mindless728
 */
public class Converter {
    /*public static void main(String [] args) {
        File file = new File("C:\\Users\\Colin\\Desktop\\Infractions.file");
        HashMap<String,LinkedList<mindless782.infract.Infraction>> old_infractions = null;
        HashMap<String,LinkedList<Infraction>> infractions = null;
        
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            old_infractions = (HashMap<String,LinkedList<mindless782.infract.Infraction>>)ois.readObject();
        } catch(Exception e){System.out.println(e);}
        
        //make sure there are some infractions there
        if(old_infractions == null || old_infractions.isEmpty()) {
            System.out.println("No infractions, nothing to do");
            return;
        }
        
        infractions = convertFromOldFile(old_infractions);
        
        //display the entire list
        for(String name : infractions.keySet()) {
            System.out.println(name+"'s infractions:");
            for(Infraction i : infractions.get(name))
                System.out.println(" ["+i.datetime+"] "+i.issuer+" "+i.type+" for "+i.reason+(i.pardoned?" - pardoned":""));
        }
    }*/
    
    public static HashMap<String,LinkedList<Infraction>> convertFromOldFile(HashMap<String,LinkedList<mindless782.infract.Infraction>> old_infractions, InfractionManager manager) {
        //copy over the infractions
        //InfractionManager manager = InfractionManager.getInstance();
        HashMap<String,LinkedList<Infraction>> infractions = new HashMap<>();
        LinkedList<Infraction> temp;
        Infraction infract;
        
        for(String name : old_infractions.keySet()) {
            temp = new LinkedList<>();
            for(mindless782.infract.Infraction old_infract : old_infractions.get(name)) {
                infract = new Infraction(old_infract.issuer, old_infract.player, old_infract.type,
                                         old_infract.reason, convertOldDateTime(old_infract.datetime));
                infract.pardoned = old_infract.pardoned;
                infract.pardoner = old_infract.pardoner;
                
                manager.addToModerator(infract.issuer, infract);
                if(infract.pardoned && !infract.pardoner.equals(infract.issuer))
                    manager.addToModerator(infract.pardoner, infract);
                
                temp.addLast(infract);
            }
            infractions.put(name,temp);
        }
        
        return infractions;
    }
    
    public static String convertOldDateTime(String datetime) {
        String[] split1 = datetime.split(" ");
        String[] split2 = split1[0].split("-");
        
        String year = split2[2];
        String month = split2[0];
        String day = split2[1];
        String time = split1[1];
        
        return year+"-"+month+"-"+day+" "+time;
    }
    
    /*public static void convertFromOldFile(HashMap<String,LinkedList<mindless782.infract.Infraction>> old_infractions) {
        InfractionManager manager = InfractionManager.getInstance();
        
        for(String name : old_infractions.keySet()) {
            for(mindless782.infract.Infraction infract : old_infractions.get(name)) {
                manager.createInfraction(infract.issuer, infract.player, infract.type, infract.reason);
                if(infract.pardoned)
                    manager.pardonInfraction(infract.pardoner, infract.player, 1);
            }
        }
    }*/
}
