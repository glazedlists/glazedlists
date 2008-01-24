/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import junit.framework.TestCase;

public class CountTest extends TestCase {

    public void testPropertyChanges() {
        final EventList<String> source = new BasicEventList<String>();
        source.add("a");

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Integer> count = Calculations.count(source);
        count.addPropertyChangeListener(counter);

        // check the initial Count state
        assertEquals(1, (int) count.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add("a");
        assertEquals(2, (int) count.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update
        source.set(1, "b");
        assertEquals(2, (int) count.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(1, (int) count.getValue());
        assertEquals(1, counter.getCountAndReset());
    }
}