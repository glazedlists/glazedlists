/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.Preconditions;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

/**
 * <b>work in progress</b>
 *
 * <code>JXTableSupport</code> prototype.
 * <p> Integrates a {@link JXTable} with Glazed Lists by
 * <ul>
 * <li>installing a {@link DefaultEventTableModel} as table model</li>
 * <li>installing a {@link DefaultEventSelectionModel} as table selection model</li>
 * <li>installing a {@link TableComparatorChooser} as sorting integration approach</li>
 * </ul>
 *
 * @author Holger Brands
 */
public class JXTableSupport<E> {

    private JXTable table;

    private TableModel oldTableModel;

    private AdvancedTableModel tableModel;

    private ListSelectionModel oldSelectionModel;

    private AdvancedListSelectionModel selectionModel;

    private TableFormat<? super E> tableFormat;

    private boolean sortable;

    private TableCellRenderer defaultRenderer;

    private boolean selectionMapperEnabled;

    private TableComparatorChooser<E> tableComparatorChooser;

    public static <E> JXTableSupport<E> install(JXTable table, EventList<E> eventList, TableFormat<E> tableFormat, SortedList<E> sortedList, Object sortingStrategy) {
        Preconditions.checkNotNull(table, "JXTable must be defined.");
        Preconditions.checkNotNull(eventList, "EventList must be defined.");
        Preconditions.checkNotNull(tableFormat, "TableFormat must be defined.");
        Preconditions.checkNotNull(sortedList, "SortedList must be defined.");
        Preconditions.checkNotNull(sortingStrategy, "SortingStrategy must be defined.");
        checkAccessThread();
        return new JXTableSupport<E>(table, eventList, tableFormat, sortedList, sortingStrategy);
    }

    public AdvancedListSelectionModel<E> getTableSelectionModel() {
        return (AdvancedListSelectionModel<E>) table.getSelectionModel();
    }

    public AdvancedTableModel<E> getTableModel() {
        return (AdvancedTableModel<E>) table.getModel();
    }

    public TableFormat<? super E> getTableFormat() {
        return tableFormat;
    }

    public TableComparatorChooser<E> getTableComparatorChooser() {
        return tableComparatorChooser;
    }

    public void uninstall() {
        checkAccessThread();
        tableComparatorChooser.dispose();
        // restore table state before install
        table.setModel(oldTableModel);
        table.setSelectionModel(oldSelectionModel);
        table.getSelectionMapper().setEnabled(selectionMapperEnabled);
        table.setSortable(sortable);
        table.getTableHeader().setDefaultRenderer(defaultRenderer);
        tableModel.dispose();
        selectionModel.dispose();
    }

    private JXTableSupport(JXTable table, EventList<E> eventList, TableFormat<? super E> tableFormat, SortedList<E> sortedList, Object sortingStrategy) {
        this.table = table;
        this.tableFormat = tableFormat;
        // remember table state for restore on uninstall
        this.oldTableModel = table.getModel();
        this.oldSelectionModel = table.getSelectionModel();
        sortable = table.isSortable();
        defaultRenderer = table.getTableHeader().getDefaultRenderer();
        selectionMapperEnabled = table.getSelectionMapper().isEnabled();

        table.getSelectionMapper().setEnabled(false);
        table.setSortable(false);
        table.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());

        tableModel = GlazedListsSwing.eventTableModelWithThreadProxyList(eventList, tableFormat);
        table.setModel(tableModel);
        selectionModel = GlazedListsSwing.eventSelectionModelWithThreadProxyList(eventList);
        table.setSelectionModel(selectionModel);

        // indicate the dependency between the TableModel & the SelectionModel's ListSelection
        // (this is crucial because it ensures the ListEventPublisher delivers events to the SelectionModel
        // *before* the TableModel, which is the correct relative order of notification)
        // @todo enable again
//        eventList.getPublisher().setRelatedListener(model, selectionModel.getListSelection());
        tableComparatorChooser = TableComparatorChooser.<E>install(table, sortedList, sortingStrategy, tableFormat);
    }

    /**
     * A convenience method to ensure {@link JXTableSupportTest} is being
     * accessed from the Event Dispatch Thread.
     */
    private static void checkAccessThread() {
        if (!SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("JXTableSupport must be accessed from the Swing Event Dispatch Thread, but was called on Thread \"" + Thread.currentThread().getName() + "\"");
    }

}
