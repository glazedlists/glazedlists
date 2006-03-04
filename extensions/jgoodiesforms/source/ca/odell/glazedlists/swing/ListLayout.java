/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.ColumnSpec;

import java.util.List;
import java.awt.*;
import java.util.ArrayList;

/**
 * Layout list elements by delegating to a {@link FormLayout}. This layout manages
 * the {@link ListLayout} by responding to list changes and shifting components
 * around as elements are inserted or removed.
 *
 * <p>Note that this class is subject to significant API changes and improvements,
 * and that only the {@link JEventListPanel} class should be referenced in
 * client source code.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class ListLayout extends LayoutDecorator {

    /** all components for the entire grid */
    private final List<CellComponents> gridComponents = new ArrayList<CellComponents>();

    /** the formlayout does the actual legwork */
    private final FormLayout formLayout;

    /** how to layout logical cells in the formlayout */
    private final JEventListPanel.Format format;

    /** the row and column styles */
    private RowSpec[] rowSpecs;
    private ColumnSpec[] columnSpecs;

    /** spacers between element columns */
    private RowSpec gapRow;
    private ColumnSpec gapColumn;

    private Container container;

    private int elementColumns = 1;
    private int elementRows = Integer.MAX_VALUE;

    /**
     * Layout the specified container using the specified format to manage
     * rows, columns and constraints.
     */
    public ListLayout(Container container, JEventListPanel.Format format) {
        this.container = container;
        this.format = format;
        this.formLayout = new FormLayout("");

        // prepare the empty panel
        this.columnSpecs = format.getElementColumns();
        this.rowSpecs = format.getElementRows();
        this.gapRow = format.getGapRow();
        this.gapColumn = format.getGapColumn();

        // tell the superclass who to delegate methods to
        super.delegateLayout = formLayout;
    }

    /**
     * Set the number of columns the layout wraps over. If set, the layout will
     * wrap from left to right, adding rows as necessary.
     */
    public void setElementColumns(int elementColumns) {
        if(elementColumns == this.elementColumns) return;
        setElementRowsAndColumns(Integer.MAX_VALUE, elementColumns);
    }

    /**
     * Set the number of rows the layout wraps over. If set, the layout will
     * wrap from top to bottom, adding rows as necessary.
     */
    public void setElementRows(int elementRows) {
        if(elementRows == this.elementRows) return;
        setElementRowsAndColumns(elementRows, Integer.MAX_VALUE);
    }

    /**
     * Adjust the number of logical rows and columns and how elements are wrapped
     * into those cells.
     */
    private void setElementRowsAndColumns(int elementRows, int elementColumns) {
        int logicalRowCountBefore = logicalRowCount();
        int logicalColumnCountBefore = logicalColumnCount();
        this.elementRows = elementRows;
        this.elementColumns = elementColumns;

        fixCellCount(logicalRowCountBefore, logicalColumnCountBefore);
    }

    /**
     * We can improve this method by replacing the before counts as parameters
     * by figuring out what the counts are by examining the layout.
     */
    private void fixCellCount(int logicalRowCountBefore, int logicalColumnCountBefore) {
        int logicalColumnCountAfter = logicalColumnCount();
        int logicalRowCountAfter = logicalRowCount();

        // make sure we have enough cells
        for(int r = logicalRowCountBefore; r < logicalRowCountAfter; r++) {
            insertLogicalRow(r);
        }
        for(int c = logicalColumnCountBefore; c < logicalColumnCountAfter; c++) {
            insertLogicalColumn(c);
        }

        // we have too many cells in one dimension, the perfect time to
        // reassign constraints safely
        reassignConstraints();

        // make sure we don't have too many cells
        for(int r = logicalRowCountBefore - 1; r >= logicalRowCountAfter; r--) {
            removeLogicalRow(r);
        }
        for(int c = logicalColumnCountBefore - 1; c >= logicalColumnCountAfter; c--) {
            removeLogicalColumn(c);
        }
    }

    private CellConstraints deriveCellConstraints(int component, int logicalColumn, int logicalRow) {
        CellConstraints constraints = format.getConstraints(component);
        constraints = (CellConstraints)constraints.clone();
        constraints.gridX += logicalToLayoutColumn(logicalColumn) + (logicalColumn > 0 && gapColumn != null ? 1 : 0);
        constraints.gridY += logicalToLayoutRow(logicalRow) + (logicalRow > 0 && gapRow != null ? 1 : 0);
        return constraints;
    }

    /** {@inheritDoc} */
    public void addLayoutComponent(Component component, Object constraints) {
        Constraints listLayoutConstraints = (Constraints)constraints;
        int index = listLayoutConstraints.getIndex();
        int formConstraints = listLayoutConstraints.getFormConstraints();

        CellComponents cellComponents = gridComponents.get(index);
        cellComponents.components.add(component);
        cellComponents.constraints.add(listLayoutConstraints);

        CellConstraints cellConstraints = deriveCellConstraints(formConstraints, logicalColumn(index), logicalRow(index));
        super.addLayoutComponent(component, cellConstraints);
    }

    private int logicalColumn(int index) {
        if(elementRows == Integer.MAX_VALUE) {
            return index % elementColumns;
        } else {
            return index / elementRows;
        }
    }
    private int logicalRow(int index) {
        if(elementRows == Integer.MAX_VALUE) {
            return index / elementColumns;
        } else {
            return index % elementRows;
        }
    }
    private int logicalRowCount() {
        if(elementRows == Integer.MAX_VALUE) {
            return (gridComponents.size() + elementColumns - 1) / elementColumns;
        } else {
            return Math.min(gridComponents.size(), elementRows);
        }
    }
    private int logicalColumnCount() {
        if(elementRows == Integer.MAX_VALUE) {
            return Math.min(gridComponents.size(), elementColumns);
        } else {
            return (gridComponents.size() + elementRows - 1) / elementRows;
        }
    }
    private void insertLogicalRow(int index) {
        int baseRow = logicalToLayoutRow(index);

        // insert the gap for all rows but the first
        if(index != 0 && gapRow != null) {
            insertRow(baseRow, gapRow);
            baseRow += 1;
        }
        // insert the element rows
        for(int r = 0; r < rowSpecs.length; r++) {
            insertRow(baseRow + r, rowSpecs[r]);
        }
    }
    private void insertLogicalColumn(int index) {
        int baseColumn = logicalToLayoutColumn(index);

        // insert the gap for all columns but the first
        if(index != 0 && gapColumn != null) {
            insertColumn(baseColumn, gapColumn);
            baseColumn += 1;
        }
        // insert the element columns
        for(int c = 0; c < columnSpecs.length; c++) {
            insertColumn(baseColumn + c, columnSpecs[c]);
        }
    }
    private void removeLogicalRow(int index) {
        int baseRow = logicalToLayoutRow(index);
        if(index != 0 && gapRow != null) {
            formLayout.removeRow(baseRow + 1);
        }
        for(int r = 0; r < rowSpecs.length; r++) {
            formLayout.removeRow(baseRow + 1);
        }
    }
    private void removeLogicalColumn(int index) {
        int baseColumn = logicalToLayoutColumn(index);
        if(index != 0 && gapColumn != null) {
            formLayout.removeColumn(baseColumn + 1);
        }
        for(int c = 0; c < columnSpecs.length; c++) {
            formLayout.removeColumn(baseColumn + 1);
        }
    }


    /**
     * @return the row that the preceding gap for this row should be shown on,
     *      or the row that the element rows should be on if this is the first row.
     */
    private int logicalToLayoutRow(int row) {
        if(row == 0) return 0;
        else return (row * rowSpecs.length) + (gapRow == null ? 0 : row - 1);
    }

    /**
     * @return the column that the preceding gap for this column should be shown on,
     *      or the column that the element columns should be on if this is the first column.
     */
    private int logicalToLayoutColumn(int column) {
        if(column == 0) return 0;
        else return (column * columnSpecs.length) + (gapColumn == null ? 0 : column - 1);
    }

    /**
     * Handle a list element being inserted by adjusting the layout.
     */
    public void insertIndex(int index) {
        int logicalColumnCountBefore = logicalColumnCount();
        int logicalRowCountBefore = logicalRowCount();

        gridComponents.add(new CellComponents());

        fixCellCount(logicalRowCountBefore, logicalColumnCountBefore);
    }

    /**
     * Handle a list element being removed by adjusting the layout.
     */
    public void removeIndex(int index) {
        int logicalColumnCountBefore = logicalColumnCount();
        int logicalRowCountBefore = logicalRowCount();

        gridComponents.remove(index);

        fixCellCount(logicalRowCountBefore, logicalColumnCountBefore);
    }
    private void reassignConstraints() {
        formLayout.invalidateLayout(container);

        for(int i = 0; i < gridComponents.size(); i++) {
            CellComponents cellComponents = gridComponents.get(i);
            for(int j = 0; j < cellComponents.components.size(); j++) {
                Component component = cellComponents.components.get(j);
                Constraints constraints = cellComponents.constraints.get(j);
                formLayout.addLayoutComponent(component, deriveCellConstraints(constraints.getFormConstraints(), logicalColumn(i), logicalRow(i)));
            }
        }
    }

    /**
     * Count the cells in the grid.
     */
    public int size() {
        return gridComponents.size();
    }

    /**
     * Insert the specified row into the layout. This accomodates
     * for the appendColumn/insertColumn API weakness in FormLayout.
     */
    private void insertRow(int index, RowSpec rowSpec) {
        if(index == formLayout.getRowCount()) formLayout.appendRow(rowSpec);
        else formLayout.insertRow(index + 1, rowSpec);
    }

    /**
     * Insert the specified column into the layout. This accomodates
     * for the appendRow/insertRow API weakness in FormLayout.
     */
    private void insertColumn(int index, ColumnSpec columnSpec) {
        if(index == formLayout.getColumnCount()) formLayout.appendColumn(columnSpec);
        else formLayout.insertColumn(index + 1, columnSpec);
    }

    /**
     * The components for a single cell.
     */
    private static class CellComponents {
        private List<Component> components = new ArrayList<Component>(1);
        private List<Constraints> constraints = new ArrayList<Constraints>(1);
    }

    /**
     * Map a {@link Component} to a list element and a location in the container.
     */
    public static class Constraints {
        private int formConstraints;
        private int index;

        public Constraints(int component, int index) {
            this.formConstraints = component;
            this.index = index;
        }

        /**
         * The form constraints specifies which {@link CellConstraints}
         * in the element cell belongs to this {@link Component}.
         */
        public int getFormConstraints() {
            return formConstraints;
        }

        /**
         * The initial index of the element, it will become stale as other
         * elements are added before it. It's only used the first time a
         * component is used to find the appropriate {@link CellComponents}
         * object.
         */
        public int getIndex() {
            return index;
        }
    }
}

