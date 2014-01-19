/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.GlazedListsImpl;

import java.util.Calendar;
import java.util.Date;

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

    /**
     * This Sequencer produces a sequence of {@link Date} objects normalized
     * to the first millisecond of each month.
     */
    private static final class MonthSequencer implements SequenceList.Sequencer<Date> {
        /** A shared Calendar; it is assumed this Sequencer is only access from a single Thread. */
        private final Calendar cal = Calendar.getInstance();

        /**
         * The previous month in the sequence. For example:
         *
         * <ul>
         *   <li> previous(February 15, 2006 3:21:22.234) returns February 1, 2006 0:00:00.000
         *   <li> previous(February 1, 2006 0:00:00.000) returns January 1, 2006 0:00:00.000
         *   <li> previous(January 1, 2006 0:00:00.000) returns December 1, 2005 0:00:00.000
         * </ul>
         */
        @Override
        public Date previous(Date date) {
            if (date == null)
                throw new IllegalArgumentException("date may not be null");

            cal.setTime(date);

            // if cal is on the month boundary, rollback to the previous month
            if (GlazedListsImpl.isMonthStart(cal))
                cal.add(Calendar.MONTH, -1);

            // normalize the Date to the first millisecond of the month
            return GlazedListsImpl.getMonthStart(cal);
        }

        /**
         * The next month in the sequence. For example:
         *
         * <ul>
         *   <li> next(November 15, 2005 3:21:22.234) returns December 1, 2005 0:00:00.000
         *   <li> next(December 1, 2005 0:00:00.000) returns January 1, 2006 0:00:00.000
         *   <li> next(January 1, 2006 0:00:00.000) returns February 1, 2006 0:00:00.000
         * </ul>
         */
        @Override
        public Date next(Date date) {
            if (date == null)
                throw new IllegalArgumentException("date may not be null");

            cal.setTime(date);
            cal.add(Calendar.MONTH, 1);

            // normalize the Date to the first millisecond of the month
            return GlazedListsImpl.getMonthStart(cal);
        }
    }
}