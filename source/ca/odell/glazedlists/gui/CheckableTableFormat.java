/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

/**
 * Specifies how to check table elements.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface CheckableTableFormat<E> extends TableFormat<E> {
    
    /**
     * Sets the specified object as checked.
     */
    public void setChecked(E baseObject, boolean checked);
    
    /**
     * Gets whether the specified object is checked.
     */
    public boolean getChecked(E baseObject);
}