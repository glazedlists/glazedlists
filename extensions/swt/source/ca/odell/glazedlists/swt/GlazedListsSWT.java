/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ThresholdList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.impl.swt.*;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.*;


/**
 * A factory for creating all sorts of objects to be used with Glazed Lists.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class GlazedListsSWT {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedListsSWT() {
        throw new UnsupportedOperationException();
    }

    // EventLists // // // // // // // // // // // // // // // // // // // // //

    /**
     * Wraps the source in an {@link EventList} that fires all of its update events
     * from the SWT user interface thread.
     */
    public static <E> TransformedList<E, E> swtThreadProxyList(EventList<E> source, Display display) {
        return new SWTThreadProxyEventList<E>(source, display);
    }

    /**
     * Returns true if <code>list</code> is an {@link EventList} that fires
     * all of its update events from the SWT event dispatch thread.
     */
    public static boolean isSWTThreadProxyList(EventList list) {
        return list instanceof SWTThreadProxyEventList;
    }

    /**
     * while holding a read lock, this method wraps the given source list with a SWT thread
     * proxy list if necessary.
     */
    static <E> EventList<E> createProxyListIfNecessary(EventList<E> source, Display display) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableModel
        EventList<E> result = source;
        source.getReadWriteLock().readLock().lock();
        try {
            final TransformedList<E,E> decorated = createSwtThreadProxyListIfNecessary(source, display);

            // if the create method actually returned a decorated form of the source,
            // record it so it may later be disposed
            if (decorated != null && decorated != source) {
                result = decorated;
            }
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
        return result;
    }

    /** wraps the given source list with a SWT thread proxy list, if necessary */
    private static <E> TransformedList<E,E> createSwtThreadProxyListIfNecessary(EventList<E> source, Display display) {
        return GlazedListsSWT.isSWTThreadProxyList(source) ? null : GlazedListsSWT.swtThreadProxyList(source, display);
    }    

    
    // LabelProviders // // // // // // // // // // // // // // // // // // //

    /**
     * Creates an {@link ILabelProvider} that returns labels for Objects via
     * Relection. The label returned will be the String value of specified
     * JavaBean property.
     */
    public static ILabelProvider beanLabelProvider(String property) {
        return new BeanLabelProvider(property);
    }
    

    // ThresholdViewers // // // // // // // // // // // // // // // // // // //

    /**
     * Creates a viewer that manipulates the lower bound of the specified
     * ThresholdList based on changes to the Slider.  The ThresholdList linked
     * to this viewer type will contain a range of Objects between the results
     * of getSelection() and getMaximum() on the Slider.
     */
    public static SelectionListener lowerThresholdViewer(ThresholdList thresholdList, Slider slider) {
        return new LowerThresholdViewer(thresholdList, BoundedRangeControlFactory.slider(slider));
    }

    /**
     * Creates a viewer that manipulates the lower bound of the specified
     * ThresholdList based on changes to Scale selection.  The ThresholdList
     * linked to this viewer type will contain a range of Objects between the
     * results of getSelection() and getMaximum() on the Scale.
     */
    public static SelectionListener lowerThresholdViewer(ThresholdList thresholdList, Scale scale) {
        return new LowerThresholdViewer(thresholdList, BoundedRangeControlFactory.scale(scale));
    }

    /**
     * Creates a viewer that manipulates the upper bound of the specified
     * ThresholdList based on changes to the Slider.  The ThresholdList linked
     * to this model type will contain a range of Objects between the results
     * of getMinimum() and getSelection() on the Slider.
     */
    public static SelectionListener upperThresholdViewer(ThresholdList thresholdList, Slider slider) {
        return new UpperThresholdViewer(thresholdList, BoundedRangeControlFactory.slider(slider));
    }

    /**
     * Creates a viewer that manipulates the upper bound of the specified
     * ThresholdList based on changes to Scale selection.  The ThresholdList
     * linked to this viewer type will contain a range of Objects between the
     * results of getMinimum() and getSelection() on the Scale.
     */
    public static SelectionListener upperThresholdViewer(ThresholdList thresholdList, Scale scale) {
        return new UpperThresholdViewer(thresholdList, BoundedRangeControlFactory.scale(scale));
    }
    
    // Viewer convenience factory methods
    
    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements will simply be displayed as the result of calling
     * toString() on the contents of the source list.
     * 
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * To do this programmatically, use {@link Display#syncExec(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     * </p>
     * 
     * @param source the EventList that provides the elements
     * @param list the list
     */
    public static <E> DefaultEventListViewer<E> eventListViewer(EventList<E> source, List list) {
    	return new DefaultEventListViewer<E>(source, list);
    }

    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements will simply be displayed as the result of calling
     * toString() on the contents of the source list.
     * 
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * </p>
     * 
     * @param source the EventList that provides the elements
     * @param list the list
     */
    public static <E> DefaultEventListViewer<E> eventListViewerWithThreadProxyList(EventList<E> source, List list) {
    	final EventList<E> proxySource = createSwtThreadProxyList(source, list.getDisplay());
    	return new DefaultEventListViewer<E>(proxySource, list, new LabelProvider(), true);
    }
        
    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} compoment will be
     * the result of calling toString() on the Objects found in source.
     * 
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * To do this programmatically, use {@link Display#syncExec(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     * </p>
     * 
     * @param source the EventList that provides the elements
     * @param combo the combo box
     */
    public static <E> DefaultEventComboViewer<E> eventComboViewer(EventList<E> source, Combo combo) {
    	return new DefaultEventComboViewer<E>(source, combo);
    }

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} compoment will be
     * the result of calling toString() on the Objects found in source.
     * 
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * </p>
     * 
     * @param source the EventList that provides the elements
     * @param combo the combo box
     */
    public static <E> DefaultEventComboViewer<E> eventComboViewerWithThreadProxyList(EventList<E> source, Combo combo) {
    	final EventList<E> proxySource = createSwtThreadProxyList(source, combo.getDisplay());
    	return new DefaultEventComboViewer<E>(proxySource, combo, new LabelProvider(), true);
    }
    
    /** Helper method to create a SwtThreadProxyList with read locks. */
    private static <E> EventList<E> createSwtThreadProxyList(EventList<E> source, Display display) {
        final EventList<E> result;
        source.getReadWriteLock().readLock().lock();
        try {
            result = GlazedListsSWT.swtThreadProxyList(source, display);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
        return result;
    }
}