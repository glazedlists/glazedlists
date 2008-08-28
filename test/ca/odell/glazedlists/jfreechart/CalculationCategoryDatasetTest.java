/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.jfreechart;

import junit.framework.TestCase;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.calculation.Calculations;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

public class CalculationCategoryDatasetTest extends TestCase {

    public void testFireChangesAppropriately() {
        final EventList<Number> numbers = new BasicEventList<Number>();

        final DatasetChangeEventCounter counter = new DatasetChangeEventCounter();
        final CalculationCategoryDataset dataset = new CalculationCategoryDataset();
        dataset.addChangeListener(counter);
        assertEquals(0, dataset.getRowCount());
        assertEquals(0, counter.getCountAndReset());

        // adding calculation produces an event
        final Calculation<Integer> intSum = Calculations.sumIntegers(numbers);
        dataset.getCalculations().add(intSum);
        assertEquals(1, dataset.getRowCount());
        assertNull(dataset.getRowKey(0));
        assertEquals(0, dataset.getValue(0, 0).intValue());
        assertEquals(1, counter.getCountAndReset());

        // changing a calculation produces an event
        numbers.add(new Integer(2));
        assertEquals(1, dataset.getRowCount());
        assertNull(dataset.getRowKey(0));
        assertEquals(2, dataset.getValue(0, 0).intValue());
        assertEquals(1, counter.getCountAndReset());

        // changing the name of the calculation produces an event
        intSum.setName("Int Sum");
        assertEquals(1, dataset.getRowCount());
        assertEquals("Int Sum", dataset.getRowKey(0));
        assertEquals(2, dataset.getValue(0, 0).intValue());
        assertEquals(1, counter.getCountAndReset());

        // removing calculation produces an event
        dataset.getCalculations().remove(intSum);
        assertEquals(0, dataset.getRowCount());
        assertEquals(1, counter.getCountAndReset());
    }

    private static final class DatasetChangeEventCounter implements DatasetChangeListener {
        private int count;

        public void datasetChanged(DatasetChangeEvent event) {
            count++;
        }

        public int getCountAndReset() {
            final int toReturn = count;
            count = 0;
            return toReturn;
        }
    }
}