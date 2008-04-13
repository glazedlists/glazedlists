/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.SearchEngineTextMatcherEditor;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;

/**
 * A {@link MatcherEditor} that matches elements that contain the filter text located
 * within a {@link Text} field. This {@link TextWidgetMatcherEditor} is directly
 * coupled with a {@link Text} that is meant to emulate a search engine's text box.
 * This matcher is fully concrete for use in SWT applications that want to present a text
 * filtering interface similiar to that of Google and other search engines.
 *
 * <p>If this {@link MatcherEditor} must be garbage collected before the underlying
 * Text, the listener can be unregistered by calling {@link #dispose()}.
 *
 * @author Holger Brands
 */
public class SearchEngineTextWidgetMatcherEditor<E> extends SearchEngineTextMatcherEditor<E> {

    /** the filter edit text field. */
    private Text text;

    /** the listener that triggers refiltering when events occur. */
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
    public SearchEngineTextWidgetMatcherEditor(Text text, TextFilterator<? super E> textFilterator) {
        super(textFilterator);
        this.text = text;
        this.text.addSelectionListener(filterChangeListener);

        // if the document is non-empty to begin with!
        refilter(this.text.getText());
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
    }

    /**
     * Implements the SelectionListener interface for text filter updates. When
     * the user clicks a button (supplied by external code), this
     * SelectionListener can be used to update the filter in response.
     */
    private class FilterChangeListener implements SelectionListener {
        public void widgetSelected(SelectionEvent e) {
            refilter(text.getText());
        }
        public void widgetDefaultSelected(SelectionEvent e) {
            refilter(text.getText());
        }
    }
}