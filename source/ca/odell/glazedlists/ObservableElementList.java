/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;

/**
 * A list that fires update events whenever elements are modified in place.
 * Changes to list elements are detected by registering an appropriate listener
 * on every list element. Listeners are registered as elements are added to
 * this list and unregistered as elements are removed from this list. Users
 * must specify an implementation of a {@link Connector} in the constructor
 * which contains the necessary logic for registering and unregistering a
 * appropriate listener for detecting modifications on an observable list
 * element.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>inserts: O(1), deletes: O(1), updates: O(1), elementChanged: O(n)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>8 bytes per element</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>{@link ObservableElementListTest}</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @see GlazedLists#beanConnector(Class)
 * @see GlazedLists#beanConnector(Class, String, String)
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=157">RFE 157</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author James Lemieux
 */
public class ObservableElementList extends TransformedList {

    /**
     * A list of the observed elements. It is necessary to track the observed
     * elements since list removals broadcast ListEvents which do not include
     * the removed element as part of the ListEvent. We use this list to locate
     * removed elements for the purpose of unregistering listeners from them.
     */
    private final List observedElements;

    /**
     * A list which parallels {@link #observedElements}. It stores the
     * {@link EventListener} associated with the observed element at the same
     * index within {@link #observedElements}.
     */
    private final List listeners;

    /**
     * The connector object containing the logic for registering and
     * unregistering a listener that detects changes within the observed
     * list elements and notifies this list of the change. The registered
     * listener is responsible for calling {@link #elementChanged(Object)}
     * to notify this list of the changed object.
     */
    private Connector elementConnector = null;

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
    public ObservableElementList(EventList source, Connector elementConnector) {
        super(source);

        this.elementConnector = elementConnector;

        // attach this list to the element connector so the listeners know
        // which List to notify of their modifications
        this.elementConnector.setObservableElementList(this);

        this.observedElements = new ArrayList(source);
        this.listeners = new ArrayList(source.size());

        // add listeners to all source list elements
        for (Iterator iter = this.iterator(); iter.hasNext();) {
            EventListener listener = this.connectElement(iter.next());
            this.listeners.add(listener);
        }

        // begin listening to the source list
        source.addListEventListener(this);
    }

    public void listChanged(ListEvent listChanges) {
        // add listeners to inserted list elements and remove listeners from deleted elements
        while(listChanges.next()) {
            final int changeIndex = listChanges.getIndex();
            final int changeType = listChanges.getType();

            // register a listener on the inserted object
            if (changeType == ListEvent.INSERT) {
                final Object inserted = get(changeIndex);
                final EventListener listener = this.connectElement(inserted);
                this.observedElements.add(changeIndex, inserted);
                this.listeners.add(changeIndex, listener);

            // unregister a listener on the deleted object
            } else if (changeType == ListEvent.DELETE) {
                final Object deleted = this.observedElements.remove(changeIndex);
                final EventListener listener = (EventListener) this.listeners.remove(changeIndex);
                this.disconnectElement(deleted, listener);

            // register/unregister listeners if the value at the changeIndex is now a different object
            } else if (changeType == ListEvent.UPDATE) {
                final Object previousValue = this.observedElements.get(changeIndex);
                final Object newValue = get(changeIndex);

                if (newValue != previousValue) {
                    this.disconnectElement(previousValue, (EventListener) this.listeners.get(changeIndex));
                    final EventListener listener = this.connectElement(newValue);
                    this.observedElements.set(changeIndex, newValue);
                    this.listeners.set(changeIndex, listener);
                }
            }
        }

        listChanges.reset();
        this.updates.forwardEvent(listChanges);
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
    private EventListener connectElement(Object listElement) {
        if (this.elementConnector == null)
            throw new IllegalStateException("This list has been disposed and can no longer be used.");

        return listElement == null ? null : this.elementConnector.installListener(listElement);
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
    private void disconnectElement(Object listElement, EventListener listener) {
        if (this.elementConnector == null)
            throw new IllegalStateException("This list has been disposed and can no longer be used.");

        if (listElement != null)
            this.elementConnector.uninstallListener(listElement, listener);
    }

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
    public void dispose() {
        // remove all listeners from all list elements
        for (int i = 0; i < this.observedElements.size(); i++) {
            final Object element = this.observedElements.get(i);
            final EventListener listener = (EventListener) this.listeners.get(i);
            this.disconnectElement(element, listener);
        }
        this.observedElements.clear();
        this.listeners.clear();

        // clear out the reference to this list from the associated connector
        this.elementConnector.setObservableElementList(null);

        // stop this list from wrongly connecting future list elements
        this.elementConnector = null;

        super.dispose();
    }

    /**
     * Handle a listener being fired for the specified element. This method
     * causes a ListEvent to be fired from this EventList indicating an update
     * occurred at all locations of the given <code>element</code>.
     *
     * <p>Note that element must be the exact object located within this list
     * (i.e. <code>element == get(i) for some i >= 0</code>).
     *
     * @param element the List element which has been modified.
     */
    public void elementChanged(Object element) {
        this.updates.beginEvent();

        // locate all indexes containing the given element
        for (int i = 0; i < size(); i++) {
            if (element == get(i))
                this.updates.addUpdate(i);
        }

        // ensure we found the element at least one time in this list
        final boolean foundElement = !this.updates.isEventEmpty();
        this.updates.commitEvent();

        // throw an IllegalStateException if the element could not be found at least
        // one time within this list since it probably represents a programmer error
        if (!foundElement)
            throw new IllegalStateException("Failed to find list element \"" + element + "\" in list " + this);
    }


    /**
     * An interface defining the methods required for registering and
     * unregistering change listeners on list elements within
     * {@link ObservableElementList}. Implementations typically install a
     * single listener, such as a {@link java.beans.PropertyChangeListener} on
     * list elements to detect changes in the state of the element. The
     * installed listener implementation in turn calls
     * {@link ObservableElementList#elementChanged(Object)} in order to have
     * the list broadcast an update at the index of the object.
     */
    public interface Connector {
        /**
         * Start listening for events from the specified <code>element</code>.
         *
         * @param element the element to be observed
         * @return the listener that was installed on the <code>element</code>
         *      to be used as a parameter to
         *      {@link #uninstallListener(Object, EventListener)}
         */
        public EventListener installListener(Object element);

        /**
         * Stop listening for events from the specified <code>element</code>.
         *
         * @param element the element to be observed
         * @param listener the listener as returned by {@link #installListener(Object)}.
         */
        public void uninstallListener(Object element, EventListener listener);

        /**
         * Sets the {@link ObservableElementList} to notify when changes occur
         * on elements.
         *
         * @param list the ObservableElementList containing the elements to
         *      observe
         */
        public void setObservableElementList(ObservableElementList list);
    }
}