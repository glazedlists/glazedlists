/**
 * Glazed Lists
 * http;//glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.util.impl.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the BooleanList works as expected.
 *
 * @author <a href="mailto;kevin@swank.ca">Kevin Maltby</a>
 */
public class BooleanListTest extends TestCase {

    /** for randomly choosing list indicies */
    private Random random = new Random(101);

    /** the BooleanList to test on */
    private BooleanList compressableList = null;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        compressableList = new BooleanList();
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        compressableList.clear();
        compressableList = null;
    }

    /**
     * Tests that adding works for values
     */
    public void testSimpleAddValue() {
        compressableList.add(0, Boolean.TRUE);
        assertEquals(1, compressableList.size());
        assertEquals(Boolean.TRUE, compressableList.get(0));
    }

    /**
     * Tests that adding works for nulls at the end of the tree
     */
    public void testAddTrailingNullOnEmptyTree() {
        compressableList.add(0, null);
        assertEquals(1, compressableList.size());
        assertEquals(null, compressableList.get(0));
    }

    /**
     * Tests that adding works for nulls at the end of the tree
     */
    public void testAddTrailingNullNonEmptyTree() {
        compressableList.add(0, Boolean.TRUE);
        compressableList.add(1, null);
        assertEquals(2, compressableList.size());
        assertEquals(Boolean.TRUE, compressableList.get(0));
        assertEquals(null, compressableList.get(1));
    }

    /**
     * Tests that adding works for nulls at the end of the tree
     */
    public void testAddLeadingNull() {
        compressableList.add(0, Boolean.TRUE);
        compressableList.add(0, null);
        assertEquals(2, compressableList.size());
        assertEquals(null, compressableList.get(0));
        assertEquals(Boolean.TRUE, compressableList.get(1));
    }


    /**
     * Tests getCompressedIndex()
     */
    public void testGetCompressedIndex() {
        compressableList.addValues(0, 10);
        compressableList.addNulls(1, 9);
        compressableList.addNulls(11, 9);
        compressableList.addNulls(21, 9);
        compressableList.addNulls(31, 9);
        compressableList.addNulls(41, 9);
        compressableList.addNulls(51, 9);
        compressableList.addNulls(61, 9);
        compressableList.addNulls(71, 9);
        compressableList.addNulls(81, 9);
        compressableList.addNulls(91, 9);

        assertEquals(10, compressableList.getCompressedList().size());

        for(int i = 0; i < 10; i++) {
            assertEquals(i, compressableList.getCompressedIndex(10*i));
        }
    }

    /**
     * Test leading and trailing nulls
     */
    public void testLeadingAndTrailingNulls() {
        compressableList.addValues(0, 10);
        for(int i = 10; i >= 0; i--) {
            compressableList.addNulls(i, i);
        }

        assertEquals(10, compressableList.getCompressedList().size());

        for(int i = 0; i < 10; i++) {
            assertEquals(i, compressableList.getLeadingNulls(i));
            assertEquals(i + 1, compressableList.getTrailingNulls(i));
        }
    }



    /**
     * Tests to verify that the sparse list is consistent after a long
     * series of list operations.
     */
    public void testListOperations() {
        List controlList = new ArrayList();

        // apply various operations to both lists
        for(int i = 0; i < 5000; i++) {

            int operation = random.nextInt(5);
            int index = controlList.isEmpty() ? 0 : random.nextInt(controlList.size());
            Object value = Boolean.TRUE;

            if(operation == 0 || controlList.isEmpty()) {
                compressableList.add(index, value);
                controlList.add(index, value);
            } else if(operation == 1) {
                compressableList.add(index, null);
                controlList.add(index, null);
            } else if(operation == 2) {
                compressableList.remove(index);
                controlList.remove(index);
            } else if(operation == 3) {
                compressableList.set(index, value);
                controlList.set(index, value);
            } else if(operation == 4) {
                compressableList.set(index, null);
                controlList.set(index, null);
            }
        }

        // verify the lists are equal
        assertEquals(controlList, compressableList);
    }

    /**
     * Performance tests this ADT.  Some of what I do in this might seem
     * somewhat odd, namely the pre-calculation of all data and indicies
     * prior to each test.  While this isn't efficient in code,, it results in
     * the tests being more accurate to the list operation being performed rather
     * than reflecting other operations as well.
     *
     */
    public static void main(String[] args) {
        System.out.println("Starting BooleanList Performance Tests.  Please Wait...\n");

        Random mainRandom = new Random(11);
        long startTime = 0;
        long endTime = 0;
        int counter = 0;
        int nulls = 0;
        int[] indexes = new int[500000];
        Integer[] values = new Integer[500000];

        // Performance Test For Creation
        System.out.println("Running the performance test for initialization.");

        System.out.println("BooleanList:");
        startTime = System.currentTimeMillis();
        BooleanList testTree = new BooleanList();
        endTime = System.currentTimeMillis();
        System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");

        System.out.println("SparseList:");
        startTime = System.currentTimeMillis();
        SparseList testList = new SparseList();
        endTime = System.currentTimeMillis();
        System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");


        for(int r = 1;r < 5;r++) {
            System.out.println("Testing Round " + r);

            // Performance Test For ADD

            // prime the indicies
            indexes[0] = 0;
            for(int i = 2;i <= 500000;i++) {
                indexes[i-1] = mainRandom.nextInt(i);
            }

            // prime the values
            nulls = 0;
            for(int i = 0;i < 500000;i++) {
                if(mainRandom.nextBoolean()) {
                    values[i] = null;
                    nulls++;

                } else values[i] = new Integer(mainRandom.nextInt());
            }

            try{
                Thread.sleep(2000);
            } catch(InterruptedException e) {
                System.out.println("Failed to pause before tests.");
            }

            System.out.println("Running the performance test for adding 500000 elements.");
            System.out.println("Values: " + (500000 - nulls));
            System.out.println("Nulls: " + nulls + "\n");


            System.out.println("BooleanList:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testTree.add(indexes[counter], values[counter]);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");

            System.out.println("SparseList:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testList.add(indexes[counter], values[counter]);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");


            // Performance Test For GET

            System.out.println("Running the performance test for looking up all of the 500000 elements.");
            System.out.println("Values: " + (500000 - nulls));
            System.out.println("Nulls: " + nulls);

            System.out.println("BooleanList:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testTree.get(indexes[counter]);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");

            System.out.println("SparseList:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testList.get(indexes[counter]);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");

            // Performance Test For SET

            // prime the indicies
            indexes[0] = 0;
            for(int i = 2;i <= 500000;i++) {
                indexes[i-1] = mainRandom.nextInt(i);
            }

            // prime the values
            nulls = 0;
            for(int i = 0;i < 500000;i++) {
                if(mainRandom.nextBoolean()) {
                    values[i] = null;
                    nulls++;

                } else values[i] = new Integer(mainRandom.nextInt());
            }

            System.out.println("Running the performance test for setting 500000 elements.");
            System.out.println("Values: " + (500000 - nulls));
            System.out.println("Nulls: " + nulls);

            System.out.println("BooleanList:");
            counter = 0;
            startTime = System.currentTimeMillis();
            try {
            for(;counter < 500000;counter++) {
                testTree.set(indexes[counter], values[counter]);
            }
            } catch(Exception e) {
                System.out.println("Error occurred at step " + counter + "\n"
                    + "Tree Size: " + testTree.size() + "\n"
                    + "Value: " + values[counter] + "\n"
                    + "Index: " + indexes[counter] + "\n"
                    + e.getMessage());
            }
            endTime = System.currentTimeMillis();
            System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");

            System.out.println("SparseList:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testList.set(indexes[counter], values[counter]);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");

            // Performance Test For DELETE

            // prime the indicies
            indexes[499999] = 0;
            for(int i = 0;i < 499999;i++) {
                indexes[i] = mainRandom.nextInt(500000 - i);
            }

            System.out.println("Running the performance test for removing 500000 elements.");

            System.out.println("BooleanList:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testTree.remove(indexes[counter]);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");

            System.out.println("SparseList:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testList.remove(indexes[counter]);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");
        }
    }
}
