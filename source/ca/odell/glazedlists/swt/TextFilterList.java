/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// access to the volatile implementation pacakge
import ca.odell.glazedlists.util.impl.*;
// SWT toolkit stuff for displaying widgets
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;


/**
 * A filter list that shows only elements that contain the filter text.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * <p><strong>Warning:</strong> This class is a developer preview and subject to
 * many bugs and API changes.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TextFilterList extends DefaultTextFilterList {

    /** the filter edit text field */
    private Text filterEdit;

    /** the document listener responds to changes, it is null when we're not listening */
    private FilterModifyListener filterModifyListener = null;

    ///** the selection listener performs a refilter when fired */
    private FilterSelectionListener filterSelectionListener = new FilterSelectionListener();

    /**
     * Creates a new filter list that filters elements out of the
     * specified source list.  Elements in the source list must implement
     * the TextFilterable interface.
     */
    public TextFilterList(EventList source) {
        this(source, (Text)null, null);
    }

    /**
     * Creates a new filter list that filters elements out of the
     * specified source list.  Elements in the source list must implement
     * the TextFilterable interface.
     */
    public TextFilterList(EventList source, Text filterEdit) {
        this(source, filterEdit, null);
    }

    /**
     * Creates a {@link TextFilterList} that filters the specified {@link EventList}
     * of elements using the JavaBeans property names specified to get the
     * {@link String}s to search.
     *
     * <p>Note that the classes which will be obfuscated may not work with
     * reflection. In this case, implement a {@link TextFilterator} manually.
     *
     * @param propertyNames an array of property names in the JavaBeans format.
     *      For example, if your list contains Objects with the methods getFirstName(),
     *      setFirstName(String), getAge(), setAge(Integer), then this array should
     *      contain the two strings "firstName" and "age". This format is specified
     *      by the JavaBeans {@link java.beans.PropertyDescriptor}.
     */
    public TextFilterList(EventList source, String[] propertyNames) {
        this(source, null, new BeanTextFilterator(propertyNames));
    }

    /**
     * Creates a {@link TextFilterList} that filters the specified {@link EventList}
     * of elements using the JavaBeans property names specified to get the
     * {@link String}s to search.
     *
     * <p>Note that the classes which will be obfuscated may not work with
     * reflection. In this case, implement a {@link TextFilterator} manually.
     *
     * @param propertyNames an array of property names in the JavaBeans format.
     *      For example, if your list contains Objects with the methods getFirstName(),
     *      setFirstName(String), getAge(), setAge(Integer), then this array should
     *      contain the two strings "firstName" and "age". This format is specified
     *      by the JavaBeans {@link java.beans.PropertyDescriptor}.
     * @param filterEdit a text field for typing in the filter text.
     */
    public TextFilterList(EventList source, String[] propertyNames, Text filterEdit) {
        this(source, filterEdit, new BeanTextFilterator(propertyNames));
    }

    /**
     * Creates a new filter list that uses a TextFilterator. A TextFilterator is something
     * that I made up. It is basically a class that knows how to take an arbitrary
     * object and get an array of strings for that object.
     */
    public TextFilterList(EventList source, Text filterEdit, TextFilterator filterator) {
        super(source, filterator);

        // listen to filter events
        this.setFilterEdit(filterEdit);
    }

    /**
     * Gets the filter edit component for editing filters.
     */
    public Text getFilterEdit() {
        return filterEdit;
    }

    /**
     * Sets the Text used to edit the filter search {@link String}.
     */
    public void setFilterEdit(Text filterEdit) {

        boolean live = true;

        // stop listening on filter events from the old filter edit
        if(this.filterEdit != null) {
            live = (filterModifyListener != null);
            setLive(false);
        }

        // start listening for filter events from the new filter edit
        this.filterEdit = filterEdit;
        setLive(live);

        // filter with the new filter edit
        reFilter();
    }

    /**
     * Directs this filter to respond to changes to the Text as they are
     * made. This uses a ModifyListener and every time the Text is
     * modified, the list is refiltered.
     *
     * <p>To avoid the processing overhead of filtering for each keystroke, use
     * a not-live filter edit and trigger the ActionListener using a Button
     * or by pressing <code>ENTER</code> in the filter edit field.
     */
    public void setLive(boolean live) {
        if(live) {
            if(filterModifyListener == null) {
                filterModifyListener = new FilterModifyListener();
                filterEdit.addModifyListener(filterModifyListener);
            }
        } else {
            if(filterModifyListener != null) {
                filterEdit.removeModifyListener(filterModifyListener);
                filterModifyListener = null;
            }
        }
    }

    /**
     * Gets a SelectionListener that refilters the list when it is fired. This
     * listener can be used to filter when the user presses a button.
     */
    public SelectionListener getFilterSelectionListener() {
        return filterSelectionListener;
    }

    /**
     * Implement the ModifyListener interface for text filter updates. When the
     * user edits the filter text field, this updates the filter to reflect
     * the current value of that text field.
     */
    class FilterModifyListener implements ModifyListener {
        public void modifyText(ModifyEvent e) {
            reFilter();
        }
    }

    /**
     * Implement the SelectionListener interface for text filter updates. When
     * the user clicks a button (supplied by external code), this
     * SelectionListener can be used to update the filter in response.
     */
    private class FilterSelectionListener implements SelectionListener {
        public void widgetDefaultSelected(SelectionEvent e) {
            reFilter();
        }
        public void widgetSelected(SelectionEvent e) {
            reFilter();
        }
    }

    /**
     * When the filter changes, first update the filter values used
     * to do filtering, then apply the filter on all list elements.
     */
    private void reFilter() {
        setFilterText(filterEdit.getText().split("[ \t]"));
    }
}
