/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

import java.util.*;
import java.io.*;
// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;


/**
 * Utility class for analyzing the performance of the TextFilterList.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TextFilterPerformance {

    /**
     * Execute a performance test that is specified on the command line.
     */
    public static void main(String[] args) throws Exception {
        if(args.length != 1) {
            System.out.println("Usage: TextFilterPerformance <testfile>");
            System.out.println("");
            System.out.println("<testfile> is a file in the following format:");
            System.out.println(" filter section:   (<filterstring><CRLF><expect><CRLF>)*");
            System.out.println(" separator:        <CRLF>");
            System.out.println(" elements section: ((<element string><CRLF>)*)<CRLF>*");
            System.out.println("");
            System.out.println("<CRLF> is a newline character");
            System.out.println("<filterstring> is a search string of space separated tokens");
            System.out.println("<expect> is an integer, the number of elements to match the filterstring");
            System.out.println("<element string> is a string component of an element");
            return;
        }
        
        // start reading
        System.out.println("Reading input");
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        String line = "";
        
        // read the filter strings
        List testFilters = new ArrayList();
        List testHitCounts = new ArrayList();
        while(!(line = in.readLine()).equals("")) {
            testFilters.add(line);
            testHitCounts.add(new Integer(in.readLine()));
        }
        
        // read the input texts
        List elements = new ArrayList();
        List currentElement = new ArrayList();
        elements.add(currentElement);
        while((line = in.readLine()) != null) {
            if(line.equals("")) {
                currentElement = new ArrayList();
                elements.add(currentElement);
            } else {
                currentElement.add(line);
            }
        }
        
        // we're done reading
        System.out.println("Read input, " + elements.size() + " elements");
        System.out.println("");
        in.close();

        // summarize what we've read
        for(int f = 0; f < testFilters.size(); f++) {
            System.out.println("Filter " + f + ": " + testFilters.get(f) + ", expect: " + testHitCounts.get(f));
        }
        
        // prepare the filter list
        BasicEventList unfiltered = new BasicEventList();
        unfiltered.addAll(elements);
        TextFilterList filtered = new TextFilterList(unfiltered, new CollectionTextFilterator());

        // track time
        System.out.println("");
        System.out.print("Running...");
        long startTime = System.currentTimeMillis();
        
        // perform the filters
        for(int i = 0; i < testFilters.size(); i++) {
            String filter = (String)testFilters.get(i);
            int expectedResult = ((Integer)testHitCounts.get(i)).intValue();
            filtered.getFilterEdit().setText(filter);
            if(filtered.size() != expectedResult) {
                System.out.println("expected size " + expectedResult + " != actual size " + filtered.size() + " for filter " + filter);
                for(int j = 0; j < filtered.size(); j++) {
                    System.out.println("" + j + ": " + filtered.get(j));
                }
                return;
            }
        }

        // print the total time
        long finishTime = System.currentTimeMillis();
        System.out.println(" done. Time: " + (finishTime - startTime) + "ms");
        System.out.println("");

        // perform the filters 1 char at a time (to simulate the user typing)
        System.out.println("Filter by the character");
        for(int i = 0; i < testFilters.size(); i++) {
            String filter = (String) testFilters.get(i);
            long totalFilteringTime = 0;
            long totalUnfilteringTime = 0;

            // simulate filter by the keystroke
            for (int j = 1; j <= filter.length(); j++) {
                String subFilter = filter.substring(0, j);

                startTime = System.currentTimeMillis();
                filtered.getFilterEdit().setText(subFilter);
                finishTime = System.currentTimeMillis();

                totalFilteringTime += (finishTime - startTime);
            }

            // simulate unfiltering by the keystroke
            for (int j = 1; j <= filter.length(); j++) {
                String subFilter = filter.substring(0, filter.length() - j);
                startTime = System.currentTimeMillis();
                filtered.getFilterEdit().setText(subFilter);
                finishTime = System.currentTimeMillis();

                totalUnfilteringTime += (finishTime - startTime);
            }
            System.out.println("  Filter " + i + ": " + filter + " completed filtering in " + totalFilteringTime + "ms and unfiltering in " + totalUnfilteringTime + "ms");
        }

    }
    
    /**
     * A TextFilterator for collections of Strings.
     */
    static class CollectionTextFilterator implements TextFilterator {
        public void getFilterStrings(List baseList, Object element) {
            baseList.addAll((Collection)element);
        }
    }
}
