/**
 * Glazed Lists
 * http;//glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.adt;

// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the SparseList works as expected.
 *
 * @author <a href="mailto;kevin@swank.ca">Kevin Maltby</a>
 */
public class SparseListTest extends TestCase {

    /** for randomly choosing list indicies */
    private Random random = new Random(101);

    /** the SparseList to test on */
    private SparseList sparseTree = null;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        sparseTree = new SparseList();
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        sparseTree.clear();
        sparseTree = null;
    }

    /**
     * Tests that adding works for values
     */
    public void testSimpleAddValue() {
        sparseTree.add(0, "Test");
        assertEquals(1, sparseTree.size());
        assertEquals("Test", sparseTree.get(0));
    }

    /**
     * Tests that adding works for nulls at the end of the tree
     */
    public void testAddTrailingNullOnEmptyTree() {
        sparseTree.add(0, null);
        assertEquals(1, sparseTree.size());
        assertEquals(null, sparseTree.get(0));
    }

    /**
     * Tests that adding works for nulls at the end of the tree
     */
    public void testAddTrailingNullNonEmptyTree() {
        sparseTree.add(0, "Test");
        sparseTree.add(1, null);
        assertEquals(2, sparseTree.size());
        assertEquals("Test", sparseTree.get(0));
        assertEquals(null, sparseTree.get(1));
    }

    /**
     * Tests that adding works for nulls at the end of the tree
     */
    public void testAddLeadingNull() {
        sparseTree.add(0, "Test");
        sparseTree.add(0, null);
        assertEquals(2, sparseTree.size());
        assertEquals(null, sparseTree.get(0));
        assertEquals("Test", sparseTree.get(1));
    }

    /**
     * Tests that adding works when adding a value at the beginning of the tree
     */
    public void testAddValueAtStartNoNulls() {
        sparseTree.add(0, "InitialValue");
        sparseTree.add(0, "Test");
        assertEquals(2, sparseTree.size());
        assertEquals("Test", sparseTree.get(0));
        assertEquals("InitialValue", sparseTree.get(1));
    }

    /**
     * Tests that adding works when adding a value at the beginning of the tree
     */
    public void testAddValueAtStartWithNulls() {
        sparseTree.add(0, "InitialValue");
        // Add some leading nulls
        for(int i = 0; i < 5; i ++) {
            sparseTree.add(0, null);
        }
        // Add some trailing nulls
        for(int i = 0; i < 5; i ++) {
            sparseTree.add(sparseTree.size(), null);
        }

        sparseTree.add(0, "Test");
        assertEquals(12, sparseTree.size());
        assertEquals("Test", sparseTree.get(0));
        assertEquals("InitialValue", sparseTree.get(6));
    }

    /**
     * Tests that adding works when adding a value at the end of the tree
     */
    public void testAddValueAtEndNoNulls() {
        sparseTree.add(0, "InitialValue");
        sparseTree.add(1, "Test");
        assertEquals(2, sparseTree.size());
        assertEquals("InitialValue", sparseTree.get(0));
        assertEquals("Test", sparseTree.get(1));
    }

    /**
     * Tests that adding works when adding a value at the end of the tree
     */
    public void testAddValueAtEndWithNulls() {
        sparseTree.add(0, "InitialValue");
        // Add some leading nulls
        for(int i = 0; i < 5; i ++) {
            sparseTree.add(0, null);
        }
        // Add some trailing nulls
        for(int i = 0; i < 5; i ++) {
            sparseTree.add(sparseTree.size(), null);
        }

        sparseTree.add(sparseTree.size(), "Test");
        assertEquals(12, sparseTree.size());
        assertEquals("Test", sparseTree.get(11));
        assertEquals("InitialValue", sparseTree.get(5));
    }

    /**
     * Tests that adding works for a value in the trailing nulls
     */
    public void testAddingValueInTrailingNulls() {
        // Add some trailing nulls
        for(int i = 0; i < 5; i ++) {
            sparseTree.add(0, null);
        }
        sparseTree.add(2, "Test");
        assertEquals(6, sparseTree.size());
        assertEquals(null, sparseTree.get(0));
        assertEquals(null, sparseTree.get(1));
        assertEquals("Test", sparseTree.get(2));
        assertEquals(null, sparseTree.get(3));
        assertEquals(null, sparseTree.get(4));
        assertEquals(null, sparseTree.get(5));
    }

    /**
     * Tests that adding works for a value somewhere in the leading nulls
     */
    public void testAddingValueInLeadingNulls() {
        sparseTree.add(0, "InitialValue");
        // Add some leading nulls
        for(int i = 0; i < 5; i ++) {
            sparseTree.add(0, null);
        }
        sparseTree.add(2, "Test");
        assertEquals(7, sparseTree.size());
        assertEquals(null, sparseTree.get(0));
        assertEquals(null, sparseTree.get(1));
        assertEquals("Test", sparseTree.get(2));
        assertEquals(null, sparseTree.get(3));
        assertEquals(null, sparseTree.get(4));
        assertEquals(null, sparseTree.get(5));
        assertEquals("InitialValue", sparseTree.get(6));
    }

    /**
     * Test that adding works after an AVL-rotation
     */
    public void testAddUntilAVLRotate() {
        for(int i = 0; i < 5; i ++) {
            sparseTree.add(0, new Integer(i));
        }

        assertEquals(5, sparseTree.size());
        for(int i = 0;i < 5;i++) {
            assertEquals(new Integer(4 - i), sparseTree.get(i));
        }
    }

    /**
     * Tests that getIndex() functions correctly
     */
    public void testGetIndex() {
        // Add values and nulls into the tree in no particular order
        for(int i = 0; i < 1000; i++) {
            int index = sparseTree.size() == 0 ? 0 : random.nextInt(sparseTree.size());

            // Add a random value at index
            if(random.nextBoolean()) sparseTree.add(index, new Integer(random.nextInt()));

            // Add a null at index
            else sparseTree.add(index, null);
        }

        // for each node, look it up and validate
        for (int i = 0; i < 1000; i ++) {
            SparseListNode node = sparseTree.getNode(i);
            if(node != null) {
                assertEquals(i, node.getIndex());
            }
        }
    }

    /**
     * Test setting a leading null to a null
     */
    public void testSetNullToNullInTree() {
        // Add three values
        sparseTree.add(0, new Integer(0));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(0, new Integer(2));
        assertEquals(3, sparseTree.size());

        // Add trailing nulls to the tree
        sparseTree.addNulls(3, 5);

        // Add leading nulls to each node
        sparseTree.addNulls(2, 4);
        sparseTree.addNulls(1, 4);
        sparseTree.addNulls(0, 4);

        assertEquals(null, sparseTree.get(2));
        assertEquals(20, sparseTree.size());
        sparseTree.set(2, null);
        assertEquals(null, sparseTree.get(2));
        assertEquals(20, sparseTree.size());
    }

    /**
     * Test setting a leading null to a value
     */
    public void testSetNullToValueInTree() {
        // Add three values
        sparseTree.add(0, new Integer(0));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(0, new Integer(2));

        // Add trailing nulls to the tree
        sparseTree.addNulls(3, 5);

        // Add leading nulls to each node
        sparseTree.addNulls(2, 4);
        sparseTree.addNulls(1, 4);
        sparseTree.addNulls(0, 4);

        assertEquals(null, sparseTree.get(2));
        assertEquals(20, sparseTree.size());
        sparseTree.set(2, new Integer(3));
        assertEquals(new Integer(3), sparseTree.get(2));
        assertEquals(20, sparseTree.size());
    }

    /**
     * Test setting a value to another value
     */
    public void testSetValueToValueInTree() {
        // Add three values
        sparseTree.add(0, new Integer(0));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(0, new Integer(2));

        // Add trailing nulls to the tree
        sparseTree.addNulls(3, 5);

        // Add leading nulls to each node
        sparseTree.addNulls(2, 4);
        sparseTree.addNulls(1, 4);
        sparseTree.addNulls(0, 4);

        assertEquals(new Integer(1), sparseTree.get(9));
        assertEquals(20, sparseTree.size());
        sparseTree.set(9, new Integer(9));
        assertEquals(new Integer(9), sparseTree.get(9));
        assertEquals(20, sparseTree.size());
    }

    /**
     * Test setting a value to a null
     */
    public void testSetValueToNullInTree() {
        // Add three values
        sparseTree.add(0, new Integer(0));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(0, new Integer(2));

        // Add trailing nulls to the tree
        sparseTree.addNulls(3, 5);

        // Add leading nulls to each node
        sparseTree.addNulls(2, 4);
        sparseTree.addNulls(1, 4);
        sparseTree.addNulls(0, 4);

        assertEquals(new Integer(1), sparseTree.get(9));
        assertEquals(20, sparseTree.size());
        sparseTree.set(9, null);
        assertEquals(null, sparseTree.get(9));
        assertEquals(20, sparseTree.size());
    }

    /**
     * Test setting a value to a null when the value is at the end of the tree
     */
    public void testSetValueAtEndOfTreeToNull() {
        // Add three values
        sparseTree.add(0, new Integer(0));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(0, new Integer(2));

        // Add trailing nulls to the tree
        sparseTree.addNulls(3, 5);

        // Add leading nulls to each node
        sparseTree.addNulls(2, 4);
        sparseTree.addNulls(1, 4);
        sparseTree.addNulls(0, 4);

        assertEquals(new Integer(0), sparseTree.get(14));
        assertEquals(20, sparseTree.size());
        sparseTree.set(14, null);
        assertEquals(null, sparseTree.get(14));
        assertEquals(20, sparseTree.size());
    }

    /**
     * Test setting a trailing null to a value
     */
    public void testSetNullToNullInTrailingNulls() {
        // Add three values
        sparseTree.add(0, new Integer(0));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(0, new Integer(2));

        // Add trailing nulls to the tree
        sparseTree.addNulls(3, 5);

        // Add leading nulls to each node
        sparseTree.addNulls(2, 4);
        sparseTree.addNulls(1, 4);
        sparseTree.addNulls(0, 4);

        assertEquals(null, sparseTree.get(17));
        assertEquals(20, sparseTree.size());
        sparseTree.set(17, null);
        assertEquals(null, sparseTree.get(17));
        assertEquals(20, sparseTree.size());
    }

    /**
     * Test setting a trailing null to a value
     */
    public void testSetNullToValueInTrailingNulls() {
        // Add three values
        sparseTree.add(0, new Integer(0));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(0, new Integer(2));

        // Add trailing nulls to the tree
        sparseTree.addNulls(3, 5);

        // Add leading nulls to each node
        sparseTree.addNulls(2, 4);
        sparseTree.addNulls(1, 4);
        sparseTree.addNulls(0, 4);

        assertEquals(null, sparseTree.get(17));
        assertEquals(20, sparseTree.size());
        sparseTree.set(17, new Integer(17));
        assertEquals(new Integer(17), sparseTree.get(17));
        assertEquals(20, sparseTree.size());
    }

    /**
     * Tests that a node gets removed from the tree
     */
    public void testSimpleRemove() {
        sparseTree.add(0, new Integer(1));
        sparseTree.remove(0);

        assertEquals(0, sparseTree.size());
    }

    /**
     * Tests that a trailing null gets removed
     */
    public void testRemoveTrailingNull() {
        sparseTree.add(0, new Integer(4));
        sparseTree.add(0, new Integer(3));
        sparseTree.add(0, new Integer(2));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(4, null);
        assertEquals(5, sparseTree.size());

        sparseTree.remove(4);

        assertEquals(4, sparseTree.size());
        assertEquals(new Integer(1), sparseTree.get(0));
        assertEquals(new Integer(2), sparseTree.get(1));
        assertEquals(new Integer(3), sparseTree.get(2));
        assertEquals(new Integer(4), sparseTree.get(3));
    }

    /**
     * Tests that a leading null gets removed
     */
    public void testRemoveLeadingNull() {
        sparseTree.add(0, new Integer(4));
        sparseTree.add(0, new Integer(3));
        sparseTree.add(0, new Integer(2));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(2, null);
        assertEquals(5, sparseTree.size());

        sparseTree.remove(2);

        assertEquals(4, sparseTree.size());
        assertEquals(new Integer(1), sparseTree.get(0));
        assertEquals(new Integer(2), sparseTree.get(1));
        assertEquals(new Integer(3), sparseTree.get(2));
        assertEquals(new Integer(4), sparseTree.get(3));
    }

    /**
     * Tests that leading nulls are correctly repositioned when the
     * node they belong to is removed.
     */
    public void testRemovingValueWithLeadingNulls() {
        sparseTree.add(0, new Integer(4));
        sparseTree.add(0, new Integer(3));
        sparseTree.add(0, new Integer(2));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(2, null);
        sparseTree.add(2, null);
        sparseTree.add(2, null);
        sparseTree.add(2, null);
        assertEquals(8, sparseTree.size());

        sparseTree.remove(6);

        assertEquals(7, sparseTree.size());
        assertEquals(new Integer(1), sparseTree.get(0));
        assertEquals(new Integer(2), sparseTree.get(1));
        assertEquals(null, sparseTree.get(2));
        assertEquals(null, sparseTree.get(3));
        assertEquals(null, sparseTree.get(4));
        assertEquals(null, sparseTree.get(5));
        assertEquals(new Integer(4), sparseTree.get(6));
    }

    /**
     * Tests that a remove causing an AVL rotation behaves
     * correctly.
     */
    public void testAVLRotateOnRemove() {
        sparseTree.add(0, new Integer(3));
        sparseTree.add(0, new Integer(2));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(3, new Integer(4));

        sparseTree.remove(0);

        assertEquals(3, sparseTree.size());
        assertEquals(new Integer(2), sparseTree.get(0));
        assertEquals(new Integer(3), sparseTree.get(1));
        assertEquals(new Integer(4), sparseTree.get(2));
    }


    /**
     * Test for illegal tree state bug
     */
    public void testIllegalTreeStateBug() {
        sparseTree.add(0, Boolean.TRUE);
        sparseTree.set(0, Boolean.FALSE);
        sparseTree.add(0, Boolean.TRUE);
        sparseTree.add(1, "*");
        sparseTree.set(2, null);
        sparseTree.set(2, Boolean.FALSE);
        sparseTree.set(2, Boolean.TRUE);
        sparseTree.add(1, "&");
        sparseTree.add(3, null);
        sparseTree.set(0, null);
        sparseTree.set(3, "+");
        sparseTree.add(0, "$");
        sparseTree.set(3, null);

        assertEquals(6, sparseTree.size());
        assertEquals("$", sparseTree.get(0));
        assertEquals(null, sparseTree.get(1));
        assertEquals("&", sparseTree.get(2));
        assertEquals(null, sparseTree.get(3));
        assertEquals("+", sparseTree.get(4));
        assertEquals(Boolean.TRUE, sparseTree.get(5));
    }

    /**
     * Test for the accidental removal of a right child when a
     * node being unlinked has two children.  The affect node
     * is the right child of the smallest node in the right subtree
     * the node being unlinked.
     */
    public void testDisappearingRightChildOnUnlink() {
        sparseTree.add(0, Boolean.TRUE);
        sparseTree.add(1, new Integer(11));
        sparseTree.add(0, null);
        sparseTree.add(0, new Integer(2));
        sparseTree.add(3, new Integer(8));
        sparseTree.add(1, new Integer(3));
        sparseTree.add(4, null);
        sparseTree.add(6, new Integer(10));
        sparseTree.add(6, new Integer(9));
        sparseTree.add(5, null);
        sparseTree.add(6, new Integer(7));
        sparseTree.add(0, new Integer(1));
        sparseTree.add(5, Boolean.TRUE);
        sparseTree.remove(4);
        sparseTree.remove(4);

        //assertEquals(11, sparseTree.size());
        assertEquals(new Integer(1), sparseTree.get(0));
        assertEquals(new Integer(2), sparseTree.get(1));
        assertEquals(new Integer(3), sparseTree.get(2));
        assertEquals(null, sparseTree.get(3));
        assertEquals(null, sparseTree.get(4));
        assertEquals(null, sparseTree.get(5));
        assertEquals(new Integer(7), sparseTree.get(6));
        assertEquals(new Integer(8), sparseTree.get(7));
        assertEquals(new Integer(9), sparseTree.get(8));
        assertEquals(new Integer(10), sparseTree.get(9));
        assertEquals(new Integer(11), sparseTree.get(10));
    }

    /**
     * Bug Validation
     */
    public void testForNewBug() {
        sparseTree.add(0, new Integer(0));
        sparseTree.add(0, null);
        sparseTree.set(1, new Integer(30));
        sparseTree.add(0, null);
        sparseTree.set(1, new Integer(31));
        sparseTree.add(0, new Integer(23));
        sparseTree.set(2, new Integer(5));
        sparseTree.set(2, null);
        sparseTree.remove(0);
        sparseTree.set(2, null);
        sparseTree.set(0, new Integer(6));
        sparseTree.remove(1);
        sparseTree.remove(1);
        sparseTree.set(0, new Integer(7));
        sparseTree.add(0, null);
        sparseTree.remove(0);
        sparseTree.remove(0);
        sparseTree.add(0, new Integer(19));
        sparseTree.add(0, new Integer(36));
        sparseTree.add(1, null);
        sparseTree.add(0, null);
        sparseTree.add(0, new Integer(28));
        sparseTree.set(3, new Integer(37));
        sparseTree.set(1, new Integer(32));
        sparseTree.add(2, new Integer(13));
        sparseTree.set(0, new Integer(14));
        sparseTree.remove(4);
        sparseTree.remove(3);
        sparseTree.add(3, null);
        sparseTree.remove(1);
        sparseTree.set(0, new Integer(15));
        sparseTree.remove(0);
        sparseTree.add(2, new Integer(16));
        sparseTree.remove(3);
        sparseTree.add(0, null);
        sparseTree.set(3, new Integer(17));
        sparseTree.set(1, new Integer(18));
        sparseTree.add(1, null);
        sparseTree.set(1, null);
        sparseTree.remove(2);
        sparseTree.set(2, new Integer(8));
        sparseTree.add(1, null);
        sparseTree.add(0, new Integer(20));
        sparseTree.add(5, null);
        sparseTree.add(0, null);
        sparseTree.set(2, null);
        sparseTree.set(2, null);
        sparseTree.add(1, new Integer(21));
        sparseTree.set(1, null);
        sparseTree.add(4, new Integer(22));
        sparseTree.set(3, new Integer(4));
        sparseTree.set(2, new Integer(24));
        sparseTree.set(9, null);
        sparseTree.add(4, null);
        sparseTree.add(10, null);
        sparseTree.set(5, new Integer(25));
        sparseTree.set(7, new Integer(26));
        sparseTree.add(4, new Integer(27));
        sparseTree.add(10, new Integer(10));
        sparseTree.set(8, new Integer(29));
        sparseTree.set(2, new Integer(2));
        sparseTree.set(0, null);
        sparseTree.set(4, null);
        sparseTree.add(0, null);
        sparseTree.add(4, new Integer(3));
        sparseTree.remove(1);
        sparseTree.set(14, null);
        sparseTree.add(12, new Integer(12));
        sparseTree.set(9, new Integer(33));
        sparseTree.add(11, new Integer(34));
        sparseTree.set(9, new Integer(35));
        sparseTree.set(11, new Integer(9));
        sparseTree.remove(9);
        sparseTree.remove(5);
        sparseTree.add(11, new Integer(11));
        sparseTree.remove(13);
        sparseTree.remove(5);
        sparseTree.add(13, null);
        sparseTree.set(5, null);
        sparseTree.set(6, null);
        sparseTree.add(2, new Integer(38));
        sparseTree.add(8, null);
        sparseTree.set(2, null);
        sparseTree.remove(2);
        sparseTree.set(8, null);

        assertEquals(16, sparseTree.size());
        assertEquals(null, sparseTree.get(0));
        assertEquals(null, sparseTree.get(1));
        assertEquals(new Integer(2), sparseTree.get(2));
        assertEquals(new Integer(3), sparseTree.get(3));
        assertEquals(new Integer(4), sparseTree.get(4));
        assertEquals(null, sparseTree.get(5));
        assertEquals(null, sparseTree.get(6));
        assertEquals(null, sparseTree.get(7));
        assertEquals(null, sparseTree.get(8));
        assertEquals(new Integer(9), sparseTree.get(9));
        assertEquals(new Integer(10), sparseTree.get(10));
        assertEquals(new Integer(11), sparseTree.get(11));
        assertEquals(new Integer(12), sparseTree.get(12));
        assertEquals(null, sparseTree.get(13));
        assertEquals(null, sparseTree.get(14));
        assertEquals(null, sparseTree.get(15));
    }


    /**
     * Tests to verify that the sparse list is consistent after a long
     * series of list operations.
     */
    public void testListOperations() {
        List controlList = new ArrayList();

        // apply various operations to both lists
        for(int i = 0; i < 5000; i++) {

            //System.out.print("\n" + i + ". ");
            int operation = random.nextInt(5);
            int index = controlList.isEmpty() ? 0 : random.nextInt(controlList.size());
            Object value = new Integer(random.nextInt());

            if(operation == 0 || controlList.isEmpty()) {
                //System.out.println("Adding " + value + " at " + index + ".");
                sparseTree.add(index, value);
                controlList.add(index, value);
            } else if(operation == 1) {
                //System.out.println("Adding null at " + index + ".");
                sparseTree.add(index, null);
                controlList.add(index, null);
            } else if(operation == 2) {
                //System.out.println("Deleting value at " + index + ".");
                sparseTree.remove(index);
                controlList.remove(index);
            } else if(operation == 3) {
                //System.out.println("Setting value at " + index + " to " + value + ".");
                sparseTree.set(index, value);
                controlList.set(index, value);
            } else if(operation == 4) {
                //System.out.println("Setting value at " + index + " to null.");
                sparseTree.set(index, null);
                controlList.set(index, null);
            }

            //assertEquals(controlList, sparseTree);
            //System.out.println("List validation successful.\n");

        }

        // verify the lists are equal
        assertEquals(controlList, sparseTree);
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
        System.out.println("Starting SparseList Performance Tests.  Please Wait...\n");

        Random mainRandom = new Random(11);
        long startTime = 0;
        long endTime = 0;
        int counter = 0;
        int nulls = 0;
        int[] indexes = new int[500000];
        Integer[] values = new Integer[500000];

        // Performance Test For Creation
        System.out.println("Running the performance test for initialization.");

        System.out.println("SparseList:");
        startTime = System.currentTimeMillis();
        SparseList testTree = new SparseList();
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


            System.out.println("SparseList:");
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

            System.out.println("SparseList:");
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

            System.out.println("SparseList:");
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

            System.out.println("SparseList:");
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
