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

    static final class SumFloat extends AbstractEventListCalculation<Float> {
        public SumFloat(EventList<? extends Number> source) {
            super(0f, source);
        }

        protected void inserted(Number element) { setValue(getValue() + element.floatValue()); }
        protected void deleted(Number element) { setValue(getValue() - element.floatValue()); }
        protected void updated(Number oldElement, Number newElement) { setValue(getValue() - oldElement.floatValue() + newElement.floatValue()); }
    }

    static final class SumDouble extends AbstractEventListCalculation<Double> {
        public SumDouble(EventList<? extends Number> source) {
            super(0d, source);
        }

        protected void inserted(Number element) { setValue(getValue() + element.doubleValue()); }
        protected void deleted(Number element) { setValue(getValue() - element.doubleValue()); }
        protected void updated(Number oldElement, Number newElement) { setValue(getValue() - oldElement.doubleValue() + newElement.doubleValue()); }
    }

    static final class SumInteger extends AbstractEventListCalculation<Integer> {
        public SumInteger(EventList<? extends Number> source) {
            super(0, source);
        }

        protected void inserted(Number element) { setValue(getValue() + element.intValue()); }
        protected void deleted(Number element) { setValue(getValue() - element.intValue()); }
        protected void updated(Number oldElement, Number newElement) { setValue(getValue() - oldElement.intValue() + newElement.intValue()); }
    }

    static final class SumLong extends AbstractEventListCalculation<Long> {
        public SumLong(EventList<? extends Number> source) {
            super(0L, source);
        }

        protected void inserted(Number element) { setValue(getValue() + element.longValue()); }
        protected void deleted(Number element) { setValue(getValue() - element.longValue()); }
        protected void updated(Number oldElement, Number newElement) { setValue(getValue() - oldElement.longValue() + newElement.longValue()); }
    }
}