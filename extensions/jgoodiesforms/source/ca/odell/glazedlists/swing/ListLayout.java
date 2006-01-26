/* Glazed Lists                                                 (c) 2003-2005 */
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
import java.util.Iterator;

/**
 *
 * Layout list elements.
 *
 * Constraints - relative constraints: indices as Integers
 * What about subconstraints?
 *
 * INTEGER
 * CELL CONSTRAINTS
 *
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListLayout extends LayoutDecorator {

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

    private static final int COLUMNS = 1;

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

    private CellConstraints deriveCellConstraints(int component, int logicalColumn, int logicalRow) {
        CellConstraints constraints = format.getConstraints(component);
        constraints = (CellConstraints)constraints.clone();
        constraints.gridX += logicalToLayoutColumn(logicalColumn) + (logicalColumn > 0 && gapColumn != null ? 1 : 0);
        constraints.gridY += logicalToLayoutRow(logicalRow) + (logicalRow > 0 && gapRow != null ? 1 : 0);
        return constraints;
    }

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
        return index % COLUMNS;
    }
    private int logicalRow(int index) {
        return index / COLUMNS;
    }
    private int logicalRowCount() {
        return (gridComponents.size() + COLUMNS - 1) / COLUMNS;
    }
    private int logicalColumnCount() {
        return Math.min(gridComponents.size(), COLUMNS);
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

    public void insertIndex(int index) {
        int logicalColumnCountBefore = logicalColumnCount();
        int logicalRowCountBefore = logicalRowCount();

        gridComponents.add(new CellComponents());

        int logicalColumnCountAfter = logicalColumnCount();
        int logicalRowCountAfter = logicalRowCount();

        // make sure we have enough cells
        if(logicalRowCountAfter > logicalRowCountBefore) {
            insertLogicalRow(logicalRowCountAfter - 1);
        }
        if(logicalColumnCountAfter > logicalColumnCountBefore) {
            insertLogicalColumn(logicalColumnCountAfter - 1);
        }

        reassignConstraints();
    }
    public void removeIndex(int index) {
        int logicalColumnCountBefore = logicalColumnCount();
        int logicalRowCountBefore = logicalRowCount();

        gridComponents.remove(index);
        reassignConstraints();

        int logicalColumnCountAfter = logicalColumnCount();
        int logicalRowCountAfter = logicalRowCount();

        // make sure we don't have too many cells
        if(logicalRowCountAfter < logicalRowCountBefore) {
            removeLogicalRow(logicalRowCountAfter);
        }
        if(logicalColumnCountAfter < logicalColumnCountBefore) {
            removeLogicalColumn(logicalColumnCountAfter);
        }
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

    public static class Constraints {
        private int formConstraints;
        private int index;

        public Constraints(int component, int index) {
            this.formConstraints = component;
            this.index = index;
        }

        public int getFormConstraints() {
            return formConstraints;
        }

        public int getIndex() {
            return index;
        }
    }
}

abstract class LayoutDecorator implements LayoutManager2 {
    protected LayoutManager2 delegateLayout;

    public void addLayoutComponent(Component component, Object constraints) {
        delegateLayout.addLayoutComponent(component, constraints);
    }

    public Dimension maximumLayoutSize(Container target) {
        return delegateLayout.maximumLayoutSize(target);
    }

    public float getLayoutAlignmentX(Container target) {
        return delegateLayout.getLayoutAlignmentX(target);
    }

    public float getLayoutAlignmentY(Container target) {
        return delegateLayout.getLayoutAlignmentY(target);
    }

    public void invalidateLayout(Container target) {
        delegateLayout.invalidateLayout(target);
    }

    public void addLayoutComponent(String name, Component component) {
        throw new UnsupportedOperationException();
    }

    public void removeLayoutComponent(Component component) {
        delegateLayout.removeLayoutComponent(component);
    }

    public Dimension preferredLayoutSize(Container container) {
        return delegateLayout.preferredLayoutSize(container);
    }

    public Dimension minimumLayoutSize(Container container) {
        return delegateLayout.minimumLayoutSize(container);
    }

    public void layoutContainer(Container container) {
        delegateLayout.layoutContainer(container);
    }
}
