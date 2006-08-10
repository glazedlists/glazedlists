/* Glazed Lists                                                 (c) 2003-2006 */
/*                                                     O'Dell Engineering Ltd.*/
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.event.*;
// SWT toolkit stuff for displaying widgets
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
// standard collections as support
import java.util.*;

/**
 * A FilterList for elements that are checked in the EventTableViewer.
 *
 * <p>The TableCheckFilterList <strong>must</strong> be used as the source list to
 * the EventTableViewer. This is because the TableCheckFilterList uses methods on
 * the Table that depend on table indices.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
final class TableCheckFilterList extends TransformedList implements org.eclipse.swt.events.SelectionListener {

    /** filter out unchecked elements */
    private CheckMatcherEditor checkMatcherEditor = new CheckMatcherEditor();

    /** the table that checkboxes are displayed in */
    private Table table;

    /** for checking table items */
    private CheckableTableFormat checkableTableFormat;

    /**
     * Creates a new filter that filters elements depending on whether they are
     * checked in the table.
     *
     * @param tableFormat if this class implements {@link CheckableTableFormat}
     *      it will be used to store check state. Otherwise check state will be
     *      stored transiently within this class' state.
     */
    public TableCheckFilterList(EventList source, Table table, TableFormat tableFormat) {
        super(new FilterList(tableFormat instanceof CheckableTableFormat ? source : new CheckableWrapperList(source), Matchers.trueMatcher()));
        this.table = table;
        if(tableFormat instanceof CheckableTableFormat) {
            this.checkableTableFormat = (CheckableTableFormat)tableFormat;
        } else {
            this.checkableTableFormat = null;
        }

        // listen for changes in checkedness
        table.addSelectionListener(this);

        // prepare the filter
        FilterList filteredSource = (FilterList)super.source;
        filteredSource.setMatcherEditor(checkMatcherEditor);

        // handle changes
        source.addListEventListener(this);
    }


    /**
     * Set the specified list element in the source list as checked.
     */
    private void setChecked(Object element, boolean checked) {
        if(checkableTableFormat != null) {
            checkableTableFormat.setChecked(element, checked);
        } else {
            ((CheckWrapped)element).setChecked(checked);
        }
    }
    /**
     * Set the specified index in the filtered list as checked.
     */
    private void setChecked(int index, boolean checked) {
        setChecked(source.get(getSourceIndex(index)), checked);
    }
    /**
     * Get whether the specified element in the source list is checked.
     */
    private boolean getChecked(Object element) {
        if(checkableTableFormat != null) {
            return checkableTableFormat.getChecked(element);
        } else {
            return ((CheckWrapped)element).getChecked();
        }
    }
    /**
     * Get whether the specified index in the filtered list is checked.
     */
    private boolean getChecked(int index) {
        return getChecked(source.get(getSourceIndex(index)));
    }


    /**
     * Match checked elements.
     */
    private class CheckMatcherEditor extends AbstractMatcherEditor {
        private boolean checkedOnly = false;
        private void setCheckedOnly(boolean checkedOnly) {
            if(checkedOnly == this.checkedOnly) return;
            if(checkedOnly) fireConstrained(new CheckMatcher());
            else fireMatchAll();
        }
        private boolean getCheckedOnly() {
            return checkedOnly;
        }
        private class CheckMatcher implements Matcher {
            public boolean matches(Object element) {
                return getChecked(element);
            }
        }
    }


    /**
     * Gets a static snapshot of the checked Objects in this list.
     */
    public List getAllChecked() {
        List result = new ArrayList();
        for(int i = 0; i < size(); i++) {
            if(getChecked(i)) {
                result.add(get(i));
            }
        }
        return result;
    }


    /**
     * Set whether this filter list displays all elements, or only checked elements.
     */
    public void setCheckedOnly(boolean checkedOnly) {
        checkMatcherEditor.setCheckedOnly(checkedOnly);
    }

    /**
     * Get whether this filter list displays all elements, or only checked elements.
     */
    public boolean getCheckedOnly() {
        return checkMatcherEditor.getCheckedOnly();
    }


    /**
     * Returns the element at the specified position in this list. This unwraps
     * a {@link CheckWrapped} object from the source if necessary.
     */
    public Object get(int index) {
        if(checkableTableFormat != null) {
            return super.get(index);
        } else {
            CheckWrapped checkWrapped = (CheckWrapped)super.get(index);
            return checkWrapped.getWrapped();
        }
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        updates.forwardEvent(listChanges);
    }


    /**
     * Sent when selection occurs in the control.
     */
    public void widgetSelected(SelectionEvent e) {
        if(e.detail == SWT.CHECK) {
            getReadWriteLock().writeLock().lock();
            try {
                updateItemChecked((TableItem)e.item);
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
        }
    }
    public void widgetDefaultSelected(SelectionEvent e) {
        if(e.detail == SWT.CHECK) {
            getReadWriteLock().writeLock().lock();
            try {
                updateItemChecked((TableItem)e.item);
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
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

        // set the checked property on the proper element
        int index = table.indexOf(updated);
        boolean checked = updated.getChecked();
        setChecked(index, checked);

        // force an update event
        int sourceIndex = getSourceIndex(index);
        source.set(sourceIndex, source.get(sourceIndex));
    }

    /**
     * Registers the specified listener to receive notification of changes
     * to this list.
     *
     * <p>The only listener added to a TableCheckFilterList may be a EventTableViewer.
     * This is because the TableCheckFilterList may not have any lists between it
     * and the EventTableViewer.
     */
    public void addListEventListener(ListEventListener listChangeListener) {
        super.addListEventListener(listChangeListener);

        // also adjust the table's checked rows
        if(listChangeListener instanceof EventTableViewer) {
            super.addListEventListener(new TableChecker());
        }
    }

    /** {@inheritDoc} */
    public void dispose() {
        if(source instanceof CheckableWrapperList) {
            ((CheckableWrapperList)source).dispose();
        }
        super.dispose();
    }

    /**
     * The TableChecker checks the table rows after they have been updated.
     */
    private class TableChecker implements ListEventListener {
        public TableChecker() {
            for(int i = 0; i < size(); i++) {
                boolean checked = getChecked(i);
                table.getItem(i).setChecked(checked);
            }
        }
        public void listChanged(ListEvent listChanges) {
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();
                if(changeType == ListEvent.INSERT || changeType == ListEvent.UPDATE) {
                    boolean checked = getChecked(changeIndex);
                    table.getItem(changeIndex).setChecked(checked);
                }
            }
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
class CheckWrapped {
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
    public boolean getChecked() {
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
