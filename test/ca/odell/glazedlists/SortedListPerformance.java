/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Utility class for analyzing the performance of the SortedList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class SortedListPerformance {

    private static Random dice = new Random(137);

    /**
     * Load issues from the specified file and return the result.
     */
    private static List loadIssues(String file) throws IOException {
        System.out.println("Reading issues...");

        List issues = new ArrayList();
        BufferedReader in = new BufferedReader(new FileReader(file));

        // trim the first few lines of the file to remove filter related items
        while(!in.readLine().equals("")) {
            // no-op to ignore the file header which is for text filtering only
        }

        // Read the actual data.
        String[] entryValues = new String[8];
        int counter = 0;
        String line;
        while((line = in.readLine()) != null) {
            if(line.equals("")) {
                issues.add(new MozillaEntry(entryValues[0], entryValues[1],
                    entryValues[2], entryValues[3], entryValues[4],
                    entryValues[5], entryValues[6], entryValues[7]));
                counter = 0;

            } else {
                try {
                    entryValues[counter] = line;
                    counter++;
                } catch (Exception e) {
                    System.out.println("exception on record: ");
                    for (int i = 0; i < entryValues.length; i++) {
                        System.out.println(entryValues[i]);
                    }
                }
            }
        }

        // we're done reading
        System.out.println("done. " + issues.size() + " issues loaded.");
        in.close();

        return issues;
    }

    /**
     * Execute a performance test that is specified on the command line.
     */
    public static void main(String[] args) throws Exception {
        if(args.length != 1) {
            System.out.println();
            System.out.println("Usage: SortedListPerformance <testfile>");
            System.out.println();
            System.out.println("<testfile> is a file containing the Mozilla Bug db");
            return;
        }

        List issues = loadIssues(args[0]);
        Comparator[] comparators = new Comparator[8];
        final String[] empty = {};
        comparators[0] = GlazedLists.beanPropertyComparator(MozillaEntry.class, "id", empty);
        comparators[1] = GlazedLists.beanPropertyComparator(MozillaEntry.class, "severity", empty);
        comparators[2] = GlazedLists.beanPropertyComparator(MozillaEntry.class, "priority", empty);
        comparators[3] = GlazedLists.beanPropertyComparator(MozillaEntry.class, "os", empty);
        comparators[4] = GlazedLists.beanPropertyComparator(MozillaEntry.class, "email", empty);
        comparators[5] = GlazedLists.beanPropertyComparator(MozillaEntry.class, "result", empty);
        comparators[6] = GlazedLists.beanPropertyComparator(MozillaEntry.class, "status", empty);
        comparators[7] = GlazedLists.beanPropertyComparator(MozillaEntry.class, "desc", empty);

        EventList issuesEventList = new BasicEventList();
        SortedList sorted = new SortedList(issuesEventList, comparators[0]);

        // populate
        System.out.print("Populating issues list...");
        long setUpStart = System.currentTimeMillis();
        issuesEventList.addAll(issues);
        long setUpEnd = System.currentTimeMillis();
        long setUpTime = setUpEnd - setUpStart;
        System.out.println("done. Time: " + setUpTime + "ms");

        // change the comparators 100 times
        System.out.print("Changing the comparators...");
        long tearDownStart = System.currentTimeMillis();
        for(int i = 0;i < 100;i++) {
            sorted.setComparator(comparators[dice.nextInt(8)]);
        }
        long tearDownEnd = System.currentTimeMillis();
        long tearDownTime = tearDownEnd - tearDownStart;
        System.out.println("done. Time: " + tearDownTime + "ms");
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
        @Override
        public String toString() {
            return "" + id;
        }
    }
}
