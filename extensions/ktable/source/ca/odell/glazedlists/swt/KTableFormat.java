/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.gui.TableFormat;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface KTableFormat extends TableFormat {

    /**
     * @see {@link de.kupzog.ktable.KTableModel#getTooltipAt}
     */
    public String getColumnTooltip(Object baseObject, int column);

    /**
     * @see {@link de.kupzog.ktable.KTableModel#getCellEditor}
     */
    public KTableCellEditor getColumnEditor(Object baseObject, int column);

    /**
     * @see {@link de.kupzog.ktable.KTableModel#getCellRenderer}
     */
    public KTableCellRenderer getColumnRenderer(Object baseObject, int column);
}