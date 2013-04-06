package net.mctitan.infraction;

/**
 * Enumeration that represents all of the infractions types
 * 
 * @author Colin
 */
public enum InfractionType {
    WARN("warned",InfractionChatColor.COLOR_TYPE),
    KICK("kicked",InfractionChatColor.COLOR_TYPE),
    BAN("banned",InfractionChatColor.COLOR_TYPE),
    PARDON("pardoned",InfractionChatColor.COLOR_PARDON);
    
   /** the output that should be used in messages */
    public String output;
    
    /** the color type used for output */
    public InfractionChatColor color;
    
    /**
     * constructs this enumeration using the output and color type
     * 
     * @param output output to show in messages in the plugin
     * @param color color type to display in messages
     */
    private InfractionType(String output, InfractionChatColor color) {
        this.output = output;
        this.color = color;
    }
}
