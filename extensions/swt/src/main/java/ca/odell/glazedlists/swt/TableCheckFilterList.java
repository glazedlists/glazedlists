/* Glazed Lists                                                 (c) 2003-2006 */
/*                                                     O'Dell Engineering Ltd.*/
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.CheckableTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

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
final class TableCheckFilterList<S, E> extends TransformedList<S, E> implements SelectionListener {

    /** filter out unchecked elements */
    private final CheckMatcherEditor<S> checkMatcherEditor = new CheckMatcherEditor<>();

    /** the table that checkboxes are displayed in */
    private final Table table;

    /** for checking table items */
    private final CheckableTableFormat checkableTableFormat;

    /** Retain a reference to the CheckableWrapperList, if we create one, so it can be disposed */
    private CheckableWrapperList checkableWrapperList;

    /** Retain a reference to the FilterList so it can be disposed */
    private FilterList filterList;

    /** Retain a reference to the TableChecker, if we create one, so it can be disposed */
    private TableChecker tableChecker;

    /**
     * Creates a new filter that filters elements depending on whether they are
     * checked in the table.
     *
     * @param source the items to decorate with checkability
     * @param table the checkable table
     * @param tableFormat if this class implements {@link CheckableTableFormat}
     *      it will be used to store check state. Otherwise check state will be
     *      stored transiently within this class' state.
     */
    public TableCheckFilterList(EventList<S> source, Table table, TableFormat tableFormat) {
        this(table, tableFormat, tableFormat instanceof CheckableTableFormat ? source : new CheckableWrapperList(source));
    }

    /**
     * A hidden constructor that allows us to store a reference to the source of
     * the FilterList, which we may have created and must dispose later.
     *
     * @param table the checkable table
     * @param tableFormat the format of the checkable table
     * @param filterListSource the source of the FilterList that backs this TableCheckFilterList
     */
    private TableCheckFilterList(Table table, TableFormat tableFormat, EventList<S> filterListSource) {
        this(new FilterList<>(filterListSource, Matchers.trueMatcher()), table, tableFormat);

        // if a CheckableWrapperList was created, store a reference so it can be disposed later
        if (filterListSource instanceof CheckableWrapperList)
            checkableWrapperList = (CheckableWrapperList) filterListSource;
    }

    /**
     * A hidden constructor that allows us to store a reference to the
     * FilterList, which we created and must dispose later.
     *
     * @param filterList the FilterList that backs this TableCheckFilterList
     * @param table the checkable table
     * @param tableFormat the format of the checkable table
     */
    private TableCheckFilterList(FilterList<S> filterList, Table table, TableFormat tableFormat) {
        super(filterList);

        this.filterList = filterList;
        this.table = table;
        this.checkableTableFormat = tableFormat instanceof CheckableTableFormat ? (CheckableTableFormat) tableFormat : null;

        // listen for changes in checkedness
        table.addSelectionListener(this);

        // prepare the filter
        filterList.setMatcherEditor(checkMatcherEditor);

        // handle changes
        filterList.addListEventListener(this);
    }

    /**
     * @return <tt>false</tt>; TableCheckFilterList is readonly
     */
    @Override
    protected boolean isWritable() {
        return false;
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
    private class CheckMatcherEditor<T> extends AbstractMatcherEditor<T> {
        private boolean checkedOnly = false;
        private void setCheckedOnly(boolean checkedOnly) {
            if(checkedOnly == this.checkedOnly) return;
            if(checkedOnly) fireConstrained(new CheckMatcher<T>());
            else fireMatchAll();
        }
        private boolean getCheckedOnly() {
            return checkedOnly;
        }
        private class CheckMatcher<V> implements Matcher<V> {
            @Override
            public boolean matches(V element) {
                return getChecked(element);
            }
        }
    }


    /**
     * Gets a static snapshot of the checked Objects in this list.
     */
    public List<E> getAllChecked() {
        final List<E> result = new ArrayList<>();
        for(int i = 0, n = size(); i < n; i++) {
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
    @Override
    public E get(int index) {
        if(checkableTableFormat != null) {
            return super.get(index);
        } else {
            CheckWrapped<E> checkWrapped = (CheckWrapped<E>)super.get(index);
            return checkWrapped.getWrapped();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent listChanges) {
        updates.forwardEvent(listChanges);
    }

    /**
     * Sent when selection occurs in the control.
     */
    @Override
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
    @Override
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

    @Override
    public void addListEventListener(ListEventListener<? super E> listChangeListener) {
        super.addListEventListener(listChangeListener);

        // also adjust the table's checked rows
        if(listChangeListener instanceof DefaultEventTableViewer) {
            tableChecker = new TableChecker();
            super.addListEventListener(tableChecker);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (checkableWrapperList != null)
            checkableWrapperList.dispose();

        if (tableChecker != null)
            removeListEventListener(tableChecker);

        filterList.dispose();

        super.dispose();
    }

    /**
     * The TableChecker checks the table rows after they have been updated.
     */
    private class TableChecker implements ListEventListener<E> {
        public TableChecker() {
            for(int i = 0; i < size(); i++) {
                boolean checked = getChecked(i);
                table.getItem(i).setChecked(checked);
            }
        }
        @Override
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
class CheckableWrapperList<S> extends TransformedList<S, CheckWrapped<S>> {

    /** wrapped list contains CheckWrapped elements only */
    private List<CheckWrapped<S>> wrappedSource = new ArrayList<>();

    public CheckableWrapperList(EventList<S> source) {
        super(source);

        prepareElements();
        source.addListEventListener(this);
    }

    /**
     * @return <tt>false</tt>; CheckableWrapperList is readonly
     */
    @Override
    protected boolean isWritable() {
        return false;
    }

    /**
     * The CheckableWrapperList supports only one write operation, which is to
     * force an update on a specified value. This requires that the parameter
     * value is an instance of CheckWrapped, which is the only value that this
     * list supports.
     */
    @Override
    public CheckWrapped<S> set(int index, CheckWrapped<S> value) {
        source.set(index, value.getWrapped());
        return value;
    }

    private void prepareElements() {
        for(int i = 0, n = source.size(); i < n; i++) {
            wrappedSource.add(i, new CheckWrapped<>(source.get(i)));
        }
    }

    @Override
    public CheckWrapped<S> get(int index) {
        return wrappedSource.get(index);
    }

    @Override
    public void listChanged(ListEvent listChanges) {
        updates.beginEvent();
        while (listChanges.next()) {
            final int changeIndex = listChanges.getIndex();
            final int changeType = listChanges.getType();

            switch (changeType) {
                case ListEvent.INSERT: wrappedSource.add(changeIndex, new CheckWrapped<>(source.get(changeIndex))); break;
                case ListEvent.UPDATE: wrappedSource.get(changeIndex).setWrapped(source.get(changeIndex)); break;
                case ListEvent.DELETE: wrappedSource.remove(changeIndex); break;
            }

            updates.addChange(changeType, changeIndex);
        }
        updates.commitEvent();
    }
}

/**
 * A simple wrapper that adds a checked property to an Object.
 */
class CheckWrapped<E> {
    private boolean checked = false;
    private E wrapped = null;

    public CheckWrapped(E wrapped) {
        this.wrapped = wrapped;
    }
    public E getWrapped() { return wrapped; }
    public void setWrapped(E wrapped) { this.wrapped = wrapped; }

    public boolean getChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }

    @Override
    public String toString() {
        if(checked) return "[*] " + wrapped;
        else return "[ ] " + wrapped;
    }
}