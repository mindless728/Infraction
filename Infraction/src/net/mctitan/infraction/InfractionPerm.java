package net.mctitan.infraction;

/**
 * Has the permission nodes for certain aspects to the plugin
 * 
 * @author mindless728
 */
public enum InfractionPerm {
    /** User permissions*/
    USER            {@Override public String toString(){return "infraction.user";}},
    
    /** Moderator permissions */
    MODERATOR       {@Override public String toString(){return "infraction.mod";}},
    
    /** Admin permissions */
    ADMIN           {@Override public String toString(){return "infraction.admin";}}
}

