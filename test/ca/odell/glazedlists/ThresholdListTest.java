/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the ThresholdList works as expected.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class ThresholdListTest extends TestCase {

    /** the source list */
    private BasicEventList source = null;

    /** the threshold list to test with */
    private ThresholdList thresholdList = null;

    /** the enumerator to use to evaluate the objects in the list */
    private IntegerEvaluator evaluator = null;

    /** for randomly choosing list indicies */
    private Random random = new Random(2);

    /**
     * Prepare for the test.
     */
    public void setUp() {
        source = new BasicEventList();
        evaluator = new IntegerEvaluator();
        thresholdList = new ThresholdList(source, evaluator);
        // monitor the fired events
        thresholdList.addListEventListener(new ConsistencyTestList(thresholdList, "thresholdList", false));
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        thresholdList = null;
        source = null;
        random = null;
    }

    /**
     * Verifies that ThresholdList fires the right events.
     */
    public void testEventFiringLower() {
        // populate our sample list
        source.addAll(Arrays.asList(new Integer[] { new Integer(25), new Integer(50),
            new Integer(50), new Integer(75), new Integer(75), new Integer(100) }));

        // make some dealbreaking changes
        thresholdList.setLowerThreshold(27);
        thresholdList.setLowerThreshold(18);
    }

    /**
     * Verifies that ThresholdList fires the right events.
     */
    public void testEventFiringUpper() {
        // populate our sample list
        source.addAll(Arrays.asList(new Integer[] { new Integer(25), new Integer(50),
            new Integer(50), new Integer(75), new Integer(75), new Integer(100) }));

        // make some dealbreaking changes
        thresholdList.setUpperThreshold(77);
        thresholdList.setUpperThreshold(102);
    }

    /**
     * Verifies that ThresholdList fires the right events.
     */
    public void testEventFiringFromOneEntryToFullViaLower() {
        thresholdList.setUpperThreshold(100);
        thresholdList.setLowerThreshold(100);

        // populate our sample list
        source.addAll(Arrays.asList(new Integer[] { new Integer(25), new Integer(50),
            new Integer(50), new Integer(75), new Integer(75), new Integer(100) }));

        assertEquals(1, thresholdList.size());

        // make a dealbreaking change
        thresholdList.setLowerThreshold(25);
        assertEquals(6, thresholdList.size());

    }

    /**
     * Verifies that ThresholdList fires the right events.
     */
    public void testEventFiringFromOneEntryToFullViaUpper() {
        thresholdList.setUpperThreshold(25);
        thresholdList.setLowerThreshold(25);

        // populate our sample list
        source.addAll(Arrays.asList(new Integer[] { new Integer(25), new Integer(50),
            new Integer(50), new Integer(75), new Integer(75), new Integer(100) }));

        assertEquals(1, thresholdList.size());

        // make a dealbreaking change
        thresholdList.setUpperThreshold(100);
        assertEquals(6, thresholdList.size());

    }

    /**
     * A simple test to validate that ThresholdList is
     * behaving as expected on a list with no missing elements.
     */
    public void testListSimple() {
        // Use two seperate lists for testing upper and lower thresholds
        ThresholdList lowerThresholdList = thresholdList;
        ThresholdList upperThresholdList = new ThresholdList(source, evaluator);

        // Fill the source list with test data such that
        // every element is equal to its index
        for(int i = 0; i < 1000; i ++) {
            source.add(i, new Integer(i));
        }

        // Test setting the threshold to different values randomly
        for(int i = 0; i < 100; i ++) {
            int num = random.nextInt(1000);
            lowerThresholdList.setLowerThreshold(num);
            upperThresholdList.setUpperThreshold(num);
            assertEquals(source.size() - num, lowerThresholdList.size());
            assertEquals(num + 1, upperThresholdList.size());
            assertEquals(new Integer(num), lowerThresholdList.get(0));
            assertEquals(new Integer(num), upperThresholdList.get(upperThresholdList.size()-1));
        }
    }

    /**
     * A test to validate that ThresholdList is
     * behaving as expected on a list with missing elements.
     */
    public void testListWithMissingElements() {
        // Use two seperate lists for testing upper and lower thresholds
        ThresholdList lowerThresholdList = thresholdList;
        ThresholdList upperThresholdList = new ThresholdList(source, evaluator);

        // Fill the source list with test data such that
        // every fifth element is equal to every fourth element
        for(int i = 0; i < 1000; i ++) {
            if(i % 5 == 0) source.add(i, new Integer(i-1));
            else source.add(i, new Integer(i));
        }

        // Prime the thresholds so that they are non-zero
        lowerThresholdList.setLowerThreshold(2);
        upperThresholdList.setUpperThreshold(2);

        // Test setting the threshold to values which don't explicitly
        // exist in the source list
        for(int i = 0; i < 1000; i += 5) {
            int num = i;
            lowerThresholdList.setLowerThreshold(num);
            upperThresholdList.setUpperThreshold(num);
            assertEquals(source.size() - num - 1, lowerThresholdList.size());
            assertEquals(num + 1, upperThresholdList.size());
            assertEquals(new Integer(num + 1), lowerThresholdList.get(0));
            assertEquals(new Integer(num - 1), upperThresholdList.get(upperThresholdList.size()-1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values beyond the threshold are added for an increasing list.
     */
    public void testAddingValuesBeyondLowerThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // set the threshold to be the middle value
        thresholdList.setLowerThreshold(1);
        int originalSize = thresholdList.size();
        // add 5 more 0's which are beyond the threshold for this list
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
        }
        // validate size and all elements
        assertEquals(originalSize, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            int enumeration = evaluator.evaluate(thresholdList.get(i));
            String message = "The value " + enumeration + " is beyond the threshold of 1.";
            assertEquals(message, true, (enumeration >= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values beyond the threshold are added for a decreasing list.
     */
    public void testAddingValuesBeyondUpperThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // First focus on the increasing list
        // set the threshold to be the middle value
        thresholdList.setUpperThreshold(1);
        int originalSize = thresholdList.size();
        // add 5 more 2's which are beyond the threshold for this list
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(2));
        }
        // validate size and all elements
        assertEquals(originalSize, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            String message = "The element at " + i + " is beyond the threshold.";
            assertEquals(message, true, (evaluator.evaluate(thresholdList.get(i)) <= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values within the threshold are added for an increasing list.
     */
    public void testAddingValuesWithinLowerThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // set the threshold to be the middle value
        thresholdList.setLowerThreshold(1);
        int originalSize = thresholdList.size();
        // add 5 more 2's which are within the threshold for this list
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(2));
        }
        // validate size and all elements
        assertEquals(originalSize + 5, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            int enumeration = evaluator.evaluate(thresholdList.get(i));
            String message = "The value " + enumeration + " is beyond the threshold of 1.";
            assertEquals(message, true, (enumeration >= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values within the threshold are added for a decreasing list.
     */
    public void testAddingValuesWithinUpperThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // First focus on the increasing list
        // set the threshold to be the middle value
        thresholdList.setUpperThreshold(1);
        int originalSize = thresholdList.size();
        // add 5 more 0's which are within the threshold for this list
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
        }
        // validate size and all elements
        assertEquals(originalSize + 5, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            String message = "The element at " + i + " is beyond the threshold.";
            assertEquals(message, true, (evaluator.evaluate(thresholdList.get(i)) <= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values at the threshold are added for an increasing list.
     */
    public void testAddingValuesAtLowerThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // set the threshold to be the middle value
        thresholdList.setLowerThreshold(1);
        int originalSize = thresholdList.size();
        // add 5 more 1's which are at the threshold for this list
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(1));
        }
        // validate size and all elements
        assertEquals(originalSize + 5, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            int enumeration = evaluator.evaluate(thresholdList.get(i));
            String message = "The value " + enumeration + " is beyond the threshold of 1.";
            assertEquals(message, true, (enumeration >= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values at the threshold are added for a decreasing list.
     */
    public void testAddingValuesAtUpperThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // First focus on the increasing list
        // set the threshold to be the middle value
        thresholdList.setUpperThreshold(1);
        int originalSize = thresholdList.size();
        // add 5 more 1's which are at the threshold for this list
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(1));
        }
        // validate size and all elements
        assertEquals(originalSize + 5, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            String message = "The element at " + i + " is beyond the threshold.";
            assertEquals(message, true, (evaluator.evaluate(thresholdList.get(i)) <= 1));
        }
    }

    /**
     * Test an edge case where no value is at the current threshold
     * and then a value is added which is at the threshold.
     */
    public void testAddingAtLowerThresholdWithNoValuesAtThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(0));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(5));
            source.add(i, new Integer(10));
        }

        // set the threshold to be a value between the middle value and the lower value
        thresholdList.setLowerThreshold(3);
        int originalSize = thresholdList.size();

        // add a value within the threshold but not at it first
        source.add(0, new Integer(4));
        assertEquals(originalSize + 1, thresholdList.size());
        assertEquals(new Integer(4), thresholdList.get(0));

        // now add a value at the threshold
        originalSize = thresholdList.size();
        source.add(0, new Integer(3));
        assertEquals(originalSize + 1, thresholdList.size());
        assertEquals(new Integer(3), thresholdList.get(0));
    }

    /**
     * Test an edge case where no value is at the current threshold
     * and then a value is added which is at the threshold.
     */
    public void testAddingAtUpperThresholdWithNoValuesAtThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(5));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(10));
        }

        // set the threshold to be a value between the middle value and the upper value
        thresholdList.setUpperThreshold(7);
        int originalSize = thresholdList.size();

        // add a value within the threshold but not at it first
        source.add(0, new Integer(6));
        assertEquals(originalSize + 1, thresholdList.size());
        assertEquals(new Integer(6), thresholdList.get(thresholdList.size() - 1));

        // now add a value at the threshold
        originalSize = thresholdList.size();
        source.add(0, new Integer(7));
        assertEquals(originalSize + 1, thresholdList.size());
        assertEquals(new Integer(7), thresholdList.get(thresholdList.size() - 1));
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values beyond the threshold are removed for an increasing list.
     */
    public void testRemovingValuesBeyondLowerThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // set the threshold to be the middle value
        thresholdList.setLowerThreshold(1);
        int originalSize = thresholdList.size();
        // remove 4 0's which are beyond the threshold for this list
        for(int i = 0; i < 4; i ++) {
            source.add(i, new Integer(0));
        }
        // validate size and all elements
        assertEquals(originalSize, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            int enumeration = evaluator.evaluate(thresholdList.get(i));
            String message = "The value " + enumeration + " is beyond the threshold of 1.";
            assertEquals(message, true, (enumeration >= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values beyond the threshold are removed for a decreasing list.
     */
    public void testRemovingValuesBeyondUpperThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // First focus on the increasing list
        // set the threshold to be the middle value
        thresholdList.setUpperThreshold(1);
        int originalSize = thresholdList.size();
        // remove 4 2's which are beyond the threshold for this list
        for(int i = 0; i < 4; i ++) {
            source.remove(new Integer(2));
        }
        // validate size and all elements
        assertEquals(originalSize, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            String message = "The element at " + i + " is beyond the threshold.";
            assertEquals(message, true, (evaluator.evaluate(thresholdList.get(i)) <= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values within the threshold are removed for an increasing list.
     */
    public void testRemovingValuesWithinLowerThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // set the threshold to be the middle value
        thresholdList.setLowerThreshold(1);
        int originalSize = thresholdList.size();
        // remove 4 2's which are within the threshold for this list
        for(int i = 0; i < 4; i ++) {
           source.remove(new Integer(2));
        }
        // validate size and all elements
        assertEquals(originalSize - 4, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            int enumeration = evaluator.evaluate(thresholdList.get(i));
            String message = "The value " + enumeration + " is beyond the threshold of 1.";
            assertEquals(message, true, (enumeration >= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values within the threshold are removed for a decreasing list.
     */
    public void testRemovingValuesWithinUpperThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // First focus on the increasing list
        // set the threshold to be the middle value
        thresholdList.setUpperThreshold(1);
        int originalSize = thresholdList.size();
        // remove 4 0's which are within the threshold for this list
        for(int i = 0; i < 4; i ++) {
            source.remove(new Integer(0));
        }
        // validate size and all elements
        assertEquals(originalSize - 4, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            String message = "The element at " + i + " is beyond the threshold.";
            assertEquals(message, true, (evaluator.evaluate(thresholdList.get(i)) <= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values at the threshold are removed for an increasing list.
     */
    public void testRemovingValuesAtLowerThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // set the threshold to be the middle value
        thresholdList.setLowerThreshold(1);
        int originalSize = thresholdList.size();
        // remove a 1 which is at the threshold for this list
        source.remove(new Integer(1));

        // validate size and all elements
        assertEquals(originalSize - 1, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            int enumeration = evaluator.evaluate(thresholdList.get(i));
            String message = "The value " + enumeration + " is beyond the threshold of 1.";
            assertEquals(message, true, (enumeration >= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values at the threshold are removed for a decreasing list.
     */
    public void testRemovingValuesAtUpperThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(2));
        }

        // set the threshold to be the middle value
        thresholdList.setUpperThreshold(1);
        int originalSize = thresholdList.size();
        // remove a 1 which is at the threshold for this list
        source.remove(new Integer(1));

        // validate size and all elements
        assertEquals(originalSize - 1, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            String message = "The element at " + i + " is beyond the threshold.";
            assertEquals(message, true, (evaluator.evaluate(thresholdList.get(i)) <= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values beyond the threshold are updated for an increasing list.
     */
    public void testUpdatingValuesBeyondLowerThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(2));
            source.add(0, new Integer(0));
        }

        // set the threshold to be the middle value
        thresholdList.setLowerThreshold(1);
        int originalSize = thresholdList.size();

        // set the 5 0's to other values which are beyond the threshold for this list
        for(int i = 0; i < 5; i ++) {
            source.set(i, new Integer(-1 - i));
        }
        // validate size and all elements
        assertEquals(originalSize, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            int enumeration = evaluator.evaluate(thresholdList.get(i));
            String message = "The value " + enumeration + " is beyond the threshold of 1.";
            assertEquals(message, true, (enumeration >= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values beyond the threshold are updated for a decreasing list.
     */
    public void testUpdatingValuesBeyondUpperThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(0, new Integer(2));
        }

        // First focus on the increasing list
        // set the threshold to be the middle value
        thresholdList.setUpperThreshold(1);
        int originalSize = thresholdList.size();

        // set the 5 2's to other values which are beyond the threshold for this list
        for(int i = 0; i < 5; i ++) {
            source.set(i, new Integer(3));
        }
        // validate size and all elements
        assertEquals(originalSize, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            String message = "The element at " + i + " is beyond the threshold.";
            assertEquals(message, true, (evaluator.evaluate(thresholdList.get(i)) <= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values within the threshold are updated for an increasing list.
     */
    public void testUpdatingValuesWithinLowerThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(0, new Integer(2));
        }

        // set the threshold to be the middle value
        thresholdList.setLowerThreshold(1);
        int originalSize = thresholdList.size();

        // set the 5 2's to other values which are within the threshold for this list
        for(int i = 0; i < 5; i ++) {
            source.set(i, new Integer(i + 3));
        }
        // validate size and all elements
        assertEquals(originalSize, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            int enumeration = evaluator.evaluate(thresholdList.get(i));
            String message = "The value " + enumeration + " is beyond the threshold of 1.";
            assertEquals(message, true, (enumeration >= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values within the threshold are updated for a decreasing list.
     */
    public void testUpdatingValuesWithinUpperThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(1));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(2));
            source.add(0, new Integer(0));
        }

        // First focus on the increasing list
        // set the threshold to be the middle value
        thresholdList.setUpperThreshold(1);
        int originalSize = thresholdList.size();

        // set the 5 0's to other values which are within the threshold for this list
        for(int i = 0; i < 5; i ++) {
            source.set(i, new Integer(-1 - i));
        }
        // validate size and all elements
        assertEquals(originalSize, thresholdList.size());
        for(int i = 0; i < thresholdList.size(); i ++) {
            String message = "The element at " + i + " is beyond the threshold.";
            assertEquals(message, true, (evaluator.evaluate(thresholdList.get(i)) <= 1));
        }
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values at the threshold are updated for an increasing list.
     */
    public void testUpdatingValuesAtLowerThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(10));
        }
        source.add(0, new Integer(5));

        // set the threshold to be the middle value
        thresholdList.setLowerThreshold(5);
        int originalSize = thresholdList.size();

        // update the value to itself
        source.set(0, new Integer(5));
        assertEquals(originalSize, thresholdList.size());
        assertEquals(new Integer(5), thresholdList.get(0));

        // update the value to a value below the threshold
        source.set(0, new Integer(6));
        assertEquals(originalSize, thresholdList.size());
        assertEquals(new Integer(6), thresholdList.get(0));

        // update the value to a value beyond the threshold
        source.set(0, new Integer(5));
        source.set(0, new Integer(4));
        assertEquals(originalSize - 1, thresholdList.size());
        assertEquals(new Integer(10), thresholdList.get(0));

        // update the value back to itself
        source.set(0, new Integer(5));
        assertEquals(originalSize, thresholdList.size());
        assertEquals(new Integer(5), thresholdList.get(0));
    }

    /**
     * A test to validate that ThresholdList is behaving as expected
     * when values at the threshold are updated for a decreasing list.
     */
    public void testUpdatingValuesAtUpperThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(10));
        }
        source.add(0, new Integer(5));

        // First focus on the increasing list
        // set the threshold to be the middle value
        thresholdList.setUpperThreshold(5);
        int originalSize = thresholdList.size();

        // update the value to itself
        source.set(0, new Integer(5));
        assertEquals(originalSize, thresholdList.size());
        assertEquals(new Integer(5), thresholdList.get(thresholdList.size() - 1));

        // update the value to a value below the threshold
        source.set(0, new Integer(4));
        assertEquals(originalSize, thresholdList.size());
        assertEquals(new Integer(4), thresholdList.get(thresholdList.size() - 1));

        // update the value to a value beyond the threshold
        source.set(0, new Integer(5));
        source.set(0, new Integer(6));
        assertEquals(originalSize - 1, thresholdList.size());
        assertEquals(new Integer(0), thresholdList.get(thresholdList.size() - 1));

        // update the value back to itself
        source.set(0, new Integer(5));
        assertEquals(originalSize, thresholdList.size());
        assertEquals(new Integer(5), thresholdList.get(thresholdList.size() - 1));
    }

    /**
     * Test an edge case where no value is at the current threshold
     * and then a value is added which is at the threshold.
     */
    public void testUpdatingAtLowerThresholdWithNoValuesAtThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(5));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(10));
        }

        // set the threshold to be a value between the middle value and the upper value
        thresholdList.setLowerThreshold(3);
        int originalSize = thresholdList.size();

        // add a value beyond the threshold
        source.add(0, new Integer(2));
        int secondSize = thresholdList.size();
        assertEquals(originalSize, secondSize);

        // update the value to be within the threshold
        source.set(0, new Integer(4));
        assertEquals(originalSize + 1, thresholdList.size());
        assertEquals(new Integer(4), thresholdList.get(0));

        // now update the value to be beyond the threshold
        originalSize = thresholdList.size();
        source.set(0, new Integer(1));
        assertEquals(originalSize - 1, thresholdList.size());
        assertEquals(new Integer(5), thresholdList.get(0));

        // update the value to be at the threshold
        originalSize = thresholdList.size();
        source.set(0, new Integer(3));
        assertEquals(originalSize + 1, thresholdList.size());
        assertEquals(new Integer(3), thresholdList.get(0));
    }

    /**
     * Test an edge case where no value is at the current threshold
     * and then a value is added which is at the threshold.
     */
    public void testUpdatingAtUpperThresholdWithNoValuesAtThreshold() {
        // Fill the source list with test data such that
        // the list contains only 3 values
        source.add(0, new Integer(5));
        for(int i = 0; i < 5; i ++) {
            source.add(i, new Integer(0));
            source.add(i, new Integer(10));
        }

        // set the threshold to be a value between the middle value and the upper value
        thresholdList.setUpperThreshold(7);
        int originalSize = thresholdList.size();

        // add a value beyond the threshold
        source.add(0, new Integer(8));

        // update the value to be within the threshold
        source.set(0, new Integer(6));
        assertEquals(originalSize + 1, thresholdList.size());
        assertEquals(new Integer(6), thresholdList.get(thresholdList.size() - 1));

        // now update the value to be beyond the threshold
        originalSize = thresholdList.size();
        source.set(0, new Integer(9));
        assertEquals(originalSize - 1, thresholdList.size());
        assertEquals(new Integer(5), thresholdList.get(thresholdList.size() - 1));

        // update the value to be at the threshold
        originalSize = thresholdList.size();
        source.set(0, new Integer(7));
        assertEquals(originalSize + 1, thresholdList.size());
        assertEquals(new Integer(7), thresholdList.get(thresholdList.size() - 1));
    }

    /**
     * A test to validate the size of an empty size and that the
     * size is still zero once a list is filled and emptied.
     */
    public void testSizeZeroing() {
        // Use two seperate lists for testing upper and lower thresholds
        ThresholdList lowerThresholdList = thresholdList;
        ThresholdList upperThresholdList = new ThresholdList(source, evaluator);

        assertEquals(0, lowerThresholdList.size());
        assertEquals(0, upperThresholdList.size());

        // Fill the source list with test data
        for(int i = 0; i < 10; i ++) {
            source.add(i, new Integer(i));
        }

        // Set the thresholds so that the lists should appear empty
        lowerThresholdList.setLowerThreshold(11);
        upperThresholdList.setUpperThreshold(-1);

        assertEquals(0, lowerThresholdList.size());
        assertEquals(0, upperThresholdList.size());

        // Remove the test data
        for(int i = 0; i < 10; i ++) {
            source.remove(0);
        }

        assertEquals(0, lowerThresholdList.size());
        assertEquals(0, upperThresholdList.size());
    }

    /**
     * A test to validate events from calls to the threshold setting methods
     */
    public void testThresholdSettingEvents() {
        // Use two seperate lists for testing upper and lower thresholds
        ThresholdList lowerThresholdList = thresholdList;
        ThresholdList upperThresholdList = new ThresholdList(source, evaluator);

        // Fill the source list with test data such that
        // every element is equal to its index
        for(int i = 0; i < 1000; i ++) {
            source.add(i, new Integer(i));
        }

        // Wrap the lists in SortedLists whose internal data representation will break if the events are in error
        SortedList sortedIncreasing = new SortedList(lowerThresholdList, new ThresholdComparator(new IntegerEvaluator()));
        SortedList sortedDecreasing = new SortedList(upperThresholdList, new ThresholdComparator(new IntegerEvaluator()));

        // validate that the lists are equal to start with
        validateListsEquals(lowerThresholdList, sortedIncreasing);
        validateListsEquals(upperThresholdList, sortedDecreasing);

        // now randomly set the threshold and validate the lists
        for(int i = 0; i < 100; i ++) {
            int num = random.nextInt(1000);
            lowerThresholdList.setLowerThreshold(num);
            upperThresholdList.setUpperThreshold(num);
            validateListsEquals(lowerThresholdList, sortedIncreasing);
            validateListsEquals(upperThresholdList, sortedDecreasing);
        }
    }

    /**
     * Test the edge conditions where a source list contains values and the
     * thresholds are set such that the list is empty.
     */
    public void testZeroSizeViewOfArbitraryList() {
        // Focus on the lower case 0/-1 case first
        thresholdList.setLowerThreshold(-5);
        thresholdList.setUpperThreshold(-3);

        // Populate the source list with values that lie beyond the thresholds
        for(int i = 0; i < 1000; i ++) {
            source.add(i, new Integer(-2 + random.nextInt(1004)));
        }

        // Make sure that everything DOES lie beyond the thresholds
        assertEquals(0, thresholdList.size());
    }

    /**
     * Tests to see if pre-built source lists are initialized correctly on a
     * freshly constructed ThresholdList without setting any thresholds.
     */
    public void testInitialization() {
        // Prime a sorted source list with data
        SortedList sortedBase = new SortedList(source, new ThresholdComparator(new IntegerEvaluator()));
        for(int i = 0; i < 500; i++) {
            sortedBase.add(i, new Integer(i));
        }

        // Wrap that preconstructed list with a ThresholdList but do not change the default thresholds
        thresholdList = new ThresholdList(sortedBase, new IntegerEvaluator());

        // Wrap the threshold list in another sorted list and see if it is all the same
        SortedList sortedCover = new SortedList(thresholdList, new ThresholdComparator(new IntegerEvaluator()));
        validateListsEquals(sortedBase, sortedCover);
    }

    /**
     * Tests to see if events are handled correctly by a freshly
     * constructed ThresholdList without setting any thresholds.
     */
    public void testEventsOnNewThresholdList() {
        // Layer a new threshold list between two sorted lists and do not change defaults
        SortedList sortedBase = new SortedList(source, new ThresholdComparator(new IntegerEvaluator()));
        thresholdList = new ThresholdList(sortedBase, new IntegerEvaluator());
        SortedList sortedCover = new SortedList(thresholdList, new ThresholdComparator(new IntegerEvaluator()));

        // Now add values before a threshold is set and validate sortedBase equals sortedCover
        for(int i = 0; i < 500; i++) {
            source.add(i, new Integer(i));
        }
        validateListsEquals(sortedBase, sortedCover);
    }

    /** 
     * Verifies that the ThresholdList fires consistent events.
     */
    public void testEventFiringIsConsistent() {
        thresholdList.setLowerThreshold(-14931);
        thresholdList.setUpperThreshold(-1931);
        thresholdList.setLowerThreshold(-13931);
        thresholdList.setUpperThreshold(-931);
        source.add(new Integer(1401));
        thresholdList.setLowerThreshold(-12926);
        thresholdList.setUpperThreshold(74);
        source.add(new Integer(2168));
        source.add(new Integer(2996));
        thresholdList.setLowerThreshold(-11930);
        thresholdList.setUpperThreshold(1070);
        source.add(new Integer(3895));
        thresholdList.setLowerThreshold(-10931);
        thresholdList.setUpperThreshold(2069);
        source.add(new Integer(4235));
        source.add(new Integer(4507));
        thresholdList.setLowerThreshold(-9931);
        thresholdList.setUpperThreshold(3069);
    }

    /** 
     * Verifies that the ThresholdList fires consistent events.
     */
    public void testNumberOfEventsFired() {
        // count events
        ListEventCounter counter = new ListEventCounter();
        thresholdList.addListEventListener(counter);
        
        // putz around with the thresholds on an empty list
        thresholdList.setLowerThreshold(-14922);
        thresholdList.setUpperThreshold(-1922);
        thresholdList.setLowerThreshold(-13923);
        thresholdList.setUpperThreshold(-923);
        assertEquals(0, counter.getEventCount());
        
        // add an element that is not in range
        source.add(new Integer(1139));
        assertEquals(0, counter.getEventCount());
        thresholdList.setLowerThreshold(-12923);
        thresholdList.setUpperThreshold(77);
        thresholdList.setLowerThreshold(-11922);
        thresholdList.setUpperThreshold(1078);
        thresholdList.setLowerThreshold(-10923);
        
        // adjust the range to include our element
        thresholdList.setUpperThreshold(2077);
        assertEquals(source, thresholdList);
        assertEquals(1, counter.getEventCount());
        assertEquals(1, counter.getChangeCount(0));

        // keep the element in range for some changes
        thresholdList.setLowerThreshold(-9923);
        thresholdList.setUpperThreshold(3077);
        thresholdList.setLowerThreshold(-8923);
        thresholdList.setUpperThreshold(4077);
        thresholdList.setLowerThreshold(-7921);
        thresholdList.setUpperThreshold(5079);
        thresholdList.setLowerThreshold(-6923);
        thresholdList.setUpperThreshold(6077);
        thresholdList.setLowerThreshold(-5923);
        thresholdList.setUpperThreshold(7077);
        thresholdList.setLowerThreshold(-4922);
        thresholdList.setUpperThreshold(8078);
        thresholdList.setLowerThreshold(-3923);
        thresholdList.setUpperThreshold(9077);
        thresholdList.setLowerThreshold(-2923);
        thresholdList.setUpperThreshold(10077);
        thresholdList.setLowerThreshold(-1922);
        thresholdList.setUpperThreshold(11078);
        thresholdList.setLowerThreshold(-923);
        thresholdList.setUpperThreshold(12077);
        thresholdList.setLowerThreshold(77);
        thresholdList.setUpperThreshold(13077);
        thresholdList.setLowerThreshold(1077);
        thresholdList.setUpperThreshold(14077);
        
        // move past our element
        thresholdList.setLowerThreshold(2078);
        assertEquals(2, counter.getEventCount());
        assertEquals(1, counter.getChangeCount(1));
        assertEquals(Collections.EMPTY_LIST, thresholdList);
    }

    /** 
     * Tests a sliding windows works. This is the case when the upper and lower
     * threshold keep shifting.
     */
    public void testSlidingWindow() {
        // count events
        ListEventCounter counter = new ListEventCounter();
        thresholdList.addListEventListener(counter);
        
        thresholdList.setLowerThreshold(-14938);
        thresholdList.setUpperThreshold(-1938);
        source.add(new Integer(995));
        thresholdList.setLowerThreshold(-13937);
        thresholdList.setUpperThreshold(-937);
        source.add(new Integer(1155));
        thresholdList.setLowerThreshold(4062);
        thresholdList.setUpperThreshold(17062);
        source.add(new Integer(19970));
        thresholdList.setLowerThreshold(5063);
        thresholdList.setUpperThreshold(18063);
        source.add(new Integer(20279));
        thresholdList.setLowerThreshold(7063);
        thresholdList.setUpperThreshold(20063);
    }

    
    /**
     * Tests that the JavaBean constructor and supporting code
     * works as expected.
     */
    public void testJavaBeanConstructor() {
        thresholdList.dispose();
        thresholdList = null;
        thresholdList = new ThresholdList(source, "value");

        for(int i = 0;i < 1000;i++) {
            source.add(new SimpleJavaBeanObject(i));
        }

        thresholdList.setLowerThreshold(500);

        assertEquals(500, thresholdList.size());
    }

    /**
     * A helper method to compare two lists for equality
     */
    private void validateListsEquals(EventList alpha, EventList beta) {
        // fast fail if the sizes are different
        assertEquals(alpha.size(), beta.size());

        for(int i = 0; i < alpha.size(); i ++) {
            assertEquals(alpha.get(i), beta.get(i));
        }
    }

    /**
     * A JavaBean to test out the Reflection constructor
     */
    public class SimpleJavaBeanObject {

        private int value = 0;

        public SimpleJavaBeanObject(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    private class IntegerEvaluator implements ThresholdEvaluator {
        /**
         * Returns an integer value which represents the object.
         */
        public int evaluate(Object object) {
            if(object == null) {
                return Integer.MIN_VALUE;
            } else if(!(object instanceof Integer)) {
                throw new IllegalArgumentException("The value passed to this enumerator is of an invalid type."
                    + " All elements in the list must be of type Integer.");
            } else {
                Integer integer = (Integer)object;
                return integer.intValue();
            }

        }
    }

    /**
     * A ThresholdComparator is a simple helper class that wraps
     * a <code>ThresholdEvaluator</code> with a <code>Comparator</code> to
     * be used for sorting of the <code>ThresholdList</code>.
     *
     * <p>This class is duplicated within this testcase for the
     * testThresholdSettingEvents test.  The code duplication exists only
     * for convienience and for the sake of that one test.
     */
    private final class ThresholdComparator implements Comparator {

        /** the underlying evaluator **/
        private ThresholdEvaluator evaluator = null;

        /**
         * Creates a new ThresholdComparator
         */
        ThresholdComparator(ThresholdEvaluator evaluator) {
            this.evaluator = evaluator;
        }

        /**
         * Compares two <code>Object</code>s, and compares them using the result
         * given when each <code>Object</code> is evaluated using the underlying
         * <code>ThresholdEvaluator</code>.
         */
        public int compare(Object alpha, Object beta) {
            int alphaValue = 0;
            if(alpha instanceof Integer) alphaValue = ((Integer)alpha).intValue();
            else alphaValue = evaluator.evaluate(alpha);

            int betaValue = 0;
            if(beta instanceof Integer) betaValue = ((Integer)beta).intValue();
            else betaValue = evaluator.evaluate(beta);

            if(alphaValue > betaValue) return 1;
            else if(alphaValue < betaValue) return -1;
            else return 0;
        }

        /**
         * Returns true iff the object passed is a <code>ThresholdComparator</code> with
         * the same underlying <code>ThresholdEvaluator</code>.
         */
        public boolean equals(Object object) {
            if(object == null || !(object instanceof ThresholdComparator)) {
            return false;
            }
            ThresholdComparator other = (ThresholdComparator)object;
            return this.evaluator == other.evaluator;
        }
    }
}
