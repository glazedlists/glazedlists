/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.matchers.Matchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test verifies that the DefaultEventSelectionModelTest works.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class DefaultEventSelectionModelTest extends SwingTestCase {

    /**
     * Tests that selection survives a sorting.
     */
    @Test
    public void testSort() {
        EventList<Comparable> list = new BasicEventList<Comparable>();
        SortedList<Comparable> sorted = new SortedList<Comparable>(list, null);
        DefaultEventSelectionModel<Comparable> eventSelectionModel = new DefaultEventSelectionModel<Comparable>(
                sorted);

        // populate the list
        list.addAll(GlazedListsTests.delimitedStringToList("E C F B A D"));

        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getSelected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getTogglingSelected());
        assertEquals(list, eventSelectionModel.getDeselected());
        assertEquals(list, eventSelectionModel.getTogglingDeselected());

        // select the vowels
        eventSelectionModel.addSelectionInterval(0, 0);
        eventSelectionModel.addSelectionInterval(4, 4);
        assertEquals(GlazedListsTests.delimitedStringToList("E A"), eventSelectionModel
                .getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("E A"), eventSelectionModel
                .getTogglingSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("C F B D"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("C F B D"), eventSelectionModel
                .getTogglingDeselected());

        // flip the list
        sorted.setComparator(GlazedLists.comparableComparator());
        assertEquals(GlazedListsTests.delimitedStringToList("A E"), eventSelectionModel
                .getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A E"), eventSelectionModel
                .getTogglingSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("B C D F"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("B C D F"), eventSelectionModel
                .getTogglingDeselected());

        // flip the list again
        sorted.setComparator(GlazedLists.reverseComparator());
        assertEquals(GlazedListsTests.delimitedStringToList("E A"), eventSelectionModel
                .getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("E A"), eventSelectionModel
                .getTogglingSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("F D C B"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("F D C B"), eventSelectionModel
                .getTogglingDeselected());
    }

    /**
     * Verifies that the selected index is cleared when the selection is cleared.
     */
    @Test
    public void testClear() {
        EventList<String> list = new BasicEventList<String>();
        DefaultEventSelectionModel eventSelectionModel = new DefaultEventSelectionModel<String>(list);

        // populate the list
        list.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));

        // make a selection
        eventSelectionModel.addSelectionInterval(1, 4);

        // test the selection
        assertEquals(list.subList(1, 5), eventSelectionModel.getSelected());
        assertEquals(list.subList(1, 5), eventSelectionModel.getTogglingSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A F"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A F"), eventSelectionModel
                .getTogglingDeselected());

        // clear the selection
        eventSelectionModel.clearSelection();

        // test the selection
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getSelected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getTogglingSelected());
        assertEquals(-1, eventSelectionModel.getMinSelectionIndex());
        assertEquals(-1, eventSelectionModel.getMaxSelectionIndex());
        assertEquals(true, eventSelectionModel.isSelectionEmpty());
        assertEquals(list, eventSelectionModel.getDeselected());
        assertEquals(list, eventSelectionModel.getTogglingDeselected());
    }

    /**
     * Tests the lists {@link DefaultEventSelectionModel#getTogglingSelected()} and
     * {@link DefaultEventSelectionModel#getTogglingDeselected()} for programmatic selection control.
     */
    @Test
    public void testToggleSelection() {
        EventList<String> list = new BasicEventList<String>();
        DefaultEventSelectionModel<String> eventSelectionModel = new DefaultEventSelectionModel<String>(list);
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getSelected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getTogglingSelected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getDeselected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getTogglingDeselected());

        // populate the list
        list.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getSelected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getTogglingSelected());
        assertEquals(list, eventSelectionModel.getDeselected());
        assertEquals(list, eventSelectionModel.getTogglingDeselected());

        // remove on TogglingDeselected selects
        eventSelectionModel.getTogglingDeselected().remove("A");
        eventSelectionModel.getTogglingDeselected().remove(1);
        eventSelectionModel.getTogglingDeselected().removeAll(
                GlazedListsTests.delimitedStringToList("F D"));
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), eventSelectionModel
                .getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), eventSelectionModel
                .getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), eventSelectionModel
                .getTogglingSelected());

        // add on TogglingDeselected deselects
        eventSelectionModel.getTogglingDeselected().add("F");
        eventSelectionModel.getTogglingDeselected().addAll(
                GlazedListsTests.delimitedStringToList("C D"));
        assertEquals(GlazedListsTests.delimitedStringToList("B C D E F"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("B C D E F"), eventSelectionModel
                .getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A"), eventSelectionModel
                .getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A"), eventSelectionModel
                .getTogglingSelected());

        // add on TogglingSelected selects
        eventSelectionModel.getTogglingSelected().add("F");
        eventSelectionModel.getTogglingSelected().addAll(
                GlazedListsTests.delimitedStringToList("C D"));
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), eventSelectionModel
                .getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), eventSelectionModel
                .getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), eventSelectionModel
                .getTogglingSelected());

        // remove on TogglingSelected deselects
        eventSelectionModel.getTogglingSelected().remove("A");
        eventSelectionModel.getTogglingSelected().remove(1);
        eventSelectionModel.getTogglingSelected().removeAll(
                GlazedListsTests.delimitedStringToList("F"));
        assertEquals(GlazedListsTests.delimitedStringToList("A B D E F"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A B D E F"), eventSelectionModel
                .getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("C"), eventSelectionModel
                .getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("C"), eventSelectionModel
                .getTogglingSelected());

        // remove on source list
        list.remove("C");
        list.removeAll(GlazedListsTests.delimitedStringToList("B E"));
        assertEquals(GlazedListsTests.delimitedStringToList("A D F"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F"), eventSelectionModel
                .getTogglingDeselected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getSelected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getTogglingSelected());

        // add on source list
        list.add("E");
        list.addAll(GlazedListsTests.delimitedStringToList("C B"));
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), eventSelectionModel
                .getTogglingDeselected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getSelected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getTogglingSelected());

        // clear on TogglingDeselected selects all deselected
        eventSelectionModel.getTogglingDeselected().clear();
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getDeselected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), eventSelectionModel
                .getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), eventSelectionModel
                .getTogglingSelected());

        // clear on TogglingSelected deselects all selected
        eventSelectionModel.getTogglingSelected().clear();
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), eventSelectionModel
                .getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), eventSelectionModel
                .getTogglingDeselected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getSelected());
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getTogglingSelected());
    }

    /**
     * Tests a problem where the {@link DefaultEventSelectionModel} fails to fire events This test was
     * contributed by: Sergey Bogatyrjov
     */
    @Test
    public void testSelectionModel() {
        EventList<Object> source = GlazedLists.<Object> eventListOf("one", "two", "three");
        FilterList<Object> filtered = new FilterList<Object>(source, Matchers.trueMatcher());

        // create selection model
        DefaultEventSelectionModel<Object> model = new DefaultEventSelectionModel<Object>(filtered);
        ListSelectionChangeCounter counter = new ListSelectionChangeCounter();
        model.addListSelectionListener(counter);

        // select the 1th
        model.setSelectionInterval(1, 1);
        assertEquals(1, counter.getCountAndReset());

        // clear the filter
        filtered.setMatcher(Matchers.falseMatcher());
        assertEquals(1, counter.getCountAndReset());

        // unclear the filter
        filtered.setMatcher(Matchers.trueMatcher());
        assertEquals(0, counter.getCountAndReset());

        // select the 0th
        model.setSelectionInterval(0, 0);
        assertEquals(1, counter.getCountAndReset());

        // clear the filter
        filtered.setMatcher(Matchers.falseMatcher());
        assertEquals(1, counter.getCountAndReset());
    }

    /**
     * If EventList changes are relegated to indexes AFTER the maxSelectionIndex then no
     * ListSelectionEvent needs to be fired. This test method verifies that the expected number
     * of ListSelectionEvents are produced when inserting and removing at all locations relative
     * to the range of list selections.
     */
    @Test
    public void testFireOnlyNecessaryEvents() {
        EventList<String> source = GlazedLists.eventListOf("Albert", "Alex", "Aaron", "Brian",
                "Bruce");

        // create selection model
        DefaultEventSelectionModel<String> model = new DefaultEventSelectionModel<String>(source);
        ListSelectionChangeCounter counter = new ListSelectionChangeCounter();
        model.addListSelectionListener(counter);

        // select 2nd element (should produce 1 ListSelectionEvent)
        model.setSelectionInterval(1, 3);
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes before the minSelectionIndex shift the existing selections and
        // thus produce ListSelectionEvents
        source.add(0, "Bart");
        assertEquals(1, counter.getCountAndReset());
        assertEquals(2, model.getMinSelectionIndex());
        assertEquals(4, model.getMaxSelectionIndex());
        source.remove(0);
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes on the minSelectionIndex shift the existing selections and thus
        // produce ListSelectionEvents
        source.add(1, "Bart");
        assertEquals(1, counter.getCountAndReset());
        assertEquals(2, model.getMinSelectionIndex());
        assertEquals(4, model.getMaxSelectionIndex());
        source.remove(1);
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes between the minSelectionIndex and maxSelectionIndex change
        // existing selections and thus produce ListSelectionEvents
        source.add(2, "Bart");
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(4, model.getMaxSelectionIndex());
        source.remove(2);
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes on the maxSelectionIndex change the existing selections and thus
        // produce ListSelectionEvents
        source.add(3, "Bart");
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(4, model.getMaxSelectionIndex());
        source.remove(3);
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes after the maxSelectionIndex do not produce ListSelectionEvents
        source.add(4, "Bart");
        assertEquals(0, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());
        source.remove(4);
        assertEquals(0, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes after the maxSelectionIndex do not produce ListSelectionEvents
        source.add(5, "Bart");
        assertEquals(0, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());
        source.remove(5);
        assertEquals(0, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());
    }

    @Test
    public void testModelChangesProducingSelectionModelEvents() {
        EventList<String> source = GlazedLists.eventListOf("Albert", "Alex", "Aaron", "Brian",
                "Bruce");

        // create EventListModel (data model)
        DefaultEventListModel<String> model = new DefaultEventListModel<String>(source);

        // create DefaultEventSelectionModel (our selection model)
        DefaultEventSelectionModel<String> eventSelectionModel = new DefaultEventSelectionModel<String>(source);
        ListSelectionChangeCounter eventSelectionModelCounter = new ListSelectionChangeCounter();
        eventSelectionModel.addListSelectionListener(eventSelectionModelCounter);

        // create DefaultListSelectionModel (SUN's selection model)
        DefaultListSelectionModel defaultListSelectionModel = new DefaultListSelectionModel();
        ListSelectionChangeCounter defaultListSelectionModelCounter = new ListSelectionChangeCounter();
        defaultListSelectionModel.addListSelectionListener(defaultListSelectionModelCounter);

        // create two different JLists (one with DefaultEventSelectionModel and one with
        // DefaultListSelectionModel) that share the same data model
        JList eventList = new JList(model);
        eventList.setSelectionModel(eventSelectionModel);

        JList defaultList = new JList(model);
        defaultList.setSelectionModel(defaultListSelectionModel);

        // select the first element in both selection models
        eventSelectionModel.setSelectionInterval(0, 0);
        defaultListSelectionModel.setSelectionInterval(0, 0);

        // verify that each selection model broadcasted the selection event
        assertEquals(1, defaultListSelectionModelCounter.getCountAndReset());
        assertEquals(1, eventSelectionModelCounter.getCountAndReset());

        // change an element in the model which is selected in the selection models
        source.set(0, "Bart");

        // selection should not have changed in either selection model
        assertEquals(0, defaultListSelectionModel.getMinSelectionIndex());
        assertEquals(0, defaultListSelectionModel.getMaxSelectionIndex());
        assertEquals(0, defaultListSelectionModel.getLeadSelectionIndex());
        assertEquals(0, defaultListSelectionModel.getAnchorSelectionIndex());

        assertEquals(0, eventSelectionModel.getMinSelectionIndex());
        assertEquals(0, eventSelectionModel.getMaxSelectionIndex());
        assertEquals(0, eventSelectionModel.getLeadSelectionIndex());
        assertEquals(0, eventSelectionModel.getAnchorSelectionIndex());

        // verify that neither DefaultListSelectionModel nor DefaultEventSelectionModel broadcasted a
        // needless event for this model change
        assertEquals(0, defaultListSelectionModelCounter.getCountAndReset());
        assertEquals(0, eventSelectionModelCounter.getCountAndReset());
    }

    @Test
    public void testDeleteSelectedRows_FixMe() {
        EventList<String> source = GlazedLists.eventListOf("one", "two", "three");

        // create selection model
        DefaultEventSelectionModel<String> model = new DefaultEventSelectionModel<String>(source);
        ListSelectionChangeCounter counter = new ListSelectionChangeCounter();
        model.addListSelectionListener(counter);

        // select all elements (should produce 1 ListSelectionEvent)
        model.setSelectionInterval(0, 2);
        assertEquals(source, model.getSelected());
        assertEquals(1, counter.getCountAndReset());

        // remove all selected elements (should produce 1 ListSelectionEvent)
        model.getSelected().clear();
        assertEquals(Collections.EMPTY_LIST, source);
        assertEquals(Collections.EMPTY_LIST, model.getSelected());
        // todo fix this defect (this is the functionality that Bruce Alspaugh is looking for)
        assertEquals(1, counter.getCountAndReset());
    }

    /**
     * Tests, that a selection is preserved when the source is a TreeList and an update event
     * happens for the selected element.
     */
    @Test
    public void testSelectionOnTreeListUpdate_FixMe() {
        final EventList<String> source = GlazedLists.eventListOf("zero", "one", "two", "three");

        final EventList<String> sourceProxy = GlazedListsSwing.swingThreadProxyList(source);
        final TreeList<String> treeList = new TreeList<String>(sourceProxy, new StringFormat(),
                TreeList.<String> nodesStartExpanded());
        final DefaultEventSelectionModel<String> selModel = new DefaultEventSelectionModel<String>(treeList);
        selModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selModel.setSelectionInterval(2, 2);
        assertEquals(Arrays.asList("two"), selModel.getSelected());
        source.set(2, source.get(2));
        assertEquals(Arrays.asList("two"), selModel.getSelected());
    }

    /** Simple Format for TreeList testing. */
    private static class StringFormat implements TreeList.Format<String> {
        @Override
        public boolean allowsChildren(String element) {
            return element.equals("zero");
        }

        @Override
        public Comparator<? super String> getComparator(int depth) {
            return null;
        }

        @Override
        public void getPath(List<String> path, String element) {
            if (!element.equals("zero")) {
                getPath(path, "zero");
            }
            path.add(element);
        }
    }

    /**
     * Counts the number of ListSelectionEvents fired.
     */
    private static class ListSelectionChangeCounter implements ListSelectionListener {
        private int count = 0;

        @Override
        public void valueChanged(ListSelectionEvent e) {
            count++;
        }

        public int getCountAndReset() {
            int result = count;
            count = 0;
            return result;
        }
    }
}
