package ca.odell.glazedlists;

import ca.odell.glazedlists.matchers.*;
import ca.odell.glazedlists.impl.matchers.NotMatcher;
import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

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

    private class NumberMatcherEditor extends AbstractMatcherEditor<Number> {
        public void setNumber(Number number) {
            this.fireChanged(new NumberMatcher(number));
        }
    }

    private class NumberMatcher implements Matcher<Number> {
        private final Number value;

        public NumberMatcher(Number value) {
            this.value = value;
        }

        public boolean matches(Number item) {
            return value.doubleValue() == item.doubleValue();
        }
    }

    private class CapitalizedStringMatcher implements Matcher<String> {
        public boolean matches(String item) {
            return item != null && item.length() > 0 && Character.isUpperCase(item.charAt(0));
        }
    }

    private class OnMatcher implements Matcher<Boolean> {
        public boolean matches(Boolean item) {
            return item != null && item.booleanValue();
        }
    }
}