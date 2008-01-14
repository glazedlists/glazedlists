/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;
import junit.framework.TestCase;

public final class SumTest extends TestCase {

    public void testSumFloat() {
        final EventList<Float> source = new BasicEventList<Float>();
        source.add(1f);

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Sum.SumFloat sum = new Sum.SumFloat(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(1f, sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(2f);
        assertEquals(3f, sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, 3f);
        assertEquals(4f, sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(4f, sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(1f, sum.getValue());
        assertEquals(1, counter.getCountAndReset());
    }

    public void testSumDouble() {
        final EventList<Double> source = new BasicEventList<Double>();
        source.add(1d);

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Sum.SumDouble sum = new Sum.SumDouble(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(1d, sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(2d);
        assertEquals(3d, sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, 3d);
        assertEquals(4d, sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(4d, sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(1d, sum.getValue());
        assertEquals(1, counter.getCountAndReset());
    }

    public void testSumInteger() {
        final EventList<Integer> source = new BasicEventList<Integer>();
        source.add(1);

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Sum.SumInteger sum = new Sum.SumInteger(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(1, (int) sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(2);
        assertEquals(3, (int) sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, 3);
        assertEquals(4, (int) sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(4, (int) sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(1, (int) sum.getValue());
        assertEquals(1, counter.getCountAndReset());
    }

    public void testSumLong() {
        final EventList<Long> source = new BasicEventList<Long>();
        source.add(1L);

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Sum.SumLong sum = new Sum.SumLong(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(1L, (long) sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(2L);
        assertEquals(3L, (long) sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, 3L);
        assertEquals(4L, (long) sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(4L, (long) sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(1L, (long) sum.getValue());
        assertEquals(1, counter.getCountAndReset());
    }
}