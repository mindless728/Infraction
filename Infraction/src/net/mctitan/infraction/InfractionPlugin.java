package net.mctitan.infraction;

import org.bukkit.plugin.java.JavaPlugin;

import net.mctitan.infraction.commands.AdminCommands;
import net.mctitan.infraction.commands.OfflineCommands;
import net.mctitan.infraction.commands.OnlineCommands;

import net.mctitan.infraction.misc.DropControl;
import net.mctitan.infraction.misc.SpamControl;

/**
 * The actual Bukkit plugin, should be responsible to get everything setup
 * for use with Bukkit
 * 
 * @author mindless728
 */
public class InfractionPlugin extends JavaPlugin {
    /** holds the static instance of this plugin */
    private static InfractionPlugin instance;
    
    private InfractionManager manager;
    
    /** listenr for player login events */
    private InfractionListener listener;
    
    /** holds the admin command executor */
    private AdminCommands adminCommands;
    
    /** holds the offline command executor */
    private OfflineCommands offlineCommands;
    
    /** holds the online command executor */
    private OnlineCommands onlineCommands;
    
    /** holds the drop controller */
    private DropControl dropControl;
    
    /** holds the chat/command controller */
    private SpamControl spamControl;
    
    /** hoe often the repeating controllers should be run */
    private int delay = 20;
    
    /** Called when the plugin is enabled */
    @Override
    public void onEnable() {
        //grab the instance, needs to be done first
        instance = this;
        manager = InfractionManager.getInstance();
        
        //create all of the objects needed
        listener = new InfractionListener();
        
        adminCommands = new AdminCommands();
        offlineCommands = new OfflineCommands();
        onlineCommands = new OnlineCommands();
        
        dropControl = new DropControl();
        spamControl = new SpamControl();
        
        //@TODO read in configuration data
        
        //register command executors will commands
        getCommand("infracts").setExecutor(adminCommands);
        getCommand("pardon").setExecutor(adminCommands);
        getCommand("delete").setExecutor(adminCommands);
        getCommand("deleteall").setExecutor(adminCommands);
        getCommand("check").setExecutor(adminCommands);
        
        getCommand("owarn").setExecutor(offlineCommands);
        getCommand("okick").setExecutor(offlineCommands);
        getCommand("oban").setExecutor(offlineCommands);
        
        getCommand("warn").setExecutor(onlineCommands);
        getCommand("kick").setExecutor(onlineCommands);
        getCommand("ban").setExecutor(onlineCommands);
        
        //register all of the listeners
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginManager().registerEvents(dropControl, this);
        getServer().getPluginManager().registerEvents(spamControl, this);
        
        //schedule repeating tasks
        getServer().getScheduler().scheduleSyncRepeatingTask(this, dropControl, 1, 20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, spamControl, 1, 20);
    }
    
    /** Called when the plugin is disabled */
    @Override
    public void onDisable() {
        manager.save();
    }
    
    /**
     * gets the singleton instance of this class
     * @return 
     */
    public static InfractionPlugin getInstance() {
        return instance;
    }
}
