/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.impl.testing.AtLeastMatcherEditor;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.*;

public class WeakReferenceMatcherEditorTest {

    @Test
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
