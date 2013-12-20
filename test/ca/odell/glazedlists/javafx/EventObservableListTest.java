/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.javafx;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

import static java.util.Arrays.*;

/**
 * Tests the behaviour of {@link EventObservableList}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class EventObservableListTest {
    EventList<String> root = null;
    SortedList<String> sorter = null;

    EventObservableList<String> wrapper;

    Queue<List<ChangeInfo>> change_queue;

    @Before
    public void setUp() {
        root = new BasicEventList<String>();
        root.add("Apple");
        root.add("Banana");
        root.add("Cantaloupe");
        root.add("Dragon fruit");

        sorter = new SortedList<String>(root, null);

        wrapper = new EventObservableList<String>(sorter);

        change_queue = new LinkedList<List<ChangeInfo>>();

        wrapper.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                List<ChangeInfo> change_list = buildChangeInfo(change);
                change_queue.add(change_list);

                // Reset, build again and ensure the same
                change.reset();
                List<ChangeInfo> dup_change_list = buildChangeInfo(change);
                assertEquals(change_list, dup_change_list);
            }

            private List<ChangeInfo> buildChangeInfo(Change<? extends String> change) {
                List<ChangeInfo> change_list = new LinkedList<ChangeInfo>();

                // For now, just making sure the toString doesn't explode
                assertNotNull(change.toString());

                while (change.next()) {
                    if (change.wasPermutated()) {
                        int[] permutation_array = new int[change.getTo() - change.getFrom()];
                        for (int i = 0; i < permutation_array.length; i++) {
                            permutation_array[i] = change.getPermutation(i + change.getFrom());
                        }

                        change_list.add(new ChangeInfo(change.getFrom(), change.getTo(),
                                permutation_array));

                        assertNotNull(change.getRemoved());
                        assertTrue(change.getRemoved().isEmpty());
                        assertEquals(0, change.getRemovedSize());

                        assertFalse(change.wasAdded());
                        assertFalse(change.wasRemoved());
                        assertFalse(change.wasReplaced());
                        assertFalse(change.wasUpdated());
                    } else {
                        ChangeType type;
                        if (change.wasAdded()) {
                            type = ChangeType.INSERT;
                            assertFalse(change.wasRemoved());
                            assertFalse(change.wasReplaced());
                            assertFalse(change.wasUpdated());
                        } else if (change.wasRemoved()) {
                            type = ChangeType.DELETE;
                            assertFalse(change.wasAdded());
                            assertFalse(change.wasReplaced());
                            assertFalse(change.wasUpdated());
                        } else if (change.wasReplaced()) {
                            type = ChangeType.REPLACE;
                            assertFalse(change.wasAdded());
                            assertFalse(change.wasRemoved());
                            assertFalse(change.wasUpdated());
                        } else {
                            throw new UnsupportedOperationException("Not supported");
                        }

                        List<? extends String> added = null;
                        if (type != ChangeType.DELETE) {
                            added = change.getAddedSubList();
                        }

                        List<? extends String> removed = null;
                        if (type != ChangeType.INSERT) {
                            removed = change.getRemoved();
                        }

                        change_list.add(new ChangeInfo(type, change.getFrom(), change.getTo(),
                                added, removed));
                    }
                }

                return change_list;
            }
        });
    }

    @After
    public void tearDown() {
        if (wrapper != null) {
            wrapper.dispose();
            wrapper = null;
        }
        root = null;
    }

    @Test
    public void testPrepopulatedReads() {
        // isEmpty, size, get
        assertFalse(wrapper.isEmpty());
        assertEquals(4, wrapper.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));

        // toArray() (no arg)
        Object[] array = wrapper.toArray();
        assertNotNull(array);
        assertEquals(4, array.length);
        assertEquals("Apple", array[0]);
        assertEquals("Banana", array[1]);
        assertEquals("Cantaloupe", array[2]);
        assertEquals("Dragon fruit", array[3]);

        // toArray(Object[]) (with arg)
        array = wrapper.toArray(new String[4]);
        // noinspection ConstantConditions
        assertTrue(array instanceof String[]);
        assertNotNull(array);
        assertEquals(4, array.length);
        assertEquals("Apple", array[0]);
        assertEquals("Banana", array[1]);
        assertEquals("Cantaloupe", array[2]);
        assertEquals("Dragon fruit", array[3]);

        // contains
        assertTrue(wrapper.contains("Apple"));
        assertTrue(wrapper.contains("Banana"));
        assertTrue(wrapper.contains("Cantaloupe"));
        assertTrue(wrapper.contains("Dragon fruit"));
        assertFalse(wrapper.contains("Elderberry"));

        // containsAll
        Set<String> fruits = new HashSet<String>();
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Cantaloupe");
        fruits.add("Dragon fruit");
        assertTrue(wrapper.containsAll(fruits));
        fruits.add("Elderberry");
        assertFalse(wrapper.containsAll(fruits));

        // iterator
        Iterator<String> iterator = wrapper.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("Apple", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Banana", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Cantaloupe", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Dragon fruit", iterator.next());
        assertFalse(iterator.hasNext());

        // listIterator
        ListIterator<String> list_iterator = wrapper.listIterator();
        assertTrue(list_iterator.hasNext());
        assertFalse(list_iterator.hasPrevious());
        assertEquals("Apple", list_iterator.next());
        assertTrue(list_iterator.hasNext());
        assertTrue(list_iterator.hasPrevious());
        assertEquals("Banana", list_iterator.next());
        assertTrue(list_iterator.hasNext());
        assertTrue(list_iterator.hasPrevious());
        assertEquals("Cantaloupe", list_iterator.next());
        assertTrue(list_iterator.hasNext());
        assertTrue(list_iterator.hasPrevious());
        assertEquals("Dragon fruit", list_iterator.next());
        assertFalse(list_iterator.hasNext());
        assertTrue(list_iterator.hasPrevious());
        assertEquals("Dragon fruit", list_iterator.previous()); // move pointer
                                                                // back
        assertTrue(list_iterator.hasNext());
        assertTrue(list_iterator.hasPrevious());
        assertEquals("Cantaloupe", list_iterator.previous()); // move pointer
                                                              // back
        assertTrue(list_iterator.hasNext());
        assertTrue(list_iterator.hasPrevious());
        assertEquals("Cantaloupe", list_iterator.next());
        assertTrue(list_iterator.hasNext());
        assertTrue(list_iterator.hasPrevious());
        assertEquals("Dragon fruit", list_iterator.next());
        assertFalse(list_iterator.hasNext());
        assertTrue(list_iterator.hasPrevious());

        // subList
        List<String> sublist = wrapper.subList(1, 3);
        assertEquals(2, sublist.size());
        assertEquals("Banana", sublist.get(0));
        assertEquals("Cantaloupe", sublist.get(1));

        // equals
        assertEquals(root, wrapper);
        assertEquals(wrapper, root);
    }

    @Test
    public void testPrepopulatedWritesToWrapper() {
        // set
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Apple", root.get(0));
        wrapper.set(0, "Acai");
        assertEquals("Acai", wrapper.get(0));
        assertEquals("Acai", root.get(0));

        // retainAll
        wrapper.retainAll("Apple", "Banana", "Cantaloupe", "Dragon fruit");
        assertEquals(3, wrapper.size());
        assertEquals(3, root.size());
        assertEquals("Banana", wrapper.get(0));
        assertEquals("Cantaloupe", wrapper.get(1));
        assertEquals("Dragon fruit", wrapper.get(2));
        assertEquals(wrapper, root);

        // add (no index)
        wrapper.add("Elderberry");
        assertEquals(4, wrapper.size());
        assertEquals(4, root.size());
        assertEquals("Banana", wrapper.get(0));
        assertEquals("Cantaloupe", wrapper.get(1));
        assertEquals("Dragon fruit", wrapper.get(2));
        assertEquals("Elderberry", wrapper.get(3));
        assertEquals(wrapper, root);

        // add (with index)
        wrapper.add(0, "Apple");
        assertEquals(5, wrapper.size());
        assertEquals(5, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals(wrapper, root);

        // addAll (vararg)
        wrapper.addAll("Fig", "Grape");
        assertEquals(7, wrapper.size());
        assertEquals(7, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals("Fig", wrapper.get(5));
        assertEquals("Grape", wrapper.get(6));
        assertEquals(wrapper, root);

        // addAll (Collection)
        wrapper.addAll("Jack fruit", "Kiwi");
        assertEquals(9, wrapper.size());
        assertEquals(9, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals("Fig", wrapper.get(5));
        assertEquals("Grape", wrapper.get(6));
        assertEquals("Jack fruit", wrapper.get(7));
        assertEquals("Kiwi", wrapper.get(8));
        assertEquals(wrapper, root);

        // addAll (Collection with index)
        wrapper.addAll(7, asList("Honeydew", "Iyokan"));
        assertEquals(11, wrapper.size());
        assertEquals(11, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals("Fig", wrapper.get(5));
        assertEquals("Grape", wrapper.get(6));
        assertEquals("Honeydew", wrapper.get(7));
        assertEquals("Iyokan", wrapper.get(8));
        assertEquals("Jack fruit", wrapper.get(9));
        assertEquals("Kiwi", wrapper.get(10));
        assertEquals(wrapper, root);

        // removeAll (varargs)
        wrapper.removeAll("Kiwi", "Jack fruit");
        assertEquals(9, wrapper.size());
        assertEquals(9, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals("Fig", wrapper.get(5));
        assertEquals("Grape", wrapper.get(6));
        assertEquals("Honeydew", wrapper.get(7));
        assertEquals("Iyokan", wrapper.get(8));
        assertEquals(wrapper, root);

        // removeAll (Collection)
        wrapper.removeAll("Fig", "Grape");
        assertEquals(7, wrapper.size());
        assertEquals(7, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals("Honeydew", wrapper.get(5));
        assertEquals("Iyokan", wrapper.get(6));
        assertEquals(wrapper, root);

        // remove (from,to)
        wrapper.remove(4, 6);
        assertEquals(5, wrapper.size());
        assertEquals(5, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Iyokan", wrapper.get(4));
        assertEquals(wrapper, root);

        // remove (arg)
        wrapper.remove("Iyokan");
        assertEquals(4, wrapper.size());
        assertEquals(4, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals(wrapper, root);

        // remove (index)
        wrapper.remove(3);
        assertEquals(3, wrapper.size());
        assertEquals(3, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals(wrapper, root);

        // setAll (varargs)
        wrapper.setAll("Apricot", "Blueberry");
        assertEquals(2, wrapper.size());
        assertEquals(2, root.size());
        assertEquals("Apricot", wrapper.get(0));
        assertEquals("Blueberry", wrapper.get(1));
        assertEquals(wrapper, root);

        // setAll (Collection)
        wrapper.setAll(asList("Acai", "Black cherry", "Cherry"));
        assertEquals(3, wrapper.size());
        assertEquals(3, root.size());
        assertEquals("Acai", wrapper.get(0));
        assertEquals("Black cherry", wrapper.get(1));
        assertEquals("Cherry", wrapper.get(2));
        assertEquals(wrapper, root);

        // clear
        wrapper.clear();
        assertTrue(wrapper.isEmpty());
        assertTrue(root.isEmpty());
        assertEquals(0, wrapper.size());
        assertEquals(0, root.size());
        assertEquals(wrapper, root);
    }

    @Test
    public void testPrepopulatedWritesToSource() {
        // set
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Apple", root.get(0));
        root.set(0, "Acai");
        expectEvent(expectation(ChangeType.REPLACE, 0, 1, asList("Acai"), asList("Apple")));
        assertEquals("Acai", wrapper.get(0));
        assertEquals("Acai", root.get(0));

        // retainAll
        Set<String> fruits = new HashSet<String>();
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Cantaloupe");
        fruits.add("Dragon fruit");
        root.retainAll(fruits);
        expectEvent(expectation(ChangeType.DELETE, 0, 1, null, asList("Acai")));
        assertEquals(3, wrapper.size());
        assertEquals(3, root.size());
        assertEquals("Banana", wrapper.get(0));
        assertEquals("Cantaloupe", wrapper.get(1));
        assertEquals("Dragon fruit", wrapper.get(2));
        assertEquals(wrapper, root);

        // add (no index)
        root.add("Elderberry");
        expectEvent(expectation(ChangeType.INSERT, 3, 4, asList("Elderberry"), null));
        assertEquals(4, wrapper.size());
        assertEquals(4, root.size());
        assertEquals("Banana", wrapper.get(0));
        assertEquals("Cantaloupe", wrapper.get(1));
        assertEquals("Dragon fruit", wrapper.get(2));
        assertEquals("Elderberry", wrapper.get(3));
        assertEquals(wrapper, root);

        // add (with index)
        root.add(0, "Apple");
        expectEvent(expectation(ChangeType.INSERT, 0, 1, asList("Apple"), null));
        assertEquals(5, wrapper.size());
        assertEquals(5, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals(wrapper, root);

        // addAll (Collection)
        root.addAll(asList("Fig", "Grape", "Jack fruit", "Kiwi"));
        // expectEvent(
        // expectation( ChangeType.INSERT, 5, 6 ),
        // expectation( ChangeType.INSERT, 6, 7 ),
        // expectation( ChangeType.INSERT, 7, 8 ),
        // expectation( ChangeType.INSERT, 8, 9 ) );
        expectEvent(expectation(ChangeType.INSERT, 5, 9,
                asList("Fig", "Grape", "Jack fruit", "Kiwi"), null));
        assertEquals(9, wrapper.size());
        assertEquals(9, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals("Fig", wrapper.get(5));
        assertEquals(root.toString(), "Grape", wrapper.get(6));
        assertEquals("Jack fruit", wrapper.get(7));
        assertEquals("Kiwi", wrapper.get(8));
        assertEquals(wrapper, root);

        // addAll (Collection with index)
        root.addAll(7, asList("Honeydew", "Iyokan"));
        // expectEvent(
        // expectation( ChangeType.INSERT, 7, 8 ),
        // expectation( ChangeType.INSERT, 8, 9 ) );
        expectEvent(expectation(ChangeType.INSERT, 7, 9, asList("Honeydew", "Iyokan"), null));
        assertEquals(11, wrapper.size());
        assertEquals(11, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals("Fig", wrapper.get(5));
        assertEquals("Grape", wrapper.get(6));
        assertEquals("Honeydew", wrapper.get(7));
        assertEquals("Iyokan", wrapper.get(8));
        assertEquals("Jack fruit", wrapper.get(9));
        assertEquals("Kiwi", wrapper.get(10));
        assertEquals(wrapper, root);

        // removeAll (Collection)
        root.removeAll(asList("Kiwi", "Jack fruit"));
        expectEvent(expectation(ChangeType.DELETE, 9, 10, null, asList("Jack fruit")),
                expectation(ChangeType.DELETE, 9, 10, null, asList("Kiwi")));
        assertEquals(9, wrapper.size());
        assertEquals(9, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals("Fig", wrapper.get(5));
        assertEquals("Grape", wrapper.get(6));
        assertEquals("Honeydew", wrapper.get(7));
        assertEquals("Iyokan", wrapper.get(8));
        assertEquals(wrapper, root);

        // remove (arg)
        root.remove("Iyokan");
        expectEvent(expectation(ChangeType.DELETE, 8, 9, null, asList("Iyokan")));
        assertEquals(8, wrapper.size());
        assertEquals(8, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));
        assertEquals("Elderberry", wrapper.get(4));
        assertEquals("Fig", wrapper.get(5));
        assertEquals("Grape", wrapper.get(6));
        assertEquals("Honeydew", wrapper.get(7));
        assertEquals(wrapper, root);

        // remove (index)
        root.remove(3);
        expectEvent(expectation(ChangeType.DELETE, 3, 4, null, asList("Dragon fruit")));
        assertEquals(7, wrapper.size());
        assertEquals(7, root.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Elderberry", wrapper.get(3));
        assertEquals("Fig", wrapper.get(4));
        assertEquals("Grape", wrapper.get(5));
        assertEquals("Honeydew", wrapper.get(6));
        assertEquals(wrapper, root);

        // clear
        root.clear();
        expectEvent(expectation(ChangeType.DELETE, 0, 1, null, asList("Apple")),
                expectation(ChangeType.DELETE, 0, 1, null, asList("Banana")),
                expectation(ChangeType.DELETE, 0, 1, null, asList("Cantaloupe")),
                expectation(ChangeType.DELETE, 0, 1, null, asList("Elderberry")),
                expectation(ChangeType.DELETE, 0, 1, null, asList("Fig")),
                expectation(ChangeType.DELETE, 0, 1, null, asList("Grape")),
                expectation(ChangeType.DELETE, 0, 1, null, asList("Honeydew")));
        assertTrue(wrapper.isEmpty());
        assertTrue(root.isEmpty());
        assertEquals(0, wrapper.size());
        assertEquals(0, root.size());
        assertEquals(wrapper, root);
    }

    @Test
    public void testReorder() {
        assertFalse(wrapper.isEmpty());
        assertEquals(4, wrapper.size());
        assertEquals("Apple", wrapper.get(0));
        assertEquals("Banana", wrapper.get(1));
        assertEquals("Cantaloupe", wrapper.get(2));
        assertEquals("Dragon fruit", wrapper.get(3));

        sorter.setComparator(GlazedLists.reverseComparator());
        expectEvent(expectationOfReorder(0, 4, 3, 2, 1, 0));

        assertFalse(wrapper.isEmpty());
        assertEquals(4, wrapper.size());
        assertEquals("Dragon fruit", wrapper.get(0));
        assertEquals("Cantaloupe", wrapper.get(1));
        assertEquals("Banana", wrapper.get(2));
        assertEquals("Apple", wrapper.get(3));
    }

    @Test
    public void testInvalidationListener() {
        final AtomicBoolean listener_called = new AtomicBoolean(false);
        InvalidationListener listener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                listener_called.set(true);
            }
        };

        wrapper.addListener(listener);

        assertFalse(listener_called.get());

	    wrapper.add( "Watermellon" );

	    assertTrue(listener_called.get());
    }

    @Test
    public void testIndexOf() {
        wrapper.add("Apple");
        wrapper.add("Banana");

        assertEquals(0, wrapper.indexOf("Apple"));
        assertEquals(4, wrapper.lastIndexOf("Apple"));

        assertEquals(1, wrapper.indexOf("Banana"));
        assertEquals(5, wrapper.lastIndexOf("Banana"));

        assertEquals(2, wrapper.indexOf("Cantaloupe"));
        assertEquals(2, wrapper.lastIndexOf("Cantaloupe"));

        assertEquals(3, wrapper.indexOf("Dragon fruit"));
        assertEquals(3, wrapper.lastIndexOf("Dragon fruit"));
    }

    @Test
    public void testRemoveListChangeListener() {
        final AtomicBoolean called_flag = new AtomicBoolean(false);

        ListChangeListener<String> listener = new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                called_flag.set(true);
            }
        };
        wrapper.addListener(listener);

        wrapper.add("Foo");

        assertTrue(called_flag.compareAndSet(true, false));

        wrapper.removeListener(listener);

        wrapper.add("Bar");

        assertFalse(called_flag.get());
    }

    @Test
    public void testListIteratorIndex() {
        ListIterator<String> it = wrapper.listIterator(2);

        assertTrue(it.hasNext());
        assertEquals("Cantaloupe", it.next());
        assertTrue(it.hasNext());
        assertEquals("Dragon fruit", it.next());
        assertFalse(it.hasNext());

        assertTrue(it.hasPrevious());
        assertEquals("Dragon fruit", it.previous());
        assertTrue(it.hasPrevious());
        assertEquals("Cantaloupe", it.previous());
    }

    private void expectEvent(ChangeExpectation... expectations) {
        List<ChangeInfo> changes = change_queue.poll();
        assertNotNull(changes);

        assertEquals("Expected: " + Arrays.toString(expectations) + "  Got: " + changes,
                expectations.length, changes.size());

        for (int i = 0; i < changes.size(); i++) {
            expectations[i].check(changes.get(i), changes);
        }
    }

    private <E> ChangeExpectation<E> expectation(ChangeType type, int from, int to, List<E> added,
            List<E> removed) {

        return new ChangeExpectation<E>(type, from, to, added, removed);
    }

    private <E> ChangeExpectation<E> expectationOfReorder(int from, int to, int... reorder_map) {

        return new ChangeExpectation<E>(ChangeType.REORDER, from, to, reorder_map);
    }

    private enum ChangeType {
        INSERT, DELETE, REPLACE, REORDER
    }

    private class ChangeExpectation<E> {
        private final ChangeType type;
        private final int from;
        private final int to;
        private final List<E> added;
        private final List<E> removed;
        private final int[] reorder_mapped_ids;

        /**
         * Constructor for non-REORDER events.
         */
        ChangeExpectation(ChangeType type, int from, int to, List<E> added, List<E> removed) {

            assertNotEquals(ChangeType.REORDER, type);

            this.type = type;
            this.from = from;
            this.to = to;
            this.added = added;
            this.removed = removed;
            this.reorder_mapped_ids = null;
        }

        /**
         * Constructor for REORDER events.
         */
        ChangeExpectation(ChangeType type, int from, int to, int... mapped_ids) {
            assertEquals(ChangeType.REORDER, type);

            this.type = type;
            this.from = from;
            this.to = to;
            this.added = null;
            this.removed = null;
            this.reorder_mapped_ids = mapped_ids;
        }

        /**
         * Checks to see if the given change matches expectations.
         */
        void check(ChangeInfo change, List<ChangeInfo> other_changes) {
            String description = "Expected \"" + toString() + "\"  Got \"" + change.toString()
                    + "\"" + "  Changes in set: " + other_changes + "  Still in queue: "
                    + change_queue;
            assertEquals(description, type, change.type);
            assertEquals(description, from, change.from);
            assertEquals(description, to, change.to);

            if (type == ChangeType.REORDER) {
                assertTrue(description, Arrays.equals(reorder_mapped_ids, change.reorder_array));
            }

            if (added != null) {
                assertEquals(description, added, change.added);
            }
            if (removed != null) {
                assertEquals(description, removed, change.removed);
            }
        }

        @Override
        public String toString() {
            return type + " " + from + "-" + to + " (" + added + " / " + removed + ")";
        }
    }

    private class ChangeInfo {
        ChangeType type;
        int from;
        int to;
        int[] reorder_array;
        List<? extends String> added;
        List<? extends String> removed;

        private ChangeInfo(ChangeType type, int from, int to, List<? extends String> added,
                List<? extends String> removed) {

            this.type = type;
            this.from = from;
            this.to = to;
            this.added = added;
            this.removed = removed;
        }

        private ChangeInfo(int from, int to, int... reorder_array) {
            this.type = ChangeType.REORDER;
            this.from = from;
            this.to = to;
            this.reorder_array = reorder_array;
            this.added = null;
            this.removed = null;
        }

        @Override
        public String toString() {
            return type + " " + from + "-" + to + " (" + added + " / " + removed + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ChangeInfo that = (ChangeInfo) o;

            if (from != that.from) {
                return false;
            }
            if (to != that.to) {
                return false;
            }
            if (!Arrays.equals(reorder_array, that.reorder_array)) {
                return false;
            }
            if (type != that.type) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + from;
            result = 31 * result + to;
            result = 31 * result + (reorder_array != null ? Arrays.hashCode(reorder_array) : 0);
            return result;
        }
    }
}
