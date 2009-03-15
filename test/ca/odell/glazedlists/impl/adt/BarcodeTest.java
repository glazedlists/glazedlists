/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt;

// for being a JUnit test case
import junit.framework.TestCase;

import java.util.*;

/**
 * This test verifies that the Barcode works as expected.
 *
 * @author <a href="mailto;kevin@swank.ca">Kevin Maltby</a>
 */
public class BarcodeTest extends TestCase {

    /** for randomly choosing list indices */
    private Random random = new Random(101);

    /** the Barcode to test on */
    private Barcode barcode = null;

    /**
     * Prepare for the test.
     */
    @Override
    public void setUp() {
        barcode = new Barcode();
    }

    /**
     * Clean up after the test.
     */
    @Override
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
     * Tests that the BarcodeIterator works correctly
     */
    public void testBarcodeIterator() {
        barcode.addBlack(0, 10);
        barcode.addWhite(10, 3);
        barcode.addWhite(7, 1);
        barcode.addWhite(5, 5);
        barcode.addWhite(3, 7);
        barcode.addWhite(1, 3);

        BarcodeIterator iterator = barcode.iterator();
        for(int i = 0; i < barcode.size(); i++) {
            assertEquals(true, iterator.hasNext());
            assertEquals(barcode.get(i), iterator.next());
            assertEquals(barcode.getBlackIndex(i), iterator.getBlackIndex());
            assertEquals(barcode.getWhiteIndex(i), iterator.getWhiteIndex());
            assertEquals(i, iterator.getIndex());
        }
        assertEquals(false, iterator.hasNext());
    }

    /**
     * Tests that a BarcodeIterator works correctly by only BLACK iteration
     * methods.
     */
    public void testBlackIterator() {
        barcode.addBlack(0, 10);
        barcode.addWhite(10, 3);
        barcode.addWhite(7, 1);
        barcode.addWhite(5, 5);
        barcode.addWhite(3, 7);
        barcode.addWhite(1, 3);

        BarcodeIterator iterator = barcode.iterator();
        for(int i = 0; i < barcode.blackSize(); i++) {
            assertEquals(true, iterator.hasNextBlack());
            assertEquals(Barcode.BLACK, iterator.nextBlack());
            assertEquals(i, iterator.getBlackIndex());
            assertEquals(-1, iterator.getWhiteIndex());
            assertEquals(barcode.getIndex(i, Barcode.BLACK), iterator.getIndex());
        }
        assertEquals(false, iterator.hasNextBlack());
    }

    /**
     * Tests that a BarcodeIterator works correctly by only WHITE iteration
     * methods
     */
    public void testWhiteIterator() {
        barcode.addBlack(0, 10);
        barcode.addWhite(10, 3);
        barcode.addWhite(7, 1);
        barcode.addWhite(5, 5);
        barcode.addWhite(3, 7);
        barcode.addWhite(1, 3);

        BarcodeIterator iterator = barcode.iterator();
        for(int i = 0; i < barcode.whiteSize(); i++) {
            assertEquals(true, iterator.hasNextWhite());
            assertEquals(Barcode.WHITE, iterator.nextWhite());
            assertEquals(i, iterator.getWhiteIndex());
            assertEquals(-1, iterator.getBlackIndex());
            assertEquals(barcode.getIndex(i, Barcode.WHITE), iterator.getIndex());
        }
        assertEquals(false, iterator.hasNextWhite());
    }

    /**
     * Tests all of the iterators on an empty barcode
     */
    public void testEmptyBarcodeIterators() {
        BarcodeIterator iterator = barcode.iterator();
        assertEquals(false, iterator.hasNext());
        assertEquals(false, iterator.hasNextWhite());
        assertEquals(false, iterator.hasNextBlack());
        assertEquals(-1, iterator.getIndex());
        assertEquals(-1, iterator.getBlackIndex());
        assertEquals(-1, iterator.getWhiteIndex());
    }

    /**
     * Tests all of the iterators on a completely WHITE barcode
     */
    public void testCompletelyWhiteBarcodeIterators() {
        barcode.addWhite(0, 10);

        BarcodeIterator whiteIterator = barcode.iterator();
        for(int i = 0; i < 10; i++) {
            assertEquals(true, whiteIterator.hasNextWhite());
            assertEquals(Barcode.WHITE, whiteIterator.nextWhite());
            assertEquals(i, whiteIterator.getIndex());
            assertEquals(-1, whiteIterator.getBlackIndex());
            assertEquals(i, whiteIterator.getWhiteIndex());
        }
        assertEquals(false, whiteIterator.hasNextWhite());

        BarcodeIterator blackIterator = barcode.iterator();
        assertEquals(false, blackIterator.hasNextBlack());
        assertEquals(-1, blackIterator.getIndex());
        assertEquals(-1, blackIterator.getBlackIndex());
        assertEquals(-1, blackIterator.getWhiteIndex());

        BarcodeIterator iterator = barcode.iterator();
        for(int i = 0; i < 10; i++) {
            assertEquals(true, iterator.hasNext());
            assertEquals(Barcode.WHITE, iterator.next());
            assertEquals(i, iterator.getIndex());
            assertEquals(-1, iterator.getBlackIndex());
            assertEquals(i, iterator.getWhiteIndex());
        }
        assertEquals(false, iterator.hasNext());
    }

    /**
     * Tests all of the iterators on a completely BLACK barcode
     */
    public void testCompletelyBlackBarcodeIterators() {
        barcode.addBlack(0, 10);

        BarcodeIterator blackIterator = barcode.iterator();
        for(int i = 0; i < 10; i++) {
            assertEquals(true, blackIterator.hasNextBlack());
            assertEquals(Barcode.BLACK, blackIterator.nextBlack());
            assertEquals(i, blackIterator.getIndex());
            assertEquals(-1, blackIterator.getWhiteIndex());
            assertEquals(i, blackIterator.getBlackIndex());
        }
        assertEquals(false, blackIterator.hasNextBlack());

        BarcodeIterator whiteIterator = barcode.iterator();
        assertEquals(false, whiteIterator.hasNextWhite());
        assertEquals(-1, whiteIterator.getIndex());
        assertEquals(-1, whiteIterator.getBlackIndex());
        assertEquals(-1, whiteIterator.getWhiteIndex());

        BarcodeIterator iterator = barcode.iterator();
        for(int i = 0; i < 10; i++) {
            assertEquals(true, iterator.hasNext());
            assertEquals(Barcode.BLACK, iterator.next());
            assertEquals(i, iterator.getIndex());
            assertEquals(-1, iterator.getWhiteIndex());
            assertEquals(i, iterator.getBlackIndex());
        }
        assertEquals(false, iterator.hasNext());
    }

    /**
     * Tests that all write ops fail appropriately on an empty Barcode
     */
    public void testEmptyBarcodeFailures() {
        BarcodeIterator fullIterator = barcode.iterator();
        try {
            fullIterator.remove();
            fail("Iterator failed to throw an exception for a write operation on an empty Barcode.");
        } catch(NoSuchElementException e) {
            // Test success
        }

        try {
            fullIterator.setBlack();
            fail("Iterator failed to throw an exception for a write operation on an empty Barcode.");
        } catch(NoSuchElementException e) {
            // Test success
        }

        try {
            fullIterator.setWhite();
            fail("Iterator failed to throw an exception for a write operation on an empty Barcode.");
        } catch(NoSuchElementException e) {
            // Test success
        }
    }

    /**
     * Tests that setting a BLACK value on a completely WHITE Barcode
     * behaves correctly.
     */
    public void testSetBlackOnCompletelyWhiteBarcodeFullIterator() {
        barcode.addWhite(0, 10);
        BarcodeIterator fullIterator = barcode.iterator();
        for(int i = 0; i < 5; i++) {
            assertEquals(true, fullIterator.hasNext());
            assertEquals(Barcode.WHITE, fullIterator.next());
        }
        assertEquals(0, fullIterator.setBlack());
        for(int i = 0; i < 5; i++) {
            assertEquals(true, fullIterator.hasNext());
            assertEquals(Barcode.WHITE, fullIterator.next());
        }
        assertEquals(false, fullIterator.hasNext());
        assertEquals(1, barcode.blackSize());
        assertEquals(4, barcode.getIndex(0, Barcode.BLACK));
    }

    /**
     * Tests that setting a WHITE value on a completely BLACK Barcode
     * behaves correctly.
     */
    public void testSetBlackOnCompletelyBlackBarcodeFullIterator() {
        barcode.addBlack(0, 10);
        BarcodeIterator fullIterator = barcode.iterator();
        for(int i = 0; i < 5; i++) {
            assertEquals(true, fullIterator.hasNext());
            assertEquals(Barcode.BLACK, fullIterator.next());
        }
        assertEquals(0, fullIterator.setWhite());
        for(int i = 0; i < 5; i++) {
            assertEquals(true, fullIterator.hasNext());
            assertEquals(Barcode.BLACK, fullIterator.next());
        }
        assertEquals(false, fullIterator.hasNext());
        assertEquals(1, barcode.whiteSize());
        assertEquals(4, barcode.getIndex(0, Barcode.WHITE));
    }

    /**
     * Tests that setting a single WHITE value on a completely BLACK Barcode
     * at the very end of the Barcode behaves correctly.
     */
    public void testSetWhiteAtEndOnCompletelyBlackBarcodeFullIterator() {
        barcode.addBlack(0, 10);
        BarcodeIterator fullIterator = barcode.iterator();
        for(int i = 0; i < 10; i++) {
            assertEquals(true, fullIterator.hasNext());
            assertEquals(Barcode.BLACK, fullIterator.next());
        }
        assertEquals(0, fullIterator.setWhite());
        assertEquals(false, fullIterator.hasNext());
        assertEquals(1, barcode.whiteSize());
        assertEquals(9, barcode.getIndex(0, Barcode.WHITE));
    }

    /**
     * Tests that setting a single BLACK value to WHITE on an otherwise
     * completely WHITE Barcode.
     */
    public void testSetOnlyBlackToWhiteFullIterator() {
        barcode.addWhite(0, 9);
        barcode.addBlack(4, 1);
        BarcodeIterator fullIterator = barcode.iterator();
        for(int i = 0; i < 4; i++) {
            assertEquals(true, fullIterator.hasNext());
            assertEquals(Barcode.WHITE, fullIterator.next());
        }
        assertEquals(Barcode.BLACK, fullIterator.next());
        assertEquals(4, fullIterator.setWhite());
        for(int i = 0; i < 5; i++) {
            assertEquals(true, fullIterator.hasNext());
            assertEquals(Barcode.WHITE, fullIterator.next());
        }
        assertEquals(false, fullIterator.hasNext());
        assertEquals(0, barcode.blackSize());
    }

    /**
     * Tests that setting the last BLACK element in a Barcode
     * containing several BLACK elements to WHITE works.
     */
    public void testSetLastBlackToWhiteFullIterator() {
        barcode.addWhite(0, 10);
        barcode.addBlack(1, 2);
        barcode.addBlack(5, 5);
        barcode.addBlack(12, 3);
        BarcodeIterator fullIterator = barcode.iterator();
        for(int i = 0; i < 14; i++) {
            assertEquals(true, fullIterator.hasNext());
            fullIterator.next();
        }

        assertEquals(Barcode.BLACK, fullIterator.next());
        assertEquals(5, fullIterator.setWhite());

        for(int i = 0; i < 5; i++) {
            assertEquals(true, fullIterator.hasNext());
            assertEquals(Barcode.WHITE, fullIterator.next());
        }
        assertEquals(false, fullIterator.hasNext());
        assertEquals(11, barcode.whiteSize());
        assertEquals(14, barcode.getIndex(5, Barcode.WHITE));
    }

    /**
     * Tests that setting the first WHITE element in the trailing
     * white space on the Barcode to BLACK works as expected.
     */
    public void testSettingLeadOfTrailingWhiteSpaceToBlackFullIterator() {
        barcode.addWhite(0, 5);
        barcode.addBlack(5, 4);
        barcode.addWhite(9, 6);
        BarcodeIterator fullIterator = barcode.iterator();
        for(int i = 0; i < 9; i++) {
            assertEquals(true, fullIterator.hasNext());
            fullIterator.next();
        }
        fullIterator.next();
        assertEquals(4, fullIterator.setBlack());
        for(int i = 0; i < 5; i++) {
            assertEquals(true, fullIterator.hasNext());
            assertEquals(Barcode.WHITE, fullIterator.next());
        }
        assertEquals(false, fullIterator.hasNext());
        assertEquals(5, barcode.blackSize());
        assertEquals(9, barcode.getIndex(4, Barcode.BLACK));
    }

    /**
     * Tests that setting the first WHITE element in an entirely
     * WHITE Barcode to BLACK works as expected.
     */
    public void testSetBlackOnFirstCompletelyWhiteFullIterator() {
        barcode.addWhite(0, 10);
        BarcodeIterator fullIterator = barcode.iterator();
        fullIterator.next();
        assertEquals(0, fullIterator.setBlack());
        for(int i = 0; i < 9; i++) {
            assertEquals(true, fullIterator.hasNext());
            assertEquals(Barcode.WHITE, fullIterator.next());
        }
        assertEquals(false, fullIterator.hasNext());
        assertEquals(1, barcode.blackSize());
        assertEquals(0, barcode.getIndex(0, Barcode.BLACK));
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
                validate(controlList, barcode);
            }
            if(msgCondition) System.out.println("List validation successful.");


        }

        // verify the lists are equal
        validate(controlList, barcode);
    }

    private void validate(List list, Barcode barcode) {
        Iterator barcodeIterator = barcode.iterator();
        for(Iterator listIterator = list.iterator(); listIterator.hasNext(); ) {
            assertEquals(true, barcodeIterator.hasNext());
            assertEquals(listIterator.next(), barcodeIterator.next());
        }
        assertEquals(false, barcodeIterator.hasNext());
    }

    /**
     * Performance tests this ADT.  Some of what I do in this might seem
     * somewhat odd, namely the pre-calculation of all data and indices
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

            // prime the indices
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

            // prime the indices
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

            // prime the indices
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