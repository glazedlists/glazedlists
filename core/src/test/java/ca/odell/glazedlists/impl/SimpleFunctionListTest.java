package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link SimpleFunctionList}.
 *
 * @author Holger Brands
 */
public class SimpleFunctionListTest {
    private static final String ZERO = "ZERO";
    private static final String ONE = "ONE";
    private static final String TWO = "TWO";

    @Test
    public void testConstructor() {
        try {
            GlazedLists.transformByFunction(new BasicEventList<String>(), null);
            fail("failed to receive an NullPointerException with null Function");
        } catch (NullPointerException e) {}

        BasicEventList<String> source = new BasicEventList<>();
        EventList<String> firstLetters = GlazedLists.transformByFunction(source, GlazedListsTests.getFirstLetterFunction());

        assertEquals(0, firstLetters.size());
        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);

        assertEquals(3, firstLetters.size());
        assertEquals("Z", firstLetters.get(0));
        assertEquals("O", firstLetters.get(1));
        assertEquals("T", firstLetters.get(2));

        firstLetters = GlazedLists.transformByFunction(source, GlazedListsTests.getFirstLetterFunction());
        assertEquals(3, firstLetters.size());
        assertEquals("Z", firstLetters.get(0));
        assertEquals("O", firstLetters.get(1));
        assertEquals("T", firstLetters.get(2));
    }

    @Test
    public void testAdd() {
        BasicEventList<String> source = new BasicEventList<>();
        EventList<String> firstLetters = GlazedLists.transformByFunction(source, GlazedListsTests.getFirstLetterFunction());

        source.add(ZERO);
        source.add(ONE);
        source.add(0, TWO);

        assertEquals(3, firstLetters.size());
        assertEquals("T", firstLetters.get(0));
        assertEquals("Z", firstLetters.get(1));
        assertEquals("O", firstLetters.get(2));

        source.addAll(Arrays.asList("THREE", "FOUR"));

        assertEquals("T", firstLetters.get(3));
        assertEquals("F", firstLetters.get(4));
    }

    @Test
    public void testSet() {
        BasicEventList<String> source = new BasicEventList<>();
        EventList<String> firstLetters = GlazedLists.transformByFunction(source, GlazedListsTests.getFirstLetterFunction());

        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);

        source.set(1, "THREE");
        assertEquals("Z", firstLetters.get(0));
        assertEquals("T", firstLetters.get(1));
        assertEquals("T", firstLetters.get(2));
        source.set(1, "ZERO");
        assertEquals("Z", firstLetters.get(0));
        assertEquals("Z", firstLetters.get(1));
        assertEquals("T", firstLetters.get(2));
    }

    @Test
    public void testRemove() {
        BasicEventList<String> source = new BasicEventList<>();
        EventList<String> firstLetters = GlazedLists.transformByFunction(source, GlazedListsTests.getFirstLetterFunction());

        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);
        source.add("THREE");

        source.remove(ZERO);
        assertEquals("O", firstLetters.get(0));
        assertEquals("T", firstLetters.get(1));
        assertEquals("T", firstLetters.get(2));

        source.remove(1);
        assertEquals("O", firstLetters.get(0));
        assertEquals("T", firstLetters.get(1));
    }

    @Test
    public void testReorder() {
        // establish a control for this test case with the normal Function
        SortedList<String> source = new SortedList<>(new BasicEventList<String>(), null);
        EventList<String> firstLetters = GlazedLists.transformByFunction(source, GlazedListsTests.getFirstLetterFunction());

        source.add(ZERO);
        source.add(ONE);
        source.add(TWO);

        assertEquals(3, firstLetters.size());
        assertEquals("Z", firstLetters.get(0));
        assertEquals("O", firstLetters.get(1));
        assertEquals("T", firstLetters.get(2));

        source.setComparator(GlazedLists.comparableComparator());

        assertEquals(3, firstLetters.size());
        assertEquals("O", firstLetters.get(0));
        assertEquals("T", firstLetters.get(1));
        assertEquals("Z", firstLetters.get(2));
    }
}
