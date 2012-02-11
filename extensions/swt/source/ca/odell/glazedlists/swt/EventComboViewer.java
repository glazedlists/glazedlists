/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;

/**
 * A view helper that displays an {@link EventList} in a {@link Combo} component.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * @deprecated Use {@link DefaultEventComboViewer} instead. This class will be removed in the GL
 *             2.0 release. The wrapping of the source list with an SWT-EDT safe list has been
 *             determined to be undesirable (it is better for the user to provide their own SWT-EDT
 *             safe list).
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author Holger Brands
 */
public class EventComboViewer<E> extends DefaultEventComboViewer<E> {

    /** indicates, if source list has to be disposed */
    private boolean disposeSource;

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} compoment will be
     * the result of calling toString() on the Objects found in source.
     */
    public EventComboViewer(EventList<E> source, Combo combo) {
        this(source, combo, new DefaultItemFormat<E>());
    }

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} compoment will be
     * formatted using the provided {@link ItemFormat}.
     *
     * @see ItemFormat
     * @see GlazedListsSWT#beanItemFormat(String)
     */
    public EventComboViewer(EventList<E> source, Combo combo, ItemFormat<? super E> itemFormat) {
    	super(createProxyList(source, combo.getDisplay()), combo, itemFormat);
    	disposeSource = (this.source != source);
    }

    /**
     * Releases the resources consumed by this {@link EventComboViewer} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventComboViewer} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventComboViewer}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link EventComboViewer} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link EventComboViewer} after it has been disposed.
     */
    public void dispose() {
        if (disposeSource) source.dispose();
        super.dispose();
    }

    /**
     * while holding a read lock, this method wraps the given source list with a SWT thread
     * proxy list.
     */
    private static <E> EventList<E> createProxyList(EventList<E> source, Display display) {
    	return GlazedListsSWT.createProxyListIfNecessary(source, display);
    }
}