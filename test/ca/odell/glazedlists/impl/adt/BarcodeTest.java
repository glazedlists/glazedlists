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
 * This test verifies that the Barcode works as expected.
 *
 * @author <a href="mailto;kevin@swank.ca">Kevin Maltby</a>
 */
public class BarcodeTest extends TestCase {

    /** for randomly choosing list indicies */
    private Random random = new Random(101);

    /** the Barcode to test on */
    private Barcode barcode = null;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        barcode = new Barcode();
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        barcode.clear();
        barcode = null;
    }

    /**
     * Tests that adding works for values
     */
    public void testSimpleAddValue() {
        barcode.add(0, Barcode.BLACK, 1);
        assertEquals(1, barcode.size());
        assertEquals(Barcode.BLACK, barcode.get(0));
    }

    /**
     * Tests that adding works for Barcode.WHITEs at the end of the tree
     */
    public void testAddTrailingWhitespaceOnEmptyTree() {
        barcode.addWhite(0, 1);
        assertEquals(1, barcode.size());
        assertEquals(Barcode.WHITE, barcode.get(0));
    }

    /**
     * Tests that adding works for Barcode.WHITEs at the end of the tree
     */
    public void testAddTrailingWhitespaceNonEmptyTree() {
        barcode.addBlack(0, 1);
        barcode.addWhite(1, 1);
        assertEquals(2, barcode.size());
        assertEquals(Barcode.BLACK, barcode.get(0));
        assertEquals(Barcode.WHITE, barcode.get(1));
    }

    /**
     * Tests that adding works for Barcode.WHITEs at the start of the tree
     */
    public void testAddLeadingWhitespace() {
        barcode.addBlack(0, 1);
        barcode.addWhite(0, 1);
        assertEquals(2, barcode.size());
        assertEquals(Barcode.WHITE, barcode.get(0));
        assertEquals(Barcode.BLACK, barcode.get(1));
    }

    /**
     * Tests getCompressedIndex()
     */
    public void testGetCompressedIndex() {
        barcode.addBlack(0, 10);
        barcode.addWhite(1, 9);
        barcode.addWhite(11, 9);
        barcode.addWhite(21, 9);
        barcode.addWhite(31, 9);
        barcode.addWhite(41, 9);
        barcode.addWhite(51, 9);
        barcode.addWhite(61, 9);
        barcode.addWhite(71, 9);
        barcode.addWhite(81, 9);
        barcode.addWhite(91, 9);

        assertEquals(10, barcode.blackSize());

        for(int i = 0; i < 10; i++) {
            assertEquals(i, barcode.getBlackIndex(10*i));
        }
    }

    /**
     * Tests that bug 121 is gone.  This bug occurred in the following case:
     * Significant trailing whitespace.
     * Root was null.
     * Set a WHITE in the Barcode to BLACK
     *
     * This resulted in the Barcode increasing in size.
     */
    public void testBug121() {
        barcode.addWhite(0, 100);
        barcode.setBlack(50,1);
        assertEquals(100, barcode.size());
        assertEquals(1, barcode.blackSize());
        assertEquals(99, barcode.whiteSize());
    }

    /**
     * Tests that WHITE based sequence indexing is working correctly.
     */
    public void testWhiteSequenceIndex() {
        barcode.addWhite(0, 1000);
        int filler = 1000;

        // randomly add blocks of black values
        while(filler > 0) {
            int whereToAdd = random.nextInt(barcode.size());
            int amountToAdd = random.nextInt(filler + 1);

            barcode.addBlack(whereToAdd, amountToAdd);
            filler -= amountToAdd;
        }

        // since accesses don't alter state just look things up in order
        for(int i = 0;i < barcode.whiteSize();i++) {
            int sequenceIndex = barcode.getWhiteSequenceIndex(i);
            int actualWhiteIndex = barcode.getIndex(i, Barcode.WHITE);
            int previousBlack = barcode.getBlackIndex(actualWhiteIndex, true);
            int actualBlackIndex = -1;
            if(previousBlack != -1) actualBlackIndex = barcode.getIndex(previousBlack, Barcode.BLACK);

            assertEquals(actualWhiteIndex - (actualBlackIndex + 1), sequenceIndex);
        }
    }

    /**
     * Tests that getBlackBeforeWhite() is working correctly.
     */
    public void testGetBlackBeforeWhite() {
        barcode.addWhite(0, 1000);
        int filler = 1000;

        // randomly add blocks of black values
        while(filler > 0) {
            int whereToAdd = random.nextInt(barcode.size());
            int amountToAdd = random.nextInt(filler + 1);

            barcode.addBlack(whereToAdd, amountToAdd);
            filler -= amountToAdd;
        }

        // since accesses don't alter state just look things up in order
        for(int i = 0;i < barcode.whiteSize();i++) {
            int actualWhiteIndex = barcode.getIndex(i, Barcode.WHITE);
            int blackBeforeIndex = barcode.getBlackBeforeWhite(i);
            int actualBlackIndex = barcode.getBlackIndex(actualWhiteIndex, true);

            assertEquals(actualBlackIndex, blackBeforeIndex);
        }
    }

    /**
     * Test that findSequenceOfMinimumSize() is working correctly for the
     * FIRST FIT implementation.
     */
    public void testFindSequenceOfMinimumSize() {
        barcode.addBlack(0, 10);
        barcode.addWhite(10, 3);
        barcode.addWhite(7, 3);
        barcode.addWhite(5, 5);
        barcode.addWhite(3, 7);
        barcode.addWhite(1, 3);

        assertEquals(0, barcode.findSequenceOfMinimumSize(1, Barcode.BLACK));
        assertEquals(4, barcode.findSequenceOfMinimumSize(2, Barcode.BLACK));
        assertEquals(-1, barcode.findSequenceOfMinimumSize(4, Barcode.BLACK));

        assertEquals(1, barcode.findSequenceOfMinimumSize(1, Barcode.WHITE));
        assertEquals(1, barcode.findSequenceOfMinimumSize(3, Barcode.WHITE));
        assertEquals(6, barcode.findSequenceOfMinimumSize(5, Barcode.WHITE));
        assertEquals(6, barcode.findSequenceOfMinimumSize(7, Barcode.WHITE));
        assertEquals(-1, barcode.findSequenceOfMinimumSize(8, Barcode.WHITE));
    }

    /**
     * Tests to verify that the sparse list is consistent after a long
     * series of list operations.
     */
    public void testListOperations() {
        List controlList = new ArrayList();
        int length = 1;

        // apply various operations to both lists
        for(int i = 0; i < 5000; i++) {

            int msgValue = -1;
            int testValue = -1;
            int validationValue = -1;
            boolean msgCondition = i == msgValue;
            boolean testCondition = i == testValue;
            boolean validationCondition = i == validationValue;

            if(testCondition) {
                System.out.println("\nBefore:");
                barcode.printDebug();
                System.out.println("\n" + barcode + "\n");
            }

            if(msgCondition) System.out.print("\n" + i + ". ");
            int operation = random.nextInt(5);
            int index = controlList.isEmpty() ? 0 : random.nextInt(controlList.size());

            if(operation == 0 || controlList.isEmpty()) {
                if(msgCondition) System.out.println("Adding Barcode.BLACK at " + index + ".");
                barcode.add(index, Barcode.BLACK, length);
                controlList.add(index, Barcode.BLACK);
            } else if(operation == 1) {
                if(msgCondition) System.out.println("Adding Barcode.WHITE at " + index + ".");
                barcode.add(index, Barcode.WHITE, length);
                controlList.add(index, Barcode.WHITE);
            } else if(operation == 2) {
                if(msgCondition) System.out.println("Deleting value at " + index + ".");
                barcode.remove(index, length);
                controlList.remove(index);
            } else if(operation == 3) {
                if(msgCondition) System.out.println("Setting value at " + index + " to Barcode.BLACK");
                barcode.set(index, Barcode.BLACK, length);
                controlList.set(index, Barcode.BLACK);
            } else if(operation == 4) {
                if(msgCondition) System.out.println("Setting value at " + index + " to Barcode.WHITE.");
                barcode.set(index, Barcode.WHITE, length);
                controlList.set(index, Barcode.WHITE);
            }

            if(testCondition) {
                System.out.println("\nAfter:");
                barcode.printDebug();
                System.out.println("\n" + barcode + "\n");
            }

            if(validationCondition) {
                try {
                    barcode.validate();
                } catch(Exception e) {
                    System.out.println("Validation failure on step " + i);
                    fail(e.getMessage());
                }
                assertEquals(controlList, barcode);
            }
            if(msgCondition) System.out.println("List validation successful.");


        }

        // verify the lists are equal
        assertEquals(controlList, barcode);
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
        System.out.println("Starting Barcode Performance Tests.  Please Wait...\n");

        Random mainRandom = new Random(11);
        long startTime = 0;
        long endTime = 0;
        int counter = 0;
        int nulls = 0;
        int[] indexes = new int[500000];
        Object[] values = new Object[500000];

        // Performance Test For Creation
        System.out.println("Running the performance test for initialization.");

        System.out.println("Barcode:");
        startTime = System.currentTimeMillis();
        Barcode testBarcode = new Barcode();
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
                    values[i] = Barcode.WHITE;
                    nulls++;

                } else values[i] = Barcode.BLACK;
            }

            try{
                Thread.sleep(2000);
            } catch(InterruptedException e) {
                System.out.println("Failed to pause before tests.");
            }

            System.out.println("Running the performance test for adding 500000 elements.");
            System.out.println("Black Elements: " + (500000 - nulls));
            System.out.println("White Elements: " + nulls + "\n");


            System.out.println("Barcode:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testBarcode.add(indexes[counter], values[counter], 1);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");


            // Performance Test For GET

            System.out.println("Running the performance test for looking up all of the 500000 elements.");
            System.out.println("Black Elements: " + (500000 - nulls));
            System.out.println("White Elements: " + nulls);

            System.out.println("Barcode:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testBarcode.get(indexes[counter]);
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
                    values[i] = Barcode.WHITE;
                    nulls++;

                } else values[i] = Barcode.BLACK;
            }

            System.out.println("Running the performance test for setting 500000 elements.");
            System.out.println("Black Elements: " + (500000 - nulls));
            System.out.println("White Elements: " + nulls);

            System.out.println("Barcode:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testBarcode.set(indexes[counter], values[counter], 1);
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

            System.out.println("Barcode:");
            counter = 0;
            startTime = System.currentTimeMillis();
            for(;counter < 500000;counter++) {
                testBarcode.remove(indexes[counter], 1);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Test completed in " + (endTime - startTime) + " milliseconds.\n");
        }
    }
}
