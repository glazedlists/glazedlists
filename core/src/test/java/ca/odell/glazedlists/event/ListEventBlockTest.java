/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.TransactionList;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListEventBlockTest {

    @Test
    public void testSortListEventBlocks() {
        TransactionList<String> list = new TransactionList<String>(new BasicEventList<String>());
        ListConsistencyListener.install(list);

        list.beginEvent();
        list.addAll(GlazedListsTests.stringToList("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        list.commitEvent();

        list.beginEvent(false);
        list.add(0, "A");
        list.add(3, "A");
        list.add(4, "A");
        list.add(10, "A");
        list.add(3, "A");
        list.add(14, "A");
        list.add(7, "A");
        list.add(5, "A");
        list.add(3, "A");
        list.add(18, "A");
        list.commitEvent();
    }

    @Test
    public void testSortListEventBlocks2() {
        TransactionList<String> list = new TransactionList<String>(new BasicEventList<String>());
        ListConsistencyListener.install(list);

        list.beginEvent();
        list.addAll(GlazedListsTests.stringToList("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        list.commitEvent();

        list.beginEvent();
        list.set(3, "A");
        list.remove(3);
        list.set(1, "A");
        list.commitEvent();
    }

    @Test
    public void testSortListEventBlocks3() {
        TransactionList<String> list = new TransactionList<String>(new BasicEventList<String>());
        ListConsistencyListener.install(list);

        list.beginEvent();
        list.addAll(GlazedListsTests.stringToList("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        list.commitEvent();

        list.beginEvent();
        list.remove(2);
        list.set(0, "A");
        list.add(2, "A");
        list.commitEvent();
    }

    @Test
    public void testSortListEventBlocks4() {
        TransactionList<String> list = new TransactionList<String>(new BasicEventList<String>());
        ListConsistencyListener.install(list);

        list.beginEvent();
        list.addAll(GlazedListsTests.stringToList("AAA"));
        list.commitEvent();

        list.beginEvent();
        list.set(0, "B");
        list.set(0, "A");
        list.set(1, "B");
        list.set(2, "B");
        list.remove(1);
        list.commitEvent();
    }

    /**
     * Sort all possible permutations of five changes on a five element list.
     */
    @Test
    public void testSortListEventBlocksAllPermutations() {
        final int LIST_INITIAL_SIZE = 4;
        final int LIST_CHANGE_COUNT = 3;
//        final int LIST_INITIAL_SIZE = 4;
//        final int LIST_CHANGE_COUNT = 4;

        // prepare the change counters
        List<ListChangeEnumeration> listChangeEnumarations = new ArrayList<ListChangeEnumeration>(LIST_CHANGE_COUNT);
        for(int i = 0; i < LIST_CHANGE_COUNT; i++) {
            listChangeEnumarations.add(new ListChangeEnumeration());
        }

        // prepare the list to perform all of our changes on
        TransactionList<String> list = new TransactionList<String>(new BasicEventList<String>());
        ListConsistencyListener.install(list);

        int count = 0;
        listChangeEnumerations:
        while(true) {
            count++;
            if(count % 100 == 0) System.out.println(count);

            // perform this series of changes
            list.beginEvent();
            for(int i = 0; i < LIST_INITIAL_SIZE; i++) {
                list.add("X");
            }
            list.commitEvent();

            // perform all required changes to this list
            list.beginEvent();
            for(Iterator i = listChangeEnumarations.iterator(); i.hasNext(); ) {
                ListChangeEnumeration listChange = (ListChangeEnumeration)i.next();
                int changeType = listChange.getChangeType();
                int changeIndex = listChange.getChangeIndex();

                if(changeType == ListEvent.INSERT) {
                    list.add(changeIndex, "X");
                } else if(changeType == ListEvent.UPDATE) {
                    list.set(changeIndex, "X");
                } else if(changeType == ListEvent.DELETE) {
                    list.remove(changeIndex);
                }
            }
            list.commitEvent();

            // increment to the next sequence of list changes
            for(int i = listChangeEnumarations.size() - 1; i >= 0; i--) {

                // calculate the list's size at this point
                int listSize = LIST_INITIAL_SIZE;
                for(int j = 0; j < i; j++) {
                    ListChangeEnumeration changeBefore = listChangeEnumarations.get(j);
                    if(changeBefore.getChangeType() == ListEvent.INSERT) listSize++;
                    else if(changeBefore.getChangeType() == ListEvent.DELETE) listSize--;
                }

                // figure out what the next change is on a list of this size
                ListChangeEnumeration enumerationToIncrement = listChangeEnumarations.get(i);
                boolean incrementSuccess = enumerationToIncrement.next(listSize);

                // if we incremented, we have a new change
                if(incrementSuccess) {
                    continue listChangeEnumerations;
                } else {
                    enumerationToIncrement.reset();
                }
            }
            break;
        }
    }

    /**
     * Enumarate through all possible list changes.
     */
    private static class ListChangeEnumeration {
        private int changeType = ListEvent.INSERT;
        private int changeIndex = 0;

        public int getChangeType() {
            return changeType;
        }

        public int getChangeIndex() {
            return changeIndex;
        }

        public boolean next(int targetListSize) {
            changeIndex++;

            // wrap around on inserts
            if(changeType == ListEvent.INSERT && changeIndex > targetListSize) {
                changeType = ListEvent.UPDATE;
                changeIndex = 0;
                return targetListSize > 0;

            // wrap around on updates
            } else if(changeType == ListEvent.UPDATE && changeIndex == targetListSize) {
                changeType = ListEvent.DELETE;
                changeIndex = 0;
                return targetListSize > 0;

            // wrap around on deletes
            } else if(changeType == ListEvent.DELETE && changeIndex == targetListSize) {
                return false;
            }

            return true;
        }

        public void reset() {
            changeType = ListEvent.INSERT;
            changeIndex = 0;
        }

        @Override
        public String toString() {
            String changeTypeAsString;
            if(changeType == ListEvent.INSERT) changeTypeAsString = "I";
            else if(changeType == ListEvent.UPDATE) changeTypeAsString = "U";
            else if(changeType == ListEvent.DELETE) changeTypeAsString = "D";
            else throw new IllegalStateException();
            return changeTypeAsString + changeIndex;
        }
    }
}
