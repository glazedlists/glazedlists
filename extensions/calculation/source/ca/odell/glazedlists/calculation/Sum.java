/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;

/**
 * Reports the sum total of the numeric elements within the backing EventList
 * as the value of these Calculations.
 *
 * @author James Lemieux
 */
public final class Sum {

    public static final class SumFloat extends AbstractEventListCalculation<Float, Float> {
        public SumFloat(EventList<Float> source) {
            super(0f, source);
        }

        protected void inserted(Float element) { setValue(getValue() + element); }
        protected void deleted(Float element) { setValue(getValue() - element); }
        protected void updated(Float oldElement, Float newElement) { setValue(getValue() - oldElement + newElement); }
    }

    public static final class SumDouble extends AbstractEventListCalculation<Double, Double> {
        public SumDouble(EventList<Double> source) {
            super(0d, source);
        }

        protected void inserted(Double element) { setValue(getValue() + element); }
        protected void deleted(Double element) { setValue(getValue() - element); }
        protected void updated(Double oldElement, Double newElement) { setValue(getValue() - oldElement + newElement); }
    }

    public static final class SumInteger extends AbstractEventListCalculation<Integer, Integer> {
        public SumInteger(EventList<Integer> source) {
            super(0, source);
        }

        protected void inserted(Integer element) { setValue(getValue() + element); }
        protected void deleted(Integer element) { setValue(getValue() - element); }
        protected void updated(Integer oldElement, Integer newElement) { setValue(getValue() - oldElement + newElement); }
    }

    public static final class SumLong extends AbstractEventListCalculation<Long, Long> {
        public SumLong(EventList<Long> source) {
            super(0L, source);
        }

        protected void inserted(Long element) { setValue(getValue() + element); }
        protected void deleted(Long element) { setValue(getValue() - element); }
        protected void updated(Long oldElement, Long newElement) { setValue(getValue() - oldElement + newElement); }
    }
}