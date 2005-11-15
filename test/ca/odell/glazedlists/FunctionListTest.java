package ca.odell.glazedlists;

import junit.framework.TestCase;

public class FunctionListTest extends TestCase {

    public void testConstructor() {
        try {
            new FunctionList(new BasicEventList(), null);
            fail("failed to receive an IllegalArgumentException with null forward Function");
        } catch (IllegalArgumentException e) {}

        new FunctionList(new BasicEventList(), new IntegerToString());
        new FunctionList(new BasicEventList(), new IntegerToString(), null);

        BasicEventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<String,Integer> intsToStrings = new FunctionList<String,Integer>(source, new IntegerToString(), new StringToInteger());
        source.add(new Integer(0));
        source.add(new Integer(1));
        source.add(new Integer(2));

        assertEquals("0", intsToStrings.get(0));
        assertEquals("1", intsToStrings.get(1));
        assertEquals("2", intsToStrings.get(2));
    }

    public void testAdd() {
        BasicEventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<String,Integer> intsToStrings = new FunctionList<String,Integer>(source, new IntegerToString(), new StringToInteger());
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
        intsToStrings = new FunctionList<String,Integer>(source, new IntegerToString());
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

    public void testSet() {
        BasicEventList<Integer> source = new BasicEventList<Integer>();
        FunctionList<String,Integer> intsToStrings = new FunctionList<String,Integer>(source, new IntegerToString(), new StringToInteger());
        source.add(new Integer(0));
        source.add(new Integer(1));
        source.add(new Integer(2));

        intsToStrings.set(0, "8");
        assertEquals("8", intsToStrings.get(0));

        source = new BasicEventList<Integer>();
        intsToStrings = new FunctionList<String,Integer>(source, new IntegerToString());
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
        FunctionList<String,Integer> intsToStrings = new FunctionList<String,Integer>(source, new IntegerToString(), new StringToInteger());
        source.add(new Integer(0));
        source.add(new Integer(1));
        source.add(new Integer(2));

        intsToStrings.remove(0);
        assertEquals("1", intsToStrings.get(0));
        assertEquals("2", intsToStrings.get(1));

        source.remove(0);
        assertEquals("2", intsToStrings.get(0));
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
}