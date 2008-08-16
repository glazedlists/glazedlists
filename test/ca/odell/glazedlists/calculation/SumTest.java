/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.GlazedLists;
import junit.framework.TestCase;

import java.util.Arrays;

public final class SumTest extends TestCase {

    public void testSumFloat() {
        final EventList<Float> source = new BasicEventList<Float>();
        source.add(new Float(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Float> sum = Calculations.sumFloats(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(new Float(1), sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(new Float(2));
        assertEquals(new Float(3), sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, new Float(3));
        assertEquals(new Float(4), sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(new Float(4), sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(new Float(1), sum.getValue());
        assertEquals(1, counter.getCountAndReset());
    }

    public void testSumDouble() {
        final EventList<Double> source = new BasicEventList<Double>();
        source.add(new Double(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Double> sum = Calculations.sumDoubles(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(new Double(1), sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(new Double(2));
        assertEquals(new Double(3), sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, new Double(3));
        assertEquals(new Double(4), sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(new Double(4), sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(new Double(1), sum.getValue());
        assertEquals(1, counter.getCountAndReset());
    }

    public void testSumInteger() {
        final EventList<Integer> source = new BasicEventList<Integer>();
        source.add(new Integer(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Integer> sum = Calculations.sumIntegers(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(1, sum.getValue().intValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(new Integer(2));
        assertEquals(3, sum.getValue().intValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, new Integer(3));
        assertEquals(4, sum.getValue().intValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(4, sum.getValue().intValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(1, (int) sum.getValue().intValue());
        assertEquals(1, counter.getCountAndReset());
    }

    public void testSumLong() {
        final EventList<Long> source = new BasicEventList<Long>();
        source.add(new Long(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Long> sum = Calculations.sumLongs(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(1L, sum.getValue().longValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(new Long(2));
        assertEquals(3L, sum.getValue().longValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, new Long(3));
        assertEquals(4L, sum.getValue().longValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(4L, sum.getValue().longValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(1L, sum.getValue().longValue());
        assertEquals(1, counter.getCountAndReset());
    }

    public void testReorderEvents() {
        final EventList<Long> source = new BasicEventList<Long>();
        final SortedList<Long> sortedSource = new SortedList<Long>(source);
        source.add(new Long(1));
        source.add(new Long(2));
        source.add(new Long(3));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Long> sum = Calculations.sumLongs(sortedSource);
        sum.addPropertyChangeListener(counter);
        
        // check the initial Sum state
        assertEquals(Arrays.asList(new Long[] {new Long(1), new Long(2), new Long(3)}), sortedSource);
        assertEquals(6L, sum.getValue().longValue());
        assertEquals(0, counter.getCountAndReset());

        // resort the source
        sortedSource.setComparator(GlazedLists.reverseComparator());
        assertEquals(Arrays.asList(new Long[] {new Long(3), new Long(2), new Long(1)}), sortedSource);
        assertEquals(6L, sum.getValue().longValue());
        assertEquals(0, counter.getCountAndReset());

        // deleted the first sorted element
        sortedSource.remove(0);
        assertEquals(Arrays.asList(new Long[] {new Long(2), new Long(1)}), sortedSource);
        assertEquals(3L, sum.getValue().longValue());
        assertEquals(1, counter.getCountAndReset());

        // add a new the first sorted element
        sortedSource.add(new Long(10));
        assertEquals(Arrays.asList(new Long[] {new Long(10), new Long(2), new Long(1)}), sortedSource);
        assertEquals(13L, sum.getValue().longValue());
        assertEquals(1, counter.getCountAndReset());
    }
}