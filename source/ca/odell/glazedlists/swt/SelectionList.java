/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// core glazed lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// access to the volatile implementation classes
import ca.odell.glazedlists.impl.adt.*;
// to act as a SelectionListener to a Selectable SWT Viewer
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
// to determine which form of selection is in use
import org.eclipse.swt.SWT;
// to store event info for forwarding on the deselected EventList
import java.util.ArrayList;

/**
 * Provides two {@link EventList}s that represent the selected and deselected
 * items in a {@link List} or {@link Table} respectively.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
class SelectionList extends TransformedList {

    /** to determine if this list is already disposed by the user */
    private boolean disposed = false;

    /** the mirror to this view of selection */
    private DeselectedList deselectedList = null;

    /** the Selectable SWT Viewer */
    private Selectable selectable = null;

    /** the selection state */
    private Barcode barcode = null;

    /** the Strategy to handle changes in selection */
    private SelectionChangeStrategy selectionChangeStrategy = null;

    /** the selection listener */
    private AdvancedSelectionListener selectionListener = null;

    /** to allow selection inversion without changing the barcode*/
    private Object selected = Barcode.BLACK;
    private Object deselected = Barcode.WHITE;

    /**
     * Creates a {@link SelectionList} that provides a view of the selected
     * items in an SWT widget.  Deselected items exist in a view accessible
     * via the {@link SelectionList#getDeselected()} method.
     */
    SelectionList(EventList source, Selectable selectable) {
        super(source);
        deselectedList = new DeselectedList(source);
        this.selectable = selectable;
        barcode = new Barcode();
        source.addListEventListener(this);
        selectionListener = new AdvancedSelectionListener();
        selectable.addSelectionListener(selectionListener);
        if((selectable.getStyle() & SWT.SINGLE) == SWT.SINGLE) {
            selectionChangeStrategy = new SingleLineSelectionChangeStrategy();
        } else if((selectable.getStyle() & SWT.MULTI) == SWT.MULTI) {
            selectable.addListener(SWT.KeyDown, selectionListener);
            selectable.addListener(SWT.KeyUp, selectionListener);
            selectionChangeStrategy = new MultiLineSelectionChangeStrategy();
        }
    }

    /** {@inheritDoc} */
    public int size() {
        return barcode.colourSize(selected);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // handle reorder events
        if(listChanges.isReordering()) {
            // prepare a reorder map for both of the EventLists
            int[] sourceReorderMap = listChanges.getReorderMap();
            int[] selectReorderMap = new int[barcode.colourSize(selected)];
            int[] deselectReorderMap = new int[barcode.colourSize(deselected)];

            // adjust the barcode & build the reorder maps to forward
            Barcode oldBarcode = barcode;
            barcode = new Barcode();
            for(int i = 0; i < sourceReorderMap.length; i++) {
                Object flag = oldBarcode.get(sourceReorderMap[i]);
                boolean wasSelected = (flag != deselected);
                barcode.add(i, flag, 1);

                // reordering a selected value
                if(wasSelected) {
                    int previousIndex = oldBarcode.getColourIndex(sourceReorderMap[i], selected);
                    int currentIndex = barcode.getColourIndex(i, selected);
                    selectReorderMap[currentIndex] = previousIndex;

                // reordering a deselected value
                } else {
                    int previousIndex = oldBarcode.getColourIndex(sourceReorderMap[i], deselected);
                    int currentIndex = barcode.getColourIndex(i, deselected);
                    deselectReorderMap[currentIndex] = previousIndex;
                }
            }

            // fire the reorder for selected values
            updates.beginEvent();
            updates.reorder(selectReorderMap);
            updates.commitEvent();

            // fire the reorder for deselected values
            deselectedList.updates().beginEvent();
            deselectedList.updates().reorder(deselectReorderMap);
            deselectedList.updates().commitEvent();

            return;
        }

        // Keep track of deselected changes as you go
        ArrayList deselectedDeletes = new ArrayList();
        ArrayList deselectedInserts = new ArrayList();
        ArrayList deselectedUpdates = new ArrayList();
        updates.beginEvent();

        // handle deletes and updates for selected values
        while(listChanges.next()) {

            // get the current change info
            int index = listChanges.getIndex();
            int changeType = listChanges.getType();

            // process a DELETE event
            if(changeType == ListEvent.DELETE) {
                // only delete selected values
                int blackIndex = barcode.getColourIndex(index, selected);
                if(blackIndex != -1) {
                    barcode.remove(index, 1);
                    updates.addDelete(blackIndex);
                } else {
                    int whiteIndex = barcode.getColourIndex(index, deselected);
                    deselectedDeletes.add(new Integer(whiteIndex));
                    barcode.remove(index, 1);
                }

            // process an UPDATE event
            } else if(changeType == ListEvent.UPDATE) {
                int blackIndex = barcode.getColourIndex(index, selected);
                if(blackIndex != -1) {
                    updates.addUpdate(blackIndex);
                } else {
                    int whiteIndex = barcode.getColourIndex(index, deselected);
                    deselectedUpdates.add(new Integer(whiteIndex));
                }

            // process an INSERT event
            } else if(changeType == ListEvent.INSERT) {
                barcode.add(index, deselected, 1);
                int whiteIndex = barcode.getColourIndex(index, deselected);
                deselectedInserts.add(new Integer(whiteIndex));
            }
        }
        updates.commitEvent();

        // Process the deselected values
        deselectedList.updates().beginEvent();

        // process all deletes of deselected values
        for(int i = 0;i < deselectedDeletes.size();i++) {
            int whiteIndex = ((Integer)deselectedDeletes.get(i)).intValue();
            deselectedList.updates().addDelete(whiteIndex);
        }
        deselectedDeletes.clear();

        // process all inserts which are implicitly deselected
        for(int i = 0;i < deselectedInserts.size();i++) {
            int whiteIndex = ((Integer)deselectedInserts.get(i)).intValue();
            deselectedList.updates().addInsert(whiteIndex);
        }
        deselectedInserts.clear();

        // process all updates of deselected values
        for(int i = 0;i < deselectedUpdates.size();i++) {
            int whiteIndex = ((Integer)deselectedUpdates.get(i)).intValue();
            deselectedList.updates().addUpdate(whiteIndex);
        }
        deselectedUpdates.clear();
        deselectedList.updates().commitEvent();
    }

    /**
     * Provides access to the {@link EventList} that contains only items
     * that are NOT currently selected in the widget.
     */
    public EventList getDeselected() {
        return deselectedList;
    }

    /**
     * Provides access to the {@link EventList} that contains only items
     * that are currently selected in the widget.  The {@link EventList} that
     * is returned is equivalent to accessing the {@link SelectionList}
     * directly.  This method was provided for symmetry.
     */
    public EventList getSelected() {
        return this;
    }

    /**
     * Returns whether or not the item with the given source index
     * is selected.
     */
    public boolean isSelected(int sourceIndex) {
        return barcode.getColourIndex(sourceIndex, selected) != -1;
    }

    /**
     * Inverts the current selection.
     */
    public void invertSelection() {
		// Switch what colour is considered 'selected' in the barcode
		if(selected == Barcode.BLACK) {
			selected = Barcode.WHITE;
			deselected = Barcode.BLACK;
		} else {
			selected = Barcode.BLACK;
			deselected = Barcode.WHITE;
		}

		// Inspect the new selection
		int[] newSelection = new int[barcode.colourSize(selected)];
		for(int i = 0;i < newSelection.length;i++) {
			newSelection[i] = barcode.getIndex(i, selected);
		}

		// Invert selection in the Selectable widget
		//selectable.deselectAll();
		//selectable.select(newSelection);

		// Update the selected list to reflect the selection inversion
		updates.beginEvent();
		updates.addDelete(0, barcode.colourSize(deselected));
		updates.addInsert(0, barcode.colourSize(selected));
		updates.commitEvent();

		// Update the deselected list to reflect the selection inversion
		deselectedList.updates().beginEvent();
		deselectedList.updates().addDelete(0, barcode.colourSize(selected));
		deselectedList.updates().addInsert(0, barcode.colourSize(deselected));
		deselectedList.updates().commitEvent();
	}

    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        return barcode.getIndex(mutationIndex, selected);
    }

    /** {@inheritDoc} */
    public void dispose() {
        selectable.removeSelectionListener(selectionListener);
        if((selectable.getStyle() & SWT.MULTI) == SWT.MULTI) {
            selectable.removeListener(SWT.KeyUp, selectionListener);
            selectable.removeListener(SWT.KeyDown, selectionListener);
        }
        super.dispose();
        disposed = true;
    }

    /**
     * This method exists to guarantee that SelectionLists get disposed
     * by the Viewer they depend on if they are unused.
     */
    boolean isDisposed() {
        return disposed;
    }

    /**
     * The {@link EventList} that contains only items that are not currently
     * selected.
     */
    private class DeselectedList extends TransformedList {

        /**
         * Creates an {@link EventList} that provides a view of the
         * deselected items in an SWT Widget.
         */
        DeselectedList(EventList source) {
            super(source);
        }

        /** {@inheritDoc} */
        public int size() {
            return barcode.colourSize(deselected);
        }

        /** {@inheritDoc} */
        protected int getSourceIndex(int mutationIndex) {
            return barcode.getIndex(mutationIndex, deselected);
        }

        /** {@inheritDoc} */
        public void listChanged(ListEvent listChanges) {
            // Do nothing as all state changes are handled in SelectionList.listChanged()
        }

        /**
         * This allows access to the EventAssembler for this list.
         */
        public ListEventAssembler updates() {
            return updates;
        }

        /**
         * A no-op dispose method to prevent the user from shooting themselves
         * in the foot.
         */
        public void dispose() {
            // Do Nothing
        }
    }

    /**
     * This {@link SelectionListener} fills in the stateMask of
     * {@link SelectionEvent}s which is left empty on item selection within
     * {@link Tables}. The stateMask is documented to hold the value of
     * modifier keys.  However, this value is also documented to be not in use
     * for some widgets, and oddly, {@link Table} is one of those widgets.
     */
    private class AdvancedSelectionListener implements Listener, SelectionListener {

        /** the selection modifier key, either NONE, CTRL, or SHIFT */
        private int currentKeyModifier = SWT.NONE;

        /**
         * Handles {@link SWT#KeyUp} and {@link SWT#KeyDown} events.  Either
         * none or both of these {@link Listener}s must be registered.
         */
        public void handleEvent(Event event) {
            // handle KeyDown events
            if(event.type == SWT.KeyDown) {
                if(event.keyCode == SWT.CTRL) currentKeyModifier = SWT.CTRL;
                else if(event.keyCode == SWT.SHIFT) currentKeyModifier = SWT.SHIFT;

            // handle KeyUp events
            } else if(event.keyCode == SWT.CTRL || event.keyCode == SWT.SHIFT) {
                currentKeyModifier = SWT.NONE;
            }
        }

        /**
         * Responds to a single click or arrow key based selection.
         */
        public void widgetSelected(SelectionEvent event) {
            event.stateMask = currentKeyModifier;
            selectionChangeStrategy.handleSelectionChanged(event);
        }

        /**
         * Responds to a double-click selection.
         */
        public void widgetDefaultSelected(SelectionEvent event) {
            event.stateMask = currentKeyModifier;
            selectionChangeStrategy.handleSelectionChanged(event);
        }
    }

    /**
     * This Strategy is applied in response to a {@link SelectionEvent} to
     * process changes in selection.
     */
    private interface SelectionChangeStrategy {

        /**
         * Process how selection has changed and forward events as necessary.
         */
        public void handleSelectionChanged(SelectionEvent event);
    }

    /**
     * This Strategy handles selection changes in Selectable widgets
     * that are created with the {@link SWT#SWT.SINGLE} selection flag.  This
     * is the simple base case where at most one item is ever selected.  The
     * value of this pair of {@link EventList}s isn't really demonstrated in
     * this mode.
     */
    private class SingleLineSelectionChangeStrategy implements SelectionChangeStrategy {

        /** The last selected line */
        private int lastSelected = -1;

        /**
         * Only one line is ever selected (or deselected) in {@link SWT#SINGLE}
         * selection mode.
         */
        public void handleSelectionChanged(SelectionEvent event) {
            int selectedIndex = selectable.getSelectionIndex();

            // there is no actual change
            if(selectedIndex == lastSelected) {
                return;

            // the change was a deselection
            } else if(selectedIndex == -1) {
                barcode.set(lastSelected, deselected, 1);

                updates.beginEvent();
                updates.addDelete(0);
                updates.commitEvent();

                deselectedList.updates().beginEvent();
                deselectedList.updates().addInsert(lastSelected);
                deselectedList.updates().commitEvent();

                lastSelected = -1;

            // a new line is selected
            } else {
                barcode.set(lastSelected, deselected, 1);
                barcode.set(selectedIndex, selected, 1);

                updates.beginEvent();
                updates.addUpdate(0);
                updates.commitEvent();

                deselectedList.updates().beginEvent();
                deselectedList.updates().addInsert(lastSelected);
                deselectedList.updates().addDelete(selectedIndex);
                deselectedList.updates().commitEvent();

                lastSelected = selectedIndex;
            }
        }
    }

    /**
     * This Strategy handles selection changes in {@link Selectable} widgets
     * that are created with the {@link SWT#MULTI} selection flag.
     */
    private class MultiLineSelectionChangeStrategy implements SelectionChangeStrategy {

        /**
         * Mutliple lines can be selected (or deselected) at any one time
         * on a {@link Selectable} widget in {@link SWT#MULTI} selection mode.
         */
        public void handleSelectionChanged(SelectionEvent event) {

            // The selection is not modified by any keys
            if(event.stateMask == SWT.NONE && selectable.getSelectionCount() == 1) {
                handleUnmodifiedSelection();

            // The selection is modified with the CTRL key
            } else if((event.stateMask & SWT.CTRL) == SWT.CTRL) {
                handleCtrlModifiedSelection();

            // The selection is modified with the SHIFT key
            } else if((event.stateMask & SWT.SHIFT) == SWT.SHIFT) {
                handleShiftModifiedSelection();

            // A selection method was called directly on the widget
            } else {
                handleCustomSelection();
            }
        }

        /**
         * Handles the case where a selection occurs that is unmodified.
         * This implies that the current selection has been replaced by
         * the selection of exactly one item.
         */
        private void handleUnmodifiedSelection() {
            int selectedIndex = selectable.getSelectionIndex();
            int blackIndex = barcode.getColourIndex(selectedIndex, selected);
            boolean wasSelected = blackIndex != -1;

            // Remove all but one from the current selection
            if(wasSelected) {
                int[] oldSelection = new int[barcode.colourSize(selected) - 1];
                updates.beginEvent();
                for(int i = 0;i < blackIndex;i++) {
                    oldSelection[i] = barcode.getIndex(0, selected);
                    barcode.set(oldSelection[i], deselected, 1);
                    updates.addDelete(0);
                }
                for(int i = blackIndex + 1;i < oldSelection.length;i++) {
                    oldSelection[i - 1] = barcode.getIndex(1, selected);
                    barcode.set(oldSelection[i - 1], deselected, 1);
                    updates.addDelete(1);
                }
                updates.commitEvent();
                deselectedList.updates().beginEvent();
                for(int i = 0;i < oldSelection.length;i++) {
                    deselectedList.updates().addInsert(oldSelection[i]);
                }
                deselectedList.updates().commitEvent();

            // Remove the current selection then add the new one
            } else {
                int[] oldSelection = new int[barcode.colourSize(selected)];
                updates.beginEvent();
                for(int i = 0;i < oldSelection.length;i++) {
                    oldSelection[i] = barcode.getIndex(0, selected);
                    barcode.set(oldSelection[i], deselected, 1);
                    updates.addDelete(0);
                }
                barcode.set(selectedIndex, selected, 1);
                updates.addInsert(0);
                updates.commitEvent();
                deselectedList.updates().beginEvent();
                for(int i = 0;i < oldSelection.length;i++) {
                    deselectedList.updates().addInsert(oldSelection[i]);
                }
                deselectedList.updates().addDelete(selectedIndex);
                deselectedList.updates().commitEvent();
            }
        }

        /**
         * Handles the case where a selection occurs that is modified by the
         * user pressing the CTRL key.  This implies that the current selection
         * has either increased or decreased by exactly one item.
         */
        private void handleCtrlModifiedSelection() {
            // Selection has increased in size
            if(selectable.getSelectionCount() > barcode.colourSize(selected)) {
                int selectedIndex = selectable.getSelectionIndex();
                barcode.set(selectedIndex, selected, 1);
                int blackIndex = barcode.getColourIndex(selectedIndex, selected);
                updates.beginEvent();
                updates.addInsert(blackIndex);
                updates.commitEvent();
                deselectedList.updates().beginEvent();
                deselectedList.updates().addDelete(selectedIndex);
                deselectedList.updates().commitEvent();

            // Selection has decreased in size
            } else if(selectable.getSelectionCount() < barcode.colourSize(selected)) {
                int index = -1;
                int blackIndex = -1;
                int[] selectedValues = selectable.getSelectionIndices();

                // search for what value is missing
                for(int i = 0;i < selectedValues.length;i++) {
                    int selectedIndex = barcode.getIndex(i, selected);
                    if(selectedIndex != selectedValues[i]) {
                        index = selectedIndex;
                        blackIndex = i;
                        break;
                    }
                }

                // The different value is the last selected value
                if(index == -1) {
                    blackIndex = selectedValues.length;
                    index = barcode.getIndex(blackIndex, selected);
                }

                // update the barcode and forward the events
                barcode.set(index, deselected, 1);
                updates.beginEvent();
                updates.addDelete(blackIndex);
                updates.commitEvent();
                int whiteIndex = barcode.getColourIndex(index, deselected);
                deselectedList.updates().beginEvent();
                deselectedList.updates().addInsert(whiteIndex);
                deselectedList.updates().commitEvent();
            }
        }

        /**
         * Handles the case where a selection occurs that is modified by the
         * user pressing the SHIFT key.  This implies that the current selection
         * is replaced by a selected range which may or may not include values
         * that were previously selected.
         *
         * @todo Currently this method is relatively brute force and could be
         * optimized later if it has a significant performance impact.
         */
        private void handleShiftModifiedSelection() {
            int[] selectedValues = selectable.getSelectionIndices();
            int[] oldSelection = new int[barcode.colourSize(selected)];
            updates.beginEvent();
            for(int i = 0;i < oldSelection.length;i++) {
                oldSelection[i] = barcode.getIndex(i, selected);
            }
            barcode = new Barcode();
            barcode.add(0, deselected, source.size());
            updates.addDelete(0, oldSelection.length);
            updates.addInsert(0, selectedValues.length);
            updates.commitEvent();

            barcode.set(selectedValues[0], selected, selectedValues.length);
            deselectedList.updates().beginEvent();
            int unchangedSelections = 0;
            for(int i = 0;i < oldSelection.length;i++) {
                int current = oldSelection[i];
                if(current < selectedValues[0] || current > selectedValues[selectedValues.length - 1]) {
                    deselectedList.updates().addInsert(oldSelection[i]);
                } else {
                    unchangedSelections++;
                }
            }
            deselectedList.updates().addDelete(selectedValues[0], selectedValues.length - unchangedSelections);
            deselectedList.updates().commitEvent();
        }

        /**
         * Handles the case where a selection occurs that is applied
         * programmatically rather than by mouse or key input.  This means that
         * one of the selection methods are called on the Selectable widget
         * directly.  This implies that no assumptions can be made about how
         * this event affects the current selection.  This results in having to
         * process the selection for each item in the widget.
         *
         * THIS COULD BE OPTIMIZED SO THAT REPAINTING SELECTION ON LIST CHANGES
         * AND INVERTING SELECTION DOESN'T REQUIRE WIDGET INSPECTION.
         */
        private void handleCustomSelection() {
			int selectedSoFar = 0;
			ArrayList changes = new ArrayList();

			// Update the barcode and forward events on the selected EventList
			updates.beginEvent();
			for(int i = 0;i < barcode.size();i++) {
				// The item is selected
				if(selectable.isSelected(i)) {
					// previously wasn't selected
					if(barcode.get(i) == deselected) {
						barcode.set(i, selected, 1);
						updates.addInsert(selectedSoFar);
						changes.add(new Integer(i));
					}
					selectedSoFar++;

				// The item is deselected and was previously selected
				} else if(barcode.get(i) == selected) {
					barcode.set(i, deselected, 1);
					updates.addDelete(selectedSoFar);
					changes.add(new Integer(i));
				}
			}
			updates.commitEvent();

			// Forward change events on the deselected EventList
			deselectedList.updates().beginEvent();
			for(int i = 0;i < changes.size();i++) {
				// Changed from deselected to selected
				int index = ((Integer)changes.get(i)).intValue();
				if(barcode.get(index) == selected) {
					deselectedList.updates().addDelete(barcode.getColourIndex(index, false, deselected));

				// Changed from selected to deselected
				} else {
					deselectedList.updates().addInsert(barcode.getColourIndex(index, deselected));
				}
			}
			deselectedList.updates().commitEvent();
			changes.clear();
        }
    }
}