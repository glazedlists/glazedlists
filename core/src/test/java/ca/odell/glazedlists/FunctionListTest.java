package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class FunctionListTest {

    private static final Integer ZERO = new Integer(0);
    private static final Integer ONE = new Integer(1);
    private static final Integer TWO = new Integer(2);
    private static final Integer NINE = new Integer(9);

    @Test
    public void testConstructor() {
        try {
            new FunctionList<Integer,String>(new BasicEventList<Integer>(), null);
            fail("failed to receive an IllegalArgumentException with null forward Function");
        } catch (IllegalArgumentException e) {}

        new FunctionList<>(new BasicEventList<Integer>(), new IntegerToString());
        new FunctionList<>(new BasicEventList<Integer>(), new IntegerToString(), null);

        BasicEventList<Integer> source = new BasicEventList<>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<>(source, new IntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);

        assertEquals(3, intsToStrings.size());
        assertEquals("0", intsToStrings.get(0));
        assertEquals("1", intsToStrings.get(1));
        assertEquals("2", intsToStrings.get(2));

        // build a FunctionList with a source that is already populated
        intsToStrings = new FunctionList<>(source, new IntegerToString(), new StringToInteger());
        assertEquals(3, intsToStrings.size());
        assertEquals("0", intsToStrings.get(0));
        assertEquals("1", intsToStrings.get(1));
        assertEquals("2", intsToStrings.get(2));
    }

    @Test
    public void testAdd() {
        EventList<Integer> source = new BasicEventList<>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<>(source, new IntegerToString(), new StringToInteger());
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

        source = new BasicEventList<>();
        intsToStrings = new FunctionList<>(source, new IntegerToString());
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

    @Test
    public void testAddAll() {
        EventList<Integer> source = new BasicEventList<>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<>(source, new IntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        List<Integer> toAdd = new ArrayList<>();
        toAdd.add(ZERO);
        toAdd.add(ONE);
        toAdd.add(TWO);

        source.addAll(toAdd);

        assertEquals("0", intsToStrings.get(0));
        assertEquals("1", intsToStrings.get(1));
        assertEquals("2", intsToStrings.get(2));
    }

    @Test
    public void testSet() {
        EventList<Integer> source = new BasicEventList<>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<>(source, new IntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);

        intsToStrings.set(0, "8");
        assertEquals("8", intsToStrings.get(0));

        source = new BasicEventList<>();
        intsToStrings = new FunctionList<>(source, new IntegerToString());
        source.add(ZERO);

        try {
            intsToStrings.set(0, "8");
            fail("failed to receive an IllegalStateException for a call to set with no reverse function specified");
        } catch (Exception e) {}

        source.set(0, NINE);
        assertEquals("9", intsToStrings.get(0));
    }

    @Test
    public void testRemove() {
        EventList<Integer> source = new BasicEventList<>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<>(source, new IntegerToString(), new StringToInteger());
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

    @Test
    public void testAdvancedFunction() {
        // establish a control for this test case with the normal Function
        EventList<Integer> source = new BasicEventList<>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<>(source, new AdvancedIntegerToString(), new StringToInteger());
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
        @Override
        public Integer evaluate(String value) {
            return new Integer(value);
        }
    }

    private static class IntegerToString implements FunctionList.Function<Integer,String> {
        @Override
        public String evaluate(Integer value) {
            return value.toString();
        }
    }

    private static class IntegerToCardinalityString implements FunctionList.Function<Integer,String> {
        @Override
        public String evaluate(Integer value) {
            switch (value.intValue()) {
                case 0: return "0th";
                case 1: return "1st";
                case 2: return "2nd";
                case 9: return "9th";
                default: throw new IllegalArgumentException("Unexpected value: " + value);
            }
        }
    }

    private static class CardinalityStringToInteger implements FunctionList.Function<String,Integer> {
        @Override
        public Integer evaluate(String value) {
            if ("0th" == value) return ZERO;
            if ("1st" == value) return ONE;
            if ("2nd" == value) return TWO;
            if ("9th" == value) return NINE;

            throw new IllegalArgumentException("Unexpected value: " + value);
        }
    }

    private static class AdvancedIntegerToString extends IntegerToString implements FunctionList.AdvancedFunction<Integer,String> {
        private int reevaluateCount = 0;
        private int disposeCount = 0;

        @Override
        public String reevaluate(Integer value, String oldValue) {
            this.reevaluateCount++;
            return this.evaluate(value);
        }

        @Override
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

    @Test
    public void testReorder() {
        // establish a control for this test case with the normal Function
        SortedList<Integer> source = new SortedList<>(new BasicEventList<Integer>(), null);
        FunctionList<Integer, String> intsToStrings = new FunctionList<>(source, new AdvancedIntegerToString(), new StringToInteger());
        ListConsistencyListener consistencyListener = ListConsistencyListener.install(intsToStrings);

        source.add(ONE);
        source.add(ZERO);
        assertEquals(2, consistencyListener.getEventCount());

        source.setComparator(GlazedLists.comparableComparator());

        assertEquals(3, consistencyListener.getEventCount());
    }

    @Test
    public void testSetForwardFunction() {
        // establish a control for this test case with the normal Function
        EventList<Integer> source = new BasicEventList<>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<>(source, new IntegerToString());
        ListConsistencyListener.install(intsToStrings);

        source.add(ZERO);
        assertEquals("0", intsToStrings.get(0));

        intsToStrings.setForwardFunction(new IntegerToCardinalityString());
        assertEquals("0th", intsToStrings.get(0));

        source.add(NINE);
        assertEquals("9th", intsToStrings.get(1));

        intsToStrings.setForwardFunction(new IntegerToString());
        assertEquals("0", intsToStrings.get(0));
        assertEquals("9", intsToStrings.get(1));

        try {
            intsToStrings.setForwardFunction(null);
            fail("failed to receive IllegalArgumentException for null forward function");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetReverseFunction() {
        // establish a control for this test case with the normal Function
        EventList<Integer> source = new BasicEventList<>();
        FunctionList<Integer, String> intsToStrings = new FunctionList<>(source, new IntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        source.add(ZERO);
        assertEquals("0", intsToStrings.get(0));
        intsToStrings.set(0, "1");
        assertEquals(ONE, source.get(0));

        intsToStrings.setForwardFunction(new IntegerToCardinalityString());
        assertEquals("1st", intsToStrings.get(0));
        try {
            intsToStrings.set(0, "2nd");
            fail("failed to throw IllegalArgumentException when using out-of-date reverse function");
        } catch (IllegalArgumentException e) {
            // expected
        }

        intsToStrings.setReverseFunction(new CardinalityStringToInteger());
        intsToStrings.set(0, "2nd");
        assertEquals(TWO, source.get(0));

        intsToStrings.setReverseFunction(null);
        try {
            intsToStrings.set(0, "9th");
            fail("failed to throw IllegalStateException when using a null reverse function to write thru a FunctionList");
        } catch (IllegalStateException e) {
            // expected
        }
    }


    // Tests basic removeIf operation as well as the fact that a single event should
    // be published for removal of multiple items
    // (note: requires source that extends AbstractEventList)
    @Test
    public void testRemoveIf() {
        EventList<Integer> source = new BasicEventList<>();
        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);

        List<ListEvent<Integer>> events = new ArrayList<>();
        source.addListEventListener(events::add);

        FunctionList<Integer, String> intsToStrings =
            new FunctionList<>(source, new IntegerToString(), new StringToInteger());
        ListConsistencyListener.install(intsToStrings);

        assertEquals(Arrays.asList("0", "1", "2"), intsToStrings);

        intsToStrings.removeIf(s -> !s.equals("0"));

        assertEquals(Collections.singletonList("0"), intsToStrings);
        assertEquals(Collections.singletonList(ZERO), source);

        // Should only dispatch one event
        assertEquals(1, events.size());
    }
}
