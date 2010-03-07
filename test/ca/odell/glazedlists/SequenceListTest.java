/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.SequenceList.Sequencer;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

import java.util.Arrays;
import java.util.Date;

import junit.framework.TestCase;

public class SequenceListTest extends TestCase {

    private static final Date apr = GlazedListsTests.createDate(2006, 3, 15);
    private static final Date may = GlazedListsTests.createDate(2006, 4, 15);
    private static final Date jun = GlazedListsTests.createDate(2006, 5, 15);
    private static final Date jul = GlazedListsTests.createDate(2006, 6, 15);
    private static final Date aug = GlazedListsTests.createDate(2006, 7, 15);
    private static final Date sep = GlazedListsTests.createDate(2006, 8, 15);

    public void testConstructor() {
        try {
            new SequenceList<Date>(new BasicEventList<Date>(), null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new SequenceList<Date>(new BasicEventList<Date>(), null, GlazedLists.comparableComparator());
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new SequenceList<Date>(new BasicEventList<Date>(), Sequencers.monthSequencer(), null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        // valid constructor arguments
        new SequenceList<Date>(new BasicEventList<Date>(), Sequencers.monthSequencer());
        new SequenceList<Date>(new BasicEventList<Date>(), Sequencers.monthSequencer(), GlazedLists.comparableComparator());

        // construct a SequenceList with a populated source list
        final EventList<Date> source = new BasicEventList<Date>();
        source.add(apr);
        source.add(aug);
        final SequenceList<Date> sequence = new SequenceList<Date>(source, Sequencers.monthSequencer());
        assertEquals(6, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(apr), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(may), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(3));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(4));
        assertEquals(GlazedListsImpl.getMonthBegin(sep), sequence.get(5));
    }

    public void testAdd() {
        final EventList<Date> source = new BasicEventList<Date>();
        final SequenceList<Date> sequence = new SequenceList<Date>(source, Sequencers.monthSequencer());
        ListConsistencyListener.install(sequence);

        source.add(jun);
        assertEquals(2, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(1));

        source.add(aug);
        // jul was inferred by the addition of aug
        assertEquals(4, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(sep), sequence.get(3));

        source.add(apr);
        // apr was inferred by the addition of apr
        assertEquals(6, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(apr), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(may), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(3));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(4));
        assertEquals(GlazedListsImpl.getMonthBegin(sep), sequence.get(5));

        // none of these additions should change the sequence
        source.add(apr);
        source.add(may);
        source.add(jun);
        source.add(jul);
        source.add(aug);
        assertEquals(6, sequence.size());
    }

    public void testRemove() {
        final EventList<Date> source = new BasicEventList<Date>();
        final SequenceList<Date> sequence = new SequenceList<Date>(source, Sequencers.monthSequencer());
        ListConsistencyListener.install(sequence);

        source.add(apr);
        source.add(jun);
        source.add(aug);
        assertEquals(6, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(apr), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(may), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(3));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(4));
        assertEquals(GlazedListsImpl.getMonthBegin(sep), sequence.get(5));

        // remove from the beginning
        source.remove(apr);
        assertEquals(4, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(sep), sequence.get(3));

        // remove from the end
        source.remove(aug);
        assertEquals(2, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(1));

        // remove the last element
        source.remove(jun);
        assertEquals(0, sequence.size());

        source.add(apr);
        source.add(jun);
        source.add(aug);
        assertEquals(6, sequence.size());

        // remove from the middle
        source.remove(jun);
        assertEquals(6, sequence.size());

        // remove all elements
        source.clear();
        assertEquals(0, sequence.size());
    }

    public void testSet() {
        final EventList<Date> source = new BasicEventList<Date>();
        final SequenceList<Date> sequence = new SequenceList<Date>(source, Sequencers.monthSequencer());
        ListConsistencyListener.install(sequence);

        source.add(apr);
        source.add(jun);
        source.add(aug);
        assertEquals(6, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(apr), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(may), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(3));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(4));
        assertEquals(GlazedListsImpl.getMonthBegin(sep), sequence.get(5));

        // set at the beginning reduces sequence
        source.set(0, may);
        assertEquals(5, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(may), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(3));
        assertEquals(GlazedListsImpl.getMonthBegin(sep), sequence.get(4));

        // set at the beginning increases sequence
        source.set(0, apr);
        assertEquals(6, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(apr), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(may), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(3));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(4));
        assertEquals(GlazedListsImpl.getMonthBegin(sep), sequence.get(5));

        // set at the end reduces sequence
        source.set(source.size()-1, jul);
        assertEquals(5, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(apr), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(may), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(3));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(4));

        // set at the end increases sequence
        source.set(source.size()-1, aug);
        assertEquals(6, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(apr), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(may), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(3));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(4));
        assertEquals(GlazedListsImpl.getMonthBegin(sep), sequence.get(5));

        // set in the middle changes nothing about the sequence
        source.set(1, may);
        assertEquals(6, sequence.size());
        assertEquals(GlazedListsImpl.getMonthBegin(apr), sequence.get(0));
        assertEquals(GlazedListsImpl.getMonthBegin(may), sequence.get(1));
        assertEquals(GlazedListsImpl.getMonthBegin(jun), sequence.get(2));
        assertEquals(GlazedListsImpl.getMonthBegin(jul), sequence.get(3));
        assertEquals(GlazedListsImpl.getMonthBegin(aug), sequence.get(4));
        assertEquals(GlazedListsImpl.getMonthBegin(sep), sequence.get(5));
    }

    public void testIssue482_FixMe() {
        EventList<Integer> src = new BasicEventList<Integer>();
        src.add(1);
        src.add(3);

        SequenceList<Integer> integers = new SequenceList<Integer>(src,
                new Sequencer<Integer>() {
                    public Integer next(Integer value) {
                        return value + 1;
                    }

                    public Integer previous(Integer value) {
                        return value - 1;
                    }
                });

        assertEquals(Arrays.asList(1, 2, 3), integers);
    }

}