/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ListSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * Provides two {@link EventList}s that represent the selected and deselected
 * items in a {@link org.eclipse.swt.widgets.List} or
 * {@link org.eclipse.swt.widgets.Table} respectively.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
class SelectionManager<E> {

    /** the model to the Selectable widget's selection */
    private ListSelection<E> selection;

    /** the Selectable SWT Viewer */
    private Selectable selectable;

    /** the selection listener */
    private SelectionListener selectionListener;

    /** to prevent extra processing of selection */
    private boolean selectionInProgress = false;

    /**
     * Creates a {@link ListSelection} that provides a view of the selected
     * items in an SWT widget.  Deselected items exist in a view accessible
     * via the {@link ListSelection#getDeselected()} method.
     */
    SelectionManager(EventList<E> source, Selectable selectable) {
        selection = new ListSelection<E>(source);
        this.selectable = selectable;
        selection.addSelectionListener(new SelectionListListener());

        // Using SINGLE_SELECTION selection mode
        if((selectable.getStyle() & SWT.SINGLE) == SWT.SINGLE) {
            selectionListener = new SingleLineSelectionListener();
            selection.setSelectionMode(ListSelection.SINGLE_SELECTION);

        // Using MULTIPLE_INTERVAL_SELECTION_DEFENSIVE selection mode
        } else if((selectable.getStyle() & SWT.MULTI) == SWT.MULTI) {
            selectionListener = new MultiLineSelectionListener();
            selection.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE);
        }
        selectable.addSelectionListener(selectionListener);
    }

    /**
     * Provides access to the {@link ListSelection} that allows manipulation
     * of selection on the {@link Selectable} widget and access to
     * selection-driven {@link EventList}s.
     */
    public ListSelection<E> getSelectionList() {
        return selection;
    }

    /**
     * Listens to selection changes on the {@link ListSelection} and updates
     * the selection on the {@link Selectable} widget.
     */
    private final class SelectionListListener implements ListSelection.Listener {

        /** {@inheritDoc} */
        @Override
        public void selectionChanged(int changeStart, int changeEnd) {
            // don't repaint selection on the Selectable widget if the
            // widget already knows about the selection change
            if(!selectionInProgress) {
                selectionInProgress = true;
                fireSelectionChanged(changeStart, changeEnd);
                selectionInProgress = false;
            }
        }
    }

    /**
     * Resync's the selection between the model in SelectionList and the
     * Selectable widget for the provided INCLUSIVE range.
     */
    void fireSelectionChanged(int start, int end) {
        // fast fail on a no-op
        if(start == -1) return;

        // Reapply selection to the Selectable widget
        for(int i = start;i <= end;i++) {
            if(selection.isSelected(i)) {
                selectable.select(i);
            } else {
                selectable.deselect(i);
            }
        }
    }

    /**
     * Disposes of this to allow it to be garbage collected.
     */
    public void dispose() {
        selectable.removeSelectionListener(selectionListener);
        selection.dispose();
    }

    /**
     * This handles selection changes in Selectable widgets that are created
     * with the {@link SWT#SINGLE} selection flag.  This is the simple base
     * case where at most one item is ever selected.
     */
    private final class SingleLineSelectionListener implements SelectionListener {

        /**
         * Responds to a double-click selection.
         */
        @Override
        public void widgetDefaultSelected(SelectionEvent event) {
            widgetSelected(event);
        }

        /**
         * Responds to a single click or arrow key based selection.
         */
        @Override
        public void widgetSelected(SelectionEvent event) {
            // don't set selection on the SelectionList if it has already been set
            if(!selectionInProgress) {
                selectionInProgress = true;
                selection.getSource().getReadWriteLock().writeLock().lock();
                try {
                    selection.setSelection(selectable.getSelectionIndex());
                } finally {
                    selectionInProgress = false;
                    selection.getSource().getReadWriteLock().writeLock().unlock();
                }
            }
        }
    }

    /**
     * This Strategy handles selection changes in {@link Selectable} widgets
     * that are created with the {@link SWT#MULTI} selection flag.
     */
    private final class MultiLineSelectionListener implements SelectionListener {

        /**
         * Responds to a double-click selection.
         */
        @Override
        public void widgetDefaultSelected(SelectionEvent event) {
            widgetSelected(event);
        }

        /**
         * Responds to a single click or arrow key based selection.
         */
        @Override
        public void widgetSelected(SelectionEvent event) {
            // don't set selection on the SelectionList if it has already been set
            if(!selectionInProgress) {
                selectionInProgress = true;
                selection.getSource().getReadWriteLock().writeLock().lock();
                try {
                    selection.setSelection(selectable.getSelectionIndices());
                } finally {
                    selectionInProgress = false;
                    selection.getSource().getReadWriteLock().writeLock().unlock();
                }
            }
        }
    }
}