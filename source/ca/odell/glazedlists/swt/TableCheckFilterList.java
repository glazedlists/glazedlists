/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// SWT toolkit stuff for displaying widgets
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
// standard collections as support
import java.util.*;

/**
 * A FilterList for elements that are displayed using EventTableViewer.
 *
 * <p>The TableCheckFilterList <strong>must</strong> be used as the source list to
 * the EventTableViewer. This is because the TableCheckFilterList uses methods on
 * the Table that depend on table indices.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TableCheckFilterList extends AbstractFilterList implements SelectionListener {
    
    /** whether this displays all or only checked elements */
    private boolean checkedOnly = false;
    
    /** the table that checkboxes are displayed in */
    private Table table;
    
    /** whether the source list's elements implement Checkable */
    private boolean elementsAreCheckable;

    /**
     * Creates a new filter that filters elements depending on whether they are
     * checked in the table.
     *
     * @param elementsAreCheckable whether the elements of the source list
     *      implement Checkable. If false, the list objects will be transparently 
     *      wrapped to provide state management.
     */
    public TableCheckFilterList(EventList source, Table table, boolean elementsAreCheckable) {
        super(elementsAreCheckable ? source : new CheckableWrapperList(source));
        this.table = table;
        this.elementsAreCheckable = elementsAreCheckable;
        
        getReadWriteLock().writeLock().lock();
        try {
            // initially, everything is unchecked
            for(Iterator i = this.source.iterator(); i.hasNext(); ) {
                ((Checkable)i.next()).setChecked(false);
            }
            
            // listen for changes in checkedness
            table.addSelectionListener(this);
            
            // prepare the filter
            handleFilterChanged();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
    
    /**
     * Returns true if the specified Object is checked.
     */
    public boolean filterMatches(Object element) {
        if(checkedOnly) {
            Checkable checkable = (Checkable)element;
            return checkable.isChecked();
        } else {
            return true;
        }
    }
    
    /**
     * Set whether this filter list displays all elements, or only checked elements.
     */
    public void setCheckedOnly(boolean checkedOnly) {
        getReadWriteLock().writeLock().lock();
        try {
            if(checkedOnly != this.checkedOnly) {
                this.checkedOnly = checkedOnly;
                handleFilterChanged();
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
    
    /**
     * Get whether this filter list displays all elements, or only checked elements.
     */
    public boolean getCheckedOnly() {
        return checkedOnly;
    }
    
    /**
     * Returns the element at the specified position in this list.
     */
    public Object get(int index) {
        if(elementsAreCheckable) {
            return super.get(index);
        } else {
            CheckWrapped checkWrapped = (CheckWrapped)super.get(index);
            return checkWrapped.getWrapped();
        }
    }
    
    /**
     * Sent when default selection occurs in the control.
     */
    public void widgetDefaultSelected(SelectionEvent e) {
        if(e.detail == SWT.CHECK) {
            updateItemChecked((TableItem)e.item);
        }
    }

    /**
     * Sent when selection occurs in the control.    
     */
    public void widgetSelected(SelectionEvent e) {
        if(e.detail == SWT.CHECK) {
            updateItemChecked((TableItem)e.item);
        }
    }
    
    /**
     * When the check status of a table item is changed, this changes the
     * checked property of the corresponding CheckWrapped object.
     *
     * <p>This uses a very lame hack to get the index of the specified TableItem.
     * It first uses the Table.indexOf(TableItem) method, and then attempts to get
     * the unfiltered index of that. This is only guaranteed to work in the case
     * where the TableCheckFilterList is the source of the EventTableViewer.
     * Otherwise everything may blow up because the list indices cannot be looked up.
     */
    private void updateItemChecked(TableItem updated) {
        if(updated == null) return;
        getReadWriteLock().writeLock().lock();
        try {
            // set the checked property on the proper element
            int filteredIndex = table.indexOf(updated);
            int sourceIndex = getSourceIndex(filteredIndex);
            boolean checked = updated.getChecked();
            Checkable checkable = (Checkable)source.get(sourceIndex);
            checkable.setChecked(checked);

            // force an update event
            source.set(sourceIndex, checkable);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
}

/**
 * A simple wrapper list that makes all elements Checkable.
 *
 * <p>This maintains a separate collection that mirrors the source collection.
 * This collection contains only CheckWrapped objects. For each element in the
 * source list at index i, that element is wrapped in the mirror list at index i.
 *
 * <p>The mirror collection is maintained in the listChanged() method only.
 * All get() calls return from the mirror collection rather than from the source
 * list.
 */
class CheckableWrapperList extends TransformedList {
    
    /** wrapped list contains CheckWrapped elements only */
    private List wrappedSource = new ArrayList();

    /** table which fires check events */
    private Table table;
    
    public CheckableWrapperList(EventList source) {
        super(source);
        
        source.getReadWriteLock().readLock().lock();
        try {
            prepareElements();
            source.addListEventListener(this);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }
    
    /**
     * The CheckableWrapperList supports only one write operation, which is to
     * force an update on a specified value. This requires that the parameter value
     * is an instance of CheckWrapped, which is the only value that this list
     * supports.
     */
    public Object set(int index, Object value) {
        CheckWrapped checkWrapped = (CheckWrapped)value;
        return source.set(index, checkWrapped.getWrapped());
    }

    private void prepareElements() {
        for(int i = 0; i < source.size(); i++) {
            CheckWrapped checkWrapped = new CheckWrapped(source.get(i));
            wrappedSource.add(i, checkWrapped);
        }
    }

    public Object get(int index) {
        return wrappedSource.get(index);
    }

    public void listChanged(ListEvent listChanges) {
        updates.beginEvent();
        while(listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();
            if(changeType == ListEvent.INSERT) {
                CheckWrapped checkWrapped = new CheckWrapped(source.get(changeIndex));
                wrappedSource.add(changeIndex, checkWrapped);
            } else if(changeType == ListEvent.UPDATE) {
                CheckWrapped checkWrapped = (CheckWrapped)wrappedSource.get(changeIndex);
                checkWrapped.setWrapped(source.get(changeIndex));
            } else if(changeType == ListEvent.DELETE) {
                wrappedSource.remove(changeIndex);
            }
            updates.addChange(changeType, changeIndex);
        }
        updates.commitEvent();
    }
}

/**
 * A simple wrapper that adds a checked property to an Object.
 */
class CheckWrapped implements Checkable {
    private boolean checked = false;
    private Object wrapped = null;
    public CheckWrapped(Object wrapped) {
        this.wrapped = wrapped;
    }
    public Object getWrapped() {
        return wrapped;
    }
    public void setWrapped(Object wrapped) {
        this.wrapped = wrapped;
    }
    public boolean isChecked() {
        return checked;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    public String toString() {
        if(checked) return "[*] " + wrapped;
        else return "[ ] " + wrapped;
    }
}
