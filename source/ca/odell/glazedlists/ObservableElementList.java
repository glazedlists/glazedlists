/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.adt.Barcode;
import ca.odell.glazedlists.impl.adt.BarcodeIterator;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

/**
 * A list that fires update events whenever elements are modified in place.
 * Changes to list elements are detected by registering an appropriate listener
 * on every list element. Listeners are registered as elements are added to
 * this list and unregistered as elements are removed from this list. Users
 * must specify an implementation of a {@link Connector} in the constructor
 * which contains the necessary logic for registering and unregistering a
 * listener capable of detecting modifications to an observable list element.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe; elementChanged(), however, is thread ready</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>inserts: O(1), deletes: O(1), updates: O(1), elementChanged: O(n)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>8 bytes per element</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>ObservableElementListTest</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @see GlazedLists#beanConnector(Class)
 * @see GlazedLists#beanConnector(Class, String, String)
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=157">RFE 157</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author James Lemieux
 */
public class ObservableElementList<E> extends TransformedList<E, E> {

    /**
     * A list of the observed elements. It is necessary to track the observed
     * elements since list removals broadcast ListEvents which do not include
     * the removed element as part of the ListEvent. We use this list to locate
     * removed elements for the purpose of unregistering listeners from them.
     * todo remove this list when ListEvent can reliably furnish us with a deleted value
     */
    private List<E> observedElements;

    /**
     * The connector object containing the logic for registering and
     * unregistering a listener that detects changes within the observed
     * list elements and notifies this list of the change. The registered
     * listener is responsible for calling {@link #elementChanged(Object)}
     * to notify this list of the changed object.
     */
    private Connector<? super E> elementConnector = null;

    /**
     * <tt>true</tt> indicates a single shared EventListener is used for each
     * element being observed. Consequently, {@link #singleEventListenerRegistry}
     * is the compact data structure used to track which elements are being
     * listened to by the {@link #singleEventListener}. <tt>false</tt>
     * indicates {@link #multiEventListenerRegistry} is used to track each
     * individual EventListener installed on each individual list element.
     */
    private boolean singleListenerMode = true;

    /**
     * A list which parallels {@link #observedElements}. It stores the unique
     * {@link EventListener} associated with the observed element at the same
     * index within {@link #observedElements}.
     */
    private List<EventListener> multiEventListenerRegistry = null;

    /**
     * The single {@link EventListener} shared by all list elements if a
     * common listener is returned from the {@link Connector} of this list.
     */
    private EventListener singleEventListener = null;

    /**
     * The compact data structure which identifies the observed elements that
     * have had the {@link #singleEventListener} registered on them.
     * {@link Barcode#BLACK} indicates the {@link #singleEventListener} has
     * been registered on the element at the index; {@link Barcode#WHITE}
     * indicates no listener was registered on the element at the index.
     */
    private Barcode singleEventListenerRegistry = null;

    /**
     * Constructs an <code>ObservableElementList</code> which wraps the given
     * <code>source</code> and uses the given <code>elementConnector</code> to
     * register/unregister change listeners on elements of the
     * <code>source</code>.
     *
     * @param source the {@link EventList} to transform
     * @param elementConnector the {@link Connector} to consult when list
     *      elements are added or removed and thus element listeners must be
     *      registered or unregistered. Note that this constructor attachs
     *      this list to the given <code>elementConnector</code> by calling
     *      {@link Connector#setObservableElementList(ObservableElementList)}.
     */
    public ObservableElementList(EventList<E> source, Connector<? super E> elementConnector) {
        super(source);

        this.elementConnector = elementConnector;

        // attach this list to the element connector so the listeners know
        // which List to notify of their modifications
        this.elementConnector.setObservableElementList(this);

        // for speed, we add all source elements together, rather than individually
        this.observedElements = new ArrayList<E>(source);

        // we initialize the single EventListener registry, as we optimistically
        // assume we'll be using a single listener for all observed elements
        this.singleEventListenerRegistry = new Barcode();
        this.singleEventListenerRegistry.addWhite(0, source.size());

        // add listeners to all source list elements
        for (int i = 0, n = size(); i < n; i++) {
            // connect a listener to the element
            final EventListener listener = this.connectElement(get(i));

            // record the listener in the registry
            this.registerListener(i, listener, false);
        }

        // begin listening to the source list
        source.addListEventListener(this);
    }

    @Override
    public void listChanged(ListEvent<E> listChanges) {
        if (this.observedElements == null)
            throw new IllegalStateException("This list has been disposed and can no longer be used.");

        // add listeners to inserted list elements and remove listeners from deleted elements
        while(listChanges.next()) {
            final int changeIndex = listChanges.getIndex();
            final int changeType = listChanges.getType();

            // register a listener on the inserted object
            if (changeType == ListEvent.INSERT) {
                final E inserted = get(changeIndex);
                this.observedElements.add(changeIndex, inserted);

                // connect a listener to the freshly inserted element
                final EventListener listener = this.connectElement(inserted);
                // record the listener in the registry
                this.registerListener(changeIndex, listener, false);

            // unregister a listener on the deleted object
            } else if (changeType == ListEvent.DELETE) {
                // try to get the previous value through the ListEvent
                E deleted = listChanges.getOldValue();
                E deletedElementFromPrivateCopy = this.observedElements.remove(changeIndex);

                // if the ListEvent could give us the previous value, use the value from our private copy of the source
                if (deleted == ListEvent.UNKNOWN_VALUE)
                    deleted = deletedElementFromPrivateCopy;

                // remove the listener from the registry
                final EventListener listener = this.unregisterListener(changeIndex);
                // disconnect the listener from the freshly deleted element
                this.disconnectElement(deleted, listener);

            // register/unregister listeners if the value at the changeIndex is now a different object
            } else if (changeType == ListEvent.UPDATE) {
                E previousValue = listChanges.getOldValue();

                // if the ListEvent could give us the previous value, use the value from our private copy of the source
                if (previousValue == ListEvent.UNKNOWN_VALUE)
                    previousValue = this.observedElements.get(changeIndex);

                final E newValue = get(changeIndex);

                // if a different object is present at the index
                if (newValue != previousValue) {
                    this.observedElements.set(changeIndex, newValue);

                    // disconnect the listener from the previous element at the index
                    this.disconnectElement(previousValue, this.getListener(changeIndex));
                    // connect the listener to the new element at the index
                    final EventListener listener = this.connectElement(newValue);
                    // replace the old listener in the registry with the new listener for the new element
                    this.registerListener(changeIndex, listener, true);
                }
            }
        }

        listChanges.reset();
        this.updates.forwardEvent(listChanges);
    }

    /**
     * A convenience method for adding a listener into the appropriate listener
     * registry. The <code>listener</code> will be registered at the specified
     * <code>index</code> and will be added if <code>replace</code> is <tt>true</tt>
     * or will replace any existing listener at the <code>index</code> if
     * <code>replace</code> is <tt>false</tt>.
     *
     * @param index the index of the observed element the <code>listener</code>
     *      is attached to
     * @param listener the {@link EventListener} registered to the observed
     *      element at the given <code>index</code>
     * @param replace <tt>true</tt> indicates the listener should be replaced
     *      at the given index; <tt>false</tt> indicates it should be added
     */
    private void registerListener(int index, EventListener listener, boolean replace) {
        if (replace) {
            // if replace is false, we should call set() on the appropriate registry
            if (this.singleListenerMode)
                this.singleEventListenerRegistry.set(index, listener == null ? Barcode.WHITE : Barcode.BLACK, 1);
            else
                this.multiEventListenerRegistry.set(index, listener);
        } else {
            // if replace is true, we should call replace() on the appropriate registry
            if (this.singleListenerMode)
                this.singleEventListenerRegistry.add(index, listener == null ? Barcode.WHITE : Barcode.BLACK, 1);
            else
                this.multiEventListenerRegistry.add(index, listener);
        }
    }

    /**
     * Returns the {@link EventListener} at the given <code>index</code>.
     *
     * @param index the location of the {@link EventListener} to be returned
     * @return the {@link EventListener} at the given <code>index</code>
     */
    private EventListener getListener(int index) {
        EventListener listener = null;

        if (this.singleListenerMode) {
            if (this.singleEventListenerRegistry.get(index) == Barcode.BLACK)
                listener = this.singleEventListener;
        } else {
            listener = this.multiEventListenerRegistry.get(index);
        }

        return listener;
    }

    /**
     * A convenience method for removing a listener at the specified
     * <code>index</code> from the appropriate listener registry.
     *
     * @param index the index of the {@link EventListener} to be unregistered
     * @return the EventListener that was unregistered or <code>null</code> if
     *      no EventListener existed at the given <code>index</code>
     */
    private EventListener unregisterListener(int index) {
        EventListener listener = null;

        if (this.singleListenerMode) {
            if (this.singleEventListenerRegistry.get(index) == Barcode.BLACK)
                listener = this.singleEventListener;
            this.singleEventListenerRegistry.remove(index, 1);
        } else {
            listener = this.multiEventListenerRegistry.remove(index);
        }

        return listener;
    }

    /**
     * A convenience method to connect listeners to the given
     * <code>listElement</code>.
     *
     * @param listElement the list element to connect change listeners to
     * @return the listener that was connected to the <code>listElement</code>
     *      or <code>null</code> if no listener was registered
     * @throws IllegalStateException if this list has been disposed and is
     *      thus no longer in a state to be managing listener registrations on
     *      list elements
     */
    private EventListener connectElement(E listElement) {
        // listeners cannot be installed on null listElements
        if (listElement == null)
            return null;

        // use the elementConnector to install a listener on the listElement
        final EventListener listener = this.elementConnector.installListener(listElement);

        // test if the new listener transfers us from single event mode to multi event mode
        if (this.singleListenerMode && listener != null) {
            if (this.singleEventListener == null)
                this.singleEventListener = listener;
            else if (listener != this.singleEventListener)
                this.switchToMultiListenerMode();
        }

        return listener;
    }

    /**
     * A convenience method to disconnect the <code>listener</code> from the
     * given <code>listElement</code>.
     *
     * @param listElement the list element to disconnect the
     *      <code>listener</code> from
     * @throws IllegalStateException if this list has been disposed and is
     *      thus no longer in a state to be managing listener registrations on
     *      list elements
     */
    private void disconnectElement(E listElement, EventListener listener) {
        if (listElement != null && listener != null)
            this.elementConnector.uninstallListener(listElement, listener);
    }

    /**
     * This method converts the data structures which are optimized for storing
     * a single instance of an EventListener shared amongst all observed
     * elements into data structures which are appropriate for storing
     * individual instances of EventListeners for each observed element.
     *
     * <p>Note: this is a one-time switch only and cannot be reversed
     */
    private void switchToMultiListenerMode() {
        if (!this.singleListenerMode) throw new IllegalStateException();

        // build a new data structure appropriate for storing individual
        // listeners for each observed element
        this.multiEventListenerRegistry = new ArrayList<EventListener>(this.source.size());
        for (int i = 0; i < source.size(); i++)
            this.multiEventListenerRegistry.add(null);

        // for each black entry in the singleEventListenerRegistry create an
        // entry in the multiEventListenerRegistry at the corresponding index
        // for the singleEventListener
        for (BarcodeIterator iter = this.singleEventListenerRegistry.iterator(); iter.hasNextBlack();) {
            iter.nextBlack();
            this.multiEventListenerRegistry.set(iter.getIndex(), this.singleEventListener);
        }
        
        // null out the reference to the single EventListener,
        // since we'll now track the EventListener for each element
        this.singleEventListener = null;

        // null out the reference to the single EventList registry, since we
        // are replacing its listener tracking mechanism with the multiEventListenerRegistry
        this.singleEventListenerRegistry = null;

        // indicate this list is no longer in single listener mode meaning we
        // no longer assume the same listener is installed on every element
        this.singleListenerMode = false;
    }

    @Override
    protected boolean isWritable() {
        return true;
    }

    /**
     * Releases the resources consumed by this {@link TransformedList} so that
     * it may eventually be garbage collected.
     *
     * In this case of this {@link TransformedList}, it uses the
     * {@link Connector} to remove all listeners from their associated list
     * elements and finally removes the reference to this list from the
     * Connector by calling
     * {@link Connector#setObservableElementList(ObservableElementList)} with a
     * <code>null</code> argument.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link TransformedList} after it has been disposed.
     */
    @Override
    public void dispose() {
        // remove all listeners from all list elements
        for (int i = 0, n = this.observedElements.size(); i < n; i++) {
            final E element = this.observedElements.get(i);
            final EventListener listener = this.getListener(i);
            this.disconnectElement(element, listener);
        }

        // clear out the reference to this list from the associated connector
        this.elementConnector.setObservableElementList(null);

        // null out all references to internal data structures
        this.observedElements = null;
        this.multiEventListenerRegistry = null;
        this.singleEventListener = null;
        this.singleEventListenerRegistry = null;
        this.elementConnector = null;

        super.dispose();
    }

    /**
     * Handle a listener being notified for the specified <code>listElement</code>.
     * This method causes a ListEvent to be fired from this EventList indicating
     * an update occurred at all locations of the given <code>listElement</code>.
     *
     * <p>Note that listElement must be the exact object located within this list
     * (i.e. <code>listElement == get(i) for some i >= 0</code>).
     *
     * <p>This method acquires the write lock for this list before locating the
     * <code>listElement</code> and broadcasting its update. It is assumed that
     * this method may be called on any Thread, so to decrease the burdens of
     * the caller in achieving multi-threaded correctness, this method is
     * Thread ready.
     *
     * @param listElement the list element which has been modified
     */
    public void elementChanged(Object listElement) {
        if (this.observedElements == null)
            throw new IllegalStateException("This list has been disposed and can no longer be used.");

        getReadWriteLock().writeLock().lock();
        try {
            this.updates.beginEvent();

            // locate all indexes containing the given listElement
            for (int i = 0, n = size(); i < n; i++) {
            	final E currentElement = get(i);
                if (listElement == currentElement) {
                    this.updates.elementUpdated(i, currentElement);
                }
            }

            this.updates.commitEvent();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }


    /**
     * An interface defining the methods required for registering and
     * unregistering change listeners on list elements within an
     * {@link ObservableElementList}. Implementations typically install a
     * single listener, such as a {@link java.beans.PropertyChangeListener} on
     * list elements to detect changes in the state of the element. The
     * installed listener implementation in turn calls
     * {@link ObservableElementList#elementChanged(Object)} in order to have
     * the list broadcast an update at the index of the object.
     */
    public interface Connector<E> {
        /**
         * Start listening for events from the specified <code>element</code>.
         * Alternatively, if the <code>element</code> does not require a
         * listener to be attached to it (e.g. the <code>element</code> is
         * immutable), <code>null</code> may be returned to signal that no
         * listener was installed.
         *
         * @param element the element to be observed
         * @return the listener that was installed on the <code>element</code>
         *      to be used as a parameter to {@link #uninstallListener(Object, EventListener)}.
         *      <code>null</code> is taken to mean no listener was installed
         *      and thus {@link #uninstallListener(Object, EventListener)} need
         *      not be called.
         */
        public EventListener installListener(E element);

        /**
         * Stop listening for events from the specified <code>element</code>.
         *
         * @param element the element to be observed
         * @param listener the listener as returned by {@link #installListener(Object)}.
         */
        public void uninstallListener(E element, EventListener listener);

        /**
         * Sets the {@link ObservableElementList} to notify when changes occur
         * on elements.
         *
         * @param list the ObservableElementList containing the elements to
         *      observe
         */
        public void setObservableElementList(ObservableElementList<? extends E> list);
    }
}