/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.*;

public final class SumTest {

    @Test
    public void testSumFloat() {
        final EventList<Float> source = new BasicEventList<>();
        source.add(Float.valueOf(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Float> sum = Calculations.sumFloats(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(Float.valueOf(1), sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(Float.valueOf(2));
        assertEquals(Float.valueOf(3), sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, Float.valueOf(3));
        assertEquals(Float.valueOf(4), sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(Float.valueOf(4), sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(Float.valueOf(1), sum.getValue());
        assertEquals(1, counter.getCountAndReset());
    }

    @Test
    public void testSumDouble() {
        final EventList<Double> source = new BasicEventList<>();
        source.add(Double.valueOf(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Double> sum = Calculations.sumDoubles(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(Double.valueOf(1), sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(Double.valueOf(2));
        assertEquals(Double.valueOf(3), sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, Double.valueOf(3));
        assertEquals(Double.valueOf(4), sum.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(Double.valueOf(4), sum.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(Double.valueOf(1), sum.getValue());
        assertEquals(1, counter.getCountAndReset());
    }

    @Test
    public void testSumInteger() {
        final EventList<Integer> source = new BasicEventList<>();
        source.add(Integer.valueOf(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Integer> sum = Calculations.sumIntegers(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(1, sum.getValue().intValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(Integer.valueOf(2));
        assertEquals(3, sum.getValue().intValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, Integer.valueOf(3));
        assertEquals(4, sum.getValue().intValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(4, sum.getValue().intValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(1, sum.getValue().intValue());
        assertEquals(1, counter.getCountAndReset());
    }

    @Test
    public void testSumLong() {
        final EventList<Long> source = new BasicEventList<>();
        source.add(Long.valueOf(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Long> sum = Calculations.sumLongs(source);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(1L, sum.getValue().longValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(Long.valueOf(2));
        assertEquals(3L, sum.getValue().longValue());
        assertEquals(1, counter.getCountAndReset());

        // test update with sum change
        source.set(1, Long.valueOf(3));
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

    @Test
    public void testReorderEvents() {
        final EventList<Long> source = new BasicEventList<>();
        final SortedList<Long> sortedSource = new SortedList<>(source);
        source.add(Long.valueOf(1));
        source.add(Long.valueOf(2));
        source.add(Long.valueOf(3));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Long> sum = Calculations.sumLongs(sortedSource);
        sum.addPropertyChangeListener(counter);

        // check the initial Sum state
        assertEquals(Arrays.asList(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3)), sortedSource);
        assertEquals(6L, sum.getValue().longValue());
        assertEquals(0, counter.getCountAndReset());

        // resort the source
        sortedSource.setComparator(GlazedLists.reverseComparator());
        assertEquals(Arrays.asList(Long.valueOf(3), Long.valueOf(2), Long.valueOf(1)), sortedSource);
        assertEquals(6L, sum.getValue().longValue());
        assertEquals(0, counter.getCountAndReset());

        // deleted the first sorted element
        sortedSource.remove(0);
        assertEquals(Arrays.asList(Long.valueOf(2), Long.valueOf(1)), sortedSource);
        assertEquals(3L, sum.getValue().longValue());
        assertEquals(1, counter.getCountAndReset());

        // add a new the first sorted element
        sortedSource.add(Long.valueOf(10));
        assertEquals(Arrays.asList(Long.valueOf(10), Long.valueOf(2), Long.valueOf(1)), sortedSource);
        assertEquals(13L, sum.getValue().longValue());
        assertEquals(1, counter.getCountAndReset());
    }
}
