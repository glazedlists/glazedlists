/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import junit.framework.TestCase;

public class ZeroElementsTest extends TestCase {

    public void testPropertyChanges() {
        final EventList<String> source = new BasicEventList<String>();

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Boolean> zeroCount = Calculations.zeroElements(source);
        zeroCount.addPropertyChangeListener(counter);

        // check the initial Count state
        assertEquals(Boolean.TRUE, zeroCount.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add("a");
        assertEquals(Boolean.FALSE, zeroCount.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update
        source.set(0, "b");
        assertEquals(Boolean.FALSE, zeroCount.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(0);
        assertEquals(Boolean.TRUE, zeroCount.getValue());
        assertEquals(1, counter.getCountAndReset());
    }
}