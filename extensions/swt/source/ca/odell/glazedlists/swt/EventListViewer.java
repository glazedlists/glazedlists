/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;

/**
 * A view helper that displays an {@link EventList} in a {@link List}.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * @deprecated Use {@link DefaultEventListViewer} instead. This class will be removed in the GL
 *             2.0 release. The wrapping of the source list with an SWT-EDT safe list has been
 *             determined to be undesirable (it is better for the user to provide their own SWT-EDT
 *             safe list).
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author Holger Brands
 */
public class EventListViewer<E> extends DefaultEventListViewer<E> {

	/** indicates, if source list has to be disposed */
    private boolean disposeSource;

    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements will simply be displayed as the result of calling
     * toString() on the contents of the source list.
     * 
     * @param source the EventList that provides the elements
     * @param list the list
     */
    public EventListViewer(EventList<E> source, List list) {
        this(source, list, new LabelProvider());
    }

    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements are formatted using the provided {@link ILabelProvider}.
     * 
     * @param source the EventList that provides the elements
     * @param list the list
     * @param labelProvider a LabelProvider for formatting the displayed values
     * 
     * @see ILabelProvider
     * @see GlazedListsSWT#beanLabelProvider(String)
     */
    public EventListViewer(EventList<E> source, List list, ILabelProvider labelProvider) {
    	super(createProxyList(source, list.getDisplay()), list, labelProvider);
    	disposeSource = (this.source != source);
    }

    /**
     * Releases the resources consumed by this {@link EventListViewer} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventListViewer} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventListViewer}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link EventListViewer} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link EventListViewer} after it has been disposed.
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