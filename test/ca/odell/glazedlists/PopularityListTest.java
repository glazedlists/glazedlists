/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A PopularityListTest tests the functionality of the PopularityList.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class PopularityListTest {

    /**
     * Test that the Popularity List works with simple data.
     */
    @Test
    public void testSimpleData() {
        EventList<String> source = new BasicEventList<String>();
        PopularityList popularityList = PopularityList.create(source);
        
        source.add("Mike");
        source.add("Kevin");
        source.add("Graham");
        source.add("Mike");
        source.add("Melissa");
        source.add("Melissa");
        source.add("Jonathan");
        source.add("Jodie");
        source.add("Andrew");
        source.add("Melissa");
        
        assertEquals("Melissa", popularityList.get(0));
        assertEquals("Mike", popularityList.get(1));
        
        source.add("Jonathan");
        source.add("Jonathan");
        source.remove("Mike");
        source.remove("Melissa");

        assertEquals("Jonathan", popularityList.get(0));
        assertEquals("Melissa", popularityList.get(1));

        source.add("Mike");
        source.add("Mike");
        source.add("Mike");

        assertEquals("Mike", popularityList.get(0));
        assertEquals("Jonathan", popularityList.get(1));
        assertEquals("Melissa", popularityList.get(2));
        
        source.clear();
    }
    
    /**
     * Tests that the Popularity List works by using a random sequence of operations.
     */
    @Test
    public void testRandom() {
        Random dice = new Random(0);
        
        EventList<Integer> source = new BasicEventList<Integer>();
        SortedList<Integer> sortedSource = SortedList.create(source);
        PopularityList<Integer> popularityList = PopularityList.create(source);
        new PopularityListValidator(popularityList, sortedSource);
        
        // add 1000
        for(int i = 0; i < 1000; i++) {
            source.add(new Integer(dice.nextInt(50)));
        }

        // remove 900
        for(int i = 0; i < 900; i++) {
            int remIndex = dice.nextInt(source.size());
            source.remove(remIndex);
        }
        
        // set 800
        for(int i = 0; i < 800; i++) {
            int updateIndex = dice.nextInt(source.size());
            Integer updateValue = new Integer(dice.nextInt(50));
            source.set(updateIndex, updateValue);
        }
    }

    /**
     * Tests that the PopularityList can handle multiple simultaneous events.
     */
    @Test
    public void testMultipleEvents() {
        EventList<int[]> source = new BasicEventList<int[]>();
        source.add(new int[] { 86, 1, 1, 1, 1, 0, 0 });
        source.add(new int[] { 86, 1, 0, 1, 1, 1, 0 });
        source.add(new int[] { 86, 1, 0, 0, 0, 0, 0 });
        source.add(new int[] { 75, 1, 1, 1, 1, 0, 1 });
        source.add(new int[] { 75, 1, 0, 0, 0, 0, 1 });
        source.add(new int[] { 75, 1, 0, 0, 0, 0, 1 });
        source.add(new int[] { 30, 1, 1, 1, 1, 0, 1 });
        source.add(new int[] { 98, 1, 1, 1, 1, 0, 1 });
        source.add(new int[] { 98, 1, 0, 0, 1, 1, 1 });

        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 0);
        FilterList<int[]> filterList = new FilterList<int[]>(source, matcherEditor);
        SortedList<int[]> sortedList = new SortedList<int[]>(source, GlazedListsTests.intArrayComparator(0));
        PopularityList<int[]> popularityList = new PopularityList<int[]>(filterList, GlazedListsTests.intArrayComparator(0));
        new PopularityListValidator(popularityList, sortedList);

        matcherEditor.setFilter(1, 1);
        matcherEditor.setFilter(2, 1);
        matcherEditor.setFilter(3, 1);
        matcherEditor.setFilter(4, 1);
        matcherEditor.setFilter(5, 1);
        matcherEditor.setFilter(6, 1);
    }

    /**
     * Tests that the PopularityList can handle edge case sets.
     */
    @Test
    public void testEdgeSets() {
        EventList<String> source = new BasicEventList<String>();
        source.add("Audi");      // A
        source.add("Audi");      // A A
        source.add("Audi");      // A A A
        source.add("BMW");       // A A A B
        source.add("Chevy");     // A A A B C
        source.add("Chevy");     // A A A B C C
        source.add("Chevy");     // A A A B C C C
        source.add("Datsun");    // A A A B C C C D

        SortedList<String> sortedList = SortedList.create(source);
        PopularityList<String> popularityList = PopularityList.create(source);
        new PopularityListValidator(popularityList, sortedList);
        
        // in sorted order changes
        source.set(2, "BMW");    // A A B B C C C D
        source.set(1, "BMW");    // A B B B C C C D
        source.set(0, "BMW");    // B B B B C C C D
        source.set(6, "Datsun"); // B B B B C C D D
        source.set(5, "Datsun"); // B B B B C D D D
        source.set(4, "Datsun"); // B B B B D D D D
        source.set(3, "Datsun"); // B B B D D D D D
        source.set(2, "Datsun"); // B B D D D D D D
        source.set(1, "Datsun"); // B D D D D D D D
        source.set(0, "Datsun"); // D D D D D D D D
        source.set(7, "Ford");   // D D D D D D D F
        source.set(6, "Ford");   // D D D D D D F F
        source.set(0, "Audi");   // A D D D D D F F
        source.set(1, "BMW");    // A B D D D D F F
        source.set(2, "BMW");    // A B B D D D F F
        source.set(3, "BMW");    // A B B B D D F F
        source.set(4, "BMW");    // A B B B B D F F
        source.set(5, "Chevy");  // A B B B B C F F
        source.set(4, "Chevy");  // A B B B C C F F
        source.set(6, "Chevy");  // A B B B C C C F
        source.set(7, "Chevy");  // A B B B C C C C
    }
    
    /**
     * Tests that the PopularityList can handle edge case sets.
     */
    @Test
    public void testLeftEdgeSet() {
        EventList<String> source = new BasicEventList<String>();
        SortedList<String> sortedList = SortedList.create(source);
        PopularityList<String> popularityList = PopularityList.create(source);
        new PopularityListValidator(popularityList, sortedList);
        
        // in sorted order changes
        source.add(0, "Audi");   // A
        source.add(1, "Audi");   // A A
        source.add(2, "BMW");    // A A B
        source.add(3, "BMW");    // A A B B
        source.set(1, "BMW");    // A B B B
    }

    /**
     * Tests that the PopularityList elements that have the same count are in sorted
     * order.
     */
    @Test
    public void testEqualPopularityOrdering() {
        EventList<String> source = new BasicEventList<String>();
        PopularityList<String> popularityList = PopularityList.create(source);
        
        // in sorted order changes
        source.add(0, "chaos"); // c
        source.add(1, "fiery"); // c f
        source.add(2, "gecko"); // c f g
        source.add(0, "banjo"); // b c f g
        source.add(2, "dingo"); // b c d f g
        source.add(5, "hippo"); // b c d f g h
        source.add(0, "album"); // a b c d f g h
        source.add(4, "eerie"); // a b c d e f g h
        List<String> sortedSingleCopy = new ArrayList<String>();
        sortedSingleCopy.addAll(source);
        Collections.sort(sortedSingleCopy);
        assertEquals(sortedSingleCopy, popularityList);

        // in sorted order changes
        source.add(0, "chaos"); // c a b c d e f g h
        source.add(1, "fiery"); // c f a b c d e f g h
        source.add(2, "gecko"); // c f g a b c d e f g h
        source.add(0, "banjo"); // b c f g a b c d e f g h
        source.add(2, "dingo"); // b c d f g a b c d e f g h
        source.add(5, "hippo"); // b c d f g h a b c d e f g h
        source.add(0, "album"); // a b c d f g h a b c d e f g h
        source.add(4, "eerie"); // a b c d e f g h a b c d e f g h
        assertEquals(sortedSingleCopy, popularityList);

        // in sorted order changes
        source.add("chaos"); // c a b c d e f g h c
        source.add("fiery"); // c f a b c d e f g h c f
        source.add("gecko"); // c f g a b c d e f g h c f g
        source.add("banjo"); // b c f g a b c d e f g h c f g b
        source.add("dingo"); // b c d f g a b c d e f g h c f g b d
        source.add("hippo"); // b c d f g h a b c d e f g h c f g b d h
        source.add("album"); // a b c d f g h a b c d e f g h c f g b d h a
        source.add("eerie"); // a b c d e f g h a b c d e f g h c f g b d h a e
        assertEquals(sortedSingleCopy, popularityList);
        
        // now break into two classes 2 x { A C E F } and 4 x { B D G H }
        source.set(0, "gecko"); // g b c d e f g h a b c d e f g h c f g b d h a e
        source.set(2, "hippo"); // g b h d e f g h a b c d e f g h c f g b d h a e
        source.set(4, "dingo"); // g b h d d f g h a b c d e f g h c f g b d h a e
        source.set(5, "banjo"); // g b h d d b g h a b c d e f g h c f g b d h a e
        List<String> expectedTwoClasses = new ArrayList<String>();
        expectedTwoClasses.add("banjo");
        expectedTwoClasses.add("dingo");
        expectedTwoClasses.add("gecko");
        expectedTwoClasses.add("hippo");
        expectedTwoClasses.add("album");
        expectedTwoClasses.add("chaos");
        expectedTwoClasses.add("eerie");
        expectedTwoClasses.add("fiery");
        assertEquals(expectedTwoClasses, popularityList);
    }

    /**
     * Validates that the state of the PopularityList is correct. Because this class
     * is a Listener, it can detect the exact change that causes the PopularityList
     * to come out of sync.
     */
    static class PopularityListValidator implements ListEventListener {

        private List elementCounts = new ArrayList();
        private PopularityList popularityList;
        private SortedList sortedSource;

        /**
         * Creates a PopularityListValidator that validates that the specified
         * popularity list ranks the elements in the speceified sortedlist in 
         * order of popularity. A SortedList is used because it has much better
         * performance for {@link List#indexOf(Object)} and {@link List#lastIndexOf(Object)}
         * operations.
         */
        public PopularityListValidator(PopularityList popularityList, SortedList sortedSource) {
            this.popularityList = popularityList;
            this.sortedSource = sortedSource;
            for(int i = 0; i < popularityList.size(); i++) {
                elementCounts.add(new Integer(count(popularityList.get(i))));
            }
            
            popularityList.addListEventListener(this);
        }
        
        /**
         * Handle the source PopularityList changing by validating that list.
         */
        @Override
        public void listChanged(ListEvent listEvent) {
            List<Integer> changedIndices = new ArrayList<Integer>();
            
            // apply the changes
            while(listEvent.next()) {
                int changeIndex = listEvent.getIndex();
                int changeType = listEvent.getType();
                changedIndices.add(new Integer(changeIndex));
                
                if(changeType == ListEvent.DELETE) {
                    elementCounts.remove(changeIndex);
                } else if(changeType == ListEvent.INSERT) {
                    elementCounts.add(changeIndex, new Integer(count(popularityList.get(changeIndex))));
                } else if(changeType == ListEvent.UPDATE) {
                    elementCounts.set(changeIndex, new Integer(count(popularityList.get(changeIndex))));
                }
            }
            
            // validate the changes
            assertEquals(popularityList.size(), elementCounts.size());
            for(Iterator<Integer> c = changedIndices.iterator(); c.hasNext(); ) {
                int changeIndex = c.next().intValue();
                for(int i = Math.max(changeIndex - 1, 0); i < Math.min(changeIndex+2, popularityList.size()); i++) {
                    assertEquals("Test index " + i + ", value: " + popularityList.get(i), elementCounts.get(i), new Integer(count(popularityList.get(i))));
                }
            }
        }

        /**
         * Get the number of copies of the specified value exist in the source list.
         */
        public int count(Object value) {
            int first = sortedSource.indexOf(value);
            if(first == -1) return 0;
            int last = sortedSource.lastIndexOf(value);
            return (last - first + 1);
        }
    }
}
