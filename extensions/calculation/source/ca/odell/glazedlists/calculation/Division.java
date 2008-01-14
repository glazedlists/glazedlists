/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

/**
 * Divides a numerator from another Calculation by a denominator from another
 * Calculation to produce the division value in these composite Calculations.
 *
 * @author James Lemieux
 */
public final class Division {

    public static final class DivisionFloat extends AbstractCompositeCalculation<Float> {
        public DivisionFloat(Calculation<Float> numerator, Calculation<Integer> denominator) {
            super(numerator, denominator);
        }

        protected Float recompute(Number[] inputs) {
            final float numerator = inputs[0].floatValue();
            final float denominator = inputs[1].floatValue();
            return denominator == 0f ? Float.NaN : (numerator / denominator);
        }
    }

    public static final class DivisionDouble extends AbstractCompositeCalculation<Double> {
        public DivisionDouble(Calculation<Double> numerator, Calculation<Integer> denominator) {
            super(numerator, denominator);
        }

        protected Double recompute(Number[] inputs) {
            final double numerator = inputs[0].doubleValue();
            final double denominator = inputs[1].doubleValue();
            return denominator == 0d ? Double.NaN : (numerator / denominator);
        }
    }
}