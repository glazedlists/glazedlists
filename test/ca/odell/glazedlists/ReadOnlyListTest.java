/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * A ReadOnlyListTest tests the functionality of the ReadOnlyList
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ReadOnlyListTest extends TestCase {

    /** attempt to modify this list */
    private EventList<String> readOnlyData = null;

    /** attempt to modify this list */
    private List<String> readOnly = null;

    /**
     * Prepare for the test.
     */
    @Override
    public void setUp() {
        // create a list of data
        readOnlyData = new BasicEventList<String>();
        readOnlyData.add("A");
        readOnlyData.add("B");
        readOnlyData.add("C");

        // our list is that data, but read only
        readOnly = GlazedLists.readOnlyList(readOnlyData);
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void tearDown() {
        readOnlyData = null;
        readOnly = null;
    }

    /**
     * Verifies that the sublist is also read only.
     */
    public void testSubList() {
        try {
            readOnly.subList(0, 3).clear();
            fail();
        } catch(UnsupportedOperationException e) {
            // read failed as expected
        }

        readOnlyData.subList(0, 3).clear();
    }

    /**
     * Verifies that the sublist is also read only.
     */
    public void testIterator() {
        try {
            Iterator i = readOnly.iterator();
            i.next();
            i.remove();
            fail();
        } catch(UnsupportedOperationException e) {
            // read failed as expected
        }

        Iterator i = readOnlyData.iterator();
        i.next();
        i.remove();
    }

    public void testReadMethods() {
        readOnlyData.clear();
        readOnlyData.addAll(GlazedListsTests.stringToList("ABCDEFGB"));

        assertEquals("A", readOnly.get(0));
        assertTrue(readOnly.contains("E"));
        assertEquals(readOnly, Arrays.asList(readOnly.toArray()));
        assertEquals(readOnly, Arrays.asList(readOnly.toArray(new String[0])));
        assertTrue(readOnly.containsAll(Collections.singletonList("B")));
        assertEquals(3, readOnly.indexOf("D"));
        assertEquals(readOnly.size()-1, readOnly.lastIndexOf("B"));
        assertEquals(GlazedListsTests.stringToList("CDE"), readOnly.subList(2, 5));
    }

    public void testWriteMethods() {
        try {
            readOnly.add(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.add(0, null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.addAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.addAll(0, null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.clear();
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.remove(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.remove(0);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.removeAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.retainAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.set(0, null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /**
     * Tests {@link GlazedLists#readOnlyList(EventList)}
     */
    @SuppressWarnings("unused")
    public void testGenericsFactoryMethod() {
    	final EventList<AbstractBase> baseList = new BasicEventList<AbstractBase>();
    	final EventList<Derived> derivedList = new BasicEventList<Derived>();
    	final EventList<Concrete> concreteList = new BasicEventList<Concrete>();

    	final EventList<AbstractBase> readOnlyBaseList = GlazedLists.readOnlyList(baseList);
    	final TransformedList<AbstractBase, AbstractBase> treadOnlyBaseList = GlazedLists.readOnlyList(baseList);
    	final EventList<Derived> readOnlyDerivedList = GlazedLists.readOnlyList(derivedList);
    	final TransformedList<Derived, Derived> treadOnlyDerivedList = GlazedLists.readOnlyList(derivedList);
		final EventList<Concrete> readOnlyConcreteList = GlazedLists.readOnlyList(concreteList);
    	final TransformedList<Concrete, Concrete> treadOnlyConcreteList = GlazedLists.readOnlyList(concreteList);

    	// wasn't possible before Bugfix GLAZEDLISTS-510:
    	final EventList<AbstractBase> readOnlyBaseDerivedList = GlazedLists.<AbstractBase>readOnlyList(derivedList);
    	final TransformedList<AbstractBase, AbstractBase> treadOnlyBaseDerivedList = GlazedLists.<AbstractBase>readOnlyList(derivedList);
    	final EventList<AbstractBase> readOnlyBaseConcreteList = GlazedLists.<AbstractBase>readOnlyList(concreteList);
    	final TransformedList<AbstractBase, AbstractBase> treadOnlyBaseConcreteList = GlazedLists.<AbstractBase>readOnlyList(concreteList);
    	final EventList<Derived> readOnlyDerivedConcreteList = GlazedLists.<Derived>readOnlyList(concreteList);
    	final TransformedList<Derived, Derived> treadOnlyDerivedConcreteList = GlazedLists.<Derived>readOnlyList(concreteList);
    }

    private static abstract class AbstractBase {
    }

    private static class Derived extends AbstractBase {
    }

    private static class Concrete extends Derived {
    }

}