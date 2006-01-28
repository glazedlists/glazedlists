/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex;

import com.sun.japex.JapexDriver;
import com.sun.japex.TestCase;
import com.sun.japex.JapexDriverBase;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import java.util.*;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListEventAssemblerDriver extends JapexDriverBase {

    private static final List<String> DISTINCT_VALUES = Arrays.asList(new String[] { "A", "B", "C", "D", "E" });
    private static final int SIZE_OF_BASE = 10000;

    private EventList<String> base = new BasicEventList<String>();
    private FilterList<String> filteredBase = new FilterList<String>(base);
    private SortedList<String> sortedBase = new SortedList<String>(filteredBase);

    public void initializeDriver() {
        Random dice = new Random(0);
        for(int i = 0; i < SIZE_OF_BASE; i++) {
            int index = dice.nextInt(DISTINCT_VALUES.size());
            base.add(DISTINCT_VALUES.get(index));
        }
    }

    public void prepare(TestCase testCase) {
        // do nothing
    }

    /**
     * Warmup is exactly the same as the run method.
     */
    public void warmup(TestCase testCase) {
        for(Iterator<String> i = DISTINCT_VALUES.iterator(); i.hasNext(); ) {
            String value = i.next();
            filteredBase.setMatcher(new SameMatcher<String>(value));
        }
        filteredBase.setMatcher((Matcher)Matchers.trueMatcher());
    }

    /**
     * Execute the specified testcase one time.
     */
    public void run(TestCase testCase) {
        for(Iterator<String> i = DISTINCT_VALUES.iterator(); i.hasNext(); ) {
            String value = i.next();
            filteredBase.setMatcher(new SameMatcher<String>(value));
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
    public static class SameMatcher<E> implements Matcher<E> {
        private final E value;
        public SameMatcher(E value) {
            this.value = value;
        }
        public boolean matches(Object item) {
            return value == item;
        }
    }
}