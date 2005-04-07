/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import java.util.*;
import java.io.*;
// the core Glazed Lists packages
import ca.odell.glazedlists.*;


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
        System.out.print("Reading input...");
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
        System.out.println(" done. " + elements.size() + " elements");
        in.close();

        // summarize what we've read
        for(int f = 0; f < testFilters.size(); f++) {
            System.out.println("Filter " + f + ": " + testFilters.get(f) + ", expect: " + testHitCounts.get(f) + " matches");
        }
        
        // prepare the filter list
        BasicEventList unfiltered = new BasicEventList();
        unfiltered.addAll(elements);
        TextMatcherEditor textMatcherEditor = new TextMatcherEditor(new CollectionTextFilterator());
        FilterList filtered = new FilterList(unfiltered, textMatcherEditor);
        
        // track time
        long startTime = 0;
        long finishTime = 0;
        
        System.out.println("");
        System.out.println("Full Filter");
        long fullFilterTime = 0;
        // perform the filters
        for(int i = 0; i < testFilters.size(); i++) {
            String filter = (String)testFilters.get(i);
            System.out.print("Filtering " + i + ", \"" + filter + "\"...");

            int expectedResult = ((Integer)testHitCounts.get(i)).intValue();
            startTime = System.currentTimeMillis();
            textMatcherEditor.setFilterText(filter.split("[ \t]"));
            finishTime = System.currentTimeMillis();
            long totalFilteringTime = (finishTime - startTime);
            fullFilterTime += totalFilteringTime;
            
            if(filtered.size() != expectedResult) {
                System.out.println("expected size " + expectedResult + " != actual size " + filtered.size() + " for filter " + filter);
                for(int j = 0; j < filtered.size(); j++) {
                    System.out.println("" + j + ": " + filtered.get(j));
                }
                return;
            }
            System.out.println(" done. Total: " + totalFilteringTime);
        }
        System.out.println("Total: " + fullFilterTime);

        System.out.println("");
        System.out.println("Character-by-character Filter");
        long characterFilterTime = 0;
        // perform the filters 1 char at a time (to simulate the user typing)
        for(int i = 0; i < testFilters.size(); i++) {
            String filter = (String)testFilters.get(i);
            long totalFilteringTime = 0;
            long totalUnfilteringTime = 0;
            System.out.print("Filtering " + i + ", \"" + filter + "\" by character...");

            // simulate filter by the keystroke
            for (int j = 1; j <= filter.length(); j++) {
                String subFilter = filter.substring(0, j);

                startTime = System.currentTimeMillis();
                textMatcherEditor.setFilterText(subFilter.split("[ \t]"));
                finishTime = System.currentTimeMillis();

                totalFilteringTime += (finishTime - startTime);
            }
            characterFilterTime += totalFilteringTime;
            
            // simulate unfiltering by the keystroke
            for (int j = 1; j <= filter.length(); j++) {
                String subFilter = filter.substring(0, filter.length() - j);
                startTime = System.currentTimeMillis();
                textMatcherEditor.setFilterText(subFilter.split("[ \t]"));
                finishTime = System.currentTimeMillis();

                totalUnfilteringTime += (finishTime - startTime);
            }
            characterFilterTime += totalUnfilteringTime;
            
            System.out.println(" done. Filter: " + totalFilteringTime + ", Unfilter: " + totalUnfilteringTime + ", Total: " + (totalFilteringTime + totalUnfilteringTime));
        }
        System.out.println("Total: " + characterFilterTime);
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
