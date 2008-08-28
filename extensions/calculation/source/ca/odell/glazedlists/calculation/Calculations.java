/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.jfreechart.CalculationCategoryDataset;

import java.util.Arrays;
import java.util.List;

public final class Calculations {

    private Calculations() {}

    /** A Calculation that reports the number of <code>elements</code> as an Integer. */
    public static Calculation<Integer> count(EventList elements) { return new Count(elements); }

    //
    // Sum
    //

    /** A Calculation that sums the given <code>numbers</code> as a Float. */
    public static Calculation<Float> sumFloats(EventList<? extends Number> numbers) { return new Sum.SumFloat(numbers); }

    /** A Calculation that sums the given <code>numbers</code> as a Double. */
    public static Calculation<Double> sumDoubles(EventList<? extends Number> numbers) { return new Sum.SumDouble(numbers); }

    /** A Calculation that sums the given <code>numbers</code> as an Integer. */
    public static Calculation<Integer> sumIntegers(EventList<? extends Number> numbers) { return new Sum.SumInteger(numbers); }

    /** A Calculation that sums the given <code>numbers</code> as a Long. */
    public static Calculation<Long> sumLongs(EventList<? extends Number> numbers) { return new Sum.SumLong(numbers); }

    //
    // Division
    //

    /** A Calculation that divides the <code>numerator</code> by the <code>denominator</code> as Floats. */
    public static Calculation<Float> divideFloats(Calculation<? extends Number> numerator, Calculation<? extends Number> denominator) { return new Division.DivisionFloat(numerator, denominator); }

    /** A Calculation that divides the <code>numerator</code> by the <code>denominator</code> as Doubles. */
    public static Calculation<Double> divideDoubles(Calculation<? extends Number> numerator, Calculation<? extends Number> denominator) { return new Division.DivisionDouble(numerator, denominator); }

    //
    // Mean Average
    //

    /** A Calculation that reports the mean average of all the <code>numbers</code> as a Float. */
    public static Calculation<Float> meanFloats(EventList<? extends Number> numbers) { return divideFloats(sumFloats(numbers), count(numbers)); }

    /** A Calculation that reports the mean average of all the <code>numbers</code> as a Double. */
    public static Calculation<Double> meanDoubles(EventList<? extends Number> numbers) { return divideDoubles(sumDoubles(numbers), count(numbers)); }

    //
    // Datasets
    //

    /** A CategoryDataset backed by the given <code>calculations</code>; each Calculation is a single-valued series in the CategoryDataset */
    public static CalculationCategoryDataset calculationCategoryDataset(Calculation<? extends Number>... calculations) {
        final CalculationCategoryDataset ccd = new CalculationCategoryDataset();
        ccd.getCalculations().addAll(Arrays.asList(calculations));
        return ccd;
    }
}