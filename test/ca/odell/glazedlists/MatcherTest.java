package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.matchers.NotMatcher;
import ca.odell.glazedlists.matchers.*;
import junit.framework.TestCase;

import java.util.*;

public class MatcherTest extends TestCase {

    public void testTrueMatcher() {
        Matcher<String> s = Matchers.trueMatcher();
        Matcher<Boolean> b = Matchers.trueMatcher();
        assertTrue((Object) s == b);
    }

    public void testFalseMatcher() {
        Matcher<String> s = Matchers.falseMatcher();
        Matcher<Boolean> b = Matchers.falseMatcher();
        assertTrue((Object) s == b);
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

        result = Matchers.filter(elements, (Matcher)new NumberMatcher(new Integer(22)));
        assertEquals(true, result);
        assertEquals(4, elements.size());

        result = Matchers.filter(elements, (Matcher)new NumberMatcher(new Integer(22)));
        assertEquals(false, result);
        assertEquals(4, elements.size());

        result = Matchers.filter(elements, (Matcher)new NumberMatcher(new Integer(33)));
        assertEquals(true, result);
        assertEquals(0, elements.size());

        result = Matchers.filter(elements, (Matcher)new NumberMatcher(new Integer(35)));
        assertEquals(false, result);
        assertEquals(0, elements.size());
    }

    public void testPropertyMatcher() {
        Matcher matcher = Matchers.beanPropertyMatcher(Collection.class, "empty", Boolean.TRUE);
        assertEquals(true, matcher.matches(Collections.EMPTY_LIST));
        assertEquals(false, matcher.matches(Collections.singletonList("A")));
        assertEquals(true, matcher.matches(Arrays.asList(new Object[] { })));
        assertEquals(false, matcher.matches(Arrays.asList(new Object[] { "B" })));
        assertEquals(true, matcher.matches(Collections.EMPTY_SET));
        assertEquals(false, matcher.matches(null));
    }

    public void testDateRangeMatcher() {
        Matcher matcher = Matchers.rangeMatcher(new Date(10000), new Date(20000));
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