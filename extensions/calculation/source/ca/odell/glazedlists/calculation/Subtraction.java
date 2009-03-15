/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

/**
 * Subtracts a value from a Calculation by a value from another
 * Calculation to produce the subtraction value in these composite Calculations.
 *
 * @author James Lemieux
 */
final class Subtraction {

    static final class SubtractionFloat extends AbstractCompositeCalculation<Float> {
        public SubtractionFloat(Calculation<? extends Number> a, Calculation<? extends Number> b) {
            super(new Calculation[] {a, b});
        }

        @Override
        protected Float recompute(Number[] inputs) {
            final float a = inputs[0].floatValue();
            final float b = inputs[1].floatValue();
            return new Float(a - b);
        }
    }

    static final class SubtractionDouble extends AbstractCompositeCalculation<Double> {
        public SubtractionDouble(Calculation<? extends Number> a, Calculation<? extends Number> b) {
            super(new Calculation[] {a, b});
        }

        @Override
        protected Double recompute(Number[] inputs) {
            final double a = inputs[0].doubleValue();
            final double b = inputs[1].doubleValue();
            return new Double(a - b);
        }
    }

    static final class SubtractionInteger extends AbstractCompositeCalculation<Integer> {
        public SubtractionInteger(Calculation<? extends Number> a, Calculation<? extends Number> b) {
            super(new Calculation[] {a, b});
        }

        @Override
        protected Integer recompute(Number[] inputs) {
            final int a = inputs[0].intValue();
            final int b = inputs[1].intValue();
            return new Integer(a - b);
        }
    }

    static final class SubtractionLong extends AbstractCompositeCalculation<Long> {
        public SubtractionLong(Calculation<? extends Number> a, Calculation<? extends Number> b) {
            super(new Calculation[] {a, b});
        }

        @Override
        protected Long recompute(Number[] inputs) {
            final long a = inputs[0].longValue();
            final long b = inputs[1].longValue();
            return new Long(a - b);
        }
    }
}