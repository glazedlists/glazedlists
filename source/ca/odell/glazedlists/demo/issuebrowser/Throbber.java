package ca.odell.glazedlists.demo.issuebrowser;

/**
 * Simple animated monitor shows if work is taking place. This can be implemented
 * in any GUI toolkit.
 *
 * @author <a href="jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface Throbber {

    public void setOn();
    
    public void setOff();
}