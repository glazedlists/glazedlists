package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.impl.testing.AtLeastMatcherEditor;
import junit.framework.TestCase;

import java.util.Arrays;

public class WeakReferenceMatcherEditorTest extends TestCase {

    public void testWeakReferenceMatcherEditor() {
        final EventList<Integer> source = new BasicEventList<Integer>();
        source.add(new Integer(0));
        source.add(new Integer(5));
        source.add(new Integer(10));
        source.add(new Integer(20));

        final AtLeastMatcherEditor alme = new AtLeastMatcherEditor(10);
        final MatcherEditor<Number> wrme = Matchers.weakReferenceProxy(alme);
        final EventList<Integer> filtered = new FilterList<Integer>(source, wrme);

        assertEquals(Arrays.asList(new Integer[] {new Integer(10), new Integer(20)}), filtered);

        alme.setMinimum(5);
        assertEquals(Arrays.asList(new Integer[] {new Integer(5), new Integer(10), new Integer(20)}), filtered);
    }
}