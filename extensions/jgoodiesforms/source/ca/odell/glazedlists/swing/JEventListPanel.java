/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
 * <p>To use {@link JEventListPanel}:
 * <ol>
 *   <li>Create an {@link EventList} of {@link JComponent}s, or an {@link EventList}
 *       of objects which each reference a set of {@link JComponent}s.
 *   <li>Implement {@link JEventListPanel.Format} for the object's in your
 *       {@link EventList}. This interface defines how a single cell is layed out.
 *       Once the layout for a single cell is known, all cells can be tiled to
 *       show all of the {@link JComponent}s in your {@link EventList}.
 *   <li>Create an {@link JEventListPanel} and add it in your application somewhere.
 *       If the number of elements in the {@link EventList} may grow unbounded,
 *       consider wrapping {@link JEventListPanel} in a {@link JScrollPane}.
 * </ol>
 *
 * @author <a href="mailto:jesse@odell.ca">Jesse Wilson</a>
 */
public class JEventListPanel<E> extends JPanel implements ListEventListener {

    /** the source contains all the JComponents */
    private TransformedList<E,E> swingSource;

    /** the components of the panel */
    private List<JComponent[]> components = new ArrayList<JComponent[]>();

    private final ListLayout listLayout;
    private final Format format;

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
            insert(i);
        }

        // listen for changes to the source
        swingSource.addListEventListener(this);
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
        JComponent[] elementComponents = (JComponent[])components.get(index);
        for(int c = 0; c < elementComponents.length; c++) {
            if(elementComponents[c] == null) continue;
            remove(elementComponents[c]);
        }

        // forget the components
        components.remove(index);

        listLayout.removeIndex(index);
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
        private final RowSpec[] rowSpecs;
        private final ColumnSpec[] columnSpecs;
        private final RowSpec gapRow;
        private final ColumnSpec gapColumn;
        private final CellConstraints[] cellConstraints;

        protected AbstractFormat(RowSpec[] rowSpecs, ColumnSpec[] columnSpecs, RowSpec gapRow, ColumnSpec gapColumn, CellConstraints[] cellConstraints) {
            this.rowSpecs = rowSpecs;
            this.columnSpecs = columnSpecs;
            this.gapRow = gapRow;
            this.gapColumn = gapColumn;
            this.cellConstraints = cellConstraints;
        }

        public AbstractFormat(String rowSpecs, String columnSpecs, String gapRow, String gapColumn, CellConstraints[] cellConstraints) {
            this(RowSpec.decodeSpecs(rowSpecs), ColumnSpec.decodeSpecs(columnSpecs), new RowSpec(gapRow), new ColumnSpec(gapColumn), cellConstraints);
        }

        public AbstractFormat(String rowSpecs, String columnSpecs, String gapRow, String gapColumn, String[] cellConstraints) {
            this(RowSpec.decodeSpecs(rowSpecs), ColumnSpec.decodeSpecs(columnSpecs), gapRow == null ? null : new RowSpec(gapRow), gapColumn == null ? null : new ColumnSpec(gapColumn), decode(cellConstraints));
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

        public BeanFormat(Class<E> beanClass, RowSpec[] rowSpecs, ColumnSpec[] columnSpecs, RowSpec gapRow, ColumnSpec gapColumn, CellConstraints[] cellConstraints, String[] properties) {
            super(rowSpecs, columnSpecs, gapRow, gapColumn, cellConstraints);
            this.properties = new BeanProperty[properties.length];
            for(int p = 0; p < properties.length; p++) {
                this.properties[p] = new BeanProperty<E>(beanClass, properties[p], true, false);
            }
        }
        public BeanFormat(Class<E> beanClass, String rowSpecs, String columnSpecs, String gapRow, String gapColumn, CellConstraints[] cellConstraints, String[] properties) {
            this(beanClass, RowSpec.decodeSpecs(rowSpecs), ColumnSpec.decodeSpecs(columnSpecs), gapRow == null ? null : new RowSpec(gapRow), gapColumn == null ? null : new ColumnSpec(gapColumn), cellConstraints, properties);
        }
        public BeanFormat(Class<E> beanClass, String rowSpecs, String columnSpecs, String gapRow, String gapColumn, String[] cellConstraints, String[] properties) {
            this(beanClass, RowSpec.decodeSpecs(rowSpecs), ColumnSpec.decodeSpecs(columnSpecs), gapRow == null ? null : new RowSpec(gapRow), gapColumn == null ? null : new ColumnSpec(gapColumn), decode(cellConstraints), properties);
        }

        /** {@inheritDoc} */
        public JComponent getComponent(E element, int component) {
            return (JComponent)properties[component].get(element);
        }
    }
}