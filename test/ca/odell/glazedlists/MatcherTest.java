package ca.odell.glazedlists;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import ca.odell.glazedlists.impl.matchers.NotMatcher;
import ca.odell.glazedlists.impl.matchers.PropertyEventNameMatcher;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

public class MatcherTest extends TestCase {

    public void testTrueMatcher() {
        Matcher<String> s = Matchers.trueMatcher();
        Matcher<Boolean> b = Matchers.trueMatcher();
        assertSame(s, b);
    }

    public void testFalseMatcher() {
        Matcher<String> s = Matchers.falseMatcher();
        Matcher<Boolean> b = Matchers.falseMatcher();
        assertSame(s, b);
    }

    public void testNotMatcher() {
        Matcher<String> s = new NotMatcher<String>(new CapitalizedStringMatcher());
        assertFalse(s.matches("James"));
        assertTrue(s.matches("james"));

        Matcher<Boolean> b = new NotMatcher<Boolean>(new OnMatcher());
        assertFalse(b.matches(Boolean.TRUE));
        assertTrue(b.matches(Boolean.FALSE));
    }

    public void testGenerics() {
        NumberMatcherEditor numMatcherEditor = new NumberMatcherEditor();
        assertTrue(numMatcherEditor.getMatcher().matches(new Integer(5)));
        numMatcherEditor.setNumber(new Integer(10));
        assertFalse(numMatcherEditor.getMatcher().matches(new Integer(5)));
        assertTrue(numMatcherEditor.getMatcher().matches(new Integer(10)));

        numMatcherEditor.setNumber(new Float(3.14f));
        assertFalse(numMatcherEditor.getMatcher().matches(new Integer(10)));
        assertTrue(numMatcherEditor.getMatcher().matches(new Float(3.14f)));

        MatcherEditor<Number> typedMatcherEditor = numMatcherEditor;
        assertFalse(typedMatcherEditor.getMatcher().matches(new Integer(10)));
        assertTrue(typedMatcherEditor.getMatcher().matches(new Float(3.14f)));

        typedMatcherEditor = new TextMatcherEditor<Number>(GlazedLists.toStringTextFilterator());
        assertTrue(typedMatcherEditor.getMatcher().matches(new Integer(10)));
        assertTrue(typedMatcherEditor.getMatcher().matches(new Float(3.14f)));

        ((TextMatcherEditor)typedMatcherEditor).setFilterText(new String[] { "3" });
        assertFalse(typedMatcherEditor.getMatcher().matches(new Integer(10)));
        assertTrue(typedMatcherEditor.getMatcher().matches(new Float(3.14f)));
    }

    public void testFilter() {
        List<Integer> elements = new ArrayList<Integer>();
        elements.add(new Integer(45));
        elements.add(new Integer(22));
        elements.add(new Integer(15));
        elements.add(new Integer(22));
        elements.add(new Integer(13));
        elements.add(new Integer(53));
        elements.add(new Integer(22));
        elements.add(new Integer(23));
        elements.add(new Integer(22));

        boolean result;

        result = Matchers.filter(elements, new NumberMatcher(new Integer(22)));
        assertEquals(true, result);
        assertEquals(4, elements.size());

        result = Matchers.filter(elements, new NumberMatcher(new Integer(22)));
        assertEquals(false, result);
        assertEquals(4, elements.size());

        result = Matchers.filter(elements, new NumberMatcher(new Integer(33)));
        assertEquals(true, result);
        assertEquals(0, elements.size());

        result = Matchers.filter(elements, new NumberMatcher(new Integer(35)));
        assertEquals(false, result);
        assertEquals(0, elements.size());
    }

    public void testSelect() {
        List<Integer> elements = new ArrayList<Integer>();
        elements.add(new Integer(45));
        elements.add(new Integer(22));
        elements.add(new Integer(15));
        elements.add(new Integer(22));
        elements.add(new Integer(13));
        elements.add(new Integer(53));
        elements.add(new Integer(22));
        elements.add(new Integer(23));
        elements.add(new Integer(22));

        Collection<? super Integer> results = Matchers.select(elements, new NumberMatcher(new Integer(22)));
        assertEquals(4, results.size());

        results = Matchers.select(elements, new NumberMatcher(new Integer(22)), results);
        assertEquals(8, results.size());

        results = Matchers.select(elements, new NumberMatcher(new Integer(45)), results);
        assertEquals(9, results.size());

        results = Matchers.select(elements, new NumberMatcher(new Integer(99)), results);
        assertEquals(9, results.size());
    }

    public void testIndexOf() {
        List<Integer> elements = new ArrayList<Integer>();
        elements.add(new Integer(45));
        elements.add(new Integer(22));
        elements.add(new Integer(15));
        elements.add(new Integer(22));
        elements.add(new Integer(13));
        elements.add(new Integer(53));
        elements.add(new Integer(22));
        elements.add(new Integer(23));
        elements.add(new Integer(22));

        assertEquals(0, Matchers.indexOf(elements, new NumberMatcher(new Integer(45))));
        assertEquals(1, Matchers.indexOf(elements, new NumberMatcher(new Integer(22))));
        assertEquals(2, Matchers.indexOf(elements, new NumberMatcher(new Integer(15))));
        assertEquals(4, Matchers.indexOf(elements, new NumberMatcher(new Integer(13))));
        assertEquals(-1, Matchers.indexOf(elements, new NumberMatcher(new Integer(99))));
    }

    public void testPropertyMatcher() {
        Matcher<Collection> matcher = Matchers.beanPropertyMatcher(Collection.class, "empty", Boolean.TRUE);
        assertEquals(true, matcher.matches(Collections.EMPTY_LIST));
        assertEquals(false, matcher.matches(Collections.singletonList("A")));
        assertEquals(true, matcher.matches(Arrays.asList(new Object[0])));
        assertEquals(false, matcher.matches(Arrays.asList("B")));
        assertEquals(true, matcher.matches(Collections.EMPTY_SET));
        assertEquals(false, matcher.matches(null));
    }

    public void testDateRangeMatcher() {
        Matcher<Date> matcher = Matchers.rangeMatcher(new Date(10000), new Date(20000));
        assertEquals(false, matcher.matches(new Date(9999)));
        assertEquals(true, matcher.matches(new Date(10000)));
        assertEquals(true, matcher.matches(new Date(15000)));
        assertEquals(true, matcher.matches(new Date(20000)));
        assertEquals(false, matcher.matches(new Date(20001)));
        assertEquals(true, matcher.matches(null));

        matcher = Matchers.rangeMatcher(null, new Date(20000));
        assertEquals(true, matcher.matches(new Date(-9999)));
        assertEquals(true, matcher.matches(new Date(9999)));
        assertEquals(true, matcher.matches(new Date(10000)));
        assertEquals(true, matcher.matches(new Date(15000)));
        assertEquals(true, matcher.matches(new Date(20000)));
        assertEquals(false, matcher.matches(new Date(20001)));
        assertEquals(true, matcher.matches(null));

        matcher = Matchers.rangeMatcher(new Date(10000), null);
        assertEquals(false, matcher.matches(new Date(9999)));
        assertEquals(true, matcher.matches(new Date(10000)));
        assertEquals(true, matcher.matches(new Date(15000)));
        assertEquals(true, matcher.matches(new Date(20000)));
        assertEquals(true, matcher.matches(new Date(20001)));
        assertEquals(true, matcher.matches(null));

        matcher = Matchers.rangeMatcher(null, null);
        assertEquals(true, matcher.matches(new Date(9999)));
        assertEquals(true, matcher.matches(new Date(10000)));
        assertEquals(true, matcher.matches(new Date(15000)));
        assertEquals(true, matcher.matches(new Date(20000)));
        assertEquals(true, matcher.matches(new Date(20001)));
        assertEquals(true, matcher.matches(null));
    }

    public void testPropertyEventNameMatcher() {
        try {
            new PropertyEventNameMatcher(true, (Collection) null);
            fail("Expected exception for PropertyEventNameMatcher constructor");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            new PropertyEventNameMatcher(true, (String[]) null);
            fail("Expected exception for PropertyEventNameMatcher constructor");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        final PropertyChangeEvent event1 = new PropertyChangeEvent(this, "test", "old", "new");
        final PropertyChangeEvent event2 = new PropertyChangeEvent(this, "test", null, null);
        final PropertyChangeEvent event3 = new PropertyChangeEvent(this, "hello", "old", "new");
        final PropertyChangeEvent event4 = new PropertyChangeEvent(this, "hello", null, null);
        final PropertyChangeEvent event5 = new PropertyChangeEvent(this, null, null, null);

        Matcher<PropertyChangeEvent> matcher = new PropertyEventNameMatcher(true, new String[] {"test", null});
        assertEquals(true, matcher.matches(event1));
        assertEquals(true, matcher.matches(event2));
        assertEquals(false, matcher.matches(event3));
        assertEquals(false, matcher.matches(event4));
        assertEquals(true, matcher.matches(event5));

        matcher = new PropertyEventNameMatcher(false, new String[] {"test", null});
        assertEquals(false, matcher.matches(event1));
        assertEquals(false, matcher.matches(event2));
        assertEquals(true, matcher.matches(event3));
        assertEquals(true, matcher.matches(event4));
        assertEquals(false, matcher.matches(event5));

        matcher = new PropertyEventNameMatcher(true, new String[] {});
        assertEquals(false, matcher.matches(event1));
        assertEquals(false, matcher.matches(event2));
        assertEquals(false, matcher.matches(event3));
        assertEquals(false, matcher.matches(event4));
        assertEquals(false, matcher.matches(event5));

        matcher = new PropertyEventNameMatcher(false, new String[] {});
        assertEquals(true, matcher.matches(event1));
        assertEquals(true, matcher.matches(event2));
        assertEquals(true, matcher.matches(event3));
        assertEquals(true, matcher.matches(event4));
        assertEquals(true, matcher.matches(event5));

        matcher = new PropertyEventNameMatcher(true, Arrays.asList("test", null));
        assertEquals(true, matcher.matches(event1));
        assertEquals(true, matcher.matches(event2));
        assertEquals(false, matcher.matches(event3));
        assertEquals(false, matcher.matches(event4));
        assertEquals(true, matcher.matches(event5));
    }

    public void testTypeMatcher() {
        final List<Number> numbers = new ArrayList<Number>();
        numbers.add(new Float(0));
        numbers.add(new Double(1));
        numbers.add(new Short((short) 2));
        numbers.add(new Integer(3));
        numbers.add(new Long(4));

        Collection<? super Number> selected;

        final Matcher<Number> intAndLongMatcher = Matchers.types(Long.class, Integer.class);
        selected = Matchers.select(numbers, intAndLongMatcher);
        assertEquals(Arrays.asList(new Integer(3), new Long(4)), selected);

        final Matcher<Number> numberMatcher = Matchers.types(Number.class);
        selected = Matchers.select(numbers, numberMatcher);
        assertEquals(Arrays.asList(new Float(0), new Double(1), new Short((short) 2), new Integer(3), new Long(4)), selected);

        final Matcher<Number> stringMatcher = Matchers.types(String.class);
        selected = Matchers.select(numbers, stringMatcher);
        assertEquals(Collections.EMPTY_LIST, selected);
    }

    private static class NumberMatcherEditor extends AbstractMatcherEditor<Number> {
        public void setNumber(Number number) {
            this.fireChanged(new NumberMatcher(number));
        }
    }

    private static class NumberMatcher implements Matcher<Number> {
        private final Number value;

        public NumberMatcher(Number value) {
            this.value = value;
        }

        public boolean matches(Number item) {
            return value.doubleValue() == item.doubleValue();
        }
    }

    private static class CapitalizedStringMatcher implements Matcher<String> {
        public boolean matches(String item) {
            return item != null && item.length() > 0 && Character.isUpperCase(item.charAt(0));
        }
    }

    private static class OnMatcher implements Matcher<Boolean> {
        public boolean matches(Boolean item) {
            return item != null && item.booleanValue();
        }
    }
}