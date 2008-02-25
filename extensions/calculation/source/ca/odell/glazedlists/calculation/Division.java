/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

/**
 * Divides a numerator from a Calculation by a denominator from another
 * Calculation to produce the division value in these composite Calculations.
 *
 * @author James Lemieux
 */
final class Division {

    static final class DivisionFloat extends AbstractCompositeCalculation<Float> {
        public DivisionFloat(Calculation<? extends Number> numerator, Calculation<? extends Number> denominator) {
            super(new Calculation[] {numerator, denominator});
        }

        protected Float recompute(Number[] inputs) {
            final float numerator = inputs[0].floatValue();
            final float denominator = inputs[1].floatValue();
            return new Float(numerator / denominator);
        }
    }

    static final class DivisionDouble extends AbstractCompositeCalculation<Double> {
        public DivisionDouble(Calculation<? extends Number> numerator, Calculation<? extends Number> denominator) {
            super(new Calculation[] {numerator, denominator});
        }

        protected Double recompute(Number[] inputs) {
            final double numerator = inputs[0].doubleValue();
            final double denominator = inputs[1].doubleValue();
            return new Double(numerator / denominator);
        }
    }
}