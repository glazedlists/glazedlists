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
public class DeltasDriver extends JapexDriverBase {

    private List<String> distinctValues;
    private int baseSize;

    private EventList<String> base;
    private FilterList<String> filteredBase;
    private SortedList<String> sortedBase;

    public void initializeDriver() {
        // do nothing
    }

    public void prepare(TestCase testCase) {
        String listEventAssemblerDelegate = getParam("GlazedLists.ListEventAssemblerDelegate");
        System.setProperty("GlazedLists.ListEventAssemblerDelegate", listEventAssemblerDelegate);

        String distinctValuesCSV = testCase.getParam("distinctValues");
        distinctValues = Arrays.asList(distinctValuesCSV.split("\\,"));
        baseSize = testCase.getIntParam("baseSize");

        base = new BasicEventList<String>();
        filteredBase = new FilterList<String>(base);
        sortedBase = new SortedList<String>(filteredBase, new FirstCharacterComparator());

        Random dice = new Random(0);
        for(int i = 0; i < baseSize; i++) {
            String characterOne = distinctValues.get(dice.nextInt(distinctValues.size()));
            String characterTwo = distinctValues.get(dice.nextInt(distinctValues.size()));
            base.add(characterOne + characterTwo);
        }
    }

    /**
     * Warmup is exactly the same as the run method.
     */
    public void warmup(TestCase testCase) {
        executeTestCase(testCase);
    }

    /**
     * Execute the specified testcase one time.
     */
    public void run(TestCase testCase) {
        executeTestCase(testCase);
    }

    private void executeTestCase(TestCase testCase) {
        for(Iterator<String> i = distinctValues.iterator(); i.hasNext(); ) {
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
    public static final class SameLastCharacterMatcher implements Matcher<String> {
        private final String value;
        public SameLastCharacterMatcher(String value) {
            this.value = value;
        }
        public boolean matches(String item) {
            return value.charAt(0) == item.charAt(1);
        }
    }
    public static final class FirstCharacterComparator implements Comparator<String> {
        public int compare(String s, String s1) {
            return s.charAt(0) - s1.charAt(0);
        }
    }
}