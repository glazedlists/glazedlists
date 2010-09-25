/* Glazed Lists                                                 (c) 2003-2010 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.Preconditions;
import ca.odell.glazedlists.impl.gui.SortingStrategy;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

/**
 * <b>Developer Preview:</b>
 * <code>JXTableSupport</code> prototype, currently supporting SwingX 1.0.x
 * <p>
 * Integrates a {@link JXTable} with Glazed Lists by
 * <ul>
 * <li>installing a {@link DefaultEventTableModel} as table model</li>
 * <li>installing a {@link DefaultEventSelectionModel} as table selection model</li>
 * <li>installing a {@link TableComparatorChooser} as sorting integration approach</li>
 * </ul>
 *
  * <p><strong><font color="#FF0000">Warning:</font></strong> This class must be
 * mutated from the Swing Event Dispatch Thread. Failure to do so will result in
 * an {@link IllegalStateException} thrown from any one of:
 *
 * <ul>
 *   <li> {@link #install(JXTable, EventList, TableFormat, SortedList, Object)}
 *   <li> {@link #uninstall()}
 * </ul>
*
 * @author Holger Brands
 */
public class JXTableSupport<E> {

    /** The JXTable to integrate with. */
    private JXTable table;

    /** The state of JXTable before install; needed for uninstall to restore state. */
    private TableModel oldTableModel;
    private ListSelectionModel oldSelectionModel;
    private boolean oldSortable;
    private TableCellRenderer oldDefaultRenderer;
    private boolean oldSelectionMapperEnabled;

    /** the installed event table model. */
    private AdvancedTableModel<E> tableModel;
    /** the installed event selection model. */
    private AdvancedListSelectionModel<E> selectionModel;
    /** the installed TableFormat. */
    private TableFormat<? super E> tableFormat;
    /** the installed TableComparatorChooser. */
    private TableComparatorChooser<E> tableComparatorChooser;

    /**
     * Installs the Glazed Lists integration on the given <code>table</code>.
     * <p>First, the current state of the table is determined to be able to restore this state
     * when the integration is uninstalled again.
     *
     * Then this method installs
     * <ul>
     * <li>a {@link DefaultEventTableModel} as table model</li>
     * <li>a {@link DefaultEventSelectionModel} as table selection model</li>
     * <li>a {@link TableComparatorChooser} as sorting integration approach</li>
     * </ul>
     * The effect of this is, that Glazed Lists takes full control of sorting (and filtering)
     * functionality and disables JXTable's sorting support.
     * </p>
     *
     * @param <E> element type of {@link EventList}
     * @param table the JXTable to integrate with
     * @param eventList the {@link EventList} with data for the table model
     * @param tableFormat the {@link TableFormat} to use
     * @param sortedList the {@link SortedList} to use with the {@link TableComparatorChooser}
     * @param sortingStrategy the {@link SortingStrategy} to use with the
     *            {@link TableComparatorChooser}
     * @return the JXTableSupport instance to use for uninstallation of GLazed Lists integration
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public static <E> JXTableSupport<E> install(JXTable table, EventList<E> eventList,
            TableFormat<? super E> tableFormat, SortedList<E> sortedList, Object sortingStrategy) {
        Preconditions.checkNotNull(table, "JXTable must be defined.");
        Preconditions.checkNotNull(eventList, "EventList must be defined.");
        Preconditions.checkNotNull(tableFormat, "TableFormat must be defined.");
        Preconditions.checkNotNull(sortedList, "SortedList must be defined.");
        Preconditions.checkNotNull(sortingStrategy, "SortingStrategy must be defined.");
        checkAccessThread();
        return new JXTableSupport<E>(table, eventList, tableFormat, sortedList, sortingStrategy);
    }

    /**
     * @return the {@link JXTable} or <code>null</code> if JXTableSupport has already been uninstalled
     */
    public JXTable getTable() {
        return table;
    }

    /**
     * @return the installed {@link AdvancedListSelectionModel} or <code>null</code> if already uninstalled
     */
    public AdvancedListSelectionModel<E> getTableSelectionModel() {
        return selectionModel;
    }

    /**
     * @return the installed {@link AdvancedTableModel} or <code>null</code> if already uninstalled
     */
    public AdvancedTableModel<E> getTableModel() {
        return tableModel;
    }

    /**
     * @return the installed {@link TableFormat} or <code>null</code> if already uninstalled
     */
    public TableFormat<? super E> getTableFormat() {
        return tableFormat;
    }

    /**
     * @return the installed {@link TableComparatorChooser} or <code>null</code> if already uninstalled
     */
    public TableComparatorChooser<E> getTableComparatorChooser() {
        return tableComparatorChooser;
    }

    /**
     * Uninstalls the GlazedLists integration and restores the state of JXTable as it was before
     * the installation.
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public void uninstall() {
        checkAccessThread();
        tableComparatorChooser.dispose();
        // restore table state before install
        table.setModel(oldTableModel);
        table.setSelectionModel(oldSelectionModel);
        table.getSelectionMapper().setEnabled(oldSelectionMapperEnabled);
        table.setSortable(oldSortable);
        table.getTableHeader().setDefaultRenderer(oldDefaultRenderer);
        tableModel.dispose();
        selectionModel.dispose();
        tableComparatorChooser = null;
        tableModel = null;
        selectionModel = null;
        table = null;
    }

    /**
     * @return  <tt>true</tt> if this {@link JXTableSupport} instance is currently
     * installed and altering the behaviour of the JXTable; <tt>false</tt> if
     * it has been {@link #uninstall}ed.
     */
    public boolean isInstalled() {
        return table != null;
    }

    /** Installs the GlazedLists integration on the given JXTable. */
    private JXTableSupport(JXTable table, EventList<E> eventList,
            TableFormat<? super E> tableFormat, SortedList<E> sortedList, Object sortingStrategy) {
        this.table = table;
        this.tableFormat = tableFormat;
        // remember table state for restore on uninstall
        this.oldTableModel = table.getModel();
        this.oldSelectionModel = table.getSelectionModel();
        oldSortable = table.isSortable();
        oldDefaultRenderer = table.getTableHeader().getDefaultRenderer();
        oldSelectionMapperEnabled = table.getSelectionMapper().isEnabled();

        table.getSelectionMapper().setEnabled(false);
        table.setSortable(false);
        table.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());

        tableModel = GlazedListsSwing.eventTableModelWithThreadProxyList(eventList, tableFormat);
        table.setModel(tableModel);
        selectionModel = GlazedListsSwing.eventSelectionModelWithThreadProxyList(eventList);
        table.setSelectionModel(selectionModel);
        tableComparatorChooser = TableComparatorChooser.<E>install(table, sortedList,
                sortingStrategy, tableFormat);
    }

    /**
     * A convenience method to ensure {@link JXTableSupportTest} is being accessed from the Event
     * Dispatch Thread.
     */
    private static void checkAccessThread() {
        if (!SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException(
                    "JXTableSupport must be accessed from the Swing Event Dispatch Thread, but was called on Thread \""
                            + Thread.currentThread().getName() + "\"");
    }
}
