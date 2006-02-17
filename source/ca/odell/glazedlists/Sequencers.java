/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.GlazedListsImpl;

import java.util.Date;
import java.util.Calendar;

/**
 * A factory for creating Sequencers.
 *
 * @author James Lemieux
 */
public final class Sequencers {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private Sequencers() {
        throw new UnsupportedOperationException();
    }

    // Sequencers // // // // // // // // // // // // // // // // // // // // //

    public static SequenceList.Sequencer<Date> monthSequencer() {
        return new MonthSequencer();
    }

    private static final class MonthSequencer implements SequenceList.Sequencer<Date> {
        private final Calendar cal = Calendar.getInstance();

        public Date previous(Date date) {
            cal.setTime(date);

            if (GlazedListsImpl.isBeginningOfMonth(cal))
                cal.add(Calendar.MONTH, -1);

            return GlazedListsImpl.getMonthBegin(cal);
        }

        public Date next(Date date) {
            cal.setTime(date);
            cal.add(Calendar.MONTH, 1);

            return GlazedListsImpl.getMonthBegin(cal);
        }
    }
}