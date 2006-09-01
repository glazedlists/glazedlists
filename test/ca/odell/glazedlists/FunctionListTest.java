package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class FunctionListTest extends TestCase {

    public void testConstructor() {
        try {
            new FunctionList(new BasicEventList(), null);
            fail("failed to receive an IllegalArgumentException with null forward Function");
        } catch (IllegalArgumentException e) {}

        new FunctionList(new BasicEventList(), new IntegerToString());
        new FunctionList(new BasicEventList(), new IntegerToString(), null);

        BasicEventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());
        source.add(new Integer(0));
        source.add(new Integer(1));
        source.add(new Integer(2));

        assertEquals(3, intsToStrings.size());
        assertEquals("0", intsToStrings.get(0));
        assertEquals("1", intsToStrings.get(1));
        assertEquals("2", intsToStrings.get(2));

        // build a FunctionList with a source that is already populated
        intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());
        assertEquals(3, intsToStrings.size());
        assertEquals("0", intsToStrings.get(0));
        assertEquals("1", intsToStrings.get(1));
        assertEquals("2", intsToStrings.get(2));
    }

    public void testAdd() {
        BasicEventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());
        source.add(new Integer(0));
        source.add(new Integer(1));
        source.add(new Integer(2));

        assertEquals("0", intsToStrings.get(0));
        assertEquals("1", intsToStrings.get(1));
        assertEquals("2", intsToStrings.get(2));

        intsToStrings.add("3");
        intsToStrings.add("4");
        intsToStrings.add("5");

        assertEquals("3", intsToStrings.get(3));
        assertEquals("4", intsToStrings.get(4));
        assertEquals("5", intsToStrings.get(5));

        intsToStrings.add(0, "8");
        assertEquals("8", intsToStrings.get(0));

        source = new BasicEventList<Integer>();
        intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString());
        source.add(new Integer(0));

        assertEquals("0", intsToStrings.get(0));

        try {
            intsToStrings.add("3");
            fail("failed to receive an IllegalStateException for a call to add with no reverse function specified");
        } catch (IllegalStateException e) {}

        try {
            intsToStrings.add(2, "3");
            fail("failed to receive an IllegalStateException for a call to add with no reverse function specified");
        } catch (IllegalStateException e) {}
    }

    public void testAddAll() {
        BasicEventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());

        List<Integer> toAdd = new ArrayList<Integer>();
        toAdd.add(new Integer(0));
        toAdd.add(new Integer(1));
        toAdd.add(new Integer(2));

        source.addAll(toAdd);

        assertEquals("0", intsToStrings.get(0));
        assertEquals("1", intsToStrings.get(1));
        assertEquals("2", intsToStrings.get(2));
    }

    public void testSet() {
        BasicEventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());
        source.add(new Integer(0));
        source.add(new Integer(1));
        source.add(new Integer(2));

        intsToStrings.set(0, "8");
        assertEquals("8", intsToStrings.get(0));

        source = new BasicEventList<Integer>();
        intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString());
        source.add(new Integer(0));

        try {
            intsToStrings.set(0, "8");
            fail("failed to receive an IllegalStateException for a call to set with no reverse function specified");
        } catch (Exception e) {}

        source.set(0, new Integer(9));
        assertEquals("9", intsToStrings.get(0));
    }

    public void testRemove() {
        BasicEventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());
        source.add(new Integer(0));
        source.add(new Integer(1));
        source.add(new Integer(2));

        intsToStrings.remove(0);
        assertEquals("1", intsToStrings.get(0));
        assertEquals("2", intsToStrings.get(1));

        source.remove(0);
        assertEquals("2", intsToStrings.get(0));
    }

    public void testAdvancedFunction() {
        // establish a control for this test case with the normal Function
        BasicEventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new AdvancedIntegerToString(), new StringToInteger());
        source.add(new Integer(0));

        // ensure that reevaluate is called when we update elements IN PLACE
        assertEquals(0, ((AdvancedIntegerToString) intsToStrings.getForwardFunction()).getReevaluateCount());
        source.set(0, source.get(0));
        assertEquals(1, ((AdvancedIntegerToString) intsToStrings.getForwardFunction()).getReevaluateCount());

        // ensure that reevaluate is NOT called when we set brand new elements into the List
        assertEquals(1, ((AdvancedIntegerToString) intsToStrings.getForwardFunction()).getReevaluateCount());
        source.set(0, new Integer(0));
        assertEquals(2, ((AdvancedIntegerToString) intsToStrings.getForwardFunction()).getReevaluateCount());

        // ensure that dispose is called when we remove elements
        assertEquals(0, ((AdvancedIntegerToString) intsToStrings.getForwardFunction()).getDisposeCount());
        source.remove(0);
        assertEquals(1, ((AdvancedIntegerToString) intsToStrings.getForwardFunction()).getDisposeCount());
    }

    private static class StringToInteger implements FunctionList.Function<String,Integer> {
        public Integer evaluate(String value) {
            return new Integer(value);
        }
    }

    private static class IntegerToString implements FunctionList.Function<Integer,String> {
        public String evaluate(Integer value) {
            return value.toString();
        }
    }

    private static class AdvancedIntegerToString extends IntegerToString implements FunctionList.AdvancedFunction<Integer,String> {
        private int reevaluateCount = 0;
        private int disposeCount = 0;

        public String reevaluate(Integer value, String oldValue) {
            this.reevaluateCount++;
            return this.evaluate(value);
        }

        public void dispose(Integer sourceValue, String transformedValue) {
            this.disposeCount++;
        }

        public int getReevaluateCount() {
            return reevaluateCount;
        }

        public int getDisposeCount() {
            return this.disposeCount;
        }
    }
}