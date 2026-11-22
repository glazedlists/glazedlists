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
        final EventList<Integer> source = new BasicEventList<>();
        source.add(Integer.valueOf(0));
        source.add(Integer.valueOf(5));
        source.add(Integer.valueOf(10));
        source.add(Integer.valueOf(20));

        final AtLeastMatcherEditor alme = new AtLeastMatcherEditor(10);
        final MatcherEditor<Number> wrme = Matchers.weakReferenceProxy(alme);
        final EventList<Integer> filtered = new FilterList<>(source, wrme);

        assertEquals(Arrays.asList(new Integer[] {Integer.valueOf(10), Integer.valueOf(20)}), filtered);

        alme.setMinimum(5);
        assertEquals(Arrays.asList(new Integer[] {Integer.valueOf(5), Integer.valueOf(10), Integer.valueOf(20)}), filtered);
    }
}
