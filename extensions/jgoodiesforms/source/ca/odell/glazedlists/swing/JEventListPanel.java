/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

/**
 * A panel that shows the contents of an EventList containing JComponents.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan="2"><font size="+2"><b>Extension: JGoodies Forms</b></font></td></tr>
 * <tr><td  colspan="2">This Glazed Lists <i>extension</i> requires the third party library <b>JGoodies Forms</b>.</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Tested Version:</b></td><td>1.0.5</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Home page:</b></td><td><a href="http://www.jgoodies.com/freeware/forms/">http://www.jgoodies.com/freeware/forms/</a></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>License:</b></td><td><a href="http://www.opensource.org/licenses/bsd-license.html">BSD</a></td></tr>
 * </td></tr>
 * </table>
 *
 * <p>To use {@link JEventListPanel}:
 * <ol>
 *   <li>Create an {@link EventList} of {@link JComponent}s, or any
 *       objects that reference a set of {@link JComponent}s.
 *   <li>Implement {@link JEventListPanel.Format} for the object's in your
 *       {@link EventList}. This interface defines how a single cell is layed out.
 *       Once the layout for a single cell is known, all cells can be tiled to
 *       show all of the {@link JComponent}s in your {@link EventList}.
 *   <li>Create an {@link JEventListPanel} and add it in your application somewhere.
 *       If the number of elements in the {@link EventList} may grow unbounded,
 *       wrap your {@link JEventListPanel} in a {@link JScrollPane}.
 * </ol>
 *
 * @author <a href="mailto:jesse@odell.ca">Jesse Wilson</a>
 */
public final class JEventListPanel<E> extends JPanel {

    /** the source contains all the JComponents */
    private TransformedList<E,E> swingSource;

    /** the components of the panel */
    private List<JComponent[]> components = new ArrayList<JComponent[]>();

    /** the layout supports a forms layout under the hood */
    private final ListLayout listLayout;

    /** the format specifies what an element cell looks like */
    private final Format<E> format;

    /** handle changes to the source {@link EventList} */
    private final SourceChangeHandler sourceChangeHandler = new SourceChangeHandler();

    /**
     * Creates a new {@link JEventListPanel} hosting the
     * {@link JComponent}s from the specified source {@link EventList}.
     */
    public JEventListPanel(EventList<E> source, Format<E> format) {
        this.swingSource = GlazedListsSwing.swingThreadProxyList(source);
        this.listLayout = new ListLayout(this, format);
        this.format = format;
        this.setLayout(listLayout);

        // populate the initial elements
        for(int i = 0; i < swingSource.size(); i++) {
            sourceChangeHandler.insert(i);
        }

        // listen for changes to the source
        swingSource.addListEventListener(sourceChangeHandler);
    }

    /**
     * Limit the number of list elements will be layed out along the X axis.
     * This is the number of logical columns, which may differ from the layout
     * columns used within each cell.
     *
     * <p>Note that when the columns are limited, rows are automatically unlimited.
     *
     * @param elementColumns the number of logical columns, between 1 and
     *      {@link Integer#MAX_VALUE}.
     */
    public void setElementColumns(int elementColumns) {
        if(elementColumns < 1) throw new IllegalArgumentException("elementColumns must be in the range [1, " + Integer.MAX_VALUE + "]");
        listLayout.setElementColumns(elementColumns);
    }

    /**
     * Limit the number of list elements will be layed out along the Y axis.
     * This is the number of logical rows, which may differ from the layout
     * rows used within each cell.
     *
     * <p>Note that when the rows are limited, columns are automatically unlimited.
     *
     * @param elementRows the number of logical rows, between 1 and
     *      {@link Integer#MAX_VALUE}.
     */
    public void setElementRows(int elementRows) {
        if(elementRows < 1) throw new IllegalArgumentException("elementRows must be in the range [1, " + Integer.MAX_VALUE + "]");
        listLayout.setElementRows(elementRows);
    }

    /**
     * Provide the binding between this panel and the source {@link EventList}.
     */
    private class SourceChangeHandler implements ListEventListener<E> {
        /**
         * Handle an inserted element.
         */
        private void insert(int index) {
            // save the components
            E element = swingSource.get(index);
            JComponent[] elementComponents = new JComponent[format.getComponentsPerElement()];
            for(int c = 0; c < elementComponents.length; c++) {
                elementComponents[c] = format.getComponent(element, c);
            }

            // remember these components
            components.add(index, elementComponents);

            listLayout.insertIndex(index);

            // insert the components
            for(int c = 0; c < elementComponents.length; c++) {
                if(elementComponents[c] == null) continue;
                add(elementComponents[c], new ListLayout.Constraints(c, index));
            }
        }

        /**
         * Handle a deleted element.
         */
        private void delete(int index) {
            // remove the components
            JComponent[] elementComponents = components.get(index);
            for(int c = 0; c < elementComponents.length; c++) {
                if(elementComponents[c] == null) continue;
                remove(elementComponents[c]);
            }

            // forget the components
            components.remove(index);

            // collapse the row from the layout
            listLayout.removeIndex(index);
        }

        /**
         * Handle an updated element.
         */
        private void update(int index) {
            // get the old components
            JComponent[] oldElementComponents = components.get(index);

            // get the new components
            E element = swingSource.get(index);
            JComponent[] newElementComponents = new JComponent[format.getComponentsPerElement()];
            for(int c = 0; c < newElementComponents.length; c++) {
                newElementComponents[c] = format.getComponent(element, c);
            }

            // swap as necessary
            for(int c = 0; c < oldElementComponents.length; c++) {
                if(oldElementComponents[c] == newElementComponents[c]) continue;
                if(oldElementComponents[c] != null) {
                    remove(oldElementComponents[c]);
                }
                if(newElementComponents[c] != null) {
                    add(newElementComponents[c], new ListLayout.Constraints(c, index));
                }
            }

            // save the latest components
            components.set(index, newElementComponents);

            // update the row in the layout
            listLayout.updateIndex(index);
        }


        /**
         * When the components list changes, this updates the panel.
         */
        public void listChanged(ListEvent<E> listChanges) {
            while(listChanges.next()) {
                int type = listChanges.getType();
                int index = listChanges.getIndex();
                if(type == ListEvent.INSERT) {
                    insert(index);
                } else if(type == ListEvent.DELETE) {
                    delete(index);
                } else if(type == ListEvent.UPDATE) {
                    update(index);
                }
            }

            // repaint the panel
            revalidate();
            repaint();
        }
    }

    /**
     * Releases the resources consumed by this {@link JEventListPanel} so that it
     * may eventually be garbage collected.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link JEventListPanel} after it has been disposed.
     */
    public void dispose() {
        swingSource.removeListEventListener(sourceChangeHandler);
        swingSource.dispose();
    }

    /**
     * Specify how the JComponents of an Object are layed out in a row.
     */
    public interface Format<E> {

        /**
         * Get the number of components for each row element.
         */
        int getComponentsPerElement();

        /**
         * Get the component from the specified list element.
         */
        JComponent getComponent(E element, int component);

        /**
         * Get the constraints to lay out the specified component.
         *
         * @param component the component to fetch constraints for.
         * @return constraints that specify how to lay out the component.
         *      The constraints' Y value must be between (baseRow + 1) and
         *      (baseRow + getElementRows().length + 1) inclusive.
         *      The constraints' X value must be between 1 and
         *      (getElementColumns().length + 1) inclusive.
         */
        CellConstraints getConstraints(int component);

        /**
         * Get the RowSpecs required for one element in the list.
         */
        RowSpec[] getElementRows();

        /**
         * Get the ColumnSpecs required for one element in the list.
         */
        ColumnSpec[] getElementColumns();

        /**
         * Get the RowSpec to separate two elements.
         *
         * @return the RowSpec for the spacer row, or <code>null</code> for
         *      no gap.
         */
        RowSpec getGapRow();

        /**
         * Get the ColumnSpecs to separate two elements.
         *
         * @return the ColumnSpec for the spacer column, or <code>null</code> for
         *      no gap.
         */
        ColumnSpec getGapColumn();
    }

    /**
     * A default implementation of the {@link Format} interface.
     */
    public static abstract class AbstractFormat<E> implements Format<E> {
        private RowSpec[] elementRows;
        private ColumnSpec[] elementColumns;
        private RowSpec gapRow;
        private ColumnSpec gapColumn;
        private CellConstraints[] cellConstraints;

        /**
         * Construct a format using the specifications and constraints specified.
         */
        protected AbstractFormat(RowSpec[] rowSpecs, ColumnSpec[] columnSpecs, RowSpec gapRow, ColumnSpec gapColumn, CellConstraints[] cellConstraints) {
            this.elementRows = rowSpecs;
            this.elementColumns = columnSpecs;
            this.gapRow = gapRow;
            this.gapColumn = gapColumn;
            this.cellConstraints = cellConstraints;
        }

        /**
         * Construct a format using the specifications and constraints specified.
         */
        protected AbstractFormat(String rowSpecs, String columnSpecs, String gapRow, String gapColumn, CellConstraints[] cellConstraints) {
            this(RowSpec.decodeSpecs(rowSpecs), ColumnSpec.decodeSpecs(columnSpecs), new RowSpec(gapRow), new ColumnSpec(gapColumn), cellConstraints);
        }

        /**
         * Construct a format using the specifications and constraints specified.
         */
        protected AbstractFormat(String rowSpecs, String columnSpecs, String gapRow, String gapColumn, String[] cellConstraints) {
            this(RowSpec.decodeSpecs(rowSpecs), ColumnSpec.decodeSpecs(columnSpecs), gapRow == null ? null : new RowSpec(gapRow), gapColumn == null ? null : new ColumnSpec(gapColumn), decode(cellConstraints));
        }

        /**
         * Construct a bare format. Extending classes must populate all specs
         * or override the corresponding getters so that all appropriate
         * specifications are available.
         */
        protected AbstractFormat() {
            // do nothing
        }

        /** {@inheritDoc} */
        public RowSpec[] getElementRows() {
            return elementRows;
        }

        /**
         * Set the rows used for a single element cell.
         */
        public void setElementRows(RowSpec[] elementRows) {
            this.elementRows = elementRows;
        }

        /** {@inheritDoc} */
        public ColumnSpec[] getElementColumns() {
            return elementColumns;
        }

        /**
         * Set the columns used for a single element cell.
         */
        public void setElementColumns(ColumnSpec[] elementColumns) {
            this.elementColumns = elementColumns;
        }

        /**
         * Set the rows and columns used for a single element cell.
         */
        public void setElementCells(String rowSpecs, String columnSpecs) {
            this.setElementRows(RowSpec.decodeSpecs(rowSpecs));
            this.setElementColumns(ColumnSpec.decodeSpecs(columnSpecs));
        }

        /** {@inheritDoc} */
        public int getComponentsPerElement() {
            return cellConstraints.length;
        }

        /** {@inheritDoc} */
        public CellConstraints getConstraints(int component) {
            return cellConstraints[component];
        }

        /**
         * Set the constraints used for the components of each element.
         */
        public void setCellConstraints(CellConstraints[] cellConstraints) {
            this.cellConstraints = cellConstraints;
        }

        /**
         * Set the constraints used for the components of each element.
         */
        public void setCellConstraints(String[] cellConstraints) {
            setCellConstraints(decode(cellConstraints));
        }

        /** {@inheritDoc} */
        public RowSpec getGapRow() {
            return gapRow;
        }

        /** {@inheritDoc} */
        public ColumnSpec getGapColumn() {
            return gapColumn;
        }

        /**
         * Set the height of the vertical gap between element cells.
         */
        public void setGapRow(RowSpec gapRow) {
            this.gapRow = gapRow;
        }

        /**
         * Set the width of the horizontal gap between element cells.
         */
        public void setGapColumn(ColumnSpec gapColumn) {
            this.gapColumn = gapColumn;
        }

        /**
         * Set the size of the gaps between element cells.
         */
        public void setGaps(String gapRow, String gapColumn) {
            this.setGapRow(gapRow != null ? new RowSpec(gapRow) : null);
            this.setGapColumn(gapColumn != null ? new ColumnSpec(gapColumn) : null);
        }

        /**
         * Decode the specified Strings into cell constraints.
         */
        private static CellConstraints[] decode(String[] cellConstraints) {
            CellConstraints[] decoded = new CellConstraints[cellConstraints.length];
            for(int c = 0; c < cellConstraints.length; c++) {
                decoded[c] = new CellConstraints(cellConstraints[c]);
            }
            return decoded;
        }
    }
}