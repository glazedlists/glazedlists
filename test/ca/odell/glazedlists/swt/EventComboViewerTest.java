/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;

/**
 * Tests for the {@link EventComboViewer}.
 *
 * @author hbrands
 */
public class EventComboViewerTest extends SwtTestCase {

	/**
	 * Tests constructor with empty source list.
	 */
	public void guiTestEmptyConstruction() {
        final BasicEventList<String> source = new BasicEventList<String>();
        final Combo combo = new Combo(getShell(), SWT.DROP_DOWN);
        final EventComboViewer<String> viewer = new EventComboViewer<String>(source, combo);
        assertSame(combo, viewer.getCombo());
        assertEquals(0, combo.getItemCount());
        assertTrue(Arrays.equals(new String[0], combo.getItems()));
	}

	/**
	 * Tests constructor with non-empty source list.
	 */
	public void guiTestPrefilledConstruction() {
		final BasicEventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        final Combo combo = new Combo(getShell(), SWT.DROP_DOWN);
        final EventComboViewer<String> viewer = new EventComboViewer<String>(source, combo);
        assertSame(combo, viewer.getCombo());
        assertEquals(source.size(), combo.getItemCount());
        assertTrue(Arrays.equals(source.toArray(new String[source.size()]), combo.getItems()));
        assertEquals("A", combo.getItem(0));
        assertEquals("B", combo.getItem(1));
        assertEquals("C", combo.getItem(2));
        assertEquals("D", combo.getItem(3));
        assertEquals("E", combo.getItem(4));
        assertEquals("F", combo.getItem(5));
	}

	/**
	 * Tests, that list chnages are reflected in the combo box.
	 */
	public void guiTestChangeList() {
        final BasicEventList<String> source = new BasicEventList<String>();
        final Combo combo = new Combo(getShell(), SWT.DROP_DOWN);
        final EventComboViewer<String> viewer = new EventComboViewer<String>(source, combo);
        source.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        assertSame(combo, viewer.getCombo());
        assertEquals(source.size(), combo.getItemCount());
        assertTrue(Arrays.equals(source.toArray(new String[source.size()]), combo.getItems()));
        source.clear();
        assertEquals(0, combo.getItemCount());
        assertTrue(Arrays.equals(new String[0], combo.getItems()));
        source.add("B");
        source.add("D");
        source.add(0, "A");
        source.add(2, "C");
        assertEquals(GlazedListsTests.delimitedStringToList("A B C D"), source);
        assertEquals(source.size(), combo.getItemCount());
        assertTrue(Arrays.equals(source.toArray(new String[source.size()]), combo.getItems()));
        source.addAll(GlazedListsTests.delimitedStringToList("E F"));
        source.removeAll(GlazedListsTests.delimitedStringToList("A B"));
        assertEquals(GlazedListsTests.delimitedStringToList("C D E F"), source);
        assertEquals(source.size(), combo.getItemCount());
        assertTrue(Arrays.equals(source.toArray(new String[source.size()]), combo.getItems()));
        source.remove(0);
        source.remove("E");
        assertEquals(GlazedListsTests.delimitedStringToList("D F"), source);
        assertEquals(source.size(), combo.getItemCount());
        assertTrue(Arrays.equals(source.toArray(new String[source.size()]), combo.getItems()));
	}

	/**
	 * Tests the given LabelProvider.
	 */
	public void guiTestLabelProvider() {
		final BasicEventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        final Combo combo = new Combo(getShell(), SWT.DROP_DOWN);
        final EventComboViewer<String> viewer = new EventComboViewer<String>(source, combo, new TestItemFormat());
        assertSame(combo, viewer.getCombo());
        assertEquals(source.size(), combo.getItemCount());
        assertEquals("AA", combo.getItem(0));
        assertEquals("BB", combo.getItem(1));
        assertEquals("CC", combo.getItem(2));
        assertEquals("DD", combo.getItem(3));
        assertEquals("EE", combo.getItem(4));
        assertEquals("FF", combo.getItem(5));
	}


    /** TestItemFormat. */
    private static class TestItemFormat implements ItemFormat<String> {

    	public String format(String element) {
    		final String result = ((element == null) ? "" : element.toString());
    		return result + result;
    	}
    }
}

