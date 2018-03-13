/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the {@link DefaultEventListViewer}.
 *
 * @author Holger Brands
 */
public class DefaultEventListViewerTest extends SwtTestCase {

	/**
	 * Tests constructor with empty source list.
	 */
    @Test
	public void testEmptyConstruction() {
        final BasicEventList<String> source = new BasicEventList<>();
        final List list = new List(getShell(), SWT.MULTI);
        final DefaultEventListViewer<String> viewer = new DefaultEventListViewer<>(source, list);
        assertSame(list, viewer.getList());
        assertEquals(0, list.getItemCount());
        assertTrue(Arrays.equals(new String[0], list.getItems()));
	}

	/**
	 * Tests constructor with non-empty source list.
	 */
    @Test
	public void testPrefilledConstruction() {
		final BasicEventList<String> source = new BasicEventList<>();
        source.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        final List list = new List(getShell(), SWT.MULTI);
        final DefaultEventListViewer<String> viewer = new DefaultEventListViewer<>(source, list);
        assertSame(list, viewer.getList());
        assertEquals(source.size(), list.getItemCount());
        assertTrue(Arrays.equals(source.toArray(new String[source.size()]), list.getItems()));
        assertEquals("A", list.getItem(0));
        assertEquals("B", list.getItem(1));
        assertEquals("C", list.getItem(2));
        assertEquals("D", list.getItem(3));
        assertEquals("E", list.getItem(4));
        assertEquals("F", list.getItem(5));
	}

	/**
	 * Tests, that list changes are reflected in the list.
	 */
    @Test
	public void testChangeList() {
        final BasicEventList<String> source = new BasicEventList<>();
        final List list = new List(getShell(), SWT.MULTI);
        final DefaultEventListViewer<String> viewer = new DefaultEventListViewer<>(source, list);
        source.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        assertSame(list, viewer.getList());
        assertEquals(source.size(), list.getItemCount());
        assertTrue(Arrays.equals(source.toArray(new String[source.size()]), list.getItems()));
        source.clear();
        assertEquals(0, list.getItemCount());
        assertTrue(Arrays.equals(new String[0], list.getItems()));
        source.add("B");
        source.add("D");
        source.add(0, "A");
        source.add(2, "C");
        assertEquals(GlazedListsTests.delimitedStringToList("A B C D"), source);
        assertEquals(source.size(), list.getItemCount());
        assertTrue(Arrays.equals(source.toArray(new String[source.size()]), list.getItems()));
        source.addAll(GlazedListsTests.delimitedStringToList("E F"));
        source.removeAll(GlazedListsTests.delimitedStringToList("A B"));
        assertEquals(GlazedListsTests.delimitedStringToList("C D E F"), source);
        assertEquals(source.size(), list.getItemCount());
        assertTrue(Arrays.equals(source.toArray(new String[source.size()]), list.getItems()));
        source.remove(0);
        source.remove("E");
        assertEquals(GlazedListsTests.delimitedStringToList("D F"), source);
        assertEquals(source.size(), list.getItemCount());
        assertTrue(Arrays.equals(source.toArray(new String[source.size()]), list.getItems()));
	}

	/**
	 * Tests the given LabelProvider.
	 */
    @Test
	public void testLabelProvider() {
		final BasicEventList<String> source = new BasicEventList<>();
        source.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        final List list = new List(getShell(), SWT.MULTI);
        final DefaultEventListViewer<String> viewer = new DefaultEventListViewer<>(source, list, new TestItemFormat());
        assertSame(list, viewer.getList());
        assertEquals(source.size(), list.getItemCount());
        assertEquals("AA", list.getItem(0));
        assertEquals("BB", list.getItem(1));
        assertEquals("CC", list.getItem(2));
        assertEquals("DD", list.getItem(3));
        assertEquals("EE", list.getItem(4));
        assertEquals("FF", list.getItem(5));
	}

    /**
     * Tests the lists {@link DefaultEventListViewer#getTogglingSelected()} and
     * {@link DefaultEventListViewer#getTogglingDeselected()} for programmatic selection control.
     */
    @Test
    public void testToggleSelection() {
        final BasicEventList<String> source = new BasicEventList<>();
        final List list = new List(getShell(), SWT.MULTI);
        final DefaultEventListViewer<String> viewer = new DefaultEventListViewer<>(source, list);
        // populate the list
        source.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        assertEquals(Collections.EMPTY_LIST, viewer.getSelected());
        assertEquals(Collections.EMPTY_LIST, viewer.getTogglingSelected());
        assertEquals(source, viewer.getDeselected());
        assertEquals(source, viewer.getTogglingDeselected());
        assertEquals(0, list.getSelectionCount());

        // remove on TogglingDeselected selects
        viewer.getTogglingDeselected().remove("A");
        viewer.getTogglingDeselected().remove(1);
        viewer.getTogglingDeselected().removeAll(GlazedListsTests.delimitedStringToList("F D"));
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), viewer.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), viewer.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), viewer.getTogglingSelected());
        assertEquals(4, list.getSelectionCount());
        assertTrue(Arrays.equals(new int[] {0, 2, 3, 5}, list.getSelectionIndices()));

        // add on TogglingDeselected deselects
        viewer.getTogglingDeselected().add("F");
        viewer.getTogglingDeselected().addAll(GlazedListsTests.delimitedStringToList("C D"));
        assertEquals(GlazedListsTests.delimitedStringToList("B C D E F"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("B C D E F"), viewer.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A"), viewer.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A"), viewer.getTogglingSelected());
        assertEquals(1, list.getSelectionCount());
        assertTrue(Arrays.equals(new int[] {0}, list.getSelectionIndices()));

        // add on TogglingSelected selects
        viewer.getTogglingSelected().add("F");
        viewer.getTogglingSelected().addAll(GlazedListsTests.delimitedStringToList("C D"));
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), viewer.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), viewer.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), viewer.getTogglingSelected());
        assertEquals(4, list.getSelectionCount());
        assertTrue(Arrays.equals(new int[] {0, 2, 3, 5}, list.getSelectionIndices()));

        // remove on TogglingSelected deselects
        viewer.getTogglingSelected().remove("A");
        viewer.getTogglingSelected().remove(1);
        viewer.getTogglingSelected().removeAll(GlazedListsTests.delimitedStringToList("F"));
        assertEquals(GlazedListsTests.delimitedStringToList("A B D E F"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A B D E F"), viewer.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("C"), viewer.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("C"), viewer.getTogglingSelected());
        assertEquals(1, list.getSelectionCount());
        assertTrue(Arrays.equals(new int[] {2}, list.getSelectionIndices()));

        // remove on source list
        source.remove("C");
        source.removeAll(GlazedListsTests.delimitedStringToList("B E"));
        assertEquals(GlazedListsTests.delimitedStringToList("A D F"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F"), viewer.getTogglingDeselected());
        assertEquals(Collections.EMPTY_LIST, viewer.getSelected());
        assertEquals(Collections.EMPTY_LIST, viewer.getTogglingSelected());
        assertEquals(0, list.getSelectionCount());

        // add on source list
        source.add("E");
        source.addAll(GlazedListsTests.delimitedStringToList("C B"));
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getTogglingDeselected());
        assertEquals(Collections.EMPTY_LIST, viewer.getSelected());
        assertEquals(Collections.EMPTY_LIST, viewer.getTogglingSelected());
        assertEquals(0, list.getSelectionCount());

        // clear on TogglingDeselected selects all deselected
        viewer.getTogglingDeselected().clear();
        assertEquals(Collections.EMPTY_LIST, viewer.getDeselected());
        assertEquals(Collections.EMPTY_LIST, viewer.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getTogglingSelected());
        assertEquals(6, list.getSelectionCount());
        assertTrue(Arrays.equals(new int[] {0, 1, 2, 3, 4, 5}, list.getSelectionIndices()));

        // clear on TogglingSelected deselects all selected
        viewer.getTogglingSelected().clear();
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getTogglingDeselected());
        assertEquals(Collections.EMPTY_LIST, viewer.getSelected());
        assertEquals(Collections.EMPTY_LIST, viewer.getTogglingSelected());
        assertEquals(0, list.getSelectionCount());
        viewer.dispose();
    }

    /** TestItemFormat. */
    private static class TestItemFormat implements ItemFormat<String> {

    	@Override
        public String format(String element) {
    		final String result = ((element == null) ? "" : element.toString());
    		return result + result;
    	}
    }
}
