/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import java.util.*;
import java.io.*;

/**
 * Utility class for analyzing the performance of the UniqueList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class UniqueListPerformance {

    /**
     * Execute a performance test that is specified on the command line.
     */
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println();
            System.out.println("Usage: UniqueListPerformance <testfile> <numOfFilters>");
            System.out.println();
            System.out.println("<testfile> is a file containing the Mozilla Bug db");
            System.out.println("<numOfFilters> is the number of times to filter the source list.");
            System.out.println();
            return;
        }

        // Start reading the test data file
        System.out.println();
        System.out.println("Reading data from testfile...");
        BasicEventList source = new BasicEventList();
        Random random = new Random(137);
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        String line = "";

        // trim the first few lines of the file to remove filter related items
        while(!(line = in.readLine()).equals("")) {
            // no-op to ignore the file header which is for text filtering
        }

        // Read the actual data.
        String[] entryValues = new String[8];
        int counter = 0;
        while((line = in.readLine()) != null) {
            if(line.equals("")) {
                source.add(new MozillaEntry(entryValues[0], entryValues[1],
                    entryValues[2], entryValues[3], entryValues[4],
                    entryValues[5], entryValues[6], entryValues[7]));
                counter = 0;

            } else {
                entryValues[counter] = line;
                counter++;
            }
        }

        // we're done reading
        System.out.println("Done.  " + source.size() + " issue entries loaded.\n");
        in.close();

        // Now set up the transformations
        System.out.println("Setting up list transformations...");
        long setUpStart = System.currentTimeMillis();
        AllOrOneValueFilter firstFilter =  new AllOrOneValueFilter(source, GlazedLists.beanPropertyComparator(MozillaEntry.class, "user"));
        UniqueList secondFilter = new UniqueList(firstFilter,  GlazedLists.beanPropertyComparator(MozillaEntry.class, "email"));
        UniqueList thirdFilter =  new UniqueList(secondFilter, GlazedLists.beanPropertyComparator(MozillaEntry.class, "priority"));
        UniqueList fourthFilter = new UniqueList(thirdFilter,  GlazedLists.beanPropertyComparator(MozillaEntry.class, "os"));
        UniqueList fifthFilter =  new UniqueList(fourthFilter, GlazedLists.beanPropertyComparator(MozillaEntry.class, "result"));
        UniqueList sixthFilter =  new UniqueList(fifthFilter,  GlazedLists.beanPropertyComparator(MozillaEntry.class, "status"));
        long setUpEnd = System.currentTimeMillis();
        long setUpTime = setUpEnd - setUpStart;
        System.out.println("Done.  List transformations took " + setUpTime + " to initialize.\n");

        System.out.println("Starting event handling performance test...");
        int filterIterations = Integer.parseInt(args[1]);
        long startTime = System.currentTimeMillis();
        for(int i = 0;i < filterIterations;i++) {
            firstFilter.setUnfilteredIndex(random.nextInt(source.size()) - 1);
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
     * An entry in the Mozilla bug db
     */
    public static class MozillaEntry {
        /** some JavaBean properties */
        private String id;
        private String user;
        private String priority;
        private String os;
        private String email;
        private String result;
        private String status;
        private String desc;

        /**
         * Models an entry in the Mozilla bug db.
         */
        MozillaEntry(String id, String user, String priority, String os, String email, String result, String status, String desc) {
            this.id = id;
            this.user = user;
            this.priority = priority;
            this.os = os;
            this.email = email;
            this.result = result;
            this.status = status;
            this.desc = desc;
        }

        /** getters for the JavaBean properties */
        public String getId() {
            return id;
        }

        public String getUser() {
            return user;
        }

        public String getPriority() {
            return priority;
        }

        public String getOs() {
            return os;
        }

        public String getEmail() {
            return email;
        }

        public String getResult() {
            return result;
        }

        public String getStatus() {
            return status;
        }

        public String getDesc() {
            return desc;
        }
    }
}
