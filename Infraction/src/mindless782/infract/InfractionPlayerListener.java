package mindless782.infract;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;
import java.util.LinkedList;

public class InfractionPlayerListener extends PlayerListener implements Runnable {
    private InfractionPlugin plugin;
    HashMap<String,Integer> map;
    
    public InfractionPlayerListener(InfractionPlugin p) {
        plugin = p;
        map = new HashMap<String,Integer>();
    }
    
    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        String player = event.getPlayer().getName();
        if(!plugin.isBanned(player)) {
            if(plugin.getInfractions(player) == null)
                return;
            int infracts = plugin.getInfractions(player).size();
            String msg = "";
            if(infracts != 0) {
                msg = ChatColor.RED+player+ChatColor.GOLD+" has "+ChatColor.GREEN+infracts+
                      ChatColor.GOLD+" infractions";
                plugin.alertMods(msg, null);
            }
            return;
        }
        
        LinkedList<Infraction> list = plugin.getInfractions(player);
        Infraction infract = list.getFirst();
        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, infract.issuer+" banned you for \""+infract.reason+"\"");
    }
    
    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        String name = event.getPlayer().getName();
        if(plugin.isBanned(name)) {
            event.setCancelled(true);
            return;
        }
        Integer i = map.get(name);
        if(i == null)
            i = new Integer(0);
        ++i;
        map.put(name,i);
        if(i > 4)//
            stopSpam(name);//
    }
    
    @Override
    public void run() {
        //for(String s : map.keySet()) {
            //if(map.get(s) > 4)
                //stopSpam(s);
        //}
        map.clear();
    }
    
    private void stopSpam(String player) {
        Infraction infract = new Infraction("StopSpam",player,"banned","Auto Spamming Bitch");
        LinkedList<Infraction> infracts = plugin.infractions.get(player);
        if(infracts == null)
            infracts = new LinkedList<Infraction>();
        infracts.addFirst(infract);
        plugin.infractions.put(player,infracts);
        plugin.banned.add(player);
        plugin.getServer().getPlayer(player).kickPlayer("StopSpam banned you for: \"Auto Spamming Bitch\"");
    }
}
