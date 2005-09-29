/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.Comparator;

/**
 * Verifies that ListEventAssembler is well behaved.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListEventAssemblerPerformance {

    /** the maximum amount of memory used thus far */
    private long highestMemoryUsage = 0;

    /**
     * Validates that the ListEventAssember cleans up its own garbage.
     */
    public void testMemoryUsage() {
        BasicEventList<Long> list = new BasicEventList<Long>();
        SortedList<Long> sorted = new SortedList<Long>(list);
        for(long i = 0; i < 100000000; i++) {
            if(i % 10000 == 0) {
                list.clear();
                reportMemory(i/10000);
            } else {
                list.add(new Long(i));
                if(i % 1000 == 0) {
                    sorted.setComparator((Comparator)GlazedLists.reverseComparator());
                } else if(i % 1000 == 1) {
                    sorted.setComparator(null);
                }
            }
        }
    }

    /**
     * Report how much memory is currently being used by the application.
     */
    private void reportMemory(long time) {
        long currentMemoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if(currentMemoryUsage > highestMemoryUsage) {
            System.out.println("");
            System.out.println(time + ": MEMORY USAGE: " + (currentMemoryUsage / (1024*1024)) + "M");
            highestMemoryUsage = currentMemoryUsage;
        } else {
            System.out.print(time + ", ");
        }
    }

    /**
     * Run the tests.
     */
    public static void main(String[] args) {
        new ListEventAssemblerPerformance().testMemoryUsage();
    }
}
