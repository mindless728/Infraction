package net.mctitan.infraction;

import org.bukkit.plugin.java.JavaPlugin;

import net.mctitan.infraction.commands.AdminCommands;
import net.mctitan.infraction.commands.DonateCommands;
import net.mctitan.infraction.commands.OfflineCommands;
import net.mctitan.infraction.commands.OnlineCommands;
import net.mctitan.infraction.misc.DropControl;
import net.mctitan.infraction.misc.SpamControl;

/**
 * The actual plugin that bukkit will run, should initialize the system
 * and add the hooks needed for the plugin to run
 * 
 * @author Colin
 */
public class InfractionPlugin extends JavaPlugin {
    /** instance of the plugin, treated like a singleton object */
    private static InfractionPlugin instance;
    
    /** infraction manager */
    private static InfractionManager manager;
    
    /** controls the admin commands */
    private AdminCommands admin;
    
    /** controls the donation commands */
    private DonateCommands donate;
    
    /** controls the offline comands */
    private OfflineCommands offline;
    
    /** controls the online command */
    private OnlineCommands online;
    
    /** controls the drop rate of players */
    private DropControl drop;
    
    /** controls the spam rate of players */
    private SpamControl spam;
    
    /** checks when people join to see if they are banned or not */
    private InfractionListener listener;
    
    /** called when the plugin is enabled */
    @Override
    public void onEnable() {
        //set the instance
        instance = this;
        manager = InfractionManager.getInstance();
        
        //create the objects needed
        admin = new AdminCommands();
        donate = new DonateCommands();
        offline = new OfflineCommands();
        online = new OnlineCommands();
        
        drop = new DropControl();
        spam = new SpamControl();
        
        listener = new InfractionListener();
        
        //get each command, and add an executor for it
        getCommand("infracts").setExecutor(admin);
        getCommand("check").setExecutor(admin);
        getCommand("pardon").setExecutor(admin);
        getCommand("delete").setExecutor(admin);
        getCommand("deleteall").setExecutor(admin);
        
        getCommand("donate_pardon").setExecutor(donate);
        getCommand("donate_delete").setExecutor(donate);
        getCommand("donate_deleteall").setExecutor(donate);
        
        getCommand("owarn").setExecutor(offline);
        getCommand("okick").setExecutor(offline);
        getCommand("oban").setExecutor(offline);
        
        getCommand("warn").setExecutor(online);
        getCommand("kick").setExecutor(online);
        getCommand("ban").setExecutor(online);
        
        //register the listeners
        getServer().getPluginManager().registerEvents(drop, this);
        getServer().getPluginManager().registerEvents(spam, this);
        getServer().getPluginManager().registerEvents(listener, this);
        
        //register tasks
        getServer().getScheduler().scheduleSyncRepeatingTask(this, drop, 1, 20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, spam, 1, 20);
    }
    
    /** called when the plugin is disabled */
    @Override
    public void onDisable() {
        manager.save();
        instance = null;
    }
    
    /**
     * get the instance of the plugin
     * 
     * @return singleton like instance of the plugin
     */
    public static InfractionPlugin getInstance() {
        return instance;
    }
}
