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

    private static Random dice = new Random(137);
    
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
        System.out.print("Setting up list transformations");
        long setUpStart = System.currentTimeMillis();
        
        UniqueList firstUnique = new UniqueList(source, GlazedLists.beanPropertyComparator(MozillaEntry.class, "email"));
        validateUniqueList(firstUnique, source, GlazedLists.beanPropertyComparator(MozillaEntry.class, "email"));
        AllOrOneValueFilter firstFilter =  new AllOrOneValueFilter(firstUnique);
        System.out.print(".");
        UniqueList secondFilter = new UniqueList(firstFilter,  GlazedLists.beanPropertyComparator(MozillaEntry.class, "severity"));
        validateUniqueList(secondFilter, firstFilter, GlazedLists.beanPropertyComparator(MozillaEntry.class, "severity"));
        System.out.print(".");
        UniqueList thirdFilter =  new UniqueList(secondFilter, GlazedLists.beanPropertyComparator(MozillaEntry.class, "priority"));
        System.out.print(".");
        UniqueList fourthFilter = new UniqueList(thirdFilter,  GlazedLists.beanPropertyComparator(MozillaEntry.class, "os"));
        System.out.print(".");
        UniqueList fifthFilter =  new UniqueList(fourthFilter, GlazedLists.beanPropertyComparator(MozillaEntry.class, "result"));
        validateUniqueList(fifthFilter, fourthFilter, GlazedLists.beanPropertyComparator(MozillaEntry.class, "result"));
        System.out.print(".");
        UniqueList sixthFilter =  new UniqueList(fifthFilter,  GlazedLists.beanPropertyComparator(MozillaEntry.class, "status"));
        
        long setUpEnd = System.currentTimeMillis();
        long setUpTime = setUpEnd - setUpStart;
        System.out.println("Done.\nList transformations took " + setUpTime + " to initialize.\n");

        System.out.println("Starting event handling performance test...");
        int filterIterations = Integer.parseInt(args[1]);
        long startTime = System.currentTimeMillis();
        for(int i = 0;i < filterIterations;i++) {
            firstFilter.pickANewFilterAtRandom();
            if(i % 20 == 0) {
                System.out.println("FIRST FILTER SIZE: " + firstFilter.size());
                int index = dice.nextInt(firstFilter.size());
                System.out.println("A RANDOM ELEMENT AT " + index + " = " + firstFilter.get(index));
            }
        }
        
        long endTime = System.currentTimeMillis();
        long testTime = endTime - startTime;
        System.out.println("Done.\nTest completed in " + testTime + " milliseconds");
    }
    
    private static void validateUniqueList(UniqueList unique, Collection parent, Comparator comparator) {
        TreeSet allUniqueElements = new TreeSet(comparator);
        allUniqueElements.addAll(parent);
        
        Iterator a = unique.iterator();
        Iterator b = allUniqueElements.iterator();
        for(; a.hasNext() && b.hasNext(); ) {
            Object eA = a.next();
            Object eB = b.next();
            if(0 != comparator.compare(eA, eB)) throw new IllegalStateException("NO MATCH: " + eA + " != " + eB);
        }
        if(a.hasNext() || b.hasNext()) throw new IllegalStateException("DIFFERENT SIZES: " + unique.size() + " != " + allUniqueElements.size());
        
        // they're equal
        System.out.println("EQUAL SETS OF SIZE : " + unique.size() + ", " + allUniqueElements.size() );
    }

    /**
     * A simple filter list that includes only items which match a particular
     * value, or everything in the source if no such value is selected.
     */
    private static class AllOrOneValueFilter extends AbstractFilterList {

        /** the only index to filter in */
        private int matchModulus = 1;

        /**
         * Create a filter list that contains a unique, sorted set of data
         * that either contains all elements which match the selected unique
         * value, or source.size() elements.
         */
        public AllOrOneValueFilter(EventList source) {
            super(source);
        }

        /** {@inheritDoc} */
        public boolean filterMatches(Object element) {
            MozillaEntry entry = (MozillaEntry)element;
            return (entry.getId() % matchModulus == 0);
        }

        /**
         * Sets which element is unfiltered.  For all elements to be unfiltered
         * call this method with -1
         */
        public void pickANewFilterAtRandom() {
            matchModulus = dice.nextInt(6) + 1;
            
            handleFilterChanged();
        }
    }

    /**
     * An entry in the Mozilla bug db
     */
    public static class MozillaEntry {
        /** some JavaBean properties */
        private int id;
        private String severity;
        private String priority;
        private String os;
        private String email;
        private String result;
        private String status;
        private String desc;

        /**
         * Models an entry in the Mozilla bug db.
         */
        MozillaEntry(String id, String severity, String priority, String os, String email, String result, String status, String desc) {
            this.id = Integer.parseInt(id);
            this.severity = severity;
            this.priority = priority;
            this.os = os;
            this.email = email;
            this.result = result;
            this.status = status;
            this.desc = desc;
        }

        /** getters for the JavaBean properties */
        public int getId() {
            return id;
        }

        public String getSeverity() {
            return severity;
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
        public String toString() {
            return "" + id;
        }
    }
}
