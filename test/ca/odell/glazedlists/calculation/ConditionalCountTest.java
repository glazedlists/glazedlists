/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConditionalCountTest {

    @Test
    public void testPropertyChanges() {
        final EventList<String> source = new BasicEventList<String>();
        final Matcher<String> aMatcher = Matchers.beanPropertyMatcher(String.class, "this", "a");
        source.add("a");
        source.add("b");

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Integer> count = Calculations.count(source, aMatcher);
        count.addPropertyChangeListener(counter);

        // check the initial Count state
        assertEquals(1, count.getValue().intValue());
        assertEquals(0, counter.getCountAndReset());

        // test add an A
        source.add("a");
        assertEquals(2, count.getValue().intValue());
        assertEquals(1, counter.getCountAndReset());

        // test add a B
        source.add("b");
        assertEquals(2, count.getValue().intValue());
        assertEquals(0, counter.getCountAndReset());

        // test update A to B
        source.set(0, "b");
        assertEquals(1, count.getValue().intValue());
        assertEquals(1, counter.getCountAndReset());

        // test update B to A
        source.set(0, "a");
        assertEquals(2, count.getValue().intValue());
        assertEquals(1, counter.getCountAndReset());

        // test remove A
        source.remove(0);
        assertEquals(1, count.getValue().intValue());
        assertEquals(1, counter.getCountAndReset());

        // test remove B
        source.remove(0);
        assertEquals(1, count.getValue().intValue());
        assertEquals(0, counter.getCountAndReset());
    }
}
