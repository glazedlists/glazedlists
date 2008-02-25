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
final class Sum {

    static final class SumFloat<N extends Number> extends AbstractEventListCalculation<Float, N> {
        public SumFloat(EventList<N> source) {
            super(new Float(0), source);
        }

        protected void inserted(Number element) { setValue(new Float(getValue().floatValue() + element.floatValue())); }
        protected void deleted(Number element) { setValue(new Float(getValue().floatValue() - element.floatValue())); }
        protected void updated(Number oldElement, Number newElement) { setValue(new Float(getValue().floatValue() - oldElement.floatValue() + newElement.floatValue())); }
    }

    static final class SumDouble<N extends Number> extends AbstractEventListCalculation<Double, N> {
        public SumDouble(EventList<N> source) {
            super(new Double(0), source);
        }

        protected void inserted(Number element) { setValue(new Double(getValue().doubleValue() + element.doubleValue())); }
        protected void deleted(Number element) { setValue(new Double(getValue().doubleValue() - element.doubleValue())); }
        protected void updated(Number oldElement, Number newElement) { setValue(new Double(getValue().doubleValue() - oldElement.doubleValue() + newElement.doubleValue())); }
    }

    static final class SumInteger<N extends Number> extends AbstractEventListCalculation<Integer, N> {
        public SumInteger(EventList<N> source) {
            super(new Integer(0), source);
        }

        protected void inserted(Number element) { setValue(new Integer(getValue().intValue() + element.intValue())); }
        protected void deleted(Number element) { setValue(new Integer(getValue().intValue() - element.intValue())); }
        protected void updated(Number oldElement, Number newElement) { setValue(new Integer(getValue().intValue() - oldElement.intValue() + newElement.intValue())); }
    }

    static final class SumLong<N extends Number> extends AbstractEventListCalculation<Long, N> {
        public SumLong(EventList<N> source) {
            super(new Long(0), source);
        }

        protected void inserted(Number element) { setValue(new Long(getValue().longValue() + element.longValue())); }
        protected void deleted(Number element) { setValue(new Long(getValue().longValue() - element.longValue())); }
        protected void updated(Number oldElement, Number newElement) { setValue(new Long(getValue().longValue() - oldElement.longValue() + newElement.longValue())); }
    }
}