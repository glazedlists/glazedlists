/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the SortedList works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SortedListTest extends TestCase {

    /** the source list */
    private BasicEventList unsortedList = null;

    /** the sorted list */
    private SortedList sortedList = null;

    /** for randomly choosing list indicies */
    private Random random = new Random(2);

    /**
     * Prepare for the test.
     */
    public void setUp() {
        unsortedList = new BasicEventList();
        sortedList = new SortedList(unsortedList);
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        unsortedList = null;
        sortedList = null;
    }
    
    /**
     * Test that the indexOf() and lastIndexOf() methods work if the SortedList
     * is not actually sorted.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=170">Bug 170</a>
     */
    public void testIndexOfUnsorted() {
        sortedList.setComparator(null);
        sortedList.add("Riders");
        sortedList.add("Stampeders");
        sortedList.add("Bombers");
        sortedList.add("Eskimos");
        sortedList.add("Argos");
        sortedList.add("Ti-Cats");
        sortedList.add("Riders");
        sortedList.add("Als");
        
        assertEquals(0, sortedList.indexOf("Riders"));
        assertEquals(6, sortedList.lastIndexOf("Riders"));
        assertEquals(8, sortedList.indexOfSimulated("Riders"));
    }

    /**
     * Test to verify that the sorted list is working correctly when it is
     * applied to a list that already has values.
     */
    public void testSortBeforeAndAfter() {
        // populate a list with strings
        for(int i = 0; i < 4000; i++) {
            unsortedList.add(new Integer(random.nextInt()));
        }

        // build a control list of the desired results
        ArrayList controlList = new ArrayList();
        controlList.addAll(unsortedList);
        Collections.sort(controlList);

        // verify the lists are equal
        assertEquals(controlList, sortedList);

        // re-sort the list
        sortedList = new SortedList(unsortedList);

        // verify the lists are equal
        assertEquals(controlList, sortedList);
    }

    /**
     * Test to verify that the SortedList is working correctly when the
     * list is changing by adds, removes and deletes.
     */
    public void testSortDynamic() {
        // apply various operations to the list of Integers
        for(int i = 0; i < 4000; i++) {
            int operation = random.nextInt(4);
            int index = unsortedList.isEmpty() ? 0 : random.nextInt(unsortedList.size());

            if(operation <= 1 || unsortedList.isEmpty()) {
                unsortedList.add(index, new Integer(random.nextInt()));
            } else if(operation == 2) {
                unsortedList.remove(index);
            } else if(operation == 3) {
                unsortedList.set(index, new Integer(random.nextInt()));
            }
        }

        // build a control list of the desired results
        ArrayList controlList = new ArrayList();
        controlList.addAll(unsortedList);
        Collections.sort(controlList);

        // verify the lists are equal
        assertEquals(controlList, sortedList);
    }

    /**
     * Tests to verify that the SortedList correctly handles modification.
     *
     * This performs a sequence of operations. Each operation is performed on
     * either the sorted list or the unsorted list. The list where the operation
     * is performed is selected at random.
     */
    public void testSortedListWritable() {
        // apply various operations to the either list
        for(int i = 0; i < 4000; i++) {
            List list;
            if(random.nextBoolean()) list = sortedList;
            else list = unsortedList;
            int operation = random.nextInt(4);
            int index = list.isEmpty() ? 0 : random.nextInt(list.size());

            if(operation <= 1 || list.isEmpty()) {
                list.add(index, new Integer(random.nextInt()));
            } else if(operation == 2) {
                list.remove(index);
            } else if(operation == 3) {
                list.set(index, new Integer(random.nextInt()));
            }
        }

        // build a control list of the desired results
        ArrayList controlList = new ArrayList();
        controlList.addAll(unsortedList);
        Collections.sort(controlList);

        // verify the lists are equal
        assertEquals(controlList, sortedList);
    }


    /**
     * Tests that sorting works on a large set of filter changes.
     */
    public void testAgressiveFiltering() {
        BasicEventList source = new BasicEventList();
        IntArrayFilterList filterList = new IntArrayFilterList(source);
        SortedList sorted = new SortedList(filterList, new IntArrayComparator(0));

        // populate a list with 1000 random arrays between 0 and 1000
        for(int i = 0; i < 1000; i++) {
            int value = random.nextInt(1000);
            int[] array = new int[] { value, random.nextInt(2), random.nextInt(2), random.nextInt(2) };
            source.add(array);
        }

        // try ten different filters
        for(int i = 0; i < 10; i++) {
            // apply the filter
            int filterColumn = random.nextInt(3);
            filterList.setFilter(filterColumn + 1, 1);

            // construct the control list
            ArrayList controlList = new ArrayList();
            controlList.addAll(filterList);
            Collections.sort(controlList, new IntArrayComparator(0));

            // verify that the control and sorted list are the same
            assertEquals(sorted.size(), controlList.size());
            for(int j = 0; j < sorted.size(); j++) {
                assertEquals(((int[])sorted.get(j))[0], ((int[])controlList.get(j))[0]);
            }
        }
    }

    /**
     * Test indexOf() consistency
     */
    public void testIndexOf() {
        BasicEventList source = new BasicEventList();
        SortedList sorted = new SortedList(source, new IntegerComparator());

        // Test containment of a 10 on an empty list
        Integer ten = new Integer(10);
        int emptyTest = sorted.indexOf(ten);
        assertEquals(-1, emptyTest);

        // Add 12 leading 1's
        Integer one = new Integer(1);
        for(int i = 0; i < 12; i++) {
            source.add(one);
        }

        // Add 13 5's in the middle
        Integer five = new Integer(5);
        for(int i = 0; i < 13; i++) {
            source.add(five);
        }

        // Add 10 trailing 9's
        Integer nine = new Integer(9);
        for(int i = 0; i < 10; i++) {
            source.add(nine);
        }

        // Look for the index of a 1
        int firstTestIndex = sorted.indexOf(one);
        assertEquals(0, firstTestIndex);

        // Look for the index of a 5
        int secondTestIndex = sorted.indexOf(five);
        assertEquals(12, secondTestIndex);

        // Look for the index of a 9
        int thirdTestIndex = sorted.indexOf(nine);
        assertEquals(25, thirdTestIndex);

        // Test containment of a 10
        int fourthTest = sorted.indexOf(ten);
        assertEquals(-1, fourthTest);
    }

    /**
     * Test lastIndexOf() consistency
     */
    public void testLastIndexOf() {
        BasicEventList source = new BasicEventList();
        SortedList sorted = new SortedList(source, new IntegerComparator());

        // Test containment of a 10 on an empty list
        Integer ten = new Integer(10);
        int emptyTest = sorted.lastIndexOf(ten);
        assertEquals(-1, emptyTest);

        // Add 12 leading 1's
        Integer one = new Integer(1);
        for(int i = 0; i < 12; i++) {
            source.add(one);
        }

        // Add 13 5's in the middle
        Integer five = new Integer(5);
        for(int i = 0; i < 13; i++) {
            source.add(five);
        }

        // Add 10 trailing 9's
        Integer nine = new Integer(9);
        for(int i = 0; i < 10; i++) {
            source.add(nine);
        }

        // Look for the index of a 1
        int firstTestIndex = sorted.lastIndexOf(one);
        assertEquals(11, firstTestIndex);

        // Look for the index of a 5
        int secondTestIndex = sorted.lastIndexOf(five);
        assertEquals(24, secondTestIndex);

        // Look for the index of a 9
        int thirdTestIndex = sorted.lastIndexOf(nine);
        assertEquals(34, thirdTestIndex);

        // Test containment of a 10
        int fourthTest = sorted.lastIndexOf(ten);
        assertEquals(-1, fourthTest);
    }

    /**
     * Test containment accuracy
     */
    public void testContains() {
        BasicEventList source = new BasicEventList();
        SortedList sorted = new SortedList(source, new IntegerComparator());

        // Test containment of a 10 on an empty list
        Integer ten = new Integer(10);
        boolean emptyTest = sorted.contains(ten);
        assertEquals(false, emptyTest);

        // Add 12 leading 1's
        Integer one = new Integer(1);
        for(int i = 0; i < 12; i++) {
            source.add(one);
        }

        // Add 13 5's in the middle
        Integer five = new Integer(5);
        for(int i = 0; i < 13; i++) {
            source.add(five);
        }

        // Add 10 trailing 9's
        Integer nine = new Integer(9);
        for(int i = 0; i < 10; i++) {
            source.add(nine);
        }

        // Test containment of a 1
        boolean firstTest = sorted.contains(one);
        assertEquals(true, firstTest);

        // Test containment of a 5
        boolean secondTest = sorted.contains(five);
        assertEquals(true, secondTest);

        // Test containment of a 9
        boolean thirdTest = sorted.contains(nine);
        assertEquals(true, thirdTest);

        // Test containment of a 10
        boolean fourthTest = sorted.contains(ten);
        assertEquals(false, fourthTest);
    }


    /**
     * Test if the SortedList fires update events rather than delete/insert
     * pairs.
     */
    public void testUpdateEventsFired() {
        // prepare a unique list with simple data
        UniqueList uniqueSource = new UniqueList(unsortedList, GlazedLists.reverseComparator());
        sortedList = new SortedList(uniqueSource);
        SortedSet data = new TreeSet(GlazedLists.reverseComparator());
        data.add("A");
        data.add("B");
        data.add("C");
        data.add("D");
        uniqueSource.replaceAll(data);

        // listen to changes on the sorted list
        ListEventCounter counter = new ListEventCounter();
        sortedList.addListEventListener(counter);

        // replace the data with an identical copy
        uniqueSource.replaceAll(data);

        // verify that only one event has occured
        assertEquals(1, counter.getEventCount());
        assertEquals(4, counter.getChangeCount(0));
    }


    /**
     * Test if the SortedList fires update events rather than delete/insert
     * pairs, even if there are duplicate copies of the same value.
     */
    public void testUpdateEventsFiredWithDuplicates() {
        // create comparators for zero and one
        Comparator intCompareAt0 = new IntArrayComparator(0);
        Comparator intCompareAt1 = new IntArrayComparator(1);

        // prepare a unique list with simple data
        UniqueList uniqueSource = new UniqueList(new BasicEventList(), intCompareAt0);
        sortedList = new SortedList(uniqueSource, intCompareAt1);
        SortedSet data = new TreeSet(intCompareAt0);
        data.add(new int[] { 0, 0 });
        data.add(new int[] { 1, 0 });
        data.add(new int[] { 2, 0 });
        data.add(new int[] { 3, 0 });
        uniqueSource.replaceAll(data);

        // listen to changes on the sorted list
        ListEventCounter counter = new ListEventCounter();
        sortedList.addListEventListener(counter);

        // replace the data with an identical copy
        uniqueSource.replaceAll(data);

        // verify that only one event has occured
        assertEquals(1, counter.getEventCount());
        assertEquals(4, counter.getChangeCount(0));
    }

    /**
     * Test if the SortedList fires update events rather than delete/insert
     * pairs, when using a ReverseComparator.
     *
     * <p>The source list uses a totally different comparator than the sorted list
     * in order to guarantee the indicies have no pattern.
     */
    public void testUpdateEventsFiredRigorous() {
        // prepare a unique list with simple data
        Comparator uniqueComparator = new ReverseStringComparator();
        UniqueList uniqueSource = new UniqueList(unsortedList, uniqueComparator);
        sortedList = new SortedList(uniqueSource);

        // populate a unique source with some random elements
        for(int i = 0; i < 500; i++) {
            uniqueSource.add("" + random.nextInt(200));
        }

        // populate a replacement set with some more random elements
        SortedSet data = new TreeSet(uniqueComparator);
        for(int i = 0; i < 500; i++) {
            data.add("" + random.nextInt(200));
        }

        // calculate the number of changes expected
        List intersection = new ArrayList();
        intersection.addAll(uniqueSource);
        intersection.retainAll(data);
        int expectedUpdateCount = intersection.size();
        int expectedDeleteCount = uniqueSource.size() - expectedUpdateCount;
        int expectedInsertCount = data.size() - expectedUpdateCount;
        int expectedChangeCount = expectedUpdateCount + expectedDeleteCount + expectedInsertCount;

        // count the number of changes performed
        ListEventCounter uniqueCounter = new ListEventCounter();
        uniqueSource.addListEventListener(uniqueCounter);
        ListEventCounter sortedCounter = new ListEventCounter();
        sortedList.addListEventListener(sortedCounter);
        //sortedList.debug = true;

        // perform the change
        uniqueSource.addListEventListener(new ConsistencyTestList(uniqueSource, "unique", false));
        sortedList.addListEventListener(new ConsistencyTestList(sortedList, "sorted", false));
        uniqueSource.replaceAll(data);

        // verify our guess on the change count is correct
        assertEquals(1, uniqueCounter.getEventCount());
        assertEquals(1, sortedCounter.getEventCount());
        assertEquals(expectedChangeCount, uniqueCounter.getChangeCount(0));
        assertEquals(expectedChangeCount, sortedCounter.getChangeCount(0));
    }


    /**
     * Tests that the SortedList can handle reordering events.
     */
    public void testReorder() {
        // prepare a source list
        SortedList source = new SortedList(new BasicEventList());
        source.add("CB");
        source.add("BC");
        source.add("DD");
        source.add("AA");

        // create a sorted view of that list
        SortedList sorted = new SortedList(source, GlazedLists.reverseComparator());

        // create a consistency test
        List consistencyTestList = new ArrayList();
        consistencyTestList.addAll(sorted);

        // change the source, this should not impact its listener
        source.setComparator(new ReverseStringComparator());
        assertEquals(consistencyTestList, sorted);

        // change the source, this should not impact its listener
        source.setComparator(null);
        assertEquals(consistencyTestList, sorted);
    }

    /**
     * Verify that the sorted list works with no compatator.
     */
    public void testNoComparator() {
        List consistencyTestList = new ArrayList();
        consistencyTestList.add("A");
        consistencyTestList.add("C");
        consistencyTestList.add("B");

        SortedList sorted = new SortedList(new BasicEventList(), null);
        sorted.addAll(consistencyTestList);
        assertEquals(consistencyTestList, sorted);

        sorted.set(2, "A");
        sorted.clear();
        assertEquals(Collections.EMPTY_LIST, sorted);
    }

    /**
     * Test that values that are indistinguishable by the SortedList are ordered
     * by their index.
     */
    public void testEqualValuesOrderedByIndex() {
        // create a sorted list that cannot distinguish between the data items
        Comparator intCompareAt0 = new StringLengthComparator();
        sortedList.dispose();
        sortedList = new SortedList(unsortedList, intCompareAt0);

        // populate the unsorted list
        unsortedList.add(0, "chaos"); // c
        unsortedList.add(1, "fiery"); // c f
        unsortedList.add(2, "gecko"); // c f g
        unsortedList.add(0, "banjo"); // b c f g
        unsortedList.add(2, "dingo"); // b c d f g
        unsortedList.add(5, "hippo"); // b c d f g h
        unsortedList.add(0, "album"); // a b c d f g h
        unsortedList.add(4, "eerie"); // a b c d e f g h
        assertEquals(unsortedList, sortedList);
    }

    /**
     * Compares Strings by their length.
     */
    class StringLengthComparator implements Comparator {
        public int compare(Object a, Object b) {
            String stringA = (String)a;
            String stringB = (String)b;
            return stringA.length() - stringB.length();
        }
    }

    /**
     * Explicit comparator for Kevin's sanity!
     */
    class IntegerComparator implements Comparator {
        public int compare(Object a, Object b) {
            int number1 = ((Integer)a).intValue();
            int number2 = ((Integer)b).intValue();

            return number1 - number2;
        }
    }

    /**
     * A Comparator that compares strings from end to beginning rather than
     * normally.
     */
    class ReverseStringComparator implements Comparator {
        public Comparator delegate = GlazedLists.comparableComparator();

        public int compare(Object a, Object b) {
            String aString = (String)a;
            String bString = (String)b;
            return delegate.compare(flip(aString), flip(bString));
        }

        public String flip(String original) {
            char[] originalAsChars = original.toCharArray();
            int length = originalAsChars.length;
            for(int i = 0; i < (length / 2); i++) {
                char temp = originalAsChars[i];
                originalAsChars[i] = originalAsChars[length - i - 1];
                originalAsChars[length - i - 1] = temp;
            }
            String originalReversed = new String(originalAsChars);
            return originalReversed;
        }
    }
}
