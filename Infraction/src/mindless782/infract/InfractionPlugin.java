package mindless782.infract;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;

public class InfractionPlugin extends JavaPlugin implements Runnable {
    HashMap<String, LinkedList<Infraction>> infractions;
    HashSet<String> banned;
    String modPermission = "infraction.mod";
    String adminPermission = "infraction.admin";
    InfractionPlayerListener playerListener;
    
    @Override
    public void onEnable() {
        infractions = new HashMap<String, LinkedList<Infraction>>();
        banned = new HashSet<String>();
        playerListener = new InfractionPlayerListener(this);
        
        
        load();
        
        getServer().getPluginManager().registerEvent(Type.PLAYER_LOGIN, playerListener, Priority.Low, this);
        getServer().getPluginManager().registerEvent(Type.PLAYER_CHAT, playerListener, Priority.Low, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, playerListener, 1, 20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 1, 1200);
        
        System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" enabled");
    }
    
    @Override
    public void onDisable() {
        save();
        System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" disabled");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName();
        if(!(sender instanceof Player)) {
            sender.sendMessage("Need to be a plyer to use the command");
            return true;
        }
        
        Player player = (Player)sender;
        if(args.length == 0) {
            help(player);
            return true;
        }
        
        if(!player.hasPermission(modPermission) && !player.hasPermission(adminPermission)) {
            player.sendRawMessage(ChatColor.RED+"You don't have permission for that command");
            return true;
        }
        
        if(cmd.equalsIgnoreCase("infracts") && (args.length == 1 || args.length == 2)) {
            check(player,args[0],(args.length==2?args[1]:"1"));
        } else if(cmd.equalsIgnoreCase("warn") && (args.length > 1)) {
            createInfraction(player,args[0],"warned",args);
        } else if(cmd.equalsIgnoreCase("kick") && (args.length > 1)) {
            createInfraction(player,args[0],"kicked",args);
        } else if(cmd.equalsIgnoreCase("ban") && (args.length > 1)) {
            createInfraction(player,args[0],"banned",args);
        } else if(cmd.equalsIgnoreCase("pardon") && (args.length == 2)) {
            removeInfraction(player,args[0],args[1]);
        } else if(cmd.equalsIgnoreCase("owarn") && (args.length > 1)) {
            createOfflineInfraction(player,args[0],"warned",args);
        } else if(cmd.equalsIgnoreCase("okick") && (args.length > 1)) {
            createOfflineInfraction(player,args[0],"kicked",args);
        } else if(cmd.equalsIgnoreCase("oban") && (args.length > 1)) {
            createOfflineInfraction(player,args[0],"banned",args);
        } else
            help(player);
        
        return true;
    }
    
    public void check(Player sender, String player, String spage) {
        Player ptemp = getServer().getPlayer(player);
        if(ptemp != null)
            player = ptemp.getName();
        
        int page = 0;
        try {
            page = Integer.parseInt(spage)-1;
        } catch(Exception e) {
            sender.sendRawMessage(ChatColor.RED+"Page number given was not a number");
            return;
        }
        
        if(!infractions.containsKey(player)) {
            sender.sendRawMessage(ChatColor.AQUA+player+ChatColor.GREEN+" has no infractions :)");
            return;
        }
        
        LinkedList<Infraction> list = infractions.get(player);
        int pages = list.size()/5+(list.size()%5==0?0:1);
        if(page < 0)
            page = 0;
        else if(page >= pages)
            page = pages-1;
        
        ListIterator<Infraction> iter = list.listIterator(5*page);
        Infraction temp = null;
        sender.sendRawMessage(ChatColor.RED+player+"'s"+ChatColor.GREEN+
                              " Infractions (Page "+ChatColor.AQUA+(page+1)+
                              ChatColor.GREEN+"/"+ChatColor.AQUA+pages+
                              ChatColor.GREEN+")");
        for(int i = 0; i < 5; ++i) {
            temp = iter.next();
            sender.sendMessage(ChatColor.AQUA+""+(5*page+i+1)+") "+temp.toString());
            if(!iter.hasNext())
                break;
        }
    }
    
    public void createInfraction(Player sender, String splayer, String type, String[] args) {
        String reason = "";
        int i;
        for(i = 1; i < args.length-1; ++i)
            reason += args[i]+" ";
        reason += args[i];
        
        Player player = getServer().getPlayer(splayer);
        
        if(player == null) {
            sender.sendMessage(ChatColor.RED+"Player: "+splayer+" is not online, cannot infract");
            return;
        }
        
        if(sender.getName().equals(player.getName())) {
            sender.sendRawMessage(ChatColor.RED+"You cannot infract yourself!");
            return;
        }
        
        if(player.hasPermission(modPermission) && !sender.hasPermission(adminPermission)) {
            sender.sendRawMessage(ChatColor.RED+"You don't have permission to infract other Moderators");
            return;
        }
        
        LinkedList<Infraction> list = infractions.get(player.getName());
        if(list == null)
            list = new LinkedList<Infraction>();
        list.addFirst(new Infraction(sender.getName(), player.getName(), type, reason));
        infractions.put(player.getName(),list);
        
        if(type.equals("warned")) {
            player.sendRawMessage(ChatColor.GREEN+sender.getName()+ChatColor.GOLD+
                                  " warned you for \""+ChatColor.GRAY+reason+
                                  ChatColor.WHITE+"\"");
            sender.sendRawMessage(ChatColor.GREEN+"You"+ChatColor.GOLD+
                                  " warned "+ChatColor.RED+player.getName()+
                                  ChatColor.GOLD+" for \""+ChatColor.GRAY+
                                  reason+ChatColor.WHITE+"\"");
            alertMods(list.getFirst().toString(),sender);
        } else if(type.equals("kicked")) {
            player.kickPlayer(sender.getName()+" kicked you for \""+reason+"\"");
            sender.sendRawMessage(ChatColor.GREEN+"You"+ChatColor.GOLD+
                                  " kicked "+ChatColor.RED+player.getName()+
                                  ChatColor.GOLD+" for \""+ChatColor.GRAY+
                                  reason+ChatColor.WHITE+"\"");
            alertMods(list.getFirst().toString(),sender);
        } else if(type.equals("banned")) {
            player.kickPlayer(sender.getName()+" banned you for \""+reason+"\"");
            sender.sendRawMessage(ChatColor.GREEN+"You"+ChatColor.GOLD+
                                  " banned "+ChatColor.RED+player.getName()+
                                  ChatColor.GOLD+" for \""+ChatColor.GRAY+
                                  reason+ChatColor.WHITE+"\"");
            banned.add(player.getName());
            alertMods(list.getFirst().toString(),sender);
        }
    }
    
    public void createOfflineInfraction(Player sender, String splayer, String type, String[] args) {
        String reason = "";
        int i;
        for(i = 1; i < args.length-1; ++i)
            reason += args[i]+" ";
        reason += args[i];
        
        Player player = getServer().getPlayer(splayer);
        
        if(player != null) {
            sender.sendMessage(ChatColor.RED+"Player: "+splayer+" is not offline, cannot infract");
            return;
        }
        
        if(sender.getName().equals(splayer)) {
            sender.sendRawMessage(ChatColor.RED+"You cannot infract yourself!");
            return;
        }
        
        if(!sender.hasPermission(adminPermission)) {
            sender.sendRawMessage(ChatColor.RED+"You don't have permission to infract offlien players");
            return;
        }
        
        LinkedList<Infraction> list = infractions.get(splayer);
        if(list == null)
            list = new LinkedList<Infraction>();
        list.addFirst(new Infraction(sender.getName(), splayer, type, reason));
        infractions.put(splayer,list);
        
        if(type.equals("warned")) {
            sender.sendRawMessage(ChatColor.GREEN+"You"+ChatColor.GOLD+
                                  " warned "+ChatColor.RED+splayer+
                                  ChatColor.GOLD+" for \""+ChatColor.GRAY+
                                  reason+ChatColor.WHITE+"\"");
            alertMods(list.getFirst().toString(),sender);
        } else if(type.equals("kicked")) {
            sender.sendRawMessage(ChatColor.GREEN+"You"+ChatColor.GOLD+
                                  " kicked "+ChatColor.RED+splayer+
                                  ChatColor.GOLD+" for \""+ChatColor.GRAY+
                                  reason+ChatColor.WHITE+"\"");
            alertMods(list.getFirst().toString(),sender);
        } else if(type.equals("banned")) {
            sender.sendRawMessage(ChatColor.GREEN+"You"+ChatColor.GOLD+
                                  " banned "+ChatColor.RED+splayer+
                                  ChatColor.GOLD+" for \""+ChatColor.GRAY+
                                  reason+ChatColor.WHITE+"\"");
            banned.add(splayer);
            alertMods(list.getFirst().toString(),sender);
        }
    }
    
    public void removeInfraction(Player sender, String player, String sid) {
        Player ptemp = getServer().getPlayer(player);
        if(ptemp != null)
            player = ptemp.getName();
        
        if(sender.getName().equals(player)) {
            sender.sendRawMessage(ChatColor.RED+"You cannot pardon yourself!");
            return;
        }
        
        int id;
        try {
            id = Integer.parseInt(sid)-1;
        } catch(Exception e) {
            sender.sendRawMessage(ChatColor.RED+"Given id was nto a number");
            return;
        }
        
        LinkedList<Infraction> infracts = infractions.get(player);
        if(infracts == null) {
            sender.sendRawMessage(ChatColor.RED+player+" does not have any infractions");
            return;
        }
        
        if(id >= infracts.size()) {
            sender.sendRawMessage(ChatColor.RED+"Invalid id: "+id+", please check and use the right one");
            return;
        }
        
        Infraction infract = infracts.get(id);
        if(infract.pardoned) {
            sender.sendRawMessage(ChatColor.RED+player+"-Id: "+id+" was already pardoned");
            return;
        }
        infract.pardoned = true;
        infract.pardoner = sender.getName();
        
        if(infract.type.equals("banned"))
            banned.remove(player);
        
        if(ptemp != null)
            ptemp.sendMessage(ChatColor.GREEN+sender.getName()+ChatColor.GOLD+
                              " pardoned you for \""+ChatColor.GRAY+
                              infract.reason+ChatColor.WHITE+"\"");
        sender.sendMessage(ChatColor.GREEN+"You"+ChatColor.GOLD+
                           " pardoned "+ChatColor.RED+infract.player+
                           ChatColor.GOLD+" for \""+ChatColor.GRAY+
                           infract.reason+ChatColor.WHITE+"\"");
        alertMods(infract.toString(),sender);
    }
    
    public void alertMods(String message, Player omit) {
        for(Player p : getServer().getOnlinePlayers()) {
            if(omit != p)
               if(p.hasPermission(modPermission) || p.hasPermission(adminPermission))
                    p.sendRawMessage(message);
        }
    }
    
    public void help(Player sender) {
        sender.sendRawMessage(ChatColor.GREEN+"Infraction Command Help");
        sender.sendRawMessage(ChatColor.AQUA+"/infracts <player> {page=1}"+
                              ChatColor.GREEN+" - shows the infraction of a player");
        sender.sendRawMessage(ChatColor.AQUA+"/warn <player> <reason>"+
                              ChatColor.GREEN+" - gives a player a warning");
        sender.sendRawMessage(ChatColor.AQUA+"/kick <player> <reason>"+
                              ChatColor.GREEN+" - kicks the player from the server");
        sender.sendRawMessage(ChatColor.AQUA+"/ban <player> <reason>"+
                              ChatColor.GREEN+" - bans the player from the server");
    }
    
    public boolean isBanned(String player) {
        return banned.contains(player);
    }
    
    public LinkedList<Infraction> getInfractions(String player) {
        return infractions.get(player);
    }
    
    public void load() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getSaveFile()));
            infractions = (HashMap<String,LinkedList<Infraction>>)ois.readObject();
            banned = (HashSet<String>)ois.readObject();
            ois.close();
        } catch(Exception e) {
        }
    }
    
    public void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSaveFile()));
            oos.writeObject(infractions);
            oos.writeObject(banned);
            oos.close();
        } catch(Exception e) {
        }
    }
    
    public File getSaveFile() {
        File folder = getDataFolder();
        if(!folder.exists())
            folder.mkdir();
        File file = new File(folder.getPath()+File.separator+"Infractions.file");
        
        return file;
    }
    
    @Override
    public void run() {
        //save the data
        save();
    }
}
