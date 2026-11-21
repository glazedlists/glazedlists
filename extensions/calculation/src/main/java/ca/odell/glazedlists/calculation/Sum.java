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
            super(Float.valueOf(0), source);
        }

        @Override
        protected void inserted(Number element) { setValue(Float.valueOf(getValue().floatValue() + element.floatValue())); }
        @Override
        protected void deleted(Number element) { setValue(Float.valueOf(getValue().floatValue() - element.floatValue())); }
        @Override
        protected void updated(Number oldElement, Number newElement) { setValue(Float.valueOf(getValue().floatValue() - oldElement.floatValue() + newElement.floatValue())); }
    }

    static final class SumDouble<N extends Number> extends AbstractEventListCalculation<Double, N> {
        public SumDouble(EventList<N> source) {
            super(Double.valueOf(0), source);
        }

        @Override
        protected void inserted(Number element) { setValue(Double.valueOf(getValue().doubleValue() + element.doubleValue())); }
        @Override
        protected void deleted(Number element) { setValue(Double.valueOf(getValue().doubleValue() - element.doubleValue())); }
        @Override
        protected void updated(Number oldElement, Number newElement) { setValue(Double.valueOf(getValue().doubleValue() - oldElement.doubleValue() + newElement.doubleValue())); }
    }

    static final class SumInteger<N extends Number> extends AbstractEventListCalculation<Integer, N> {
        public SumInteger(EventList<N> source) {
            super(Integer.valueOf(0), source);
        }

        @Override
        protected void inserted(Number element) { setValue(Integer.valueOf(getValue().intValue() + element.intValue())); }
        @Override
        protected void deleted(Number element) { setValue(Integer.valueOf(getValue().intValue() - element.intValue())); }
        @Override
        protected void updated(Number oldElement, Number newElement) { setValue(Integer.valueOf(getValue().intValue() - oldElement.intValue() + newElement.intValue())); }
    }

    static final class SumLong<N extends Number> extends AbstractEventListCalculation<Long, N> {
        public SumLong(EventList<N> source) {
            super(Long.valueOf(0), source);
        }

        @Override
        protected void inserted(Number element) { setValue(Long.valueOf(getValue().longValue() + element.longValue())); }
        @Override
        protected void deleted(Number element) { setValue(Long.valueOf(getValue().longValue() - element.longValue())); }
        @Override
        protected void updated(Number oldElement, Number newElement) { setValue(Long.valueOf(getValue().longValue() - oldElement.longValue() + newElement.longValue())); }
    }
}