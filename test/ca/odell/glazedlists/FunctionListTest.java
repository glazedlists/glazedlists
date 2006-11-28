package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class FunctionListTest extends TestCase {

    private static final Integer ZERO = new Integer(0);
    private static final Integer ONE = new Integer(1);
    private static final Integer TWO = new Integer(2);
    private static final Integer NINE = new Integer(9);

    public void testConstructor() {
        try {
            new FunctionList<Integer,String>(new BasicEventList<Integer>(), null);
            fail("failed to receive an IllegalArgumentException with null forward Function");
        } catch (IllegalArgumentException e) {}

        new FunctionList<Integer,String>(new BasicEventList<Integer>(), new IntegerToString());
        new FunctionList<Integer,String>(new BasicEventList<Integer>(), new IntegerToString(), null);

        BasicEventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);

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
        EventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);

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
        source.add(ZERO);

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
        EventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        List<Integer> toAdd = new ArrayList<Integer>();
        toAdd.add(ZERO);
        toAdd.add(ONE);
        toAdd.add(TWO);

        source.addAll(toAdd);

        assertEquals("0", intsToStrings.get(0));
        assertEquals("1", intsToStrings.get(1));
        assertEquals("2", intsToStrings.get(2));
    }

    public void testSet() {
        EventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);

        intsToStrings.set(0, "8");
        assertEquals("8", intsToStrings.get(0));

        source = new BasicEventList<Integer>();
        intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString());
        source.add(ZERO);

        try {
            intsToStrings.set(0, "8");
            fail("failed to receive an IllegalStateException for a call to set with no reverse function specified");
        } catch (Exception e) {}

        source.set(0, NINE);
        assertEquals("9", intsToStrings.get(0));
    }

    public void testRemove() {
        EventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new IntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);

        intsToStrings.remove(0);
        assertEquals("1", intsToStrings.get(0));
        assertEquals("2", intsToStrings.get(1));

        source.remove(0);
        assertEquals("2", intsToStrings.get(0));
    }

    public void testAdvancedFunction() {
        // establish a control for this test case with the normal Function
        EventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new AdvancedIntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        source.add(ZERO);

        // ensure that reevaluate is called when we update elements IN PLACE
        assertEquals(0, ((AdvancedIntegerToString) intsToStrings.getForwardFunction()).getReevaluateCount());
        source.set(0, source.get(0));
        assertEquals(1, ((AdvancedIntegerToString) intsToStrings.getForwardFunction()).getReevaluateCount());

        // ensure that reevaluate is NOT called when we set brand new elements into the List
        assertEquals(1, ((AdvancedIntegerToString) intsToStrings.getForwardFunction()).getReevaluateCount());
        source.set(0, ZERO);
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

    public void testReorder() {
        // establish a control for this test case with the normal Function
        SortedList<Integer> source = new SortedList<Integer>(new BasicEventList<Integer>(), null);
        FunctionList<Integer, String> intsToStrings = new FunctionList<Integer, String>(source, new AdvancedIntegerToString(), new StringToInteger());
        ListConsistencyListener consistencyListener = ListConsistencyListener.install(intsToStrings);

        source.add(ONE);
        source.add(ZERO);
        assertEquals(2, consistencyListener.getEventCount());

        source.setComparator(GlazedLists.comparableComparator());

        assertEquals(3, consistencyListener.getEventCount());
    }
}