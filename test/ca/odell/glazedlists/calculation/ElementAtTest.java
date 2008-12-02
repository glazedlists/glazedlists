/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import junit.framework.TestCase;

public class ElementAtTest extends TestCase {

    public void testPropertyChanges() throws Exception {
        final EventList<String> source = new BasicEventList<String>();
        
        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<String> elementAt = Calculations.elementAt(source, 0, "default");
        elementAt.addPropertyChangeListener(counter);

        // check the initial Count state
        assertEquals("default", elementAt.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add (list is now [a])
        source.add("a");
        assertEquals("a", elementAt.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test add 2nd element (list is now [a, b])
        source.add("b");
        assertEquals("a", elementAt.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test update (list is now [c, b])
        source.set(0, "c");
        assertEquals("c", elementAt.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update, non-index (list is now [c, d])
        source.set(1, "d");
        assertEquals("c", elementAt.getValue());
        assertEquals(0, counter.getCountAndReset());
        
        // test remove (list is now [d])
        source.remove(0);
        assertEquals("d", elementAt.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test remove last element (list is now [])
        source.remove(0);
        assertEquals("default", elementAt.getValue());
        assertEquals(1, counter.getCountAndReset());
    }
}