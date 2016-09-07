/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

/**
 * Specifies how to edit the elements of table.
 *
 * <p>This class can be used as an alternative to the simple {@link TableFormat}
 * class to provide editable cells. The
 * {@link ca.odell.glazedlists.swing.DefaultEventTableModel EventTableModel} detects if a
 * class implements {@link WritableTableFormat} for modifying the table. If a table
 * is not editable at all, it is sufficient to implement {@link TableFormat}
 * only.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 *
 * @see AdvancedTableFormat
 * @see TableFormat
 */
public interface WritableTableFormat<E> extends TableFormat<E> {

    /**
     * For editing fields. This returns true if the specified Object in the
     * specified column can be edited by the user.
     *
     * @param baseObject the Object to test as editable or not. This will be
     *      an element from the source list.
     * @param column the column to test.
     * @return true if the object and column are editable, false otherwise.
     * @since 2004-August-27, as a replacement for isColumnEditable(int).
     */
    public boolean isEditable(E baseObject, int column);

    /**
     * Sets the specified field of the base object to the edited value. When
     * a column of a table is edited, this method is called so that the user
     * can specify how to modify the base object for each column.
     *
     * @param baseObject the Object to be edited. This will be the original
     *      Object from the source list.
     * @param editedValue the newly constructed value for the edited column
     * @param column the column which has been edited
     * @return the revised Object, or null if the revision shall be discarded.
     *      If not null, the EventTableModel will set() this revised value in
     *      the list and overwrite the previous value.
     */
    public E setColumnValue(E baseObject, Object editedValue, int column);
}