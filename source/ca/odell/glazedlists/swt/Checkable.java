/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

/**
 * An Object that maintains state for checked and unchecked.
 *
 * <p><strong>Warning:</strong> It is an error for a single Checkable instance to
 * be managed by multiple clients simultaneously. In effect, for some client to
 * a Checkable instance calls setChecked(), the Checkable instance must return the
 * specified value from all calls to isChecked() by that client.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface Checkable {
    
    /**
     * Return whether or not this Checkable is checked.
     */
    public boolean isChecked();
    
    /**
     * Mark this Checkable as checked or not.
     */
    public void setChecked(boolean checked);
}

