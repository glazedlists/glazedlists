/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import java.util.*;

/**
 * Utility class for analyzing the performance of the UniqueList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class UniqueListPerformance {

    /**
     * Execute a performance test that is specified on the command line.
     */
    public static void main(String[] args) {
        if(args.length != 0) {
            return;
        }

        System.out.println("Generating Test Data...");
        BasicEventList source = new BasicEventList();
        Random random = new Random(137);
        for(int i = 0;i < 10000;i++) {
            source.add(new SimpleBusinessObject(random.nextInt(500), random.nextInt(500), random.nextInt(500), random.nextInt(500), random.nextInt(500)));
        }
        AllOrOneValueFilter firstFilter =  new AllOrOneValueFilter(source, GlazedLists.beanPropertyComparator(SimpleBusinessObject.class, "a"));
        UniqueList secondFilter = new UniqueList(firstFilter,  GlazedLists.beanPropertyComparator(SimpleBusinessObject.class, "b"));
        UniqueList thirdFilter =  new UniqueList(secondFilter, GlazedLists.beanPropertyComparator(SimpleBusinessObject.class, "c"));
        UniqueList fourthFilter = new UniqueList(thirdFilter,  GlazedLists.beanPropertyComparator(SimpleBusinessObject.class, "d"));
        UniqueList fifthFilter =  new UniqueList(fourthFilter, GlazedLists.beanPropertyComparator(SimpleBusinessObject.class, "e"));
        System.out.println("Done.\n");

        System.out.println("Starting performance test...");
        long startTime = System.currentTimeMillis();
        for(int i = 0;i < 1000;i++) {
            firstFilter.setUnfilteredIndex(random.nextInt(500) - 1);
        }
        long endTime = System.currentTimeMillis();
        long testTime = endTime - startTime;
        System.out.println("Done.\n");
        System.out.println("Test completed in " + testTime + " milliseconds");
    }

    /**
     * A simple filter list that includes only items which match a particular
     * value, or everything in the source if no such value is selected.
     */
    private static class AllOrOneValueFilter extends AbstractFilterList {

        /** the only index to filter in */
        private int unfilteredIndex = -1;

        /** the only thing in the list if not all */
        private Object unfilteredItem = null;

        /** the comparator to use to compare objects */
        private Comparator comparator;

        /**
         * Create a filter list that contains a unique, sorted set of data
         * that either contains all elements which match the selected unique
         * value, or source.size() elements.
         */
        public AllOrOneValueFilter(EventList source, Comparator comparator) {
            super(new UniqueList(source, comparator));
            this.comparator = comparator;
        }

        /** {@inheritDoc} */
        public boolean filterMatches(Object element) {
            if(unfilteredIndex == -1) return true;

            return 0 == comparator.compare(element, unfilteredItem);
        }

        /**
         * Sets which element is unfiltered.  For all elements to be unfiltered
         * call this method with -1
         */
        public void setUnfilteredIndex(int index) {
            unfilteredIndex = index;
            if(unfilteredIndex == -1) unfilteredItem = null;
            else unfilteredItem = source.get(unfilteredIndex);
            handleFilterChanged();
        }
    }

    /**
     * A simple business object to use for testing.
     */
    public static class SimpleBusinessObject {

        /** some JavaBean properties */
        private int a;
        private int b;
        private int c;
        private int d;
        private int e;

        /**
         * Creates a new SimpleBusinessObject to use for performance testing
         */
        SimpleBusinessObject(int a, int b, int c, int d, int e) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
        }

        /** getters for the JavaBean properties */
        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public int getC() {
            return c;
        }

        public int getD() {
            return d;
        }

        public int getE() {
            return e;
        }
    }
}
