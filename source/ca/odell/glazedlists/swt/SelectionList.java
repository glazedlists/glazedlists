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

	/** the mirror to this view of selection */
	private DeselectedList deselected = null;

	/** the Selectable SWT Viewer */
	private Selectable selectable = null;

	/** the selection state */
	private Barcode barcode = null;

	/** the Strategy to handle changes in selection */
	private SelectionChangeStrategy selectionChangeStrategy = null;

	/** the selection listener */
	private AdvancedSelectionListener selectionListener = null;

	/**
	 * Creates a {@link SelectionList} that provides a view of the selected
	 * items in an SWT widget.  Deselected items exist in a view accessible
	 * via the {@link SelectionList#getDeselected()} method.
	 */
	SelectionList(EventList source, Selectable selectable) {
		super(source);
		deselected = new DeselectedList(source);
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
        return barcode.blackSize();
    }

	/** {@inheritDoc} */
	public void listChanged(ListEvent listChanges) {
		// handle reorder events
		if(listChanges.isReordering()) {
			// prepare a reorder map for both of the EventLists
			int[] sourceReorderMap = listChanges.getReorderMap();
			int[] selectReorderMap = new int[barcode.blackSize()];
			int[] deselectReorderMap = new int[barcode.whiteSize()];

			// adjust the barcode & build the reorder maps to forward
			Barcode oldBarcode = barcode;
			barcode = new Barcode();
			for(int i = 0; i < sourceReorderMap.length; i++) {
				Object flag = oldBarcode.get(sourceReorderMap[i]);
				boolean wasSelected = (flag != Barcode.WHITE);
				barcode.add(i, flag, 1);

				// reordering a selected value
				if(wasSelected) {
					int previousIndex = oldBarcode.getBlackIndex(sourceReorderMap[i]);
					int currentIndex = barcode.getBlackIndex(i);
					selectReorderMap[currentIndex] = previousIndex;

				// reordering a deselected value
				} else {
					int previousIndex = oldBarcode.getWhiteIndex(sourceReorderMap[i]);
					int currentIndex = barcode.getWhiteIndex(i);
					deselectReorderMap[currentIndex] = previousIndex;
				}
			}

			// fire the reorder for selected values
			updates.beginEvent();
			updates.reorder(selectReorderMap);
			updates.commitEvent();

			// fire the reorder for deselected values
			deselected.updates().beginEvent();
			deselected.updates().reorder(deselectReorderMap);
			deselected.updates().commitEvent();

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
			int lastDeleted = -1;

			// process a DELETE event
			if(changeType == ListEvent.DELETE) {
				// only delete selected values
				int blackIndex = barcode.getBlackIndex(index);
				if(blackIndex != -1) {
					barcode.remove(index, 1);
					updates.addDelete(blackIndex);
				} else {
					int whiteIndex = barcode.getWhiteIndex(index);
					deselectedDeletes.add(new Integer(whiteIndex));
					barcode.remove(index, 1);
				}

			// process an UPDATE event
			} else if(changeType == ListEvent.UPDATE) {
				int blackIndex = barcode.getBlackIndex(index);
				if(blackIndex != -1) {
					updates.addUpdate(blackIndex);
				} else {
					int whiteIndex = barcode.getWhiteIndex(index);
					deselectedUpdates.add(new Integer(whiteIndex));
				}

			// process an INSERT event
			} else if(changeType == ListEvent.INSERT) {
				barcode.addWhite(index, 1);
				int whiteIndex = barcode.getWhiteIndex(index);
				deselectedInserts.add(new Integer(whiteIndex));
			}
		}
		updates.commitEvent();

		// Process the deselected values
		deselected.updates().beginEvent();

		// process all deletes of deselected values
		for(int i = 0;i < deselectedDeletes.size();i++) {
			int whiteIndex = ((Integer)deselectedDeletes.get(i)).intValue();
			deselected.updates().addDelete(whiteIndex);
		}
		deselectedDeletes.clear();

		// process all inserts which are implicitly deselected
		for(int i = 0;i < deselectedInserts.size();i++) {
			int whiteIndex = ((Integer)deselectedInserts.get(i)).intValue();
			deselected.updates().addInsert(whiteIndex);
		}
		deselectedInserts.clear();

		// process all updates of deselected values
		for(int i = 0;i < deselectedUpdates.size();i++) {
			int whiteIndex = ((Integer)deselectedUpdates.get(i)).intValue();
			deselected.updates().addUpdate(whiteIndex);
		}
		deselectedUpdates.clear();
		deselected.updates().commitEvent();
	}

    /**
     * Provides access to the {@link EventList} that contains only items
     * that are NOT currently selected in the widget.
     */
	public EventList getDeselected() {
		return deselected;
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
		return barcode.getBlackIndex(sourceIndex) != -1;
	}

    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        return barcode.getIndex(mutationIndex, Barcode.BLACK);
    }

    /** {@inheritDoc} */
    public void dispose() {
		selectable.removeSelectionListener(selectionListener);
		if((selectable.getStyle() & SWT.MULTI) == SWT.MULTI) {
			selectable.removeListener(SWT.KeyUp, selectionListener);
			selectable.removeListener(SWT.KeyDown, selectionListener);
		}
		super.dispose();
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
			return barcode.whiteSize();
		}

		/** {@inheritDoc} */
		protected int getSourceIndex(int mutationIndex) {
			return barcode.getIndex(mutationIndex, Barcode.WHITE);
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
			int selected = selectable.getSelectionIndex();

			// there is no actual change
			if(selected == lastSelected) {
				return;

			// the change was a deselection
			} else if(selected == -1) {
				barcode.setWhite(lastSelected, 1);

				updates.beginEvent();
				updates.addDelete(0);
				updates.commitEvent();

				deselected.updates().beginEvent();
				deselected.updates().addInsert(lastSelected);
				deselected.updates().commitEvent();

				lastSelected = -1;

			// a new line is selected
			} else {
				barcode.setWhite(lastSelected, 1);
				barcode.setBlack(selected, 1);

				updates.beginEvent();
				updates.addUpdate(0);
				updates.commitEvent();

				deselected.updates().beginEvent();
				deselected.updates().addInsert(lastSelected);
				deselected.updates().addDelete(selected);
				deselected.updates().commitEvent();

				lastSelected = selected;
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
			int selected = selectable.getSelectionIndex();
			int blackIndex = barcode.getBlackIndex(selected);
			boolean wasSelected = blackIndex != -1;

			// Remove all but one from the current selection
			if(wasSelected) {
				int[] oldSelection = new int[barcode.blackSize() - 1];
				updates.beginEvent();
				for(int i = 0;i < blackIndex;i++) {
					oldSelection[i] = barcode.getIndex(0, Barcode.BLACK);
					barcode.setWhite(oldSelection[i], 1);
					updates.addDelete(0);
				}
				for(int i = blackIndex + 1;i < oldSelection.length;i++) {
					oldSelection[i - 1] = barcode.getIndex(1, Barcode.BLACK);
					barcode.setWhite(oldSelection[i - 1], 1);
					updates.addDelete(1);
				}
				updates.commitEvent();
				deselected.updates().beginEvent();
				for(int i = 0;i < oldSelection.length;i++) {
					deselected.updates().addInsert(oldSelection[i]);
				}
				deselected.updates().commitEvent();

			// Remove the current selection then add the new one
			} else {
				int[] oldSelection = new int[barcode.blackSize()];
				updates.beginEvent();
				for(int i = 0;i < oldSelection.length;i++) {
					oldSelection[i] = barcode.getIndex(0, Barcode.BLACK);
					barcode.setWhite(oldSelection[i], 1);
					updates.addDelete(0);
				}
				barcode.setBlack(selected, 1);
				updates.addInsert(0);
				updates.commitEvent();
				deselected.updates().beginEvent();
				for(int i = 0;i < oldSelection.length;i++) {
					deselected.updates().addInsert(oldSelection[i]);
				}
				deselected.updates().addDelete(selected);
				deselected.updates().commitEvent();
			}
		}

		/**
		 * Handles the case where a selection occurs that is modified by the
		 * user pressing the CTRL key.  This implies that the current selection
		 * has either increased or decreased by exactly one item.
		 */
		private void handleCtrlModifiedSelection() {
			// Selection has increased in size
			if(selectable.getSelectionCount() > barcode.blackSize()) {
				int selected = selectable.getSelectionIndex();
				barcode.setBlack(selected, 1);
				int blackIndex = barcode.getBlackIndex(selected);
				updates.beginEvent();
				updates.addInsert(blackIndex);
				updates.commitEvent();
				deselected.updates().beginEvent();
				deselected.updates().addDelete(selected);
				deselected.updates().commitEvent();

			// Selection has decreased in size
			} else if(selectable.getSelectionCount() < barcode.blackSize()) {
				int index = -1;
				int blackIndex = -1;
				int[] selectedValues = selectable.getSelectionIndices();

				// search for what value is missing
				for(int i = 0;i < selectedValues.length;i++) {
					int selectedIndex = barcode.getIndex(i, Barcode.BLACK);
					if(selectedIndex != selectedValues[i]) {
						index = selectedIndex;
						blackIndex = i;
						break;
					}
				}

				// The different value is the last selected value
				if(index == -1) {
					blackIndex = selectedValues.length;
					index = barcode.getIndex(blackIndex, Barcode.BLACK);
				}

				// update the barcode and forward the events
				barcode.setWhite(index, 1);
				updates.beginEvent();
				updates.addDelete(blackIndex);
				updates.commitEvent();
				int whiteIndex = barcode.getWhiteIndex(index);
				deselected.updates().beginEvent();
				deselected.updates().addInsert(whiteIndex);
				deselected.updates().commitEvent();
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
			int[] oldSelection = new int[barcode.blackSize()];
			updates.beginEvent();
			for(int i = 0;i < oldSelection.length;i++) {
				oldSelection[i] = barcode.getIndex(i, Barcode.BLACK);
			}
			barcode = new Barcode();
			barcode.addWhite(0, source.size());
			updates.addDelete(0, oldSelection.length);
			updates.addInsert(0, selectedValues.length);
			updates.commitEvent();

			barcode.setBlack(selectedValues[0], selectedValues.length);
			deselected.updates().beginEvent();
			int unchangedSelections = 0;
			for(int i = 0;i < oldSelection.length;i++) {
				int current = oldSelection[i];
				if(current < selectedValues[0] || current > selectedValues[selectedValues.length - 1]) {
					deselected.updates().addInsert(oldSelection[i]);
				} else {
					unchangedSelections++;
				}
			}
			deselected.updates().addDelete(selectedValues[0], selectedValues.length - unchangedSelections);
			deselected.updates().commitEvent();
		}

		/**
		 * Handles the case where a selection occurs that is applied
		 * programmatically rather than by user input.  This means that
		 * one of the selection methods are called on the Selectable widget
		 * directly.  This implies that no assumptions can be made about
		 * how this event affects the current selection.
		 */
		private void handleCustomSelection() {
			// Fail silently because XP sucks and wants to send error reports on the demo app
			//throw new UnsupportedOperationException();
		}
	}
}