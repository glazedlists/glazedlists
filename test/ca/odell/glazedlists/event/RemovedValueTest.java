/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test if providing the deleted element in a list event works.
 *
 * @author jessewilson
 */
public class RemovedValueTest extends TestCase {

    /**
     * A simple test to see if deleted elements make their way through the system.
     */
    public void testGetRemovedValue() {
        // prep recording the deleted elements
        EventList<String> source = new RemovedValueEventList<String>(new BasicEventList<String>());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(source);
        listConsistencyListener.setPreviousElementTracked(true);

        RemovedValuesListener<String> removedValuesListener = new RemovedValuesListener<String>();
        source.addListEventListener(removedValuesListener);
        source.addAll(GlazedListsTests.stringToList("GLAZEDLISTS"));

        // delete an 'A', make sure it's logged
        source.remove("A");
        assertEquals(GlazedListsTests.stringToList("A"), removedValuesListener.deleteLog);

        // delete more characters, make sure they're logged
        source.remove("D");
        source.removeAll(GlazedListsTests.stringToList("S"));
        assertEquals(GlazedListsTests.stringToList("ADSS"), removedValuesListener.deleteLog);

        // delete still more characters, make sure they're logged
        source.removeAll(GlazedListsTests.stringToList("GLZT"));
        assertEquals(GlazedListsTests.stringToList("ADSSGLZLT"), removedValuesListener.deleteLog);
    }

    /**
     * Record the deleted elements as they happen.
     */
    static class RemovedValuesListener<E> implements ListEventListener<E> {
        List<E> deleteLog = new ArrayList<E>();

        public void listChanged(ListEvent<E> listChanges) {
            Tree4DeltasListEvent<E> deltasListEvent = (Tree4DeltasListEvent<E>)listChanges;

            while(listChanges.next()) {
                int type = listChanges.getType();
                if(type == ListEvent.DELETE) {
                    deleteLog.add(deltasListEvent.getOldValue());
                }
            }
        }
    }

    /**
     * Wrap any EventList and make sure that the deleted element is available.
     */
    static class RemovedValueEventList<E> extends TransformedList<E,E> {
        public final List<E> sourceValues = new ArrayList<E>();
        public RemovedValueEventList(EventList<E> source) {
            super(source);
            sourceValues.addAll(source);
            source.addListEventListener(this);
        }

        protected boolean isWritable() {
            return true;
        }

        public void listChanged(ListEvent<E> listChanges) {
            updates.beginEvent();
            while(listChanges.next()) {
                int type = listChanges.getType();
                int index = listChanges.getIndex();
                if(type == ListEvent.INSERT) {
                    // TODO(jessewilson): use updates.getNewValue() instead here
                    E newValue = source.get(index);
                    sourceValues.add(index, newValue);
                    updates.elementInserted(index, newValue);
                } else if(type == ListEvent.UPDATE) {
                    // TODO(jessewilson): use updates.getNewValue() instead here
                    E newValue = source.get(index);
                    E oldValue = sourceValues.set(index, newValue);
                    updates.elementUpdated(index, oldValue, newValue);
                } else if(type == ListEvent.DELETE) {
                    E deleted = sourceValues.remove(index);
                    updates.elementDeleted(index, deleted);
                }
            }
            updates.commitEvent();
        }
    }
}
