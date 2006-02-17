/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.Date;
import java.util.Calendar;

import ca.odell.glazedlists.impl.GlazedListsImpl;

public class SequenceListTest extends TestCase {

    public void testAdd() {
        final EventList<Date> source = new BasicEventList<Date>();
        final SequenceList<Date> sequence = new SequenceList<Date>(source, Sequencers.monthSequencer());

        final Date apr = new Date(106, 3, 15);
        final Date may = new Date(106, 4, 15);
        final Date jun = new Date(106, 5, 15);
        final Date jul = new Date(106, 6, 15);
        final Date aug = new Date(106, 7, 15);
        final Date sep = new Date(106, 8, 15);

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
}