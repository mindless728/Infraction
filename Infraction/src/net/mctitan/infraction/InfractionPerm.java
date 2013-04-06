/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mctitan.infraction;

/**
 *
 * @author Colin
 */
public enum InfractionPerm {
    USER("infraction.user"),
    MODERATOR("infraction.moderator"),
    ADMIN("infraction.admin");
    
    /** string that bukkit will test against */
    public String perm;
    
    /**
     * constructs the enmuneration using a string permission
     * 
     * @param perm string permission that bukkit will use
     */
    private InfractionPerm(String perm) {
        this.perm = perm;
    }
}
