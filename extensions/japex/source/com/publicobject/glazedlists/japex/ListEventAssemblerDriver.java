/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex;

import com.sun.japex.TestCase;
import com.sun.japex.JapexDriverBase;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import java.util.*;

/**
 * This simple Japex driver tests how quickly the ListEventAssembler can arrange
 * complicated events from a source SortedList.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListEventAssemblerDriver extends JapexDriverBase {

    private static final List<String> DISTINCT_VALUES = Arrays.asList(new String[] { "A", "B", "C", "D", "E" });
    private static final int SIZE_OF_BASE = 20000;

    private EventList<String> base;
    private FilterList<String> filteredBase;
    private SortedList<String> sortedBase;

    public void initializeDriver() {
        // do nothing
    }

    public void prepare(TestCase testCase) {
        base = new BasicEventList<String>();
        filteredBase = new FilterList<String>(base);
        sortedBase = new SortedList<String>(filteredBase, new FirstCharacterComparator());

        Random dice = new Random(0);
        for(int i = 0; i < SIZE_OF_BASE; i++) {
            String characterOne = DISTINCT_VALUES.get(dice.nextInt(DISTINCT_VALUES.size()));
            String characterTwo = DISTINCT_VALUES.get(dice.nextInt(DISTINCT_VALUES.size()));
            base.add(characterOne + characterTwo);
        }
    }

    /**
     * Warmup is exactly the same as the run method.
     */
    public void warmup(TestCase testCase) {
        for(Iterator<String> i = DISTINCT_VALUES.iterator(); i.hasNext(); ) {
            String value = i.next();
            filteredBase.setMatcher(new SameLastCharacterMatcher(value));
        }
        filteredBase.setMatcher((Matcher)Matchers.trueMatcher());
    }

    /**
     * Execute the specified testcase one time.
     */
    public void run(TestCase testCase) {
        for(Iterator<String> i = DISTINCT_VALUES.iterator(); i.hasNext(); ) {
            String value = i.next();
            filteredBase.setMatcher(new SameLastCharacterMatcher(value));
        }
        filteredBase.setMatcher((Matcher)Matchers.trueMatcher());
    }

    public void finish(TestCase testCase) {
        // do nothing
    }

    public void terminateDriver() {
        // do nothing
    }

    /**
     * Test if values are the same as the specified value.
     */
    public static class SameLastCharacterMatcher implements Matcher<String> {
        private final String value;
        public SameLastCharacterMatcher(String value) {
            this.value = value;
        }
        public boolean matches(String item) {
            return value.charAt(0) == item.charAt(1);
        }
    }
    public static class FirstCharacterComparator implements Comparator<String> {
        public int compare(String s, String s1) {
            return s.charAt(0) - s1.charAt(0);
        }
    }
}