/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.EventListener;


/**
 * A list that fires update events whenever an element is modified. Changes are
 * handled by registering a listener against every list element.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=157">RFE 157</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ObservableElementList extends TransformedList {

    private List observedElements = new ArrayList();
    private List listeners = new ArrayList();
    private Connector elementConnector = null;

    public ObservableElementList(EventList source, Connector elementConnector) {
        super(source);
        this.elementConnector = elementConnector;

        this.elementConnector.setObservableElementList(this);
        source.addListEventListener(this);
    }

    public void listChanged(ListEvent listChanges) {
        while(listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            if(changeType == ListEvent.INSERT) {
                Object inserted = get(changeIndex);
                EventListener listener = inserted != null ? elementConnector.installListener(inserted) : null;
                observedElements.add(changeIndex, inserted);
                listeners.add(changeIndex, listener);

            } else if(changeType == ListEvent.DELETE) {
                Object deleted = observedElements.remove(changeIndex);
                EventListener listener = (EventListener)listeners.remove(changeIndex);
                if(deleted != null) elementConnector.uninstallListener(deleted, listener);

            } else if(changeType == ListEvent.UPDATE) {
                Object newValue = get(changeIndex);
                Object previousValue = observedElements.get(changeIndex);
                if(newValue == previousValue) continue;
                if(previousValue != null) elementConnector.uninstallListener(previousValue, (EventListener)listeners.get(changeIndex));
                EventListener listener = newValue != null ? elementConnector.installListener(newValue) : null;
                observedElements.set(changeIndex, newValue);
                listeners.set(changeIndex, listener);
            }
        }

        listChanges.reset();
        updates.forwardEvent(listChanges);
    }

    protected boolean isWritable() {
        return true;
    }

    /**
     * Handle a listener being fired for the specified element.
     *
     * @param element the List element which has been modified.
     */
    public void elementChanged(Object element) {
        for(int i = 0; i < size(); i++) {
            if(element == get(i)) {
                updates.beginEvent();
                updates.addUpdate(i);
                updates.commitEvent();
                return;
            }
        }
        throw new IllegalStateException("Failed to find list element \"" + element + "\" in list " + this);
    }


    /**
     *
     */
    public interface Connector {

        /**
         * Listen for events from the specified <code>target</code>.
         *
         * @param target the element from your EventList to be observed
         * @return a handle to the listener to be used when removing the listener
         */
        public EventListener installListener(Object target);

        /**
         * Stop listening for events from the specified <code>target</code>.
         *
         * @param target the element from your EventList to be observed
         * @param listener a handle to the listener as returned by {@link #installListener(Object)}.
         */
        public void uninstallListener(Object target, EventListener listener);


        /**
         * Set the list to notify when listeners are invoked. The listeners should be
         * invoked whenever a list element is modified.
         *
         * @param list the adaptee which will update
         */
        public void setObservableElementList(ObservableElementList list);
    }
}