package net.mctitan.infraction;

import org.bukkit.ChatColor;

/**
 * enumeration for the chat colors used by the plugin
 * 
 * @author Colin
 */
public enum InfractionChatColor {
    COLOR_DATETIME(ChatColor.LIGHT_PURPLE,InfractionRegex.COLOR_DATETIME_REGEX),
    COLOR_ISSUER(ChatColor.GREEN,InfractionRegex.COLOR_ISSUER_REGEX),
    COLOR_TYPE(ChatColor.GOLD,InfractionRegex.COLOR_TYPE_REGEX),
    COLOR_PARDON(ChatColor.AQUA,InfractionRegex.COLOR_TYPE_REGEX),
    COLOR_BAD_PLAYER(ChatColor.RED,InfractionRegex.COLOR_PLAYER_REGEX),
    COLOR_GOOD_PLAYER(ChatColor.AQUA,InfractionRegex.COLOR_PLAYER_REGEX),
    COlOR_NORMAL(ChatColor.WHITE,InfractionRegex.COLOR_NORMAL_REGEX),
    COLOR_REASON(ChatColor.GRAY,InfractionRegex.COLOR_REASON_REGEX),
    COLOR_AMOUNT(ChatColor.GOLD,InfractionRegex.COLOR_AMOUNT_REGEX),
    COLOR_ID(ChatColor.AQUA,InfractionRegex.COLOR_ID_REGEX),
    COLOR_PAGE(ChatColor.AQUA,InfractionRegex.COLOR_PAGE_REGEX);
    
    /** color used for output */
    public ChatColor color;
    
    /** regex that matches this type of color */
    public String regex;
    
    /**
     * constructs the infraction color
     * 
     * @param color output color to display
     * @param regex regex to match the color type
     */
    private InfractionChatColor(ChatColor color, String regex) {
        this.color = color;
        this.regex = regex;
    }
    
    /**
     * gets the color in String format
     * @return color in String format
     */
    public String getColor() {
        return ""+color;
    }
    
    /**
     * replaces a regex in the msg checking against color type given
     * 
     * @param msg msg to replace regexes in
     * @param color color type to test against
     * 
     * @return modified msg after replacements
     */
    public static String replaceColor(String msg, InfractionChatColor color) {
        return msg.replace(color.regex,color.getColor());
    }
}
