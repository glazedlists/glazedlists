/* Glazed Lists                                                 (c) 2003-2008 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests for the {@link EventListViewer}.
 *
 * @author hbrands
 */
public class EventListViewerTest extends SwtTestCase {

    /**
     * Tests the lists {@link EventListViewer#getTogglingSelected()} and
     * {@link EventListViewer#getTogglingDeselected()} for programmatic selection control.
     */
    public void guiTestToggleSelection() {
        final BasicEventList<String> source = new BasicEventList<String>();
        final List list = new List(getShell(), SWT.MULTI);
        final EventListViewer<String> viewer = new EventListViewer<String>(source, list);
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

}
