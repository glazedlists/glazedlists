/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.gui;

/**
 * Specifies how a set of records are rendered in a table.
 *
 * @see ca.odell.glazedlists.GlazedLists#tableFormat(Class,String[],String[])
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface TableFormat {

    /**
     * The number of columns to display.
     */
    public int getColumnCount();

    /**
     * Gets the title of the specified column. 
     */
    public String getColumnName(int column);
    
    /**
     * Gets the value of the specified field for the specified object. This
     * is the value that will be passed to the editor and renderer for the
     * column. If you have defined a custom renderer, you may choose to return
     * simply the baseObject.
     */
    public Object getColumnValue(Object baseObject, int column);
}
