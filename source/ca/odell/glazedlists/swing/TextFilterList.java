/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.matchers.*;
// access to the volatile implementation pacakge
import ca.odell.glazedlists.impl.filter.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
// for automatically responding to changes in the filter field
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * An {@link EventList} that shows only elements that contain a filter text string.
 * The {@link TextFilterList} uses a {@link JTextField} to allow the user to edit
 * the filter text. As this filter text is edited, the contents of the
 * {@link TextFilterList} are changed to reflect the elements that match the text.
 *
 * <p>The {@link TextFilterList} either requires that a {@link TextFilterator} be
 * specified in its constructor, or that every object in the source list implements
 * the {@link TextFilterable} interface. These are used to specify the {@link String}s
 * to search for each element.
 *
 * <p>The {@link TextFilterList} initially refilters the list after each change in
 * the {@link JTextField}. If this live filtering does not have adequate performance,
 * it can be turned off. In this case, the list will refiltered by pressing
 * <tt>ENTER</tt> in the {@link JTextField} and on every action to the {@link ActionListener}.
 * This {@link ActionListener} will be returned from the method {@link #getFilterActionListener()}
 * and can be used to refilter in response to a button click.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N), filter changes O(N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>O(N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TextFilterList extends TransformedList {

    /** the text matcher editor does all the real work */
    private TextMatcherEditor textMatcherEditor;
    
    /** the field where the filter strings are edited */
    private JTextField filterEdit = null;

    /** the document listener responds to changes, it is null when we're not listening */
    private FilterEditListener filterEditListener = null;

    /** the action listener performs a refilter when fired */
    private FilterActionListener filterActionListener = new FilterActionListener();

    /**
     * Creates a {@link TextFilterList} that filters the specified {@link EventList}
     * of elements that implement the {@link TextFilterable} interface.
     */
    public TextFilterList(EventList source) {
        this(source, (TextFilterator)null, new JTextField(""));
    }

    /**
     * Creates a {@link TextFilterList} that filters the specified {@link EventList}
     * of elements using the specified {@link TextFilterator} to get the
     * {@link String}s to search.
     */
    public TextFilterList(EventList source, TextFilterator filterator) {
        this(source, filterator, new JTextField(""));
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
        this(source, GlazedLists.textFilterator(propertyNames), new JTextField(""));
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
    public TextFilterList(EventList source, String[] propertyNames, JTextField filterEdit) {
        this(source, GlazedLists.textFilterator(propertyNames), filterEdit);
    }

    /**
     * Creates a {@link TextFilterList} that filters the specified {@link EventList}
     * of elements using the specified {@link TextFilterator} to get the
     * {@link String}s to search.
     *
     * @param filterEdit a text field for typing in the filter text.
     */
    public TextFilterList(EventList source, TextFilterator filterator, JTextField filterEdit) {
        super(new FilterList(source));
        textMatcherEditor = new TextMatcherEditor(filterator);
        ((FilterList)this.source).setMatcherEditor(textMatcherEditor);

        // listen to filter events
        this.setFilterEdit(filterEdit);
        
        // handle changes
        this.source.addListEventListener(this);
    }

    /**
     * Gets the {@link JTextField} used to edit the filter search {@link String}.
     */
    public JTextField getFilterEdit() {
        return filterEdit;
    }

    /**
     * Sets the {@link JTextField} used to edit the filter search {@link String}.
     */
    public void setFilterEdit(JTextField filterEdit) {
        boolean live = true;

        // stop listening on filter events from the old filter edit
        if(this.filterEdit != null) {
            this.filterEdit.removeActionListener(filterActionListener);
            live = (filterEditListener != null);
            setLive(false);
        }

        // start listening for filter events from the new filter edit
        this.filterEdit = filterEdit;
        filterEdit.addActionListener(filterActionListener);
        setLive(live);

        // filter with the new filter edit
        reFilter();
    }

    /**
     * Directs this filter to respond to changes to the {@link JTextField} as they are
     * made. This uses a {@link DocumentListener} and every time the
     * {@link JTextField} is modified, the list is refiltered.
     *
     * <p>To avoid the processing overhead of filtering for each keystroke, use
     * a not-live filter edit and trigger the {@link ActionListener} using a
     * button or by pressing <tt>ENTER</tt> in the {@link JTextField}.
     */
    public void setLive(boolean live) {
        if(live) {
            if(filterEditListener == null) {
                filterEditListener = new FilterEditListener();
                filterEdit.getDocument().addDocumentListener(filterEditListener);
            }
        } else {
            if(filterEditListener != null) {
                filterEdit.getDocument().removeDocumentListener(filterEditListener);
                filterEditListener = null;
            }
        }
    }

    /**
     * Gets an {@link ActionListener} that refilters the list when it is fired. This
     * listener can be used to filter when the user presses a button.
     */
    public ActionListener getFilterActionListener() {
        return filterActionListener;
    }

    /**
     * Implement the {@link DocumentListener} interface for text filter updates. When
     * The user edits the filter {@link JTextField}, this updates the filter to reflect
     * the current value of that {@link JTextField}.
     */
    private class FilterEditListener implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {
            reFilter();
        }
        public void insertUpdate(DocumentEvent e) {
            reFilter();
        }
        public void removeUpdate(DocumentEvent e) {
            reFilter();
        }
    }

    /**
     * Implement the {@link ActionListener} interface for text filter updates. When
     * the user clicks a button (supplied by external code), this
     * {@link ActionListener} can be used to update the filter in response.
     */
    private class FilterActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            reFilter();
        }
    }

    /**
     * When the filter changes, first update the filter values used
     * to do filtering, then apply the filter on all list elements.
     */
    private void reFilter() {
        textMatcherEditor.setFilterText(filterEdit.getText().split("[ \t]"));
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // just pass on the changes
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void dispose() {
        FilterList filteredSource = (FilterList)source;
        super.dispose();
        filteredSource.dispose();
    }
}