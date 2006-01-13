/* O'Dell Swing Extensions                                                    */
/* COPYRIGHT 2005 O'DELL ENGINEERING LTD.                                     */
package ca.odell.glazedlists.swing;

import java.util.*;
// user interface
import javax.swing.*;
// JGoodies is industrial layout
import com.jgoodies.forms.layout.*;
// observable lists are used to store rules
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.beans.BeanProperty;
import ca.odell.glazedlists.event.*;

/**
 * A panel that shows the contents of an EventList containing JComponents.
 *
 * @author <a href="mailto:jesse@odell.ca">Jesse Wilson</a>
 */
public class JEventListPanel<E> extends JPanel implements ListEventListener {

    /** the source contains all the JComponents */
    private TransformedList<E,E> swingSource;

    /** the components of the panel */
    private List<JComponent[]> components = new ArrayList<JComponent[]>();

    /** the display */
    private FormLayout layout = new FormLayout("");

    /** the row and column styles */
    private Format format;
    private boolean vertical;
    private RowSpec[] rowSpecs;
    private ColumnSpec[] columnSpecs;

    /** spacers between element columns */
    private RowSpec gapRow;
    private ColumnSpec gapColumn;

    /**
     * Creates a new {@link JEventListPanel} hosting the
     * {@link JComponent}s from the specified source {@link EventList}.
     */
    public JEventListPanel(EventList<E> source, Format<E> format) {
        this.swingSource = GlazedListsSwing.swingThreadProxyList(source);
        this.format = format;

        // prepare the empty panel
        this.vertical = format.isVertical();
        this.columnSpecs = format.getElementColumns();
        this.rowSpecs = format.getElementRows();
        this.gapRow = format.getGapRow();
        this.gapColumn = format.getGapColumn();
        this.setLayout(layout);

        // create the fixed axis
        if(vertical) {
            for(int c = 0; c < columnSpecs.length; c++) {
                layout.appendColumn(columnSpecs[c]);
            }
        } else {
            for(int r = 0; r < rowSpecs.length; r++) {
                layout.appendRow(rowSpecs[r]);
            }
        }

        // populate the initial elements
        for(int i = 0; i < swingSource.size(); i++) {
            insert(i);
        }

        // listen for changes to the source
        swingSource.addListEventListener(this);
    }

    /**
     * @return the row that the preceding gap for this row should be shown on,
     *      or the row that the element rows should be on if this is the first row.
     */
    private int baseLayoutRow(int row) {
        if(!vertical) return 0;
        if(row == 0) return 0;
        else return (row * rowSpecs.length) + (gapRow == null ? 0 : row - 1);
    }

    /**
     * @return the column that the preceding gap for this column should be shown on,
     *      or the column that the element columns should be on if this is the first column.
     */
    private int baseLayoutColumn(int column) {
        if(vertical) return 0;
        if(column == 0) return 0;
        else return (column * columnSpecs.length) + (gapColumn == null ? 0 : column - 1);
    }

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
        components.add(index, elementComponents);

        // make room on the variable axis
        int baseRow = baseLayoutRow(index);
        int baseColumn = baseLayoutColumn(index);
        if(vertical) {
            // insert the gap for all rows but the first
            if(index != 0 && gapRow != null) {
                if(baseRow == layout.getRowCount()) layout.appendRow(gapRow);
                else layout.insertRow(baseRow + 1, gapRow);
                baseRow += 1;
            }
            // insert the element rows
            for(int r = 0; r < rowSpecs.length; r++) {
                if(baseRow + r == layout.getRowCount()) layout.appendRow(rowSpecs[r]);
                else layout.insertRow(baseRow + r + 1, rowSpecs[r]);
            }
        } else {
            // insert the gap for all columns but the first
            if(index != 0 && gapColumn != null) {
                if(baseColumn == layout.getColumnCount()) layout.appendColumn(gapColumn);
                else layout.insertColumn(baseColumn + 1, gapColumn);
                baseColumn += 1;
            }
            // insert the element columns
            for(int c = 0; c < columnSpecs.length; c++) {
                if(baseColumn + c == layout.getColumnCount()) layout.appendColumn(columnSpecs[c]);
                else layout.insertColumn(baseColumn + c + 1, columnSpecs[c]);
            }
        }

        // insert the components
        for(int c = 0; c < elementComponents.length; c++) {
            if(elementComponents[c] == null) continue;
            CellConstraints constraints = format.getConstraints(c);
            constraints = (CellConstraints)constraints.clone();
            constraints.gridX += baseColumn;
            constraints.gridY += baseRow;
            add(elementComponents[c], constraints);
        }
    }

    /**
     * Handle a deleted element.
     */
    private void delete(int index) {
        // remove the components
        JComponent[] elementComponents = (JComponent[])components.get(index);
        for(int c = 0; c < elementComponents.length; c++) {
            if(elementComponents[c] == null) continue;
            remove(elementComponents[c]);
        }

        // remove the space from the variable axis
        if(vertical) {
            int baseRow = baseLayoutRow(index);
            if(index != 0 && gapRow != null) {
                layout.removeRow(baseRow + 1);
            }
            for(int r = 0; r < rowSpecs.length; r++) {
                layout.removeRow(baseRow + 1);
            }
        } else {
            int baseColumn = baseLayoutColumn(index);
            if(index != 0 && gapColumn != null) {
                layout.removeColumn(baseColumn + 1);
            }
            for(int c = 0; c < columnSpecs.length; c++) {
                layout.removeColumn(baseColumn + 1);
            }
        }

        // forget the components
        components.remove(index);
    }

    /**
     * Handle an updated element.
     */
    private void update(int index) {
        // get the old components
        JComponent[] oldElementComponents = (JComponent[])components.get(index);

        // get the new components
        E element = swingSource.get(index);
        JComponent[] newElementComponents = new JComponent[format.getComponentsPerElement()];
        for(int c = 0; c < newElementComponents.length; c++) {
            newElementComponents[c] = format.getComponent(element, c);
        }

        // swap as necessary
        int baseRow = baseLayoutRow(index);
        int baseColumn = baseLayoutColumn(index);
        for(int c = 0; c < oldElementComponents.length; c++) {
            if(oldElementComponents[c] == newElementComponents[c]) continue;
            if(oldElementComponents[c] != null) {
                remove(oldElementComponents[c]);
            }
            if(newElementComponents[c] != null) {
                CellConstraints constraints = format.getConstraints(c);
                constraints = (CellConstraints)constraints.clone();
                constraints.gridX += baseColumn;
                constraints.gridY += baseRow;
                add(newElementComponents[c], constraints);
            }
        }

        // save the latest components
        components.set(index, newElementComponents);
    }

    /**
     * When the components list changes, this updates the panel.
     */
    public void listChanged(ListEvent listChanges) {
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

    /**
     * Releases the resources consumed by this {@link JEventListPanel} so that it
     * may eventually be garbage collected.
     * 
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link JEventListPanel} after it has been disposed.
     */
    public void dispose() {
        swingSource.dispose();
    }

    /**
     * Specify how the JComponents of an Object are layed out in a row.
     */
    public interface Format<E> {

        /**
         * Whether to lay out components in series horizontally or vertically.
         *
         * @return true for components to be layed above one another in
         *      rows, or false for components to be layed beside one another in columns.
         */
        boolean isVertical();

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
        private final boolean vertical;
        private final RowSpec[] rowSpecs;
        private final ColumnSpec[] columnSpecs;
        private final RowSpec gapRow;
        private final ColumnSpec gapColumn;
        private final CellConstraints[] cellConstraints;

        protected AbstractFormat(boolean vertical, RowSpec[] rowSpecs, ColumnSpec[] columnSpecs, RowSpec gapRow, ColumnSpec gapColumn, CellConstraints[] cellConstraints) {
            this.vertical = vertical;
            this.rowSpecs = rowSpecs;
            this.columnSpecs = columnSpecs;
            this.gapRow = gapRow;
            this.gapColumn = gapColumn;
            this.cellConstraints = cellConstraints;
        }

        public AbstractFormat(boolean vertical, String rowSpecs, String columnSpecs, String gapRow, String gapColumn, CellConstraints[] cellConstraints) {
            this(vertical, RowSpec.decodeSpecs(rowSpecs), ColumnSpec.decodeSpecs(columnSpecs), new RowSpec(gapRow), new ColumnSpec(gapColumn), cellConstraints);
        }

        public AbstractFormat(boolean vertical, String rowSpecs, String columnSpecs, String gapRow, String gapColumn, String[] cellConstraints) {
            this(vertical, RowSpec.decodeSpecs(rowSpecs), ColumnSpec.decodeSpecs(columnSpecs), new RowSpec(gapRow), new ColumnSpec(gapColumn), decode(cellConstraints));
        }

        /** {@inheritDoc} */
        public boolean isVertical() {
            return vertical;
        }

        /** {@inheritDoc} */
        public RowSpec[] getElementRows() {
            return rowSpecs;
        }

        /** {@inheritDoc} */
        public ColumnSpec[] getElementColumns() {
            return columnSpecs;
        }

        /** {@inheritDoc} */
        public int getComponentsPerElement() {
            return cellConstraints.length;
        }

        /** {@inheritDoc} */
        public CellConstraints getConstraints(int component) {
            return cellConstraints[component];
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
         * Decode the specified Strings ino cell constraints.
         */
        protected static CellConstraints[] decode(String[] cellConstraints) {
            CellConstraints[] decoded = new CellConstraints[cellConstraints.length];
            for(int c = 0; c < cellConstraints.length; c++) {
                decoded[c] = new CellConstraints(cellConstraints[c]);
            }
            return decoded;
        }
    }

    /**
     * A default implementation of {@link Format} that uses bean properties.
     */
    public static class BeanFormat<E> extends AbstractFormat<E> implements Format<E> {
        private final BeanProperty<E>[] properties;

        public BeanFormat(Class<E> beanClass, boolean vertical, RowSpec[] rowSpecs, ColumnSpec[] columnSpecs, RowSpec gapRow, ColumnSpec gapColumn, CellConstraints[] cellConstraints, String[] properties) {
            super(vertical, rowSpecs, columnSpecs, gapRow, gapColumn, cellConstraints);
            this.properties = new BeanProperty[properties.length];
            for(int p = 0; p < properties.length; p++) {
                this.properties[p] = new BeanProperty<E>(beanClass, properties[p], true, false);
            }
        }
        public BeanFormat(Class<E> beanClass, boolean vertical, String rowSpecs, String columnSpecs, String gapRow, String gapColumn, CellConstraints[] cellConstraints, String[] properties) {
            this(beanClass, vertical, RowSpec.decodeSpecs(rowSpecs), ColumnSpec.decodeSpecs(columnSpecs), new RowSpec(gapRow), new ColumnSpec(gapColumn), cellConstraints, properties);
        }
        public BeanFormat(Class<E> beanClass, boolean vertical, String rowSpecs, String columnSpecs, String gapRow, String gapColumn, String[] cellConstraints, String[] properties) {
            this(beanClass, vertical, RowSpec.decodeSpecs(rowSpecs), ColumnSpec.decodeSpecs(columnSpecs), new RowSpec(gapRow), new ColumnSpec(gapColumn), decode(cellConstraints), properties);
        }

        /** {@inheritDoc} */
        public JComponent getComponent(E element, int component) {
            return (JComponent)properties[component].get(element);
        }
    }
}