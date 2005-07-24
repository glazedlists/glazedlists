/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                     publicbobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.*;
// for working with SWT Text widgets
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

/**
 * A {@link MatcherEditor} that matches elements that contain the filter text located
 * within a {@link Text} field. This {@link TextWidgetMatcherEditor} is directly
 * coupled with a {@link Text} and fires {@link MatcherEditor} events in response to
 * {@link ModifyEvent}s received from the {@link Text}. This matcher is fully
 * concrete for use in SWT applications.
 *
 * <p>If this {@link MatcherEditor} must be garbage collected before the underlying
 * Text, the listener can be unregistered by calling {@link #dispose()}.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class TextWidgetMatcherEditor extends TextMatcherEditor {

    /** the filter edit text field */
    private Text text;

    /** the listener that triggers refiltering when events occur */
    private FilterChangeListener filterChangeListener = new FilterChangeListener();

    /**
     * Creates a TextWidgetMatcherEditor bound to the provided {@link Text}
     * with the given <code>textFilterator</code>.
     *
     * @param text the {@link Text} widget that drives the text-filtering
     * @param textFilterator an object capable of producing Strings from the
     *      objects being filtered. If <code>textFilterator</code> is
     *      <code>null</code> then all filtered objects are expected to
     *      implement {@link ca.odell.glazedlists.TextFilterable}.
     *
     * @see GlazedLists#textFilterator(String[])
     */
    public TextWidgetMatcherEditor(Text text, TextFilterator textFilterator) {
        this(text, textFilterator, true);
    }

    /**
     * Creates a TextWidgetMatcherEditor bound to the provided {@link Text}
     * with the given <code>textFilterator</code> where filtering can
     * be specified as "live" or to be based on another event such as the
     * user pressing Enter or a button being clicked.
     *
     * @param text the {@link Text} widget that drives the text-filtering
     * @param textFilterator an object capable of producing Strings from the
     *      objects being filtered. If <code>textFilterator</code> is
     *      <code>null</code> then all filtered objects are expected to
     *      implement {@link ca.odell.glazedlists.TextFilterable}.
     * @param live <code>true</code> to filter by the keystroke or <code>false</code>
     *      to filter only when the ENTER key is pressed within the Text.  Optionnally,
     *      you can use the {@link SelectionListener} provided via {@link #getFilterSelectionListener()}
     *      to register a Button or other component to trigger filtering.
     *
     * @see GlazedLists#textFilterator(String[])
     */
    public TextWidgetMatcherEditor(Text text, TextFilterator textFilterator, boolean live) {
        super(textFilterator);
        this.text = text;

        // add the appropriate listeners
        this.text.addSelectionListener(filterChangeListener);
        if(live) this.text.addModifyListener(filterChangeListener);

        // if the document is non-empty to begin with!
        refilter();
    }

    /**
     * Gets a SelectionListener that refilters the list when it is fired. This
     * listener can be used to filter when the user presses a 'Search' button.
     */
    public SelectionListener getFilterSelectionListener() {
        return filterChangeListener;
    }

    /**
     * A cleanup method which stops this MatcherEditor from listening to
     * changes on the {@link Text} component, thus freeing the
     * MatcherEditor to be garbage collected.  Garbage collection could be
     * blocked if you have registered the SelectionListener provided by
     * {@link #getFilterSelectionListener()} and not removed that listener
     * (of disposed of the widget it was registered to).
     */
    public void dispose() {
        text.removeSelectionListener(filterChangeListener);
        text.removeModifyListener(filterChangeListener);
    }

    /**
     * Refilter based on the new contents of the Text..
     */
    private void refilter() {
        setFilterText(text.getText().split("[ \t]"));
    }

    /**
     * Implements the SelectionListener interface for text filter updates. When
     * the user clicks a button (supplied by external code), this
     * SelectionListener can be used to update the filter in response.
     */
    private class FilterChangeListener implements SelectionListener, ModifyListener {
        public void widgetSelected(SelectionEvent e) {
            refilter();
        }
        public void widgetDefaultSelected(SelectionEvent e) {
            refilter();
        }
        public void modifyText(ModifyEvent e) {
            refilter();
        }
    }
}